package com.uber.UberBookingService.dto;

import com.uber.UberEntityService.models.BookingStatus;
import com.uber.UberEntityService.models.Driver;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateBookingResponseDTO {

    private Long bookingId;
    private BookingStatus bookingStatus;
    private Optional<Driver>  driver;
}
