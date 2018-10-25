package com.sgo.saldomu.activities;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.ScanQRUtils;
import com.sgo.saldomu.widgets.BaseActivity;

public class ActivityProfileQr extends BaseActivity {



    // DATA
    String sourceAcct = "", sourceAcctName ="" , lvlMember = "";

    // UI LAYOUT
    TextView tv_name, tv_phone_no, tv_lvl_member_value;
    CardView btn_upgrade;
    ImageView imageQR;




    @Override
    protected int getLayoutResource() {
        return R.layout.activity_profile_qr;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        InitializeToolbar();

        initPreferences();

        initLayout();
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
    private void initPreferences() {
        if(getIntent() != null){
            sourceAcct =  sp.getString(DefineValue.USERID_PHONE,"");;
            sourceAcctName = NoHPFormat.formatTo08(sp.getString(DefineValue.CUST_NAME,""));

        }

    }

    private void setView() {

        imageQR.setImageBitmap(ScanQRUtils.getInstance(this).generateQRCode(DefineValue.QR_TYPE_ONLY_SOURCE,sourceAcct,sourceAcctName));
    }

    private void initLayout() {
        tv_name = findViewById(R.id.tv_name);
        tv_phone_no = findViewById(R.id.tv_phone_no);
        tv_lvl_member_value = findViewById(R.id.tv_lvl_member_value);
        btn_upgrade = findViewById(R.id.btn_upgrade);
        imageQR = findViewById(R.id.iv_qr);

        tv_name.setText(sourceAcctName);
        tv_phone_no.setText(sourceAcct);
        tv_lvl_member_value.setText(getLvl());

        btn_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(ActivityProfileQr.this,"We're sorry this feature currently unavailable",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void InitializeToolbar(){

        setActionBarTitle(getString(R.string.lbl_profil_saya));
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



}
