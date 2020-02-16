package com.example.geolocationdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewLocationHistory extends AppCompatActivity implements AsyncTaskFilter.LocationFilterUpdateListener {
    LocationApiInterface apiInterface;
    private ProgressBar progressBar;
    private LocationHistoryAdapter locationHistoryAdapter;
    private List<LocationData> locationData = new ArrayList<>();
    private Spinner locationSpinner;
    private Spinner dateSpinner;
    int numofLocations = 0;
    String date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location_history);
        apiInterface = APIClient.getClient().create(LocationApiInterface.class);
        progressBar = findViewById(R.id.progress_bar);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        locationSpinner = findViewById(R.id.location_filter);
        dateSpinner = findViewById(R.id.date_range);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        locationHistoryAdapter = new LocationHistoryAdapter(this, locationData);
        recyclerView.setAdapter(locationHistoryAdapter);
        progressBar.setVisibility(View.VISIBLE);
        initSpinner();
        callGetLocationHistoryApi();
    }

    /*----------Date & Location Filter initialization------------- */
    private void initSpinner() {
        String[] locationitems = new String[]{"All", "Last 10", "Last 30"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, locationitems);

        locationSpinner.setAdapter(adapter);

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("Last 10")) {
                    numofLocations = 10;
                } else if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("Last 30")) {
                    numofLocations = 30;
                } else {
                    numofLocations = 0;
                }
                initData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        String[] dateitems = new String[]{"All", "Past 24 Hours", "Past Week"};

        ArrayAdapter<String> adapter_date = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, dateitems);

        dateSpinner.setAdapter(adapter_date);

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                date = (String) parent.getItemAtPosition(position);
                Log.d("TAG", "date" + date);
                initData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void callGetLocationHistoryApi() {
        Call<List<LocationData>> call = apiInterface.getLocationHistory("13794");
        call.enqueue(new Callback<List<LocationData>>() {
            @Override
            public void onResponse(Call<List<LocationData>> call, Response<List<LocationData>> response) {
                // Log.d("TAG",response.body()+"");
                progressBar.setVisibility(View.GONE);
                locationData = response.body();
                initData();


            }

            @Override
            public void onFailure(Call<List<LocationData>> call, Throwable t) {
                Log.d("TAG", t.getMessage() + "");
                call.cancel();
            }
        });
    }

    @Override
    public void onLocationFilterUpdate(List<LocationData> locationData) {
        Log.d("TAG", "size:" + locationData.size() + "");
        locationHistoryAdapter.updateList(locationData);

    }

    private void initData() {
        AsyncTaskFilter asyncTaskLocationFilter = new AsyncTaskFilter(this, locationData, numofLocations, date);
        asyncTaskLocationFilter.execute();
    }
}
