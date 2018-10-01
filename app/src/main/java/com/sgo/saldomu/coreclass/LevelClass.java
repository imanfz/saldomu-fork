package com.sgo.saldomu.coreclass;/*
  Created by Administrator on 3/6/2015.
 */

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.MyProfileNewActivity;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

public class LevelClass {

    private FragmentActivity activity;
    private String listAddress, listContactPhone;
    private SecurePreferences sp;
    private Boolean isAllowedLevel,isLevel1,isRegisteredLevel;
//    private ProgressDialog progdialog;


    public LevelClass(FragmentActivity activity, SecurePreferences sp) {
        this.setActivity(activity);
        this.setSp(sp);
        refreshData();
    }

    public LevelClass(FragmentActivity activity) {
        this.setActivity(activity);
        this.setSp(CustomSecurePref.getInstance().getmSecurePrefs());
        refreshData();
    }

    public Boolean isLevel1QAC(){
        return isAllowedLevel && isLevel1;
    }

    public void showDialogLevel(){
        refreshData();
        if(isRegisteredLevel){
            Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getActivity().getString(R.string.level_dialog_finish_title),
                    getActivity().getString(R.string.level_dialog_finish_message) + "\n" + listAddress + "\n" +
                            getActivity().getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone,
                    new DefinedDialog.DialogButtonListener() {
                        @Override
                        public void onClickButton(View v, boolean isLongClick) {

                        }
                    }
            );

            dialognya.show();
        }
        else {
            final AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getActivity().getString(R.string.level_dialog_title),
                    getActivity().getString(R.string.level_dialog_message), getActivity().getString(R.string.level_dialog_btn_ok),
                    getActivity().getString(R.string.cancel), false);
            dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent mI = new Intent(getActivity(), MyProfileNewActivity.class);
                    getActivity().startActivityForResult(mI, MainPage.ACTIVITY_RESULT);
                }
            });
            dialog_frag.setCancelListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog_frag.dismiss();
                }
            });

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.add(dialog_frag,null);
            ft.commitAllowingStateLoss();
        }
    }

    private void getHelpList() {
        try {
            String ownerId = getSp().getString(DefineValue.USERID_PHONE,"");
            String accessKey = getSp().getString(DefineValue.ACCESS_KEY,"");

            RequestParams param = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_HELP_LIST,
                    ownerId,accessKey);
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_HELP_LIST);
            params.put(WebParams.USER_ID, ownerId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params help list:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_HELP_LIST, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("isi params help list:"+response.toString());

                                    String contactCenter = response.getString(WebParams.CONTACT_DATA);

                                    SecurePreferences.Editor mEditor = getSp().edit();
                                    mEditor.putString(DefineValue.LIST_CONTACT_CENTER, response.getString(WebParams.CONTACT_DATA));
                                    mEditor.apply();

                                    try {
                                        JSONArray arrayContact = new JSONArray(contactCenter);
                                        for(int i=0 ; i<arrayContact.length() ; i++) {
                                            if(i == 0) {
                                                listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
                                                listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else if(code.equals(WebParams.LOGOUT_CODE)){
                                    Timber.d("isi response autologout:"+response.toString());
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(),message);
                                }
                                else {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                }

//                        progdialog.dismiss();

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
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void refreshData() {
        String contactCenter = getSp().getString(DefineValue.LIST_CONTACT_CENTER, "");
        if(sp.contains(DefineValue.LEVEL_VALUE)) {
            String i = sp.getString(DefineValue.LEVEL_VALUE, "0");
            isLevel1 = Integer.valueOf(i) == 1;
        }
        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
        isAllowedLevel = sp.getBoolean(DefineValue.ALLOW_MEMBER_LEVEL, false);
        if(contactCenter.equals("")) {
            getHelpList();
        }
        else {
            try {
                JSONArray arrayContact = new JSONArray(contactCenter);
                for (int i = 0; i < arrayContact.length(); i++) {
                    if (i == 0) {
                        listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
                        listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private FragmentActivity getActivity() {
        return activity;
    }

    private void setActivity(FragmentActivity activity) {
        this.activity = activity;
    }

    private SecurePreferences getSp() {
        return sp;
    }

    private void setSp(SecurePreferences sp) {
        this.sp = sp;
    }
}
