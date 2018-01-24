package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.NotificationActivity;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/*
  Created by Administrator on 8/18/2014.
 */
public class NotificationHandler {

    private SecurePreferences sp=null;
    private Context mContext = null;
    private String userID;
    private String accessKey;

    public NotificationHandler(Context dContext, SecurePreferences mSp) {
        mContext = dContext;
        sp = mSp;
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
    }

    public void sentRetrieveNotif(){
        try{
            if(!sp.getString(DefineValue.USERID_PHONE,"").isEmpty()) {
                RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_NOTIF_RETRIEVE,
                        userID, accessKey);
                params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
                params.put(WebParams.MEMBER_ID, userID);
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());

                Timber.d("isi params Retrieve Notif Handler:" + params.toString());

                MyApiClient.sentRetrieveNotif(mContext, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);
                            Timber.w("isi response Retrieve Notif handler:" + response.toString());

                            if (code.equals(WebParams.SUCCESS_CODE) || code.equals(ErrorDefinition.NO_TRANSACTION)) {
                                JSONArray mArrayData = new JSONArray(response.getString(WebParams.NOTIF_DATA));
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
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:" + response.toString());
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                setLogout(message);
                            } else {
                                code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                                //Toast.makeText(mContext, code, Toast.LENGTH_LONG).show();
                                Timber.d("error Notification handler:" + code);
                            }

                        } catch (JSONException e) {

                            Toast.makeText(mContext, mContext.getString(R.string.internal_error), Toast.LENGTH_LONG).show();
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
                        Timber.w("Error Koneksi Notification Handler:" + throwable.toString());
                    }
                });
            }
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void setNotifCount(String _count){
        if (mContext == null)
            return;

        MainPage fca = (MainPage) mContext;
        fca.setNotifAmount(_count);
    }

    private void setLogout(String _message){
        Timber.w("masuk setLogout");
        if (mContext == null)
            return;

        MainPage fca = (MainPage) mContext;
        AlertDialogLogout test = AlertDialogLogout.getInstance();
        test.showDialoginMain(fca, _message);
    }
}
