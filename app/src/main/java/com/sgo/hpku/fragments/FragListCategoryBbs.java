package com.sgo.hpku.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.BbsSearchAgentActivity;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.adapter.BbsSearchCategoryAdapter;
import com.sgo.hpku.adapter.GridBbsCategory;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DateTimeFormat;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.HashMessage;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.models.ShopCategory;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import timber.log.Timber;

/**
 * Created by thinkpad on 1/25/2017.
 */

public class FragListCategoryBbs extends ListFragment {

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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurePreferences sp    = CustomSecurePref.getInstance().getmSecurePrefs();

        progdialog              = DefinedDialog.CreateProgressDialog(getActivity(), "");
        progdialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        v = inflater.inflate(R.layout.frag_list_category_bbs, container, false);

        gridBbsCategoryAdapter = new GridBbsCategory(getActivity(), shopCategories);
        gridCategory            = (GridView) v.findViewById(R.id.gridBbsCategory);
        gridCategory.setAdapter(gridBbsCategoryAdapter);

        swMobilityAgent         = (Switch) v.findViewById(R.id.swMobilityAgent);
        swMobilityAgent.setChecked(false);

        llJumlah                = (LinearLayout) v.findViewById(R.id.llJumlah);
        llJumlah.setVisibility(View.GONE);

        etJumlah                = (EditText) v.findViewById(R.id.etJumlah);





        RequestParams params    = new RequestParams();
        UUID rcUUID             = UUID.randomUUID();
        String dtime            = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppIDHpku);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.SHOP_ID, "");

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppIDHpku));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.getCategoryList(getActivity().getApplicationContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    progdialog.dismiss();
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
                        Toast.makeText(getActivity().getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                ifFailure(throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                ifFailure(throwable);
            }

            private void ifFailure(Throwable throwable) {
                progdialog.dismiss();
                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getActivity().getApplication(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity().getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error Koneksi login:" + throwable.toString());

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
                }
            }

        });

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
}
