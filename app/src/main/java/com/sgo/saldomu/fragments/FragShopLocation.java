package com.sgo.saldomu.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.sgo.saldomu.activities.MapsActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.widgets.BaseFragment;

import java.io.IOException;
import java.util.List;

public class FragShopLocation extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    View v;
    Bundle bundle;
    EditText et_address;
    Spinner sp_city;
    Button bt_regist, bt_back;
    TextView useCurrLoc, setCoordinate;

    String memberCode, commCode;
    Double latitude, longitude;
    boolean locationUpdateState;

    private GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location lastLocation;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    static int LOCATION_PERMISSION_REQUEST_CODE = 1;
    static int REQUEST_CHECK_SETTINGS = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_regist_shop_location, container, false);

        useCurrLoc = v.findViewById(R.id.curr_loc_text);
        setCoordinate = v.findViewById(R.id.regis_shop_showmap);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        createLocationRequest();

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                lastLocation = locationResult.getLastLocation();
//                placeMarkerOnMap(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                useCurrLoc.setVisibility(View.VISIBLE);
                placeMarkerOnMap(map.getCameraPosition().target);
            }
        };

        bundle = getArguments();
        Bundle bundle = getArguments();
        if (bundle != null) {
            memberCode = bundle.getString(DefineValue.MEMBER_CODE, "");
            commCode = bundle.getString(DefineValue.COMMUNITY_CODE, "");
        }

        setCoordinate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), MapsActivity.class), 100);
            }
        });

        et_address = v.findViewById(R.id.et_address);
        sp_city = v.findViewById(R.id.sp_city);
        bt_back = v.findViewById(R.id.btn_cancel);
        bt_regist = v.findViewById(R.id.bt_registTokoDGI);
    }

    void getCurrLoc() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
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

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(),
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

    void placeMarkerOnMap(LatLng latLng) {
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);

        String title = getAddress(latLng) + "\nGunakan Alamat ini?";

        useCurrLoc.setText(title);

//        markerOptions.title(title);

//        map.addMarker(markerOptions);

    }

    String getAddress(LatLng latLng) {
        // 1
        Geocoder geocoder = new Geocoder(getActivity());
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

    void createLocationRequest(){
        locationRequest = new LocationRequest();

        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
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
                        ((ResolvableApiException) e).startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);


    }

    @Override
    public void onResume() {
        super.onResume();
        if (!locationUpdateState) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(this);

        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
//                currLocLayout.setVisibility(View.GONE);
            }
        });

        getCurrLoc();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100:
                if (resultCode == 201) {
                    if (data != null && data.getExtras() != null) {
                        String address = data.getStringExtra("address");

                        setCoordinate.setText(address);
//                        if (UtilityManager.getInstance().getBitmap() != null) {
//                            Drawable d = new BitmapDrawable(getResources(), UtilityManager.getInstance().getBitmap());
////                            gmapsBackground.setBackground(d);
//                            gmapsBackground.setImageBitmap(UtilityManager.getInstance().getBitmap());
//                        }
//                        googleMapsBtn.setError(null);
                        longitude = data.getDoubleExtra("longitude", 0);
                        latitude = data.getDoubleExtra("latitude", 0);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
