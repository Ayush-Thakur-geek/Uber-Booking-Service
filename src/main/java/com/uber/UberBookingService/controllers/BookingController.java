package com.uber.UberBookingService.controllers;

import com.uber.UberBookingService.dto.CreateBookingDTO;
import com.uber.UberBookingService.dto.CreateBookingResponseDTO;
import com.uber.UberBookingService.services.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<CreateBookingResponseDTO> createBooking(@RequestBody CreateBookingDTO  createBookingDTO) {
        return new ResponseEntity<>(bookingService.createBooking(createBookingDTO), HttpStatus.OK);
    }
}
