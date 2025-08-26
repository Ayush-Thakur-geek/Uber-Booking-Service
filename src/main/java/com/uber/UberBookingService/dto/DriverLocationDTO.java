package com.uber.UberBookingService.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DriverLocationDTO {

    String driverId;
    Double latitude;
    Double longitude;

}
