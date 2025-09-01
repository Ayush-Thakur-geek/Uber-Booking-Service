package com.uber.UberBookingService.apis;

import com.uber.UberBookingService.dto.DriverLocationDTO;
import com.uber.UberBookingService.dto.NearByDriverRequestDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LocationServiceApi {

    @POST("/api/v1/location/nearby/drivers")
    Call<DriverLocationDTO[]> getNearbyDrivers(@Body NearByDriverRequestDTO requestDto);

}
