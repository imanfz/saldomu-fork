package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.NotificationActivity;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.NotifModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/*
  Created by Administrator on 8/18/2014.
 */
public class NotificationHandler {

    private SecurePreferences sp;
    private Context mContext;
    private String userID;

    public NotificationHandler(Context dContext, SecurePreferences mSp) {
        mContext = dContext;
        sp = mSp;
        userID = sp.getString(DefineValue.USERID_PHONE, "");
    }

    public void sentRetrieveNotif() {
        try {
            if (!sp.getString(DefineValue.USERID_PHONE, "").isEmpty()) {
                HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_NOTIF_RETRIEVE);
                params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
                params.put(WebParams.MEMBER_ID, userID);
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());

                Timber.d("isi params Retrieve Notif Handler:%s", params.toString());

                RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_NOTIF_RETRIEVE, params,
                        new ResponseListener() {
                            @Override
                            public void onResponses(JsonObject object) {
                                try {
                                    Gson gson = new Gson();
                                    NotifModel model = gson.fromJson(object, NotifModel.class);

                                    String code = model.getError_code();
                                    String message = model.getError_message();
                                    if (code.equals(WebParams.SUCCESS_CODE) || code.equals(ErrorDefinition.NO_TRANSACTION)) {
                                        JSONArray mArrayData = new JSONArray(gson.toJson(model.getData_user_notif()));
                                        int idx = 0;
                                        JSONObject mObject;
                                        for (int i = 0; i < mArrayData.length(); i++) {
                                            mObject = mArrayData.getJSONObject(i);
                                            if (mObject != null) {
                                                if (mObject.getInt(WebParams.NOTIF_READ) == NotificationActivity.UNREAD) {
                                                    String notif_detail_string = mArrayData.getJSONObject(i).optString(WebParams.NOTIF_DETAIL, "");
                                                    if (!notif_detail_string.isEmpty())
                                                        idx++;
                                                }
                                            }
                                        }
                                        setNotifCount(String.valueOf(idx));
                                        //setNotifCount(response.getString(WebParams.UNREAD));
                                    } else {
                                        //Toast.makeText(mContext, code, Toast.LENGTH_LONG).show();
                                        Timber.d("error Notification handler:" + code + ":" + message);
                                    }

                                } catch (JSONException e) {

                                    Toast.makeText(mContext, mContext.getString(R.string.internal_error), Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void setNotifCount(String _count) {
        if (mContext == null)
            return;

        MainPage fca = (MainPage) mContext;
        fca.setNotifAmount(_count);
    }
}
