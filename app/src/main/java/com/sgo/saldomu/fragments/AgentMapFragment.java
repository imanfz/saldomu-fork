package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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
import com.sgo.saldomu.adapter.GooglePlacesAutoCompleteArrayAdapter;
import com.sgo.saldomu.coreclass.AgentConstant;
import com.sgo.saldomu.widgets.CustomAutoCompleteTextViewWithRadioButton;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MainResultReceiver;
//import com.sgo.saldomu.dialogs.AgentDetailFragmentDialog;
import com.sgo.saldomu.models.ShopDetail;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lenovo Thinkpad on 12/1/2016.
 */
public class AgentMapFragment extends Fragment implements MainResultReceiver.Receiver,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener, AdapterView.OnItemClickListener ,TextView.OnEditorActionListener,
        AgentListFragment.OnListAgentItemClick
{

    private Double searchLatitude;
    private Double searchLongitude;
    private Double lastLatitude;
    private Double lastLongitude;
    private boolean searchLocationChecked;
    private String pickup;
    private JSONArray agentLocation = null;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private ArrayList<ShopDetail> tempDetails = new ArrayList<>();
    private GoogleApiClient googleApiClient;

    private GoogleMap globalMap;
    private Marker mapMarker;
    private Marker[] markerArray;
    View rootView;
    private double p;
    private HashMap<Integer, Marker> hashMarker;
    private AgentMapFragment thisClass;
    private boolean receiverStatus = false;
    private boolean singleAgentFlag = false;
    private int agentPosition;
    private ImageView currentLocationBtn;
    private ImageView searchLocationBtn;
    private LinearLayout searchLocationContainer, llLegendAgent, llLegendShop, llLegendYou;
    private TextView pickupTextView, tvAgent, tvShop, tvYou;
    private Double currentLatitude;
    private Double currentLongitude;
    private Marker lastCoordinateMarker;
    private BbsSearchAgentActivity mainBbsActivity;
    private CustomAutoCompleteTextViewWithRadioButton searchLocationEditText;
    GooglePlacesAutoCompleteArrayAdapter googlePlacesAutoCompleteBbsArrayAdapter;
    List<Polyline> lines;
    Polyline line;
    private Location currentLocation;
    private LocationRequest mLocationRequest;
    SupportMapFragment mapFrag;
    private String mobility, completeAddress;
    SecurePreferences sp;
    private final int RC_PHONE_CALL = 503;


    public AgentMapFragment() {
        lastCoordinateMarker = null;
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
            this.mobility = args.getString("mobility");
            this.currentLatitude = args.getDouble("currentLatitude");
            this.currentLongitude = args.getDouble("currentLongitude");
            this.completeAddress = args.getString("completeAddress");
        }
    }

    //set option for single agent or multiple agent
    public void setSingleAgent(int position) {
        agentPosition = position;
        singleAgentFlag = true;

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

            llLegendAgent   = (LinearLayout) rootView.findViewById(R.id.llLegendAgent);
            llLegendShop    = (LinearLayout) rootView.findViewById(R.id.llLegendShop);
            llLegendYou     = (LinearLayout) rootView.findViewById(R.id.llLegendYou);

            if ( this.mobility.equals(DefineValue.STRING_NO) ) {
                llLegendAgent.setVisibility(View.GONE);
            } else {
                llLegendShop.setVisibility(View.GONE);
            }

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            sp                  = CustomSecurePref.getInstance().getmSecurePrefs();
        }

        //searchLocationEditText.setText(this.completeAddress);

        return rootView;
    }

    //Implements MainBbsResultReceiver.Receiver
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        //setelah proses camera position finish, maka proses ini baru boleh dijalankan :
        //if(resultCode == 0 && receiverStatus)
        //{
        //Toast.makeText(getActivity(), "update agent map", Toast.LENGTH_SHORT).show();
        //String errorMsg = resultData.getString("resultValue");

        //start buat marker dari awal
        //recreateAllMarker();
        //}
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

            if ( lines.size() > 0 ) {
                Polyline dataLine = lines.get(0);
                dataLine.remove();

                lines.clear();
            }


                String isSetPolyLine = shopDetails.get(agentPosition).getIsPolyline();
                if ( isSetPolyLine != null && isSetPolyLine.equals("1") ) {
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


            /*for (int idx = 0; idx < shopDetails.size(); idx++) {
                String isSetPolyLine = shopDetails.get(idx).getIsPolyline();

                if ( isSetPolyLine != null && isSetPolyLine.equals("1") ) {
                    String encodedPoints = shopDetails.get(idx).getEncodedPoints();
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
            }*/
        }
    }

    private void resetAllMarker() {
        if (globalMap != null && getActivity() != null) {
            //menunggu sampai view map sudah selesai di rendering semua, baru jalankan ini :
            globalMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
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
                }
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
        pickup = preferences.getString("pickupLocation", "N/A"); //get pickup point

        //convert string to double
        lastLatitude = Double.parseDouble(latitudeString);
        lastLongitude = Double.parseDouble(longitudeString);
    }

    private void getAgentLocationSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String dataJson = preferences.getString(AgentConstant.AGENT_INFO_SHARED_PREFERENCES, "");

        if (!dataJson.equals("")) {
            try {
                agentLocation = new JSONArray(dataJson);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void process() {
        hashMarker = new HashMap<>();

        thisClass = this;

        //get object activity
        mainBbsActivity = (BbsSearchAgentActivity) getActivity();

    }

    //implements View.OnClickListener
    @Override
    public void onClick(View view) {
        if (currentLatitude != null && currentLongitude != null)
            setCameraPosition(lastLatitude, lastLongitude);

        if (view.getId() == currentLocationBtn.getId()) {
            setCameraPosition(lastLatitude, lastLongitude);
        } else if (view.getId() == searchLocationBtn.getId()) {
            setCameraPosition(searchLatitude, searchLongitude);
        }
    }

    private void displayMap() {
        if ( mapFrag == null ) {
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
            globalMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback()
            {
                @Override
                public void onMapLoaded()
                {
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
                }
            });
        }

    }

    private void mapSetting()
    {
        try
        {
            globalMap.setMyLocationEnabled(false);
            globalMap.clear();
        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }

        //disable map gesture untuk sementara sampai camera position selesai
        globalMap.getUiSettings().setAllGesturesEnabled(true);
        globalMap.getUiSettings().setMapToolbarEnabled(false);
        globalMap.setIndoorEnabled(false);
    }

    private void setCameraPosition(Double latitude, Double longitude)
    {
        LatLng latLng = new LatLng(latitude, longitude);

        //add camera position and configuration
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng) // Center Set
                .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                //.tilt(30) // Tilt of the camera to 30 degrees
                .build(); // Creates a CameraPosition from the builder

        globalMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }



    private void setMapCamera()
    {
        if ( currentLongitude != null && currentLatitude != null ) {
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
                    //jika receiver dijalankan sebelum camera position selesai, maka map tidak akan ter-rendering sempurna
                    receiverStatus = true;

                    //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                    globalMap.getUiSettings().setAllGesturesEnabled(true);
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    private void setSearchMarker()
    {

        if (currentLatitude != null && currentLongitude != null && globalMap != null) {

            LatLng latLng = new LatLng(currentLatitude, currentLongitude);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    //.title("")
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.house));
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.search_location, 70, 90)));
            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            Marker marker = globalMap.addMarker(markerOptions);
            //hashMarker.put(0, marker);
        }
    }

    private void setLastMarker()
    {

    }

    //for resize icon
    public Bitmap resizeMapIcons(int image, int width, int height)
    {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), image);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void setAgentMarker()
    {
        if ( shopDetails.size() > 0 ) {
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

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    private void setAgentMarkerOption(int item)
    {

        Double latitude   = shopDetails.get(item).getShopLatitude();
        Double longitude  = shopDetails.get(item).getShopLongitude();
        //String nama = shopDetails.get(item).getMemberName();

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions;

        if ( shopDetails.get(item).getShopMobility().equals(DefineValue.STRING_NO) ) {
            markerOptions = new MarkerOptions()
                    .position(latLng)
                    //.title("")
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.map_home, 90, 90)));
        } else {
            markerOptions = new MarkerOptions()
                    .position(latLng)
                    //.title("")
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.map_person, 90, 90)));
        }

        Marker marker = globalMap.addMarker(markerOptions);
        hashMarker.put(item, marker);

    }


    //GoogleMap.OnMarkerClickListener
    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if ( shopDetails.size() > 0 && mobility.equals(DefineValue.STRING_NO) ) {
            for (Integer index : hashMarker.keySet()) {
                if (marker.equals(hashMarker.get(index))) {
                    this.shopDetails.get(index).setIsPolyline("1");

                } else {
                    this.shopDetails.get(index).setIsPolyline("0");
                }

//                if (marker.equals(hashMarker.get(index))) {
//                    ShopDetail shopDetail = this.shopDetails.get(index);
//                    FragmentManager fragmentManager = getFragmentManager();
//                    AgentDetailFragmentDialog agentDetailBbsFragmentDialog = new AgentDetailFragmentDialog();
//                    agentDetailBbsFragmentDialog.setAgentInfoSingle(shopDetail, index);
//                    agentDetailBbsFragmentDialog.setCurrentLatitude(currentLatitude);
//                    agentDetailBbsFragmentDialog.setCurrentLongitude(currentLongitude);
//                    agentDetailBbsFragmentDialog.setCancelable(false);
//                    agentDetailBbsFragmentDialog.setMobility(mobility);
//                    agentDetailBbsFragmentDialog.show(fragmentManager, AgentConstant.AGENT_DETAIL_FRAGMENT_DIALOG_TAG);
//                }
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
            if ( shopDetails.size() > 0 ) {


                this.shopDetails.addAll(shopDetails);


                //process();
            } else {

                if ( currentLatitude != null && currentLongitude != null ) {

                    setMapCamera();
                    setSearchMarker();
                }

                return;
            }



            if (this.currentLatitude != null && this.currentLongitude != null) {
                lastLatitude = this.currentLatitude;
                lastLongitude = this.currentLongitude;

                if ( hashMarker == null ) {
                    hashMarker = new HashMap<>();
                } else {
                    hashMarker.clear();
                }
                //if ( hashMarker.size() > 0 ) {
                    //hashMarker.clear();
                //}

                recreateAllMarker();

            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String searchLocationString = searchLocationEditText.getText().toString().trim();
        try
        {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

            List<Address> multiAddress = geocoder.getFromLocationName(searchLocationString, 1);

            if(multiAddress != null && !multiAddress.isEmpty() && multiAddress.size() > 0)
            {

                SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                SecurePreferences.Editor mEditor = prefs.edit();
                mEditor.putString(DefineValue.BBS_TX_ID, "");
                mEditor.apply();

                Address singleAddress = multiAddress.get(0);
                ArrayList<String> addressArray = new ArrayList<String>();

                for (int i = 0; i < singleAddress.getMaxAddressLineIndex(); i++) {
                    addressArray.add(singleAddress.getAddressLine(i));
                }

                String fullAddress = TextUtils.join(System.getProperty("line.separator"), addressArray);

                //changeMap(singleAddress.getLatitude(), singleAddress.getLongitude());
                currentLatitude = singleAddress.getLatitude();
                currentLongitude = singleAddress.getLongitude();

                mainBbsActivity = (BbsSearchAgentActivity) getActivity();
                mainBbsActivity.setCoordinate(currentLatitude, currentLongitude, searchLocationString);

                globalMap.clear();

                recreateAllMarker();

                searchLocationEditText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            }
            else
            {

            }
        }
        catch(IOException ioException)
        {
            // Catch network or other I/O problems.
            //errorMessage = "Catch : Network or other I/O problems - No geocoder available";
            Log.d("onIOException ", "Catch : Network or other I/O problems - No geocoder available");
        }
        catch(IllegalArgumentException illegalArgumentException)
        {
            // Catch invalid latitude or longitude values.
            //errorMessage = "Catch : Invalid latitude or longitude values";
            //Log.d("IllegalArgumentException ", "Catch : Invalid latitude or longitude values");

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
    public void OnIconLocationClickListener(int position, ArrayList<ShopDetail> shopDetails) {
        agentPosition = position;
        setPolyline();
    }
}
