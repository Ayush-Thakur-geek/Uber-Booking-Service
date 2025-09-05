package com.uber.UberBookingService.apis;

import com.uber.UberBookingService.dto.DriverLocationDTO;
import com.uber.UberBookingService.dto.NearByDriverRequestDTO;
import com.uber.UberBookingService.dto.RideRequestDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UberSocketApi {

    @POST("/api/v1/socket/new_ride")
    Call<Boolean> raiseRideRequest(@Body RideRequestDTO requestDto);
}
