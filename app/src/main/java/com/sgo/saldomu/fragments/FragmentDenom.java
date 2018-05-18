package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MyProfileActivity;
import com.sgo.saldomu.coreclass.MyApiClient;

import java.util.ArrayList;

public class FragmentDenom extends Fragment{

    View v;
    ArrayAdapter<String> bankProductAdapter;

    TextView itemName;

    ArrayList<String> bankProductList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_denom, container,false);
        itemName = v.findViewById(R.id.frag_denom_item_name_field);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bankProductAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, bankProductList);
        bankProductAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        itemName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    void getBankProductList(){
        try{

//            extraSignature = txId + comm_id;
//            RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin,MyApiClient.LINK_GET_TRX_STATUS,
//                    userPhoneID,accessKey, extraSignature);
//
//            params.put(WebParams.TX_ID, txId);
//
//            Timber.d("isi params sent get Trx Status:"+params.toString());
//
//            MyApiClient.sentGetTRXStatus(getActivity(),params, new JsonHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                    try {
//                        progdialog.dismiss();
//                        Timber.d("isi response sent get Trx Status:"+response.toString());
//                        String code = response.getString(WebParams.ERROR_CODE);
//                        if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {
//
//                            String txstatus = response.getString(WebParams.TX_STATUS);
//                            showReportBillerDialog(sp.getString(DefineValue.USER_NAME, ""), DateTimeFormat.formatToID(response.optString(WebParams.CREATED, "")),
//                                    sp.getString(DefineValue.USERID_PHONE, ""), txId, item_name,
//                                    txstatus, response.optString(WebParams.TX_REMARK, ""), _amount,response, response.optString(WebParams.BILLER_DETAIL),
//                                    response.optString(WebParams.BUSS_SCHEME_CODE), response.optString(WebParams.BUSS_SCHEME_NAME), response.optString(WebParams.PRODUCT_NAME));
//                        } else if(code.equals(WebParams.LOGOUT_CODE)){
//                            Timber.d("isi response autologout:"+response.toString());
//                            String message = response.getString(WebParams.ERROR_MESSAGE);
//                            AlertDialogLogout test = AlertDialogLogout.getInstance();
//                            test.showDialoginActivity(getActivity(),message);
//                        }
//                        else {
//                            String msg = response.getString(WebParams.ERROR_MESSAGE);
//                            showDialog(msg);
//                        }
//
//                        btn_submit.setEnabled(true);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    super.onFailure(statusCode, headers, responseString, throwable);
//                    failure(throwable);
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                    super.onFailure(statusCode, headers, throwable, errorResponse);
//                    failure(throwable);
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                    super.onFailure(statusCode, headers, throwable, errorResponse);
//                    failure(throwable);
//                }
//
//                private void failure(Throwable throwable){
//                    if(MyApiClient.PROD_FAILURE_FLAG)
//                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
//                    else
//                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
//
//                    if(progdialog.isShowing())
//                        progdialog.dismiss();
//                    btn_submit.setEnabled(true);
//                    Timber.w("Error Koneksi trx stat biller confirm:"+throwable.toString());
//                }
//            });
        }catch (Exception e){
            e.printStackTrace();
//            Timber.d("httpclient:"+e.getMessage());
        }
    }

    void getItemID(){

    }
}
