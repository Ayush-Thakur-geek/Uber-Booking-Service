package com.uber.UberBookingService.services.impl;

import com.uber.UberBookingService.apis.LocationServiceApi;
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

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            LocationServiceApi locationServiceApi,
            DriverRepository driverRepository) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.restTemplate = new RestTemplate();
        this.locationServiceApi = locationServiceApi;
        this.driverRepository = driverRepository;
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
        processNearByDriversAsync(nearByDriverRequestDTO);

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
    public UpdateBookingResponseDTO updateBooking(UpdateBookingRequestDTO updateBookingDTO, long bookingId) {
        Optional<Driver>  driver = driverRepository.findById(bookingId);
        driver.ifPresent(value -> bookingRepository.updateBookingStatusAndDriverById(
                bookingId,
                (BookingStatus) updateBookingDTO.getBookingStatus(),
                value));


        Optional<Booking> booking = bookingRepository.findById(bookingId);

        return UpdateBookingResponseDTO.builder()
                .bookingId(bookingId)
                .bookingStatus(booking.get().getBookingStatus())
                .driver(Optional.ofNullable(booking.get().getDriver()))
                .build();
    }

    private void processNearByDriversAsync(NearByDriverRequestDTO requestDto) {
        Call<DriverLocationDTO[]> call = locationServiceApi.getNearbyDrivers(requestDto);
        log.info("looking good");
        call.enqueue(new Callback<DriverLocationDTO[]>() {
            @Override
            public void onResponse(Call<DriverLocationDTO[]> call, Response<DriverLocationDTO[]> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DriverLocationDTO> driverLocations = Arrays.asList(response.body());

                    driverLocations.forEach(driverLocation -> {
                        log.info(driverLocation.getDriverId() +
                                " Latitude: " +
                                driverLocation.getLatitude() +
                                " Longitude: " +
                                driverLocation.getLongitude());
                    });

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
}
