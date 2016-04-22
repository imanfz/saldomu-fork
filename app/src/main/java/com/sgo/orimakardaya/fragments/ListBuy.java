package com.sgo.orimakardaya.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.BuyFragmentTabAdapter;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DateTimeFormat;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.dialogs.InformationDialog;
import com.viewpagerindicator.TabPageIndicator;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

/*
  Created by Administrator on 1/30/2015.
 */
public class ListBuy extends Fragment implements InformationDialog.OnDialogOkCallback {

    View v,layout_empty;
    TabPageIndicator tabs;
    ViewPager pager;
    BuyFragmentTabAdapter adapternya;
    ProgressDialog out;
    String userID,accessKey;
    private InformationDialog dialogI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_buy, container, false);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        tabs = (TabPageIndicator)v.findViewById(R.id.buy_tabs);
        pager = (ViewPager) v.findViewById(R.id.buy_pager);
        layout_empty = v.findViewById(R.id.empty_layout);

        layout_empty.setVisibility(View.GONE);
        Button btn_refresh = (Button) layout_empty.findViewById(R.id.btnRefresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataBiller();
            }
        });

        pager.setPageMargin(pageMargin);
        dialogI = InformationDialog.newInstance(this,8);
        getDataBiller();

    }

    public void getDataBiller(){
        try{
            out = DefinedDialog.CreateProgressDialog(getActivity(), null);

            MyApiClient.getBillerType(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response get Biller Type:" + response.toString());
                            String arrayBiller = response.getString(WebParams.BILLER_TYPE_DATA);
                            getDataCollection(new JSONArray(arrayBiller));
                        } else {
                            out.dismiss();
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
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if (out.isShowing())
                        out.dismiss();

                    Timber.w("Error Koneksi biller data list buy:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void getDataCollection(final JSONArray arrayBiller){
        try{
            SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_COMM_ACCOUNT_COLLECTION,
                    userID,accessKey);

            params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.CUST_ID, "") );
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("Isi params CommAccountCollection:"+params.toString());

            MyApiClient.sentCommAccountCollection(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Timber.d("Isi response CommAccountCollection:"+response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {
                            JSONArray arrayCollection ;
                            String data = response.getString(WebParams.COMMUNITY);
                            if(code.equals("0003")||data.equals("")){
                                arrayCollection = new JSONArray();
                            }
                            else
                                arrayCollection = new JSONArray(data);
                            initializeData(arrayBiller,arrayCollection);
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }
                        else {
                            out.dismiss();
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

                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(out.isShowing())
                        out.dismiss();

                    Timber.w("Error Koneksi collect data list buy:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void initializeData(JSONArray arrayBiller, JSONArray arrayCollection){

        ArrayList<String> Title_tab = new ArrayList<>();
        HashMap<String,String> mPurchase = new HashMap<>();
        HashMap<String,String> mPayment = new HashMap<>();
        if(arrayBiller.length()>0){
            for (int i = 0; i < arrayBiller.length(); i++) {
                try {
                if(arrayBiller.getJSONObject(i).getString(WebParams.BILLER_TYPE).equals(DefineValue.BIL_TYPE_BUY)){
                    mPurchase.put(arrayBiller.getJSONObject(i).getString(WebParams.BILLER_TYPE_NAME),
                            arrayBiller.getJSONObject(i).getString(WebParams.BILLER_TYPE_CODE));
                }
                else {
                    mPayment.put(arrayBiller.getJSONObject(i).getString(WebParams.BILLER_TYPE_NAME),
                            arrayBiller.getJSONObject(i).getString(WebParams.BILLER_TYPE_CODE));
                }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(!mPurchase.isEmpty())
                Title_tab.add(getString(R.string.purchase));
            if(!mPayment.isEmpty())
                Title_tab.add(getString(R.string.payment));
        }

        if(arrayCollection.length() > 0) {
            Title_tab.add(getString(R.string.collection));
        }

        if(Title_tab.isEmpty())
            layout_empty.setVisibility(View.VISIBLE);
        else
            layout_empty.setVisibility(View.GONE);

        adapternya = new BuyFragmentTabAdapter(getChildFragmentManager(),getActivity(),mPurchase,
                mPayment,arrayCollection,Title_tab);

        pager.setAdapter(adapternya);
        tabs.setViewPager(pager);
        out.dismiss();
        pager.setVisibility(View.VISIBLE);
        tabs.setVisibility(View.VISIBLE);

    }

    private void switchFragment(android.support.v4.app.Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void onOkButton() {

    }
}