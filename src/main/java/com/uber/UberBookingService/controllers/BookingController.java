package com.uber.UberBookingService.controllers;

import com.uber.UberBookingService.dto.CreateBookingDTO;
import com.uber.UberBookingService.dto.CreateBookingResponseDTO;
import com.uber.UberBookingService.dto.UpdateBookingResponseDTO;
import com.uber.UberBookingService.dto.UpdateBookingRequestDTO;
import com.uber.UberBookingService.services.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{bookingId}")
    public ResponseEntity<UpdateBookingResponseDTO> updateBooking(
            @RequestBody UpdateBookingRequestDTO requestDTO,
            @PathVariable Long bookingId
    ) {
        return new ResponseEntity<>(bookingService.updateBooking(requestDTO, bookingId), HttpStatus.OK);
    }
}
