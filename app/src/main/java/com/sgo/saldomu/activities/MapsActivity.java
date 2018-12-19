package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sgo.saldomu.R;
import com.sgo.saldomu.widgets.BaseActivity;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location lastLocation;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    boolean locationUpdateState;

    static int LOCATION_PERMISSION_REQUEST_CODE = 1;
    static int REQUEST_CHECK_SETTINGS = 2;

    ImageView currLocImage;
    TextView useCurrLoc;
    LinearLayout currLocLayout;
    Bitmap bitmap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        currLocImage = findViewById(R.id.activity_maps_get_curr_loc);
        useCurrLoc = findViewById(R.id.activity_maps_use_curr_loc);
        currLocLayout = findViewById(R.id.activity_maps_use_curr_loc_layout);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                lastLocation = locationResult.getLastLocation();
//                placeMarkerOnMap(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                currLocLayout.setVisibility(View.VISIBLE);
                placeMarkerOnMap(map.getCameraPosition().target);
            }
        };

        currLocLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                captureScreen();

                LatLng latLng = map.getCameraPosition().target;
                Intent i = new Intent();
                i.putExtra("address", getAddress(latLng));
                i.putExtra("latitude", latLng.latitude);
                i.putExtra("longitude", latLng.longitude);
                setResult(201, i);
//                finishActivity(200);
                finish();
            }
        });

        createLocationRequest();
    }

    void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);


    }

    void createLocationRequest(){
        locationRequest = new LocationRequest();

        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                locationUpdateState = true;
                startLocationUpdates();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ((ResolvableApiException) e).startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    void getCurrLoc() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this,
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            lastLocation = location;
                            LatLng currLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                            placeMarkerOnMap(currLatlng);
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currLatlng
                                    , 12f));
                        }
                    }
                });

    }

    String getAddress(LatLng latLng) {
        // 1
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        Address address;
        String addressText = "";

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            // 3
            if (addresses != null && !addresses.isEmpty()) {
                address = addresses.get(0);
                int c = address.getMaxAddressLineIndex();
                for (int i = 0; i <= c; i++) {
                    if (i == 0) {
                        addressText += address.getAddressLine(i);
                    } else addressText += "\n" + address.getAddressLine(i);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressText;
    }

    void placeMarkerOnMap(LatLng latLng) {
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);

        String title = getAddress(latLng) + "\nGunakan Alamat ini?";

        useCurrLoc.setText(title);

//        markerOptions.title(title);

//        map.addMarker(markerOptions);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        LatLng myPlace = new LatLng(40.73, -73.99);// this is New York
//        map.addMarker(new MarkerOptions().position(myPlace).title("My Favorite City"));
//        map.moveCamera(CameraUpdateFactory.newLatLng(myPlace));

        // change icon current location
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
//                BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)))


        map = googleMap;

        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(this);

        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                currLocLayout.setVisibility(View.GONE);
            }
        });

        getCurrLoc();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_maps;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!locationUpdateState) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK_SETTINGS){
            if (resultCode == Activity.RESULT_OK){
                locationUpdateState = true;
                startLocationUpdates();
            }
        }
    }
}
