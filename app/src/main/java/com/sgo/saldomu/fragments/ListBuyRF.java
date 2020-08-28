package com.sgo.saldomu.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.Account_Collection_Model;
import com.sgo.saldomu.Beans.Biller_Data_Model;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.interfaces.ObjListeners;

import org.joda.time.DateTimeComparator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

/*
  Created by Administrator on 1/30/2015.
 */
public class ListBuyRF extends Fragment{

    public static final String LISTBUYRF_TAG = "listbuyRF";

    View v;
    private String userID;
    private String accessKey;
    private SecurePreferences sp;

    private Realm realm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        realm = Realm.getDefaultInstance();

        getDataBiller();
    }


    private void getDataBiller(){
        String date = realm.where(Biller_Type_Data_Model.class).findFirst().getLast_update();

        int compare = 100;
        if(date != null) {
            Date dob = DateTimeFormat.convertStringtoCustomDate(date);
            Date now = DateTimeFormat.getCurrDate();

            if (dob != null) {
                if (now != null) {
                    compare = DateTimeComparator.getDateOnlyInstance().compare(dob,now);
                }
            }
        }

        if(compare != 0)
            getBiller();
    }


    private void getBiller(){
        try{
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_GET_BILLER_TYPE,
                    "");

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_BILLER_TYPE, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("Isi response get Biller Type:" + response.toString());
                                    String arrayBiller = response.getString(WebParams.BILLER_TYPE_DATA);
                                    getDataCollection(new JSONArray(arrayBiller));
                                } else {
                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
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
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void getDataCollection(final JSONArray arrayBiller){
        try{

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_COMM_ACCOUNT_COLLECTION);

            params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.CUST_ID, "") );
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("Isi params CommAccountCollection:"+params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_COMM_ACCOUNT_COLLECTION, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
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

                            } catch (JSONException e) {
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
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


    private void initializeData(final JSONArray arrayBiller, final JSONArray arrayCollection){
        final String curr_date = DateTimeFormat.getCurrentDate();


        Biller_Type_Data_Model mBillerObj;
        RealmResults<Biller_Type_Data_Model> ResultData = realm.where(Biller_Type_Data_Model.class).findAll();
        List<Biller_Type_Data_Model> temptData = realm.copyFromRealm(ResultData);

        realm.beginTransaction();

        if (arrayBiller.length() > 0) {
            realm.delete(Biller_Type_Data_Model.class);
            Biller_Data_Model mListBillerData;
            int i;
            int j;
            int k;
            for ( i= 0; i < arrayBiller.length(); i++) {
                try {
                    mBillerObj = realm.createObjectFromJson(Biller_Type_Data_Model.class, arrayBiller.getJSONObject(i));
                    mBillerObj.setLast_update(curr_date);

                    for (j = 0; j < temptData.size(); j++) {
                        if (mBillerObj.getBiller_type_code().equals(temptData.get(j).getBiller_type_code())) {
                            for ( k = 0; k < temptData.get(j).getBiller_data_models().size(); k++) {
                                mListBillerData = realm.where(Biller_Data_Model.class).
                                        equalTo("comm_id", temptData.get(j).getBiller_data_models().get(k).getComm_id()).
                                        equalTo("comm_name", temptData.get(j).getBiller_data_models().get(k).getComm_name()).
                                        findFirst();
                                mBillerObj.getBiller_data_models().add(mListBillerData);
                            }
                            break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Boolean isHave = false;
            Biller_Data_Model delList;
            for(i = 0 ; i < temptData.size() ; i++){
                for (j = 0 ; j < ResultData.size() ; j++){
                    if(temptData.get(i).getBiller_type_code().equals(ResultData.get(j).getBiller_type_code())){
                        isHave = true;
                    }
                }

                if(!isHave){
                    for ( j = 0; j < temptData.get(i).getBiller_data_models().size(); j++) {
                        delList = realm.where(Biller_Data_Model.class).
                                equalTo("comm_id", temptData.get(i).getBiller_data_models().get(j).getComm_id()).
                                equalTo("comm_name", temptData.get(i).getBiller_data_models().get(j).getComm_name()).
                                findFirst();
                        if(delList.getDenom_data_models().size()>0)
                            delList.getDenom_data_models().deleteAllFromRealm();
                        delList.deleteFromRealm();
                    }
                }
                isHave = false;
            }
        }


        if (arrayCollection.length() > 0) {
          realm.delete(Account_Collection_Model.class);
          Account_Collection_Model mACLobj;
          try {
              for (int i = 0; i < arrayCollection.length(); i++) {
                  mACLobj = realm.createObjectFromJson(Account_Collection_Model.class, arrayCollection.getJSONObject(i));
                  mACLobj.setLast_update(curr_date);
              }
          } catch (JSONException e) {
              e.printStackTrace();
          }
        }

        realm.commitTransaction();
    }

    @Override
    public void onDestroy() {
        if(!realm.isInTransaction() && !realm.isClosed())
            RealmManager.closeRealm(realm);
        super.onDestroy();
    }
}