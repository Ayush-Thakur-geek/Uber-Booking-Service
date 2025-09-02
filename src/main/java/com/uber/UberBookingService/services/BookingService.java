package com.uber.UberBookingService.services;

import com.uber.UberBookingService.dto.CreateBookingDTO;
import com.uber.UberBookingService.dto.CreateBookingResponseDTO;
import com.uber.UberBookingService.dto.UpdateBookingRequestDTO;
import com.uber.UberBookingService.dto.UpdateBookingResponseDTO;

public interface BookingService {
    public CreateBookingResponseDTO createBooking(CreateBookingDTO createBookingDTO);
    public UpdateBookingResponseDTO updateBooking(UpdateBookingRequestDTO updateBookingDTO, long bookingId);
}
