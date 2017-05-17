package com.sgo.hpku.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.Beans.HelpModel;
import com.sgo.hpku.R;
import com.sgo.hpku.adapter.HelpAdapter;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.DefinedDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

/*
 Created by thinkpad on 6/9/2015.
 */
public class HelpFragment extends Fragment {

    private SecurePreferences sp;
    private View v;
    private Activity act;
    private String ownerId;
    private String accessKey;

    private ArrayList<HelpModel> listHelp;
    private HelpAdapter mAdapter;
    private ProgressDialog progdialog;

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_help_center, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        act = getActivity();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        ownerId = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        listHelp = new ArrayList<>();
        ListView mListView = (ListView) v.findViewById(R.id.lvHelpCenter);

        getHelpList();

        mAdapter = new HelpAdapter(act, listHelp);
        mListView.setAdapter(mAdapter);
    }

    private void getHelpList() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(act, "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_USER_CONTACT_INSERT,
                    ownerId,accessKey);
            params.put(WebParams.USER_ID, ownerId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params help list:" + params.toString());

            MyApiClient.getHelpList(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params help list:"+response.toString());
                            String count = response.getString(WebParams.COUNT);
                            if(count.equals("0")) {
                                Timber.d("isi help list kosong");
                            }
                            else {
                                JSONArray mArrayContact = new JSONArray(response.getString(WebParams.CONTACT_DATA));

                                for (int i = 0; i < mArrayContact.length(); i++) {
                                    HelpModel helpModel = new HelpModel();
                                    helpModel.setId(mArrayContact.getJSONObject(i).getString(WebParams.ID));
                                    helpModel.setName(mArrayContact.getJSONObject(i).getString(WebParams.NAME));
                                    helpModel.setDesc(mArrayContact.getJSONObject(i).getString(WebParams.DESCRIPTION));
                                    helpModel.setPhone(mArrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE));
                                    helpModel.setMail(mArrayContact.getJSONObject(i).getString(WebParams.CONTACT_EMAIL));
                                    listHelp.add(helpModel);
                                }
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(act,message);
                        }
                        else {
                            Timber.d("isi error help list:"+response.toString());
                            Toast.makeText(act, message, Toast.LENGTH_LONG).show();
                        }

                        progdialog.dismiss();

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

                    if(progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi help list help:"+throwable.toString());
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

}
