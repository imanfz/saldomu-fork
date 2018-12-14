package com.sgo.saldomu.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.ScanQRUtils;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

public class ActivityProfileQr extends BaseActivity {



    // DATA
    String sourceAcct = "", sourceAcctName ="" , lvlMember = "";
    private LevelClass levelClass;
    private boolean is_first_time = false;
    private String reject_npwp;
    private String listContactPhone = "";
    private String listAddress = "";
    private String contactCenter="";
    private boolean is_agent = false;//saat antri untuk diverifikasi
    private boolean isUpgradeAgent =false; //saat antri untuk diverifikasi upgrade agent
    private boolean isRegisteredLevel = false;

    // UI LAYOUT
    TextView tv_name, tv_phone_no, tv_lvl_member_value;
    CardView btn_upgrade;
    ImageView imageQR;
    ProgressDialog progdialog;
    RelativeLayout lytUpgrade,lytDetail;



    // Listener
    RelativeLayout.OnClickListener detailOnClick = new RelativeLayout.OnClickListener() {
        @Override
        public void onClick(View v) {

//            if (isUpgradeAgent && !is_agent)
//            {
                showDialogMessage();
//            }
        }
    };



    @Override
    protected int getLayoutResource() {
        return R.layout.activity_profile_qr;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        levelClass = new LevelClass(this,sp);



        InitializeToolbar();
        initData();

        checkContactCenter();

        initLayout();
        checkAgent();
        setView();

    }

