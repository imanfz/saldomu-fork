package com.sgo.hpku.coreclass;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.ResultReceiver;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lenovo Thinkpad on 12/5/2016.
 */
public class MainAgentIntentService extends IntentService {

    private Location lastLocation;

    public MainAgentIntentService() {
        super("MainAgentIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        lastLocation = intent.getParcelableExtra("location");

        setLastLocation();

        boolean searchLocationChecked = intent.getBooleanExtra("searchLocationChecked",false);
        if(!searchLocationChecked) setPickupLocation();

        ResultReceiver agentMapResultReceiver = intent.getParcelableExtra("agentMapResultReceiver");
        ResultReceiver agentListMapResultReceiver = intent.getParcelableExtra("agentListMapResultReceiver");

        //beritahu dan refresh fragment agent map bahwa ada location terbaru
        agentMapResultReceiver.send(0, null);

        //beritahu dan refresh fragment agent list map bahwa ada data agent terbaru
        agentListMapResultReceiver.send(0, null);

        /*String val = intent.getStringExtra("foo");
        Bundle bundle = new Bundle();
        bundle.putString("resultValue", "My Result Value");*/
    }

    private void setLastLocation()
    {
        try
        {
            if(lastLocation != null)
            {
                //save data to session
                String latitudeString  = Double.toString(lastLocation.getLatitude());
                String longitudeString = Double.toString(lastLocation.getLongitude());

                SharedPreferences preferences = getSharedPreferences(AgentConstant.LAST_LOCATION_SHARED_PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("latitude", latitudeString);
                editor.putString("longitude", longitudeString);
                editor.apply();
            }
        }
        catch (SecurityException e)
        {

        }
    }

    private void setPickupLocation()
    {
        if(lastLocation != null)
        {
            try
            {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                List<Address> multiAddress = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);

                Address singleAddress = multiAddress.get(0);
                ArrayList<String> addressArray = new ArrayList<String>();

                for (int i = 0; i < singleAddress.getMaxAddressLineIndex(); i++) {
                    addressArray.add(singleAddress.getAddressLine(i));
                }

                String fullAddress = TextUtils.join(" ", addressArray);

                SharedPreferences preferences = getSharedPreferences(AgentConstant.LAST_LOCATION_SHARED_PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("pickupLocation", fullAddress);
                editor.apply();

            } catch (IOException ioException) {

            } catch (IllegalArgumentException illegalArgumentException) {

            }
        }
    }
}
