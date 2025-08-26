package com.uber.UberBookingService.dto;

import com.uber.UberEntityService.models.ExactLocation;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBookingDTO {

    private Long passengerId;

    private ExactLocation startLocation;

    private ExactLocation endLocation;

}