    private String getLvl(){

        int tempLvl = sp.getInt(DefineValue.LEVEL_VALUE,1);
        boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

        if(isAgent){
            return getString(R.string.lbl_member_lvl_agent);
        }else{
            if(tempLvl == 1){
                return getString(R.string.lbl_member_lvl_silver);
            } else if(tempLvl == 2){
                return getString(R.string.lbl_member_lvl_gold);
            }
        }
        return "";
    }
    private void initData() {
        if(getIntent() != null){
            sourceAcct =  NoHPFormat.formatTo08(sp.getString(DefineValue.USERID_PHONE,""));;
            sourceAcctName = sp.getString(DefineValue.CUST_NAME,"");
            contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER,"");

            if(getIntent().hasExtra(DefineValue.IS_FIRST)) {
                is_first_time = getIntent().getStringExtra(DefineValue.IS_FIRST).equals(DefineValue.YES);
            }
        }

        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
        isUpgradeAgent = sp.getBoolean(DefineValue.IS_UPGRADE_AGENT, false);
        is_agent = sp.getBoolean(DefineValue.IS_AGENT, false);
        reject_npwp = sp.getString(DefineValue.REJECT_NPWP,"N");

    }

    private void setView() {

        imageQR.setImageBitmap(ScanQRUtils.getInstance(this).generateQRCode(DefineValue.QR_TYPE_FROM_DEFAULT_ACCOUNT,sourceAcct,sourceAcctName));
        if(isShowUpgradeStatus()){
            viewOnProggressUpgrade();
        }else{
            hideOnProgUpgrade();
        }

    }

    private void initLayout() {
        tv_name = findViewById(R.id.tv_name);
        tv_phone_no = findViewById(R.id.tv_phone_no);
        tv_lvl_member_value = findViewById(R.id.tv_lvl_member_value);
        btn_upgrade = findViewById(R.id.btn_upgrade);
        imageQR = findViewById(R.id.iv_qr);
        lytUpgrade = findViewById(R.id.lyt_upgrade_detail);
        lytDetail = findViewById(R.id.lyt_detail);


        tv_name.setText(sourceAcctName);
        tv_phone_no.setText(sourceAcct);
        tv_lvl_member_value.setText(getLvl());


        viewOnProggressUpgrade();


        btn_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Toast.makeText(ActivityProfileQr.this,"We're sorry this feature currently unavailable",Toast.LENGTH_SHORT).show();
                checkIsLv1();

                checkIsLv2();


            }
        });
    }

    private boolean isShowUpgradeStatus(){

        if(levelClass.isLevel1QAC() && isRegisteredLevel){
            return true;
        }

        if (isUpgradeAgent && !is_agent)
        {
            return true;
        }
        return false;
    }

    private void InitializeToolbar(){

        setActionBarTitle(getString(R.string.lbl_member_saya));
        setActionBarIcon(R.drawable.ic_arrow_left);
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount()>0)
            getFragmentManager().popBackStack();
        else super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                if(getFragmentManager().getBackStackEntryCount()>0)
                    getFragmentManager().popBackStack();
                else finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchViewUpgradeVerified(){
        finish();
        Intent i = new Intent(this,MyProfileNewActivity.class);
        startActivity(i);
    }


    private void viewOnProggressUpgrade(){
        btn_upgrade.setCardBackgroundColor(getResources().getColor(R.color.colorSecondaryWhiteFixed));
        btn_upgrade.setEnabled(false);

        lytUpgrade.setVisibility(View.VISIBLE);
        lytDetail.setOnClickListener(detailOnClick);

    }
    private void hideOnProgUpgrade(){
        btn_upgrade.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        btn_upgrade.setEnabled(true);
        lytUpgrade.setVisibility(View.GONE);

    }
    private void checkIsLv1(){



        if(levelClass.isLevel1QAC())
        {
            android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(ActivityProfileQr.this);
            builder1.setTitle(R.string.upgrade_member);
            builder1.setMessage(R.string.message_upgrade_member);
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            switchViewUpgradeVerified();
//                            dataVerifiedMember.setVisibility(View.VISIBLE);
//                            et_nama.setEnabled(false);
//                            tv_dob.setEnabled(false);
//                            btn1.setVisibility(View.GONE);
//                            if(is_first_time) {
//                                setResult(MainPage.RESULT_FIRST_TIME);
//                            }
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.dismiss();
//                            tv_dob.setEnabled(false);
//                            if(is_first_time) {
//                                RESULT = MainPage.RESULT_FIRST_TIME;
//                                setResult(MainPage.RESULT_FIRST_TIME);
//                                finish();
//                            }else
//                                finish();
                        }
                    });

            android.support.v7.app.AlertDialog alert11 = builder1.create();
            alert11.show();

        }
    }

    private void checkIsLv2(){
        if (!is_agent && !levelClass.isLevel1QAC() && !isUpgradeAgent)
        {
            android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(ActivityProfileQr.this);
            builder1.setTitle(R.string.upgrade_agent);
            builder1.setMessage(R.string.message_upgrade_agent);
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

//                            checkAgent();

                            finish();
                            Intent intent = new Intent(ActivityProfileQr.this, UpgradeAgentActivity.class);
                            startActivity(intent);
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.dismiss();
//                            if(is_first_time) {
//                                RESULT = MainPage.RESULT_FIRST_TIME;
//                                setResult(MainPage.RESULT_FIRST_TIME);
//                                finish();
//                            }else
//                                finish();
                        }
                    });

            android.support.v7.app.AlertDialog alert11 = builder1.create();
            alert11.show();
        }


    }



    private void checkAgent(){

        if (is_agent && !levelClass.isLevel1QAC() && !isUpgradeAgent)
        {
            lytUpgrade.setVisibility(View.GONE);
            btn_upgrade.setVisibility(View.GONE);
        }else if(is_agent && !reject_npwp.isEmpty()){
            finish();
            Intent intent1 = new Intent(ActivityProfileQr.this, UpgradeAgentActivity.class);
            startActivity(intent1);
        }
    }

    private void showDialogMessage()
    {

        final Dialog dialognya = DefinedDialog.MessageDialog(ActivityProfileQr.this, this.getString(R.string.upgrade_dialog_finish_title),
                this.getString(R.string.level_dialog_finish_message) + "\n" + listAddress + "\n" +
                        this.getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone,
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {

                    }
                }
        );

        dialognya.setCanceledOnTouchOutside(false);
        dialognya.setCancelable(false);

        dialognya.show();
    }

    private void checkContactCenter(){

        if(contactCenter.equals("")) {
            getHelpList();
        }
        else {
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
    }


    private void getHelpList() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_USER_CONTACT_INSERT);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params help list:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_USER_CONTACT_INSERT, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("isi params help list:"+response.toString());

                                    contactCenter = response.getString(WebParams.CONTACT_DATA);

                                    SecurePreferences.Editor mEditor = sp.edit();
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
                                    test.showDialoginActivity(ActivityProfileQr.this,message);
                                }
                                else {
                                    Timber.d("isi error help list:"+response.toString());
                                    Toast.makeText(ActivityProfileQr.this, message, Toast.LENGTH_LONG).show();
                                }



                            } catch (JSONException e) {
                                e.printStackTrace();
                                Timber.d("Error JSON catch contact:"+e.toString());
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
            if(progdialog.isShowing())
                progdialog.dismiss();
            Timber.d("httpclient:"+e.getMessage());
        }
    }


}