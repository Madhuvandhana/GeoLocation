package com.example.geolocationdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private LocationManager locationMangaer = null;
    private LocationListener locationListener = null;

    private Button btnGetLocation = null;
    private EditText editLocation = null;
    private ProgressBar pb = null;

    private static final String TAG = "Debug";
    private Boolean flag = false;
    private Boolean isError = false;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private StitchAppClient stitchAppClient;
    private LocationData locationData;
    private RemoteMongoClient remoteMongoClient;
    private Document document;
    private GoogleMap map;
    private LatLng latlng;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo
                .SCREEN_ORIENTATION_PORTRAIT);

        locationData = new LocationData();
        document = new Document();


        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        editLocation = (EditText) findViewById(R.id.editTextLocation);

        btnGetLocation = (Button) findViewById(R.id.btnLocation);
        btnGetLocation.setOnClickListener(this);

        locationMangaer = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Stitch.initializeDefaultAppClient(
                getResources().getString(R.string.my_app_id)
        );
        initStitchAppLogin();
        Button btn_share = findViewById(R.id.shareit);
        btn_share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initShare();
            }
        });


    }

    private void initShare() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Current Location");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getCurrentLocationUrl(latlng));
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }


    private String getCurrentLocationUrl(LatLng origin) {

        if (latlng != null) {
            String str_origin = "q=loc:" + origin.latitude + "," + origin.longitude;
            String sensor = "sensor=false";
            String mode = "mode=driving";
            String parameters = str_origin + "&" + sensor + "&" + mode;
            String output = "json";

            String url = "http://maps.google.de/maps?" + parameters;
            Log.d(TAG, url);


            return url;
        } else
            return "Map not available";
    }

    /*----------Anonymous Login into mongoDb and initialization ------------- */
    private void initStitchAppLogin() {

        stitchAppClient = Stitch.getDefaultAppClient();
        stitchAppClient.getAuth().loginWithCredential(new AnonymousCredential()).addOnCompleteListener(new OnCompleteListener<StitchUser>() {
            @Override
            public void onComplete(@NonNull final Task<StitchUser> task) {
                if (task.isSuccessful()) {
                    document.append("user_id", task.getResult().getId());
                    initMongoDB();
                    isError = false;
                } else {
                    isError = true;
                    editLocation.setText("Something went wrong with the connection. Please try again");
                    Log.e("stitch", "failed to log in anonymously", task.getException());
                }
            }
        });
    }

    private void initMongoDB() {
        remoteMongoClient = stitchAppClient.getServiceClient(
                RemoteMongoClient.factory,
                "mongodb-atlas"
        );
    }

    @Override
    public void onClick(View v) {
        initLocationMap();

    }

    private void initLocationMap() {
        flag = displayGpsStatus();
        if (!isError) {
            if (flag) {

                Log.v(TAG, "onClick");

                editLocation.setText("Please!! move your device to" +
                        " see the changes in coordinates." + "\nWait..");

                pb.setVisibility(View.VISIBLE);
                locationListener = new MyLocationListener();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            Toast.makeText(this, "Access Location permission allows us to track your current location. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
                        }
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                        return;
                    }
                }
                locationMangaer.requestLocationUpdates(LocationManager
                        .GPS_PROVIDER, 5000, 10, locationListener);

            } else {
                alertbox();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Permission Denied. You cannot access location.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /*----Method to Check GPS is enable or disable ----- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    protected void alertbox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Device's GPS is Disable")
                .setCancelable(false)
                .setTitle("** Gps Status **")
                .setPositiveButton("Gps On",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent myIntent = new Intent(
                                        Settings.ACTION_SECURITY_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.setMyLocationEnabled(true);
        Log.v(TAG, "Map ready");
        //initLocationMap();
    }

    /*----------Add latitute & Longitude markers on the map ------------- */
    private void initMap(double lat, double lo) {
        latlng = new LatLng(lat, lo);
        if (map != null) {
            map.addMarker(new
                    MarkerOptions().position(latlng).title(""));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14.0f));
        }
    }

    /*----------Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            editLocation.setText("");
            String outputAddress = "";
            pb.setVisibility(View.INVISIBLE);
            Toast.makeText(getBaseContext(), "Location Updated",
                    Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v(TAG, latitude);

            /*----------to get City-Name from coordinates ------------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc
                        .getLongitude(), 1);
                if (addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
                outputAddress += addresses.get(0).getAddressLine(0);
                Log.v(TAG, "o/p addr" + outputAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }


            String s = longitude + "\n" + latitude +
                    "\n\nMy Current City is: " + cityName;
            initMap(loc.getLatitude(), loc.getLongitude());
            long date = new Date().getTime();
            int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                    | DateUtils.FORMAT_SHOW_WEEKDAY;
            String dateStr = DateUtils.formatDateTime(MainActivity.this, date, flags);
            document.append("time", dateStr);
            document.append("address", outputAddress);
            initDB();
            editLocation.setText(outputAddress);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    private void initDB() {
        RemoteMongoCollection<Document> collection = remoteMongoClient.getDatabase("location")
                .getCollection("locationHistory");
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
        String date = s.format(new Date());
        document.append("_id", date);
        collection.insertOne(document)
                .addOnSuccessListener(new OnSuccessListener<RemoteInsertOneResult>() {
                    @Override
                    public void onSuccess(RemoteInsertOneResult remoteInsertOneResult) {
                        Log.d(TAG, "One document inserted");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage() != null ? e.getMessage() : "error");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.geolocation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {

            case R.id.location_history:
                Intent intent = new Intent(this, ViewLocationHistory.class);
                startActivity(intent);

                break;

            default:
        }
        return false;
    }
}
