package com.sgo.saldomu.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.BooleanResult;
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
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.models.ShopDetail;
import com.sgo.saldomu.services.UpdateLocationService;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.speech.tts.TextToSpeech;

import timber.log.Timber;

public class BbsMapNagivationActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, TextToSpeech.OnInitListener {

    Double targetLatitude, targetLongitude, currentLatitude, currentLongitude;
    Intent updateLocationIntent;
    SupportMapFragment mapFrag;
    private GoogleMap globalMap;
    TextView tvDirection;
    //TextToSpeech mTextToSpeech;
    List<Polyline> lines;
    Polyline line;
    String encodedPoints = "";
    String htmlDirections   = "";
    TextToSpeech textToSpeech;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeToolbar(getString(R.string.map_navigation));
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.agentMap);
        mapFrag.getMapAsync(this);
        mapFrag.getView().setVisibility(View.GONE);

        textToSpeech = new TextToSpeech(BbsMapNagivationActivity.this, BbsMapNagivationActivity.this);
        textToSpeech.setLanguage(Locale.getDefault());

        lines                   = new ArrayList<>();
        tvDirection             = (TextView) findViewById(R.id.tvDirection);
        Intent receiveIntent    = this.getIntent();
        targetLatitude          = receiveIntent.getDoubleExtra("targetLatitude", 0);
        targetLongitude         = receiveIntent.getDoubleExtra("targetLongitude", 0);
        currentLatitude         = receiveIntent.getDoubleExtra("currentLatitude", 0);
        currentLongitude        = receiveIntent.getDoubleExtra("currentLongitude", 0);

        //tvDirection.setText(Html.fromHtml("<h2>Title</h2><p>Description here</p>"));
        updateLocationIntent    = new Intent(this, UpdateLocationService.class);
        startService(updateLocationIntent);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("UpdateLocationIntent"));

        /*mTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        mTextToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);*/
    }


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_map_nagivation;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(updateLocationIntent);
        textToSpeech.shutdown();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentLatitude     = intent.getDoubleExtra("latitude", 0);
            currentLongitude    = intent.getDoubleExtra("longitude", 0);


            if ( globalMap != null ) {
                //mapFrag.getView().setVisibility(View.VISIBLE);
                setMapCamera();
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        stopService(updateLocationIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        //listener ketika button back di action bar diklik
        if(id == android.R.id.home)
        {
            //kembali ke activity sebelumnya
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void initializeToolbar(String title)
    {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(title);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        globalMap = googleMap;

        if ( globalMap != null ) {
            mapFrag.getView().setVisibility(View.VISIBLE);
            setMapCamera();
        }
    }

    private void setMapCamera()
    {
        if ( currentLongitude != 0 && currentLatitude != 0 ) {
            globalMap.clear();

            new GoogleMapRouteDirectionTask(targetLatitude, targetLongitude, currentLatitude, currentLongitude).execute();

            LatLng latLng = new LatLng(currentLatitude, currentLongitude);

            globalMap.getUiSettings().setMapToolbarEnabled(false);
            globalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    //.title("")
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.house));
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.ic_directions_car_black, 70, 90)));
            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            Marker marker = globalMap.addMarker(markerOptions);

            LatLng targetLatLng = new LatLng(targetLatitude, targetLongitude);
            MarkerOptions markerTargetOptions = new MarkerOptions()
                    .position(targetLatLng)
                    //.title("")
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.map_person, 90, 90)));
            globalMap.addMarker(markerTargetOptions);

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

                    //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                    globalMap.getUiSettings().setAllGesturesEnabled(true);
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    public void onPause(){
        /*if(mTextToSpeech !=null){
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }*/
        super.onPause();
    }

    private void setTargetMarker()
    {

        if (targetLatitude != 0 && targetLongitude != 0) {

            LatLng latLng = new LatLng(currentLatitude, currentLongitude);


        }
    }

    //for resize icon
    public Bitmap resizeMapIcons(int image, int width, int height)
    {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), image);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    @Override
    public void onInit(int Text2SpeechCurrentStatus) {
        if (Text2SpeechCurrentStatus == TextToSpeech.SUCCESS) {

            textToSpeech.setLanguage(Locale.getDefault());
            TextToSpeechFunction();
        }
    }

    private class GoogleMapRouteDirectionTask extends AsyncTask<Void, Void, Integer> {

        private ArrayList<ShopDetail> dataDetails = new ArrayList<>();
        private Double dataCurrentLatitude;
        private Double dataCurrentLongitude;
        private Double targetLatitude;
        private Double targetLongitude;

        public GoogleMapRouteDirectionTask(Double targetLatitude, Double targetLongitude, Double currentLatitude, Double currentLongitude)
        {
            this.targetLatitude = targetLatitude;
            this.targetLongitude = targetLongitude;
            dataCurrentLatitude = currentLatitude;
            dataCurrentLongitude = currentLongitude;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if ( !htmlDirections.equals("") ) {
                tvDirection.setText(Html.fromHtml(htmlDirections));
                TextToSpeechFunction();
            }
            setPolyline();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            String nextParams = "origin="+dataCurrentLatitude.toString()+","+dataCurrentLongitude.toString();
            nextParams += "&sensor=false";
            nextParams += "&units=metric";
            nextParams += "&mode="+DefineValue.GMAP_MODE;
            nextParams += "&language="+Locale.getDefault().getLanguage();

            RequestParams rqParams = new RequestParams();
            rqParams.put("origin", dataCurrentLatitude.toString()+","+dataCurrentLongitude.toString());
            rqParams.put("sensor", "false");
            rqParams.put("units", "metric");
            rqParams.put("mode", DefineValue.GMAP_MODE);
            rqParams.put("language", Locale.getDefault().getLanguage() );


            String tempParams = nextParams;
            tempParams += "&destination=" + targetLatitude.toString() + "," + targetLongitude.toString();

            getGoogleMapRoute(tempParams, 0);
            return null;
        }

    }

    public void getGoogleMapRoute(String tempParams, final int idx) {
        MyApiClient.getGoogleMapRoute(getApplicationContext(), tempParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Timber.w("Error Koneksi login:" + response.toString());
                try {

                    JSONArray array = response.getJSONArray("routes");
                    JSONObject routes = array.getJSONObject(0);
                    JSONArray legs = routes.getJSONArray("legs");
                    JSONObject steps = legs.getJSONObject(0);
                    JSONObject distance = steps.getJSONObject("distance");
                    String parsedDistance = distance.getString("text");

                    JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");
                    String points = overviewPolyline.getString("points");

                    encodedPoints = points;

                    JSONArray directions = steps.getJSONArray("steps");

                    if ( directions.length() > 0 ) {
                        JSONObject toDirection = directions.getJSONObject(0);
                        htmlDirections = toDirection.getString("html_instructions");

                        JSONArray toDistanceArray = toDirection.getJSONArray("distance");
                        JSONObject toDistanceObject = toDistanceArray.getJSONObject(0);
                        String toDistanceString = toDistanceObject.getString("text");

                        htmlDirections += " ( " + toDistanceString + " ) ";
                        //tvDirection.setText(Html.fromHtml(toDirection.getString("html_instructions")));
                    }




                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                ifFailure(throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                ifFailure(throwable);
            }

            private void ifFailure(Throwable throwable) {

                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });



    }

    public void setPolyline() {
        List<LatLng> list = new ArrayList<>();

        if ( !encodedPoints.equals("") && globalMap != null) {

            if ( lines.size() > 0 ) {
                Polyline dataLine = lines.get(0);
                dataLine.remove();

                lines.clear();
            }


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

    public void TextToSpeechFunction()
    {
        if ( !htmlDirections.equals("") ) {
            textToSpeech.speak(android.text.Html.fromHtml(htmlDirections).toString(), TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
