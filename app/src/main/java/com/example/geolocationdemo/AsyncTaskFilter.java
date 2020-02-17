package com.example.geolocationdemo;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class AsyncTaskFilter extends AsyncTask<Object, Object, Object> {
    private LocationFilterUpdateListener locationFilterUpdateListener;
    private List<LocationData> locationData;
    private int numLocations;
    private List<LocationData> result;
    private List<LocationData> result1;
    private String userId;
    private String dateRange;


    public AsyncTaskFilter(LocationFilterUpdateListener listener, List<LocationData> locationData, int numberofLocations, String date, String userId) {
        this.locationFilterUpdateListener = listener;
        this.locationData = locationData;
        numLocations = numberofLocations;
        dateRange = date;
        this.userId = userId;
    }

    /*----------Populate data according to the date & location filter applied ------------- */
    @Override
    protected String doInBackground(Object... params) {
        publishProgress("Loading..."); // Calls onProgressUpdate()
        result = new ArrayList<>();
        result1 = new ArrayList<>();
        int n = locationData.size() - 1;

        String dateFormat = "EEEE, MMM dd, yyyy, hh:mm aa";
        if ((n >= 10 && numLocations == 10) || (n >= 30 && numLocations == 30)) {
            for (int i = n; i > (n - numLocations); i--) {
                /*----------Only current users data is populated------------ */
                if(userId.equalsIgnoreCase(locationData.get(i).getUserId())) {
                    result.add(locationData.get(i));
                }
            }
        } else {
            for (int i = n; i >= 0; i--) {
                if(userId.equalsIgnoreCase(locationData.get(i).getUserId())) {
                    result.add(locationData.get(i));
                }
            }
        }
        int n1 = result.size();
        if (dateRange.equalsIgnoreCase("Past 24 Hours")) {
            for (int i = 0; i < n1; i++) {
                if (DateUtility.getTwentyFourHrsTime(result.get(i).getDate(), dateFormat) <= 24) {
                    result1.add(result.get(i));
                }
            }
        } else if (dateRange.equalsIgnoreCase("Past Week")) {
            for (int i = 0; i < n1; i++) {
                if (DateUtility.getDayFromDateString(result.get(i).getDate(), dateFormat) <= 168) {
                    result1.add(result.get(i));
                }
            }
        } else {
            result1.addAll(result);
        }
        return null;
    }


    @Override
    protected void onPostExecute(Object o) {
        locationFilterUpdateListener.onLocationFilterUpdate(result1);

    }


    @Override
    protected void onPreExecute() {

    }

    interface LocationFilterUpdateListener {
        void onLocationFilterUpdate(List<LocationData> locationData);
    }


}
