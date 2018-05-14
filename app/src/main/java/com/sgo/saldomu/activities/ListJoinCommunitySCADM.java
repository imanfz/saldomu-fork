package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.SCADMModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.ListSCADMAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.widgets.BaseActivity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/8/2018.
 */

public class ListJoinCommunitySCADM extends BaseActivity {
    SecurePreferences sp;
    private ProgressDialog progdialog;
    private RecyclerView recyclerView;
    private ListSCADMAdapter listSCADMAdapter;
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    private ArrayList<SCADMModel> scadmModelArrayList = new ArrayList<>();

    @Override
    protected int getLayoutResource() {
        return R.layout.list_join_community_scadm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        recyclerView = findViewById(R.id.recyclerView);

        InitializeToolbar();
        initializeAdapter();

        getListCommunity();
    }

    private void initializeAdapter() {
        listSCADMAdapter = new ListSCADMAdapter();
        recyclerView.setAdapter(listSCADMAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    public void InitializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.scadm_join));
    }

    public void getListCommunity() {
        try {

            progdialog = DefinedDialog.CreateProgressDialog(this, "");

            RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_GET_LIST_COMMUNITY_SCADM,
                    userPhoneID, accessKey);
            params.put(WebParams.COMM_ID_REMARK, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params get list community scadm:" + params.toString());

            JsonHttpResponseHandler mHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi response get list community scadm:" + response.toString());




                            listSCADMAdapter.updateData(scadmModelArrayList);

//                            if(isAdded())
//                                initializeLayout();
//                            else
                            progdialog.dismiss();

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(ListJoinCommunitySCADM.this, message);
                        } else {
                            Timber.d("Error isi responce get list community scadm:" + response.toString());
                            code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);

                            Toast.makeText(ListJoinCommunitySCADM.this, code, Toast.LENGTH_LONG).show();
                            finish();
                        }
                        if (progdialog.isShowing())
                            progdialog.dismiss();

                    } catch (JSONException e) {
                        progdialog.dismiss();
                        Toast.makeText(ListJoinCommunitySCADM.this, getString(R.string.internal_error), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(ListJoinCommunitySCADM.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ListJoinCommunitySCADM.this, throwable.toString(), Toast.LENGTH_SHORT).show();

                    if (progdialog.isShowing())
                        progdialog.dismiss();
                    getFragmentManager().popBackStack();
                    Timber.w("Error Koneksi get list community scadm:" + throwable.toString());
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
//                    if(!isAdded())
//                        MyApiClient.CancelRequestWS(getActivity(), true);
                }

                @Override
                public void onCancel() {
                    super.onCancel();
                    if (progdialog.isShowing())
                        progdialog.dismiss();
                }
            };

            MyApiClient.getListCommunitySCADM(ListJoinCommunitySCADM.this, params, mHandler);
//            if(!isAdded())
            //MyApiClient.getClient().cancelRequests(get);

        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}