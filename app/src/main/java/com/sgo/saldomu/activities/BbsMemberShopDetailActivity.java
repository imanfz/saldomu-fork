package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class BbsMemberShopDetailActivity extends BaseActivity {

    String shopId, memberId;
    ProgressDialog progDialog;
    SecurePreferences sp;
    TextView tvMemberName, tvShopName;
    String[] arrayItems = new String[3];

    String[] actualValues = new String[3];
    String memberType = "";
    ImageView ivLocation, ivCategory, ivCloseShop;
    String flagApprove, setupOpenHour;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvMemberName = findViewById(R.id.tvMemberName);
        tvShopName = findViewById(R.id.tvShopName);
        ivLocation = findViewById(R.id.ivLocation);
        ivCategory = findViewById(R.id.ivCategory);
        ivCloseShop = findViewById(R.id.ivCloseShop);

        progDialog = DefinedDialog.CreateProgressDialog(this, "");
        memberId = getIntent().getStringExtra(DefineValue.MEMBER_ID);
        shopId = getIntent().getStringExtra(DefineValue.SHOP_ID);
        flagApprove = getIntent().getStringExtra(DefineValue.FLAG_APPROVE);
        setupOpenHour = "";
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        initializeToolbar();

        arrayItems[0] = "Silakan Pilih";
        arrayItems[1] = getString(R.string.yes);
        arrayItems[2] = getString(R.string.no);

        actualValues[0] = "";
        actualValues[1] = DefineValue.STRING_YES;
        actualValues[2] = DefineValue.STRING_NO;

        HashMap<String, Object> params = new HashMap<>();
        UUID rcUUID = UUID.randomUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.SHOP_ID, shopId);
        params.put(WebParams.MEMBER_ID, memberId);
        params.put(WebParams.FLAG_APPROVE, flagApprove);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + memberId + shopId + BuildConfig.APP_ID + flagApprove));

        params.put(WebParams.SIGNATURE, signature);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_MEMBER_SHOP_DETAIL, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            String code = response.getString(WebParams.ERROR_CODE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                tvMemberName.setText(response.getString(WebParams.MEMBER_NAME));
                                tvShopName.setText(response.getString("shop_name"));
                                setupOpenHour = response.getString("setup_open_hour");

                                memberType = response.getString(WebParams.MEMBER_TYPE);
                                int defaultPosition = 0;

                                if (response.getString("shop_closed").equals(DefineValue.STRING_YES)) {
                                    defaultPosition = 1;
                                } else if (response.getString("shop_closed").equals(DefineValue.STRING_NO)) {
                                    defaultPosition = 2;
                                }

                                if (flagApprove.equals(DefineValue.STRING_NO)) {
                                    if (response.getString("shop_latitude").equals("") && response.getString("shop_longitude").equals("")) {
                                        ivLocation.setVisibility(View.VISIBLE);
                                    } else {
                                        ivLocation.setVisibility(View.GONE);
                                    }

                                    if (memberType.equals(DefineValue.SHOP_MERCHANT)) {
                                        ivCategory.setVisibility(View.VISIBLE);
                                    } else {
                                        ivCategory.setVisibility(View.GONE);
                                    }
                                    ivCloseShop.setVisibility(View.VISIBLE);
                                } else {
                                    if (memberType.equals(DefineValue.SHOP_MERCHANT)) {
                                        ivCategory.setVisibility(View.VISIBLE);
                                    } else {
                                        ivCategory.setVisibility(View.GONE);
                                    }
                                    ivLocation.setVisibility(View.GONE);
                                    ivCloseShop.setVisibility(View.VISIBLE);
                                }

                                //spPilihan.setSelection(defaultPosition);

                            } else if (code.equals(WebParams.LOGOUT_CODE)) {

                            } else {
                                code = response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
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
                        progDialog.dismiss();
                    }
                });

        ivLocation.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), BbsMemberLocationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(DefineValue.MEMBER_ID, memberId);
            intent.putExtra(DefineValue.SHOP_ID, shopId);
            startActivity(intent);
            finish();
        });

        ivCategory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BbsMerchantCategoryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(DefineValue.MEMBER_ID, memberId);
                intent.putExtra(DefineValue.SHOP_ID, shopId);
                intent.putExtra(DefineValue.FLAG_APPROVE, flagApprove);
                intent.putExtra(DefineValue.SETUP_OPEN_HOUR, setupOpenHour);
                startActivity(intent);
                finish();
            }
        });

        ivCloseShop.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), BbsSetupShopClosedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(DefineValue.MEMBER_ID, memberId);
            intent.putExtra(DefineValue.SHOP_ID, shopId);
            intent.putExtra(DefineValue.FLAG_APPROVE, flagApprove);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_member_shop_detail;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //listener ketika button back di action bar diklik
        if (id == android.R.id.home) {
            //kembali ke activity sebelumnya
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.shop_member_detail));
    }

}
