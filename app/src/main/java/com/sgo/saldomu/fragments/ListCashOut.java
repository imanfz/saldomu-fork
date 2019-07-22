package com.sgo.saldomu.fragments;

import android.app.Dialog;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.CashoutActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.BankCashoutModel;
import com.sgo.saldomu.models.retrofit.ContactDataModel;
import com.sgo.saldomu.models.retrofit.GetHelpModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/*
  Created by Administrator on 11/5/2014.
 */
public class ListCashOut extends ListFragment {

    SecurePreferences sp;
    View v,nodata_view;
    ArrayList<String> _listType;
    EasyAdapter adapter;
    private InformationDialog dialogI;
    String userID, accessKey, memberID;
    Boolean is_full_activity = false,isLevel1,isRegisteredLevel,isAllowedLevel,agent;
    String contactCenter,listContactPhone = "", listAddress="";
    ProgressDialog progdialog;
    static boolean successUpgrade = false;
    private Gson gson;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_cashout, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        successUpgrade = false;
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        memberID = sp.getString(DefineValue.MEMBER_ID, "");
        agent = sp.getBoolean(DefineValue.IS_AGENT,false);
        nodata_view = v.findViewById(R.id.layout_no_data);
        nodata_view.setVisibility(View.GONE);

        if(sp.contains(DefineValue.LEVEL_VALUE)) {
            int i = sp.getInt(DefineValue.LEVEL_VALUE, 0);
            isLevel1 = i == 1;
            isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
            isAllowedLevel = sp.getBoolean(DefineValue.ALLOW_MEMBER_LEVEL, false);
        }

        Bundle mArgs = getArguments();
        if(mArgs != null && !mArgs.isEmpty())
            is_full_activity = mArgs.getBoolean(DefineValue.IS_ACTIVITY_FULL,false);

        _listType = new ArrayList<>();
        Collections.addAll(_listType, getResources().getStringArray(R.array.list_cash_out));

        adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _listType);

        ListView listView1 = v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

        dialogI = InformationDialog.newInstance(4);

        if(isAdded())
            getBankCashout();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void getBankCashout(){
        try {
            if (isAdded() || isVisible()) {
                final ProgressDialog prodDialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

                HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BANKCASHOUT, memberID);
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                params.put(WebParams.MEMBER_ID, memberID );
                params.put(WebParams.USER_ID, userID);

                Timber.d("isi params get Bank cashout:" + params.toString());

                RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BANKCASHOUT, params,
                        new ResponseListener() {
                            @Override
                            public void onResponses(JsonObject object) {
//                                BankCashoutModel model = getGson().fromJson(object, BankCashoutModel.class);
//
//                                String code = model.getError_code();
//                                if (code.equals(WebParams.SUCCESS_CODE)) {
//                                    if (isAdded()) {
//                                        SecurePreferences.Editor mEditor = sp.edit();
//                                        mEditor.putString(DefineValue.BANK_CASHOUT, model.getBank_cashout());
//                                        mEditor.apply();
//                                    }
//                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
//                                    String message = model.getError_message();
//                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
//                                    if (is_full_activity)
//                                        test.showDialoginActivity(getActivity(), message);
//                                    else
//                                        test.showDialoginMain(getActivity(), message);
//                                } else {
//                                    code = model.getError_message();
//                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
//                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onComplete() {
                                if (prodDialog.isShowing())
                                    prodDialog.dismiss();
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
            if(agent) {
                i = new Intent(getActivity(), CashoutActivity.class);
                i.putExtra(DefineValue.CASHOUT_TYPE, DefineValue.CASHOUT_BANK);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
            }
            else {
                if (isAllowedLevel && isLevel1) {
                    if (isRegisteredLevel) {
                        setListContact();
                    }
//                    else
//                    showDialogLevel();
                } else {
                    i = new Intent(getActivity(), CashoutActivity.class);
                    i.putExtra(DefineValue.CASHOUT_TYPE, DefineValue.CASHOUT_BANK);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                }
            }
        }
        else if(position == 1) {
            i = new Intent(getActivity(), CashoutActivity.class);
            i.putExtra(DefineValue.CASHOUT_TYPE,DefineValue.CASHOUT_AGEN);
            switchActivity(i,MainPage.ACTIVITY_RESULT);
        }
        else if(position == 2) {
            if (agent) {
                i = new Intent(getActivity(), CashoutActivity.class);
                i.putExtra(DefineValue.CASHOUT_TYPE, DefineValue.CASHOUT_LKD);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
            }
            else {
                if (isAllowedLevel && isLevel1) {
                    if (isRegisteredLevel) {
                        setListContact();
                    }
//                    else
//                        showDialogLevel();
                } else {
                    i = new Intent(getActivity(), CashoutActivity.class);
                    i.putExtra(DefineValue.CASHOUT_TYPE, DefineValue.CASHOUT_LKD);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                }
            }
        }

    }

//    private void showDialogLevel(){
//        final AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getString(R.string.level_dialog_title),
//                getString(R.string.level_dialog_message),getString(R.string.level_dialog_btn_ok),getString(R.string.cancel),false);
//        dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent mI = new Intent(getActivity(), LevelFormRegisterActivity.class);
//                switchActivity(mI, MainPage.ACTIVITY_RESULT);
//            }
//        });
//        dialog_frag.setCancelListener(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog_frag.dismiss();
//            }
//        });
//        dialog_frag.setTargetFragment(ListCashOut.this, 0);
////        dialog_frag.show(getActivity().getSupportFragmentManager(), AlertDialogFrag.TAG);
//        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//        ft.add(dialog_frag, null);
//        ft.commitAllowingStateLoss();
//    }

    private void showDialogLevelRegistered(){
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getString(R.string.level_dialog_finish_title),
                getString(R.string.level_dialog_finish_message) + "\n" + listAddress + "\n" +
                        getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone,
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {

                    }
                }
        );
        dialognya.show();
    }

    private void setListContact() {
        contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER, "");

        if(contactCenter.equals("")) {
            getHelpList();
        }
        else {

            Type type = new TypeToken<List<ContactDataModel>>() {}.getType();
            List<ContactDataModel> temp = getGson().fromJson(contactCenter, type);

            for(int i=0 ; i<temp.size() ; i++) {
                if(i == 0) {
                    listContactPhone = temp.get(i).getContact_phone();
                    listAddress = temp.get(i).getAddress();
                }
            }
            showDialogLevelRegistered();
        }
    }

    public void getHelpList() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();
            String ownerId = sp.getString(DefineValue.USERID_PHONE,"");
            String accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignature(MyApiClient.LINK_USER_CONTACT_INSERT);
            params.put(WebParams.USER_ID, ownerId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params help list:" + params.toString());

            RetrofitService.getInstance().GetObjectRequest(MyApiClient.LINK_USER_CONTACT_INSERT,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            GetHelpModel model = gson.fromJson(object, GetHelpModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                                Timber.d("isi params help list:"+response.toString());

//                                contactCenter = model.getContact_data();

                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.LIST_CONTACT_CENTER, getGson().toJson(model.getContact_data()));
                                mEditor.apply();

                                for(int i=0 ; i<model.getContact_data().size() ; i++) {
                                    if(i == 0) {
                                        listContactPhone = model.getContact_data().get(i).getContact_phone();
                                        listAddress = model.getContact_data().get(i).getAddress();
                                    }
                                }

                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
//                                Timber.d("isi response autologout:"+response.toString());
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else {
//                                if(MyApiClient.PROD_FAILURE_FLAG)
//                                    Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
//                                else
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if(progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(successUpgrade) {
            if(sp.contains(DefineValue.LEVEL_VALUE)) {
                int i = sp.getInt(DefineValue.LEVEL_VALUE, 0);
                isLevel1 = i == 1;
                isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
                isAllowedLevel = sp.getBoolean(DefineValue.ALLOW_MEMBER_LEVEL, false);
            }
        }
    }

    private void switchActivity(Intent mIntent,int j){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,j);
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

    public Gson getGson() {
        if (gson == null)
            gson = new Gson();
        return gson;
    }
}