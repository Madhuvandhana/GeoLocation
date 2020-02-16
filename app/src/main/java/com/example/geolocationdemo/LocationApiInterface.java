package com.example.geolocationdemo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LocationApiInterface {
    @GET("api/client/v2.0/app/geolocation-wymfo/service/GeoLocation/incoming_webhook/geolocation")
    Call<List<LocationData>> getLocationHistory(@Query(value = "secret", encoded = true) String secret);
}
