package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.CashoutActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.InformationDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import timber.log.Timber;

/*
  Created by Administrator on 11/5/2014.
 */
public class ListCashOut extends ListFragment {

    private SecurePreferences sp;
    private View v;
    private InformationDialog dialogI;
    private String userID;
    private String accessKey;
    private String memberID;
    private Boolean is_full_activity = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_topup, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        memberID = sp.getString(DefineValue.MEMBER_ID, "");

        Bundle mArgs = getArguments();
        if(mArgs != null && !mArgs.isEmpty())
            is_full_activity = mArgs.getBoolean(DefineValue.IS_ACTIVITY_FULL,false);

        ArrayList<String> _listType = new ArrayList<>();
        Collections.addAll(_listType, getResources().getStringArray(R.array.list_cash_out));

        EasyAdapter adapter = new EasyAdapter(getActivity(), R.layout.list_view_item_with_arrow, _listType);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

        dialogI = InformationDialog.newInstance(4);
        dialogI.setTargetFragment(this,0);

        if(isAdded())
            getBankCashout();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void getBankCashout(){
        try {
            if (isAdded() || isVisible()) {
                final ProgressDialog prodDialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

                RequestParams params =  MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_BANKCASHOUT,
                        userID, accessKey);
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                params.put(WebParams.MEMBER_ID, memberID );
                params.put(WebParams.USER_ID, userID);

                Timber.d("isi params get Bank cashout:" + params.toString());

                MyApiClient.getBankCashout(getActivity(), params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Timber.d("response Listbank cashout:" + response.toString());
                                if (isAdded()) {
                                    SecurePreferences.Editor mEditor = sp.edit();
                                    mEditor.putString(DefineValue.BANK_CASHOUT, response.optString(WebParams.BANK_CASHOUT, ""));
                                    mEditor.apply();
                                    prodDialog.dismiss();
                                }
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:" + response.toString());
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                if (is_full_activity)
                                    test.showDialoginActivity(getActivity(), message);
                                else
                                    test.showDialoginMain(getActivity(), message);
                            } else {
                                Timber.d("Error bank cashout:" + response.toString());
                                code = response.getString(WebParams.ERROR_MESSAGE);
                                prodDialog.dismiss();
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
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            if (MyApiClient.PROD_FAILURE_FLAG)
                                Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                            if (prodDialog.isShowing())
                                prodDialog.dismiss();
                        }
                        Timber.w("Error Koneksi bank list list topup:" + throwable.toString());
                    }
                });
            }
        }catch(Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i;
//        String itemName = String.valueOf(l.getAdapter().getItem(position));
        if(position == 0) {
            i = new Intent(getActivity(), CashoutActivity.class);
            i.putExtra(DefineValue.CASHOUT_TYPE,DefineValue.CASHOUT_BANK);
            switchActivity(i);
        }
        else if(position == 1) {
            i = new Intent(getActivity(), CashoutActivity.class);
            i.putExtra(DefineValue.CASHOUT_TYPE,DefineValue.CASHOUT_AGEN);
            switchActivity(i);
        }

    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getActivity().getSupportFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}