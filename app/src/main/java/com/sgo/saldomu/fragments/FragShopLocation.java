package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.Beans.CustomAdapterModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MapsActivity;
import com.sgo.saldomu.adapter.CustomAutoCompleteAdapter;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place;
import com.sgo.saldomu.widgets.BaseFragment;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class FragShopLocation extends BaseFragment {
//        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    View v;
    Bundle bundle;
    EditText et_address;
    Spinner sp_city;
    Button bt_regist, bt_back;
    TextView useCurrLoc, setCoordinate, codeStore;
    AutoCompleteTextView cityLocField;

    CustomAutoCompleteAdapter adapter;
    ArrayAdapter<String> adapters;

    String memberCode, commCode;
    List<CustomAdapterModel> locList;
    List<String> locLists;
    Double latitude, longitude;
//    boolean locationUpdateState;

//    private GoogleMap map;
//    FusedLocationProviderClient fusedLocationProviderClient;
//    Location lastLocation;
//    LocationCallback locationCallback;
//    LocationRequest locationRequest;

    static int LOCATION_PERMISSION_REQUEST_CODE = 1;
    static int REQUEST_CHECK_SETTINGS = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_regist_shop_location, container, false);

        useCurrLoc = v.findViewById(R.id.curr_loc_text);
        setCoordinate = v.findViewById(R.id.regis_shop_showmap);
        codeStore = v.findViewById(R.id.regis_shop_store_code);
        cityLocField = v.findViewById(R.id.get_shop_location_list);

        et_address = v.findViewById(R.id.et_address);
        sp_city = v.findViewById(R.id.sp_city);
        bt_back = v.findViewById(R.id.btn_cancel);
        bt_regist = v.findViewById(R.id.btn_shop_register);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

//        createLocationRequest();

//        locationCallback = new LocationCallback(){
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult);
//
//                lastLocation = locationResult.getLastLocation();
////                placeMarkerOnMap(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
//                useCurrLoc.setVisibility(View.VISIBLE);
//                placeMarkerOnMap(map.getCameraPosition().target);
//            }
//        };

        bundle = getArguments();
        Bundle bundle = getArguments();
        if (bundle != null) {
            memberCode = bundle.getString(DefineValue.MEMBER_CODE, "");
            commCode = bundle.getString(DefineValue.COMMUNITY_CODE, "");

            codeStore.setText(memberCode);
        }

        locList = new ArrayList<>();
        locLists = new ArrayList<>();
        adapter = new CustomAutoCompleteAdapter(getActivity(), locList, android.R.layout.simple_spinner_dropdown_item);
        adapters = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, locLists);
        cityLocField.setAdapter(adapters);
//        cityLocField.setDropDownBackgroundResource(R.color.white);
        cityLocField.setThreshold(2);

        Realm realm = Realm.getInstance(RealmManager.BBSConfiguration);
        RealmResults<List_BBS_Birth_Place> results = realm.where(List_BBS_Birth_Place.class).findAll();
        List<List_BBS_Birth_Place> list_bbs_birth_place = new ArrayList<>(realm.copyFromRealm(results));

        for (List_BBS_Birth_Place model: list_bbs_birth_place
             ) {
            locList.add(new CustomAdapterModel(model));
            locLists.add(model.getBirthPlace_city());
        }

        adapters.notifyDataSetChanged();

        setCoordinate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), MapsActivity.class), 100);
            }
        });

        bt_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput()) {
                    setMemberLocation();
                }
            }
        });


    }

    boolean checkInput() {
        if (et_address.getText().toString().trim().length() == 0) {
            et_address.setError("Alamat kosong");
            et_address.requestFocus();
            return false;
        } else if (cityLocField.getText().toString().trim().length() == 0) {
            cityLocField.setError("Kota kosong");
            cityLocField.requestFocus();
            return false;
        } else if (setCoordinate.getText().toString().equalsIgnoreCase("Koordinat belum terpasang")) {
            Toast.makeText(getActivity(), "Koordinat letak belum siap", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

//    void getCurrLoc() {
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        map.setMyLocationEnabled(true);
//
//        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(),
//                new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        if (location != null) {
//                            lastLocation = location;
//                            LatLng currLatlng = new LatLng(location.getLatitude(), location.getLongitude());
//                            placeMarkerOnMap(currLatlng);
//                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currLatlng
//                                    , 12f));
//                        }
//                    }
//                });
//
//    }

//    void placeMarkerOnMap(LatLng latLng) {
////        MarkerOptions markerOptions = new MarkerOptions();
////        markerOptions.position(latLng);
//
//        String title = getAddress(latLng) + "\nGunakan Alamat ini?";
//
//        useCurrLoc.setText(title);
//
////        markerOptions.title(title);
//
////        map.addMarker(markerOptions);
//
//    }

//    String getAddress(LatLng latLng) {
//        // 1
//        Geocoder geocoder = new Geocoder(getActivity());
//        List<Address> addresses;
//        Address address;
//        String addressText = "";
//
//        try {
//            // 2
//            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//            // 3
//            if (addresses != null && !addresses.isEmpty()) {
//                address = addresses.get(0);
//                int c = address.getMaxAddressLineIndex();
//                for (int i = 0; i <= c; i++) {
//                    if (i == 0) {
//                        addressText += address.getAddressLine(i);
//                    } else addressText += "\n" + address.getAddressLine(i);
//
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return addressText;
//    }

//    void createLocationRequest(){
//        locationRequest = new LocationRequest();
//
//        locationRequest.setInterval(10000);
//        locationRequest.setFastestInterval(5000);
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(locationRequest);
//
//        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
//        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
//
//        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
//            @Override
//            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                locationUpdateState = true;
//                startLocationUpdates();
//            }
//        });
//        task.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                if (e instanceof ResolvableApiException) {
//                    try {
//                        ((ResolvableApiException) e).startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
//                    } catch (IntentSender.SendIntentException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//        });
//    }
//
//    void startLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION
//                    , Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//        } else
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
//
//
//    }

    public void setMemberLocation() {
        try {
            showProgressDialog();
            extraSignature = commCode + memberCode;

            RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_SET_MEMBER_LOC,
                    userPhoneID, accessKey, extraSignature);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_CODE, MyApiClient.COMM_ID);
            params.put(WebParams.ADDRESS, et_address.getText().toString());
            params.put(WebParams.LATITUDE, latitude);
            params.put(WebParams.LONGITUDE, longitude);
            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
            params.put(WebParams.CITY, cityLocField.getText().toString());

            Timber.d("isi params get Balance Collector:" + params.toString());

            MyApiClient.setMemberLoc(getActivity(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {

                        dismissProgressDialog();
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response getBalance Collector:" + response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            Toast.makeText(getActivity(), "Sukses", Toast.LENGTH_SHORT).show();

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            if (getActivity().isFinishing()) {
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginMain(getActivity(), message);
                            }
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    Timber.w("Error Koneksi getBalance Collector:" + throwable.toString());
                    dismissProgressDialog();
                }
            });

        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (!locationUpdateState) {
//            startLocationUpdates();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        map = googleMap;
//
//        map.getUiSettings().setZoomControlsEnabled(true);
//        map.setOnMarkerClickListener(this);
//
//        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
//            @Override
//            public void onCameraMove() {
////                currLocLayout.setVisibility(View.GONE);
//            }
//        });
//
//        getCurrLoc();
//    }

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

//    @Override
//    public boolean onMarkerClick(Marker marker) {
//        return false;
//    }
}
