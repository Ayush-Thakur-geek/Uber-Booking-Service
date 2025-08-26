package com.uber.UberBookingService.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NearByDriverRequestDTO {
    Double latitude;
    Double longitude;
}
