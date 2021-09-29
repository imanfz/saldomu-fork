package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsSearchAgentActivity;
import com.sgo.saldomu.coreclass.AgentConstant;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MainResultReceiver;
import com.sgo.saldomu.models.ShopDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import com.sgo.saldomu.dialogs.AgentDetailFragmentDialog;

/**
 * Created by Lenovo Thinkpad on 12/1/2016.
 */
public class AgentMapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener, TextView.OnEditorActionListener,
        AgentListFragment.OnListAgentItemClick {

    private Double searchLatitude;
    private Double searchLongitude;
    private Double lastLatitude;
    private Double lastLongitude;
    private boolean searchLocationChecked;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();

    private GoogleMap globalMap;
    View rootView;
    private HashMap<Integer, Marker> hashMarker;
    private AgentMapFragment thisClass;
    private int agentPosition;
    private LinearLayout llLegendAgent, llLegendShop;
    private Double currentLatitude;
    private Double currentLongitude;
    private BbsSearchAgentActivity mainBbsActivity;
    List<Polyline> lines;
    Polyline line;
    SupportMapFragment mapFrag;
    private String mobility;
    SecurePreferences sp;

    public AgentMapFragment() {
        lines = new ArrayList<>();
    }

    /**
     * During creation, if arguments have been supplied to the fragment
     * then parse those out.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            this.mobility = args.getString(DefineValue.BBS_AGENT_MOBILITY);
            this.currentLatitude = args.getDouble(DefineValue.CURRENT_LATITUDE);
            this.currentLongitude = args.getDouble(DefineValue.CURRENT_LONGITUDE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.agent_map_fragment, container, false);

            //get data session
            getDataSharedPreferences();

            mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.agentMap);
            //mapFrag.getView().setVisibility(View.GONE);
            mapFrag.getMapAsync(this);

            llLegendAgent = (LinearLayout) rootView.findViewById(R.id.llLegendAgent);
            llLegendShop = (LinearLayout) rootView.findViewById(R.id.llLegendShop);

            if (this.mobility.equals(DefineValue.STRING_NO)) {
                llLegendAgent.setVisibility(View.GONE);
            } else {
                llLegendShop.setVisibility(View.GONE);
            }

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            sp = CustomSecurePref.getInstance().getmSecurePrefs();
        }

        //searchLocationEditText.setText(this.completeAddress);

        return rootView;
    }

    private void recreateAllMarker() {
        if (globalMap != null && getActivity() != null) {

            globalMap.clear();

            setMapCamera();

            //add search location marker
            setSearchMarker();

            setAgentMarker();

            setPolyline();
            globalMap.setOnMarkerClickListener(this);

        }
    }

    public void setPolyline() {
        List<LatLng> list = new ArrayList<>();

        if (shopDetails.size() > 0 && globalMap != null) {

            if (lines.size() > 0) {
                Polyline dataLine = lines.get(0);
                dataLine.remove();

                lines.clear();
            }


            String isSetPolyLine = shopDetails.get(agentPosition).getIsPolyline();
            if (isSetPolyLine != null && isSetPolyLine.equals("1")) {
                String encodedPoints = shopDetails.get(agentPosition).getEncodedPoints();
                if (encodedPoints != null) {
                    list = decodePoly(encodedPoints);
                    line = globalMap.addPolyline(new PolylineOptions()
                            .addAll(list)
                            .width(3)
                            .color(Color.RED)
                            .geodesic(true)
                    );
                    lines.add(line);
                }
            }
        }
    }

    private void resetAllMarker() {
        if (globalMap != null && getActivity() != null) {
            //menunggu sampai view map sudah selesai di rendering semua, baru jalankan ini :
            globalMap.setOnMapLoadedCallback(() -> {
                //delete semua marker
                globalMap.clear();

                setMapCamera();

                //add search location marker
                setSearchMarker();

                //getLastLocationSharedPreferences();
                //pickupTextView.setText(pickup);
                setLastMarker();

                //getAgentLocationSharedPreferences();
                setAgentMarker();

                setPolyline();

                //set action ketika marker diklik
                globalMap.setOnMarkerClickListener(thisClass);
            });
        }
    }

    private void getDataSharedPreferences() {
        getSearchLocationSharedPreferences();
        getLastLocationSharedPreferences();
        getAgentLocationSharedPreferences();
    }

    private void getSearchLocationSharedPreferences() {
        SharedPreferences preferences = getActivity().getSharedPreferences(AgentConstant.SEARCH_LOCATION_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String latitudeString = preferences.getString("latitude", "0");
        String longitudeString = preferences.getString("longitude", "0");

        //convert string to double
        searchLatitude = Double.parseDouble(latitudeString);
        searchLongitude = Double.parseDouble(longitudeString);
    }

    private void getLastLocationSharedPreferences() {
        SharedPreferences preferences = getActivity().getSharedPreferences(AgentConstant.LAST_LOCATION_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String latitudeString = preferences.getString("latitude", "0");
        String longitudeString = preferences.getString("longitude", "0");
        searchLocationChecked = preferences.getBoolean("searchLocationChecked", false); //get checked radio button

        //convert string to double
        lastLatitude = Double.parseDouble(latitudeString);
        lastLongitude = Double.parseDouble(longitudeString);
    }

    private void getAgentLocationSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String dataJson = preferences.getString(AgentConstant.AGENT_INFO_SHARED_PREFERENCES, "");
    }

    //implements View.OnClickListener
    @Override
    public void onClick(View view) {
        if (currentLatitude != null && currentLongitude != null)
            setCameraPosition(lastLatitude, lastLongitude);
    }

    private void displayMap() {
        if (mapFrag == null) {
            mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.agentMap);

        }
    }

    //implements OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap map) {
        //set global map
        globalMap = map;

        if (globalMap != null && getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //globalMap.setMyLocationEnabled(true);
            setMapCamera();
            globalMap.setOnMarkerClickListener(this);

            //menunggu sampai view map sudah selesai di rendering semua, baru jalankan ini :
            globalMap.setOnMapLoadedCallback(() -> {
                //set map configuration
                mapSetting();

                //add camera position
                setMapCamera();

                //add search location marker
                setSearchMarker();

                //add last location marker
                //setLastMarker();

                //add agent location marker
                //setAgentMarkerDummy();
                setAgentMarker();

                setPolyline();

                //set action ketika marker diklik
                //globalMap.setOnMarkerClickListener(thisClass);
            });
        }

    }

    private void mapSetting() {
        try {
            globalMap.setMyLocationEnabled(false);
            globalMap.clear();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        //disable map gesture untuk sementara sampai camera position selesai
        globalMap.getUiSettings().setAllGesturesEnabled(true);
        globalMap.getUiSettings().setMapToolbarEnabled(false);
        globalMap.setIndoorEnabled(false);
    }

    private void setCameraPosition(Double latitude, Double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);

        //add camera position and configuration
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng) // Center Set
                .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                //.tilt(30) // Tilt of the camera to 30 degrees
                .build(); // Creates a CameraPosition from the builder

        globalMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private void setMapCamera() {
        if (currentLongitude != null && currentLatitude != null) {
            LatLng latLng = new LatLng(currentLatitude, currentLongitude);

            //jika radio button search location dichecked
            if (searchLocationChecked) {
                latLng = new LatLng(searchLatitude, searchLongitude);
            }

            globalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //add camera position and configuration
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng) // Center Set
                    .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                    .build(); // Creates a CameraPosition from the builder

            globalMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    //jika animate camera position sudah selesai, maka on receiver baru boleh dijalankan.

                    //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                    globalMap.getUiSettings().setAllGesturesEnabled(true);
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    private void setSearchMarker() {

        if (currentLatitude != null && currentLongitude != null && globalMap != null) {

            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        }
    }

    private void setLastMarker() {

    }

    //for resize icon
    public Bitmap resizeMapIcons(int image, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), image);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void setAgentMarker() {
        if (shopDetails.size() > 0) {
            for (int idx1 = 0; idx1 < shopDetails.size(); idx1++) {
                setAgentMarkerOption(idx1);
            }
        }

    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void setAgentMarkerOption(int item) {

        Double latitude = shopDetails.get(item).getShopLatitude();
        Double longitude = shopDetails.get(item).getShopLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions;

        if (shopDetails.get(item).getShopMobility().equals(DefineValue.STRING_NO)) {
            markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.map_home, 90, 90)));
        } else {
            markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.map_person, 90, 90)));
        }

        Marker marker = globalMap.addMarker(markerOptions);
        hashMarker.put(item, marker);

    }


    //GoogleMap.OnMarkerClickListener
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (shopDetails.size() > 0 && mobility.equals(DefineValue.STRING_NO)) {
            for (Integer index : hashMarker.keySet()) {
                if (marker.equals(hashMarker.get(index))) {
                    this.shopDetails.get(index).setIsPolyline("1");

                } else {
                    this.shopDetails.get(index).setIsPolyline("0");
                }
            }

            setPolyline();
        }


        return false;

    }

    public void updateView(ArrayList<ShopDetail> shopDetails) {

        try {
            ArrayList<Double> tempCoordinate = ((BbsSearchAgentActivity) getActivity()).getCurrentCoordinate();
            displayMap();
            if (tempCoordinate.size() > 0) {
                this.currentLatitude = tempCoordinate.get(0);
                this.currentLongitude = tempCoordinate.get(1);

                if (this.currentLatitude == 0.0) {
                    this.currentLatitude = null;
                }

                if (this.currentLongitude == 0.0) {
                    this.currentLongitude = null;
                }
            }

            this.shopDetails.clear();
            if (shopDetails.size() > 0) {


                this.shopDetails.addAll(shopDetails);


                //process();
            } else {

                if (currentLatitude != null && currentLongitude != null) {

                    setMapCamera();
                    setSearchMarker();
                }

                return;
            }


            if (this.currentLatitude != null && this.currentLongitude != null) {
                lastLatitude = this.currentLatitude;
                lastLongitude = this.currentLongitude;

                if (hashMarker == null) {
                    hashMarker = new HashMap<>();
                } else {
                    hashMarker.clear();
                }
                //if ( hashMarker.size() > 0 ) {
                //hashMarker.clear();
                //}

                recreateAllMarker();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void OnIconLocationClickListener(int position) {
        agentPosition = position;
        setPolyline();
    }
}
