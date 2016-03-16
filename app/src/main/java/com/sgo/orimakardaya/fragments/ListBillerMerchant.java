package com.sgo.orimakardaya.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.BillerActivity;
import com.sgo.orimakardaya.adapter.EasyAdapter;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/*
  Created by Administrator on 3/2/2015.
 */
public class ListBillerMerchant extends ListFragment {

    View v;
    String billerData,nameToolbar,biller_type,userID,accessKey;
    List<ListObject> mdata;
    String[] _data;
    ProgressDialog out;


    private class ListObject{
        private String biller_name;
        private String item_id;
        private String comm_id;
        private String callback_url;
        private String api_key;
        private String comm_code;

        public ListObject(String _billerName,String _itemId, String _comm_id, String _comm_code, String _api_key, String _callback_url){
            this.setBiller_name(_billerName);
            this.setItem_id(_itemId);
            this.setComm_id(_comm_id);
            this.setComm_code(_comm_code);
            this.setApi_key(_api_key);
            this.setCallback_url(_callback_url);
        }

        public String getBiller_name() {
            return biller_name;
        }

        public void setBiller_name(String biller_name) {
            this.biller_name = biller_name;
        }

        public String getItem_id() {
            return item_id;
        }

        public void setItem_id(String item_id) {
            this.item_id = item_id;
        }

        public String getComm_id() {
            return comm_id;
        }

        public void setComm_id(String comm_id) {
            this.comm_id = comm_id;
        }

        public String getComm_code() {
            return comm_code;
        }

        public void setComm_code(String comm_code) {
            this.comm_code = comm_code;
        }

        public String getCallback_url() {
            return callback_url;
        }

        public void setCallback_url(String callback_url) {
            this.callback_url = callback_url;
        }

        public String getApi_key() {
            return api_key;
        }

        public void setApi_key(String api_key) {
            this.api_key = api_key;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_list_biller_tab, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        initializeData();

        EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

    }

    public void initializeData(){
        Bundle args = getArguments();
        billerData = args.getString(DefineValue.BILLER_DATA);
        nameToolbar = args.getString(DefineValue.BILLER_NAME);
        biller_type = args.getString(DefineValue.BILLER_TYPE);
        setActionBarTitle(getString(R.string.biller_ab_title) + "-" + nameToolbar);
        mdata = new ArrayList<ListObject>();

        try {
            JSONArray mArray = new JSONArray(billerData);
            _data = new String[mArray.length()];
            for (int i = 0 ;i< mArray.length();i++){
                _data[i] = mArray.getJSONObject(i).getString(WebParams.COMM_NAME);
                mdata.add(new ListObject(mArray.getJSONObject(i).getString(WebParams.COMM_NAME),
                                         mArray.getJSONObject(i).getString(WebParams.DENOM_ITEM_ID),
                                         mArray.getJSONObject(i).getString(WebParams.COMM_ID),
                                         mArray.getJSONObject(i).getString(WebParams.COMM_CODE),
                                         mArray.getJSONObject(i).getString(WebParams.API_KEY),
                                         mArray.getJSONObject(i).getString(WebParams.CALLBACK_URL)
                        ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String _merchant_name = l.getAdapter().getItem(position).toString();

        if(mdata.get(position).getItem_id().isEmpty())
            getDenomRetail(_merchant_name, mdata.get(position).getComm_id(),
                    mdata.get(position).getComm_code(),mdata.get(position).getApi_key(),mdata.get(position).getCallback_url());
        else
            changeToInputBiller(null,_merchant_name,mdata.get(position).getItem_id(),
                    mdata.get(position).getComm_id(),mdata.get(position).getComm_code(),mdata.get(position).getApi_key(),
                    mdata.get(position).getCallback_url());
    }


    /*public void getCommEspay(final String comm_id, final int position){
        try{
            out = DefinedDialog.CreateProgressDialog(getActivity(), null);
            out.show();

            RequestParams params = new RequestParams();
            params.put(WebParams.COMM_ID, comm_id);

            Timber.d("isi params sent Comm Espay", params.toString());

            MyApiClient.sentCommEspay(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Timber.d("Isi response comm Espay", response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            if(mdata.get(position).getItem_id().isEmpty())
                                getDenomRetail(mdata.get(position).getBiller_name(), mdata.get(position).getComm_id(),
                                        mdata.get(position).getComm_code(),response.getString(WebParams.API_KEY));
                            else {
                                changeToInputBiller(null, mdata.get(position).getBiller_name(), mdata.get(position).getItem_id(),
                                        mdata.get(position).getComm_id(), mdata.get(position).getComm_code(), response.getString(WebParams.API_KEY));
                                out.dismiss();
                            }

                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            getActivity().finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.w("Error Koneksi get Biller Type", throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient", e.getMessage());
        }
    }*/

    public void getDenomRetail( final String _merchant_name, final String _comm_id, final String _comm_code, final String _api_key,final String _callback_url){
        try{

            out = DefinedDialog.CreateProgressDialog(getActivity(), null);

            RequestParams params = MyApiClient.getSignatureWithParams(_comm_id,MyApiClient.LINK_DENOM_RETAIL,
                    userID,accessKey);
            //params.put(WebParams.BILLER_ID, _biller_id);
            params.put(WebParams.COMM_ID, _comm_id);
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params sent Denom Retail list biller merchant:" + params.toString());

            MyApiClient.sentDenomRetail(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        out.dismiss();
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response Denom list biller merchant:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            String arrayDenom = response.getString(WebParams.DENOM_DATA);
                            changeToInputBiller(arrayDenom, _merchant_name, null, _comm_id, _comm_code,_api_key,_callback_url);
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
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

                    Timber.w("Error Koneksi denom retail list bil merchant:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void changeToInputBiller(String _data_denom, String _name_merchant, String _biller_item_id, String _comm_id,
                                     String _comm_code, String _api_key,String _callback_url){
        Bundle mArgs = new Bundle();
        mArgs.putString(DefineValue.DENOM_DATA,_data_denom);
        mArgs.putString(DefineValue.BILLER_TYPE,biller_type);
        mArgs.putString(DefineValue.BILLER_ITEM_ID,_biller_item_id);
        mArgs.putString(DefineValue.BILLER_COMM_ID,_comm_id);
        mArgs.putString(DefineValue.BILLER_COMM_CODE,_comm_code);
        mArgs.putString(DefineValue.BILLER_API_KEY,_api_key);
        mArgs.putString(DefineValue.BILLER_NAME,_name_merchant);
        mArgs.putString(DefineValue.CALLBACK_URL,_callback_url);

        BillerInput mBI = new BillerInput() ;
        mBI.setArguments(mArgs);

        String fragname = nameToolbar+"-"+_name_merchant;

        switchFragment(mBI,BillerActivity.FRAG_BIL_LIST_MERCHANT,fragname,true);
    }

    private void switchFragment(android.support.v4.app.Fragment i, String name,String next_name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.switchContent(i,name,next_name,isBackstack);
    }

    private void setActionBarTitle(String _title){
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.setToolbarTitle(_title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}