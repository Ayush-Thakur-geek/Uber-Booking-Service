package com.uber.UberBookingService.services.impl;

import com.uber.UberBookingService.dto.CreateBookingDTO;
import com.uber.UberBookingService.dto.CreateBookingResponseDTO;
import com.uber.UberBookingService.dto.DriverLocationDTO;
import com.uber.UberBookingService.dto.NearByDriverRequestDTO;
import com.uber.UberBookingService.repositories.BookingRepository;
import com.uber.UberBookingService.repositories.PassengerRepository;
import com.uber.UberBookingService.services.BookingService;
import com.uber.UberEntityService.models.Booking;
import com.uber.UberEntityService.models.BookingStatus;
import com.uber.UberEntityService.models.Passenger;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final RestTemplate restTemplate;
    private final String LOCATION_SERVICE_URL = "http://localhost:8083";

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.restTemplate = new RestTemplate();
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

        //make an api call to location service to get nearby driver location
        ResponseEntity<DriverLocationDTO[]> result = restTemplate.postForEntity(
                LOCATION_SERVICE_URL + "/api/v1/location/nearby/drivers",
                nearByDriverRequestDTO,
                DriverLocationDTO[].class
        );

        if (result.getStatusCode().is2xxSuccessful() && result.getBody() != null) {
            List<DriverLocationDTO> driverLocations = Arrays.asList(result.getBody());

            driverLocations.forEach(driverLocation -> {
                log.info(driverLocation.getDriverId() +
                        " Latitude: " +
                        driverLocation.getLatitude() +
                        " Longitude: " +
                        driverLocation.getLongitude());
            });

        }

        return CreateBookingResponseDTO.builder()
                .bookingId(savedBooking.getId())
                .driver(Optional.ofNullable(savedBooking.getDriver()))
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .build();
    }
}
