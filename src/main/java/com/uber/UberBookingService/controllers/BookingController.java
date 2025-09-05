package com.uber.UberBookingService.controllers;

import com.uber.UberBookingService.dto.CreateBookingDTO;
import com.uber.UberBookingService.dto.CreateBookingResponseDTO;
import com.uber.UberBookingService.dto.UpdateBookingResponseDTO;
import com.uber.UberBookingService.dto.UpdateBookingRequestDTO;
import com.uber.UberBookingService.services.BookingService;
import com.uber.UberBookingService.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/booking")
@Log4j2
public class BookingController {

    private final BookingService bookingService;
    private final JwtUtil jwtUtil;

    public BookingController(BookingService bookingService, JwtUtil jwtUtil) {
        this.bookingService = bookingService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public String test() {
        return "Booking API works!";
    }


    @PostMapping
    public ResponseEntity<CreateBookingResponseDTO> createBooking(
            @RequestBody CreateBookingDTO  createBookingDTO,
            HttpServletRequest request
    ) {
        String token = null;
        try {
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("JwtToken")) {
                    token = cookie.getValue();
                    System.out.println(token);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (token == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else if (!jwtUtil.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
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
