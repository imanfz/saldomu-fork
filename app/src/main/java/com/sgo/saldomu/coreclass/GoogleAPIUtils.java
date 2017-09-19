package com.sgo.saldomu.coreclass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Lenovo on 06/09/2017.
 */

public class GoogleAPIUtils {

    public static ArrayList<HashMap<String, String>> getResponseGoogleAPI(JSONObject response) {
        HashMap<String, String> hmObject = new HashMap<String, String>();
        try {
            JSONArray objResults        = response.getJSONArray("results");
            JSONObject objGeometry      = objResults.getJSONObject(0).getJSONObject("geometry");
            JSONObject objLocation      = objGeometry.getJSONObject("location");

            hmObject.put("latitude", objLocation.getString("lat"));
            hmObject.put("longitude", objLocation.getString("lng"));


            JSONArray addressComponents        = objResults.getJSONObject(0).getJSONArray("address_components");
            for(int i =0 ; i < addressComponents.length(); i++){
                JSONArray addressTypes  = addressComponents.getJSONObject(i).getJSONArray("types");
                for(int j=0;j<addressTypes.length();j++) {
                    String type = addressTypes.get(0).toString();
                    if ( type.equals("administrative_area_level_1") ) {
                        hmObject.put("province", addressComponents.getJSONObject(i).getString("long_name"));
                    } else if ( type.equals("administrative_area_level_2") ) {
                        hmObject.put("district", addressComponents.getJSONObject(i).getString("long_name"));
                    } else if ( type.equals("administrative_area_level_3") ) {
                        hmObject.put("subdistrict", addressComponents.getJSONObject(i).getString("long_name"));
                    } else if ( type.equals("postal_code") ) {
                        hmObject.put("postal_code", addressComponents.getJSONObject(i).getString("long_name"));
                    } else if ( type.equals("country") ) {
                        hmObject.put("country", addressComponents.getJSONObject(i).getString("long_name"));
                    }
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();
        data.add(hmObject);

        return data;
    }

}
