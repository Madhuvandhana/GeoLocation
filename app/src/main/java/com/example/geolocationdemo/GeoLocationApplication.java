package com.example.geolocationdemo;

import android.app.Application;

import com.mongodb.stitch.android.core.Stitch;

/*----------Initialization of Stitch mongo db------------- */
public class GeoLocationApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stitch.initializeDefaultAppClient(
                getResources().getString(R.string.my_app_id)
        );
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
