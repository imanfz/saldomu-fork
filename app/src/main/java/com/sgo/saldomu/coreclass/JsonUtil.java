package com.sgo.saldomu.coreclass;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by yuddistirakiki on 3/20/17.
 */

public class JsonUtil {

    private Context mContext;

    public JsonUtil(Context context){
        this.mContext = context;
    }

    public JSONObject readFromRaw(int idFile){
        JSONObject json = null;
        try {

            InputStream is = mContext.getResources().openRawResource(idFile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, "UTF-8");
            json = new JSONObject(jsonString);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
