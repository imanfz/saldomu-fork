package com.sgo.orimakardaya.coreclass;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/*
  Created by Administrator on 8/18/2014.
 */
public class BalanceHandler {

    SecurePreferences sp=null;
    Context mContext = null;
    String userID,accessKey;

    public static final long DISCONNECT_TIMEOUT = 30000; // 30 detik = 30 * 1000 ms

    private Handler disconnectHandler = new Handler(){
        public void handleMessage(Message msg) {
        }
    };

    private Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {
            sentData();
        }
    };

    public void resetDisconnectTimer(){
        disconnectHandler.removeCallbacks(disconnectCallback);
        disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT);
    }

    public void stopDisconnectTimer(){
        disconnectHandler.removeCallbacks(disconnectCallback);
    }

    public BalanceHandler(Context dContext, SecurePreferences mSp) {
        mContext = dContext;
        sp = mSp;
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

    }

    public void sentData(){
        try{

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_SALDO,
                    userID,accessKey);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get Balance handler:"+params.toString());

            MyApiClient.getSaldo(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response getBalance:"+response.toString());

                           setBalance(CurrencyFormat.format(response.getInt(WebParams.AMOUNT)));
                            setMonthlyLimit(CurrencyFormat.format(response.getInt(WebParams.REMAIN_LIMIT)),response.getString(WebParams.PERIOD_LIMIT));

                            SecurePreferences.Editor mEditor = sp.edit();
                            mEditor.putString(DefineValue.BALANCE, CurrencyFormat.format(response.getDouble(WebParams.AMOUNT)));
                            mEditor.apply();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            setLogout(message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(mContext, code, Toast.LENGTH_LONG).show();
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

                private void failure(Throwable throwable){
                    Timber.w("Error Koneksi get Saldo:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void setBalance(String _balance){
        Timber.w("masuk setBalance");
        if (mContext == null)
            return;

        MainPage fca = (MainPage) mContext;
        fca.setBalance(_balance);
    }

    private void setMonthlyLimit(String _limit, String _periode_limit){
        Timber.w("masuk setMonthlyLimit");
        if (mContext == null)
            return;

        MainPage fca = (MainPage) mContext;
        fca.setMonthlyLimit(_limit, _periode_limit);
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
