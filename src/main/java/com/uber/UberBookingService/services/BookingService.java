package com.uber.UberBookingService.services;

import com.uber.UberBookingService.dto.CreateBookingDTO;
import com.uber.UberBookingService.dto.CreateBookingResponseDTO;

public interface BookingService {
    public CreateBookingResponseDTO createBooking(CreateBookingDTO createBookingDTO);
}
