package com.uber.UberBookingService.services.impl;

import com.uber.UberBookingService.apis.LocationServiceApi;
import com.uber.UberBookingService.apis.UberSocketApi;
import com.uber.UberBookingService.dto.*;
import com.uber.UberBookingService.repositories.BookingRepository;
import com.uber.UberBookingService.repositories.DriverRepository;
import com.uber.UberBookingService.repositories.PassengerRepository;
import com.uber.UberBookingService.services.BookingService;
import com.uber.UberEntityService.models.Booking;
import com.uber.UberEntityService.models.BookingStatus;
import com.uber.UberEntityService.models.Driver;
import com.uber.UberEntityService.models.Passenger;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final RestTemplate restTemplate;
    private final LocationServiceApi locationServiceApi;
    private final DriverRepository driverRepository;
    private final UberSocketApi uberSocketApi;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            LocationServiceApi locationServiceApi,
            DriverRepository driverRepository,
            UberSocketApi uberSocketApi) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.restTemplate = new RestTemplate();
        this.locationServiceApi = locationServiceApi;
        this.driverRepository = driverRepository;
        this.uberSocketApi = uberSocketApi;
    }

    @Override
    public CreateBookingResponseDTO createBooking(CreateBookingDTO createBookingDTO) {

        Optional<Passenger> passenger = passengerRepository.findById(createBookingDTO.getPassengerId());

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(createBookingDTO.getStartLocation())
                .endLocation(createBookingDTO.getEndLocation())
                .passenger(passenger.get())
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        NearByDriverRequestDTO nearByDriverRequestDTO = NearByDriverRequestDTO.builder()
                .latitude(createBookingDTO.getStartLocation().getLatitude())
                .longitude(createBookingDTO.getStartLocation().getLongitude())
                .build();

        log.info("finding the nearby driver via making async call to location service");
        System.out.println("booking id: " + savedBooking.getId());
        processNearByDriversAsync(nearByDriverRequestDTO, createBookingDTO, savedBooking.getId());

//        //make an api call to location service to get nearby driver location
//        ResponseEntity<DriverLocationDTO[]> result = restTemplate.postForEntity(
//                LOCATION_SERVICE_URL + "/api/v1/location/nearby/drivers",
//                nearByDriverRequestDTO,
//                DriverLocationDTO[].class
//        );
//
//        if (result.getStatusCode().is2xxSuccessful() && result.getBody() != null) {
//            List<DriverLocationDTO> driverLocations = Arrays.asList(result.getBody());
//
//            driverLocations.forEach(driverLocation -> {
//                log.info(driverLocation.getDriverId() +
//                        " Latitude: " +
//                        driverLocation.getLatitude() +
//                        " Longitude: " +
//                        driverLocation.getLongitude());
//            });
//
//        }

        log.info("found driver");

        return CreateBookingResponseDTO.builder()
                .bookingId(savedBooking.getId())
                .driver(Optional.ofNullable(savedBooking.getDriver()))
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .build();
    }

    @Override
    public synchronized UpdateBookingResponseDTO updateBooking(UpdateBookingRequestDTO updateBookingDTO, long bookingId) {
        // Get driver by driverId from DTO
        Optional<Driver> driver = driverRepository.findById(updateBookingDTO.getDriverId());

        System.out.println("driver id: " + driver.get().getId());
        driver.ifPresent(value -> {
            bookingRepository.updateBookingStatusAndDriverById(
                    bookingId,
                    updateBookingDTO.getBookingStatus(),  // ensure correct type
                    value);
            System.out.println("driver id: " + value.getId());
            driverRepository.updateDriverStatus(value.getId());
        });

        Optional<Booking> booking = bookingRepository.findById(bookingId);

        if (booking.isEmpty()) {
            System.out.println("no booking found");
        }

        Booking foundBooking = booking.get();

        return UpdateBookingResponseDTO.builder()
                .bookingId(bookingId)
                .bookingStatus(foundBooking.getBookingStatus())
                .driver(Optional.ofNullable(foundBooking.getDriver()))
                .build();
    }


    private void processNearByDriversAsync(NearByDriverRequestDTO requestDto,
                                           CreateBookingDTO createBookingDTO,
                                           long bookingId) {
        Call<DriverLocationDTO[]> call = locationServiceApi.getNearbyDrivers(requestDto);
        log.info("looking good");
        call.enqueue(new Callback<DriverLocationDTO[]>() {
            @Override
            public void onResponse(Call<DriverLocationDTO[]> call, Response<DriverLocationDTO[]> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DriverLocationDTO> driverLocations = Arrays.asList(response.body());
                    List<Long> driverIds = new ArrayList<>();

                    driverLocations.forEach(driverLocation -> {
                        driverIds.add(Long.parseLong(driverLocation.getDriverId()));
                        log.info(driverLocation.getDriverId() +
                                " Latitude: " +
                                driverLocation.getLatitude() +
                                " Longitude: " +
                                driverLocation.getLongitude());
                    });

                    try {
                        System.out.println("one more confirmation: " + bookingId);
                        raiseRideRequest(RideRequestDTO.builder()
                                .driverIds(driverIds)
                                .passengerId(createBookingDTO.getPassengerId())
                                .endLocation(createBookingDTO.getEndLocation())
                                .startLocation(createBookingDTO.getStartLocation())
                                .bookingId(bookingId)
                                .build());
                    } catch (Exception e) {
                        throw e;
                    }

                } else {
                    log.info("Request failed {}", response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<DriverLocationDTO[]> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    private void raiseRideRequest(RideRequestDTO rideRequestDTO) {
        Call<Boolean> call = uberSocketApi.raiseRideRequest(rideRequestDTO);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                Boolean result = response.body();
                log.info("Driver Response {}", result.toString());
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                log.error("Request failed {}", t.getMessage());
            }
        });
    }
}
