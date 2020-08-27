package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsSearchAgentActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.BbsSearchCategoryAdapter;
import com.sgo.saldomu.adapter.GridBbsCategory;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.ShopCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * Created by thinkpad on 1/25/2017.
 */

public class FragListCategoryBbs extends ListFragment implements EasyPermissions.PermissionCallbacks {

    private View v;
    BbsSearchCategoryAdapter bbsSearchCategoryAdapter;
    ArrayList<ShopCategory> shopCategories = new ArrayList<>();
    JSONArray categories;
    ProgressDialog progdialog;
    GridView gridCategory;
    GridBbsCategory gridBbsCategoryAdapter;
    Switch swMobilityAgent;
    LinearLayout llJumlah;
    EditText etJumlah;
    SecurePreferences sp;
    private static final int RC_LOCATION_PERM = 500;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp    = CustomSecurePref.getInstance().getmSecurePrefs();

        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Have permission, do the thing!
            //Toast.makeText(getContext(), "TODO: Camera things", Toast.LENGTH_LONG).show();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                    RC_LOCATION_PERM, Manifest.permission.ACCESS_FINE_LOCATION);
        }

        progdialog              = DefinedDialog.CreateProgressDialog(getActivity(), "");
        progdialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        v = inflater.inflate(R.layout.frag_list_category_bbs, container, false);

        gridBbsCategoryAdapter = new GridBbsCategory(getActivity(), shopCategories);
        gridCategory            = v.findViewById(R.id.gridBbsCategory);
        gridCategory.setAdapter(gridBbsCategoryAdapter);

        swMobilityAgent         = v.findViewById(R.id.swMobilityAgent);
        swMobilityAgent.setChecked(false);

        llJumlah                = v.findViewById(R.id.llJumlah);
        llJumlah.setVisibility(View.GONE);

        etJumlah                = v.findViewById(R.id.etJumlah);





        HashMap<String, Object> params = new HashMap<>();
        UUID rcUUID             = UUID.randomUUID();
        String dtime            = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.SHOP_ID, "");

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.APP_ID));

        params.put(WebParams.SIGNATURE, signature);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CATEGORY_LIST, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                categories = response.getJSONArray("category");

                                for (int i = 0; i < categories.length(); i++) {

                                    JSONObject object = categories.getJSONObject(i);
                                    ShopCategory shopCategory = new ShopCategory();
                                    shopCategory.setCategoryId(object.getString("category_id"));
                                    shopCategory.setCategoryName(object.getString("category_name"));
                                    shopCategories.add(shopCategory);
                                }

                                gridBbsCategoryAdapter.notifyDataSetChanged();

                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE)
                                        , Toast.LENGTH_LONG).show();
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
                        progdialog.dismiss();
                    }
                });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        etJumlah.addTextChangedListener(jumlahChangeListener);

        swMobilityAgent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    llJumlah.setVisibility(View.VISIBLE);
                } else {
                    llJumlah.setVisibility(View.GONE);
                }

            }
        });

        gridCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("masuk gridcategoryonitemclicklistener");

                Boolean hasError = false;
                if ( swMobilityAgent.isChecked() ) {
                    if(etJumlah.getText().toString().length()==0){
                        etJumlah.requestFocus();
                        etJumlah.setError(getString(R.string.sgoplus_validation_jumlahSGOplus));
                        hasError = true;
                    }
                    else if(Long.parseLong(etJumlah.getText().toString()) < 1){
                        etJumlah.requestFocus();
                        etJumlah.setError(getString(R.string.payfriends_amount_zero));
                        hasError = true;
                    }
                } else {
                    hasError = false;
                }

                if ( !hasError ) {
                    /*
                    Intent i = new Intent(getActivity(), BbsSearchAgentActivity.class);

                    i.putExtra(DefineValue.CATEGORY_ID, shopCategories.get(position).getCategoryId());
                    i.putExtra(DefineValue.CATEGORY_NAME, shopCategories.get(position).getCategoryName());

                    if (!swMobilityAgent.isChecked()) {
                        i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                        i.putExtra(DefineValue.AMOUNT, "");
                    } else {
                        i.putExtra(DefineValue.AMOUNT, etJumlah.getText().toString());
                        i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                    }
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                    */

                    Intent i=new Intent(getActivity(),BbsSearchAgentActivity.class);
                    i.putExtra(DefineValue.CATEGORY_ID, shopCategories.get(position).getCategoryId());
                    i.putExtra(DefineValue.CATEGORY_NAME, shopCategories.get(position).getCategoryName());

                    if (!swMobilityAgent.isChecked()) {
                        i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                        i.putExtra(DefineValue.AMOUNT, "");
                    } else {
                        i.putExtra(DefineValue.AMOUNT, etJumlah.getText().toString());
                        i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                    }

                    SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                    SecurePreferences.Editor mEditor = prefs.edit();
                    mEditor.putString(DefineValue.BBS_TX_ID, "");
                    mEditor.apply();

                    startActivityForResult(i, DefineValue.IDX_CATEGORY_SEARCH_AGENT);// Activity is started with requestCode 2
                }
            }

        });

    }

    // Call Back method  to get the Message form other Activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==DefineValue.IDX_CATEGORY_SEARCH_AGENT)
        {

            try {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    if (extras.containsKey(DefineValue.MSG_NOTIF)) {
                        String message = data.getStringExtra(DefineValue.MSG_NOTIF);

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                        builder1.setTitle(getString(R.string.transaction));
                        builder1.setMessage(message);
                        builder1.setCancelable(true);
                        builder1.setNeutralButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                }
            }catch( Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
//        Intent i = new Intent(getActivity(), BbsSearchAgentActivity.class);
//        i.putExtra(DefineValue.CATEGORY_ID, shopCategories.get(position).getCategoryId());
//        i.putExtra(DefineValue.CATEGORY_NAME, shopCategories.get(position).getCategoryName() );
//        switchActivity(i, MainPage.ACTIVITY_RESULT);

    }

    private void switchActivity(Intent mIntent, int j){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,j);
    }

    private TextWatcher jumlahChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().equals("0"))etJumlah.setText("");
            if(s.length() > 0 && s.charAt(0) == '0'){
                int i = 0;
                for (; i < s.length(); i++){
                    if(s.charAt(i) != '0')break;
                }
                etJumlah.setText(s.toString().substring(i));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}
