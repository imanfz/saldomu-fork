package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;

import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsSearchAgentActivity;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.GridHome;
import com.sgo.saldomu.coreclass.BaseFragmentMainPage;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.models.ShopCategory;
import com.sgo.saldomu.services.BalanceService;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import in.srain.cube.views.ptr.PtrFrameLayout;
import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/10/2017.
 */
public class FragHomeNew extends BaseFragmentMainPage {
    GridView GridHome;
    Button btn_beli;
    TextView tv_saldo;
    EditText input;
    TextView tv_pulsa;
    TextView tv_bpjs;
    TextView tv_listrikPLN;
    View view_pulsa;
    View view_bpjs;
    View view_listrikPLN;
    View v;
    Boolean is_first_time=true;
    private LevelClass levelClass;
    private SecurePreferences sp;
    ProgressDialog progdialog;
    ArrayList<ShopCategory> shopCategories = new ArrayList<>();

    int[] imageId = {
            R.drawable.ic_tambahsaldo,
            R.drawable.ic_bayarteman1,
            R.drawable.ic_mintauang,
            R.drawable.ic_belanja,
            R.drawable.ic_laporan,
            R.drawable.ic_location_on_black,
            R.drawable.ic_tariktunai,
            R.drawable.ic_tariktunai,
    };
//    String[] text = {
//            getString(R.string.newhome_title_topup),
//            "BAYAR TEMAN",
//            "MINTA UANG",
//            "BELANJA",
//            "LAPORAN",
//            "CASH IN",
//            "CASH OUT",
//    } ;

    public FragHomeNew() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag_home_new, container, false);
        GridHome=(GridView)v.findViewById(R.id.grid);
        tv_saldo = (TextView)v.findViewById(R.id.tv_saldo);
        return v;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        levelClass = new LevelClass(getActivity(),sp);

        btn_beli = (Button) v.findViewById(R.id.btn_beli);
        input = (EditText) v.findViewById(R.id.input);
        tv_pulsa = (TextView) v.findViewById(R.id.tv_pulsa);
        tv_bpjs =(TextView) v.findViewById(R.id.tv_bpjs);
        tv_listrikPLN = (TextView) v.findViewById(R.id.tv_listrikPLN);
        view_pulsa = v.findViewById(R.id.view_pulsa);
        view_bpjs = v.findViewById(R.id.view_bpjs);
        view_listrikPLN = v.findViewById(R.id.view_listrikPLN);

        Boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);

        if ( isAgent ) {
            GridHome adapter = new GridHome(getActivity(), SetupListMenu(), SetupListMenuIcons());
            GridHome.setAdapter(adapter);
        } else {
            progdialog              = DefinedDialog.CreateProgressDialog(getActivity(), "");
            RequestParams params = new RequestParams();
            UUID rcUUID = UUID.randomUUID();
            String dtime = DateTimeFormat.getCurrentDateTime();

            params.put(WebParams.RC_UUID, rcUUID);
            params.put(WebParams.RC_DATETIME, dtime);
            params.put(WebParams.APP_ID, BuildConfig.AppID);
            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params.put(WebParams.SHOP_ID, "");

            String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                    DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID));

            params.put(WebParams.SIGNATURE, signature);

            MyApiClient.getCategoryList(getActivity().getApplicationContext(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try {
                        progdialog.dismiss();
                        String code = response.getString(WebParams.ERROR_CODE);


                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            JSONArray categories = response.getJSONArray("category");

                            for (int i = 0; i < categories.length(); i++) {

                                JSONObject object = categories.getJSONObject(i);
                                ShopCategory shopCategory = new ShopCategory();
                                shopCategory.setCategoryId(object.getString("category_id"));
                                shopCategory.setCategoryName(object.getString("category_name"));
                                shopCategories.add(shopCategory);
                            }



                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);
                        }

                        //gridBbsCategoryAdapter.notifyDataSetChanged();
                        GridHome adapter = new GridHome(getActivity(), SetupListMenu(), SetupListMenuIcons());
                        GridHome.setAdapter(adapter);

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

                    //gridBbsCategoryAdapter.notifyDataSetChanged();
                    GridHome adapter = new GridHome(getActivity(), SetupListMenu(), SetupListMenuIcons());
                    GridHome.setAdapter(adapter);
                }

            });
        }




        btn_beli.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(view_pulsa.getVisibility()==View.VISIBLE)
                {
                    Bundle bundle;
                    bundle= new Bundle();
                    bundle.putString(DefineValue.PHONE_NUMBER, input.getText().toString());
                    switchMenu(NavigationDrawMenu.MDAP,bundle);
                }
                if(view_bpjs.getVisibility()==View.VISIBLE)
                {
                    Intent intent = new Intent(getActivity(), BillerActivity.class);
                    intent.putExtra(DefineValue.BILLER_TYPE, "BPJS");
                    intent.putExtra(DefineValue.BILLER_ID_NUMBER, input.getText().toString());
                    intent.putExtra(DefineValue.BILLER_NAME, "BPJS");
                    startActivity(intent);
                }
                if (view_listrikPLN.getVisibility()==View.VISIBLE)
                {
                    Intent intent = new Intent(getActivity(), BillerActivity.class);
                    intent.putExtra(DefineValue.BILLER_TYPE, "TKN");
                    intent.putExtra(DefineValue.BILLER_ID_NUMBER, input.getText().toString());
                    intent.putExtra(DefineValue.BILLER_NAME, "Voucher Token Listrik");
                    startActivity(intent);
                }
            }
        });
        tv_pulsa.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                input.setText("");
                view_pulsa.setVisibility(View.VISIBLE);
                view_bpjs.setVisibility(View.INVISIBLE);
                view_listrikPLN.setVisibility(View.INVISIBLE);
                input.setHint("Masukkan No. Hp");
            }
        });
        tv_bpjs.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                input.setText("");
                view_pulsa.setVisibility(View.INVISIBLE);
                view_bpjs.setVisibility(View.VISIBLE);
                view_listrikPLN.setVisibility(View.INVISIBLE);
                input.setHint("Masukkan No. BPJS");
            }
        });
        tv_listrikPLN.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                input.setText("");
                view_pulsa.setVisibility(View.INVISIBLE);
                view_bpjs.setVisibility(View.INVISIBLE);
                view_listrikPLN.setVisibility(View.VISIBLE);
                input.setHint("Masukkan No. Listrik");
            }
        });

        GridHome.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("masuk gridhomeonitemclicklistener");

                String menuItemName = ((TextView) view.findViewById(R.id.grid_text)).getText().toString();

                if ( menuItemName.equals(getString(R.string.newhome_title_topup)) ) {
                    switchMenu(NavigationDrawMenu.MTOPUP, null);
                } else if ( menuItemName.equals(getString(R.string.menu_item_title_pay_friends)) ) {
                    if (levelClass.isLevel1QAC()) {
                        levelClass.showDialogLevel();
                    } else switchMenu(NavigationDrawMenu.MPAYFRIENDS, null);
                } else if ( menuItemName.equals(getString(R.string.menu_item_title_ask_for_money)) ) {
                    if (levelClass.isLevel1QAC()) {
                        levelClass.showDialogLevel();
                    } else switchMenu(NavigationDrawMenu.MASK4MONEY, null);
                } else if ( menuItemName.equals(getString(R.string.menu_item_title_buy)) ) {
                    switchMenu(NavigationDrawMenu.MBUY, null);
                } else if ( menuItemName.equals(getString(R.string.menu_item_title_report)) ) {
                    switchMenu(NavigationDrawMenu.MREPORT, null);
                } else if (menuItemName.equals(getString(R.string.menu_item_search_agent)) ) {
                    Bundle bundle = new Bundle();
                    switchMenu(NavigationDrawMenu.MCATEGORYBBS, bundle);
                /*} else if ( menuItemName.equals(getString(R.string.menu_item_search_agent_cta)) ) {
//                    Bundle bundle = new Bundle();
//                    switchMenu(NavigationDrawMenu.MBBSCTA, bundle);
                    Intent i = new Intent(getActivity(), BbsSearchAgentActivity.class);
                    i.putExtra(DefineValue.CATEGORY_ID, "CAT2");
                    i.putExtra(DefineValue.CATEGORY_NAME, "SETOR TUNAI");
                    i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                    i.putExtra(DefineValue.AMOUNT, "");
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                } else if ( menuItemName.equals(getString(R.string.menu_item_search_agent_atc)) ) {
//                    Bundle bundle = new Bundle();
//                    switchMenu(NavigationDrawMenu.MCATEGORYBBS, bundle);

                    Intent i = new Intent(getActivity(), BbsSearchAgentActivity.class);
                    i.putExtra(DefineValue.CATEGORY_ID, "CAT3");
                    i.putExtra(DefineValue.CATEGORY_NAME, "TARIK TUNAI");
                    i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                    i.putExtra(DefineValue.AMOUNT, "");
                    switchActivity(i, MainPage.ACTIVITY_RESULT);*/
                } else if ( menuItemName.equals(getString(R.string.cash_in)) ) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                    bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                    switchMenu(NavigationDrawMenu.MBBS, bundle);

                } else if ( menuItemName.equals(getString(R.string.cash_out)) ) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                    bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                    switchMenu(NavigationDrawMenu.MBBS, bundle);
                }
                else
                {
                    for(int x=0;x<shopCategories.size();x++) {
                        String categoryName = shopCategories.get(x).getCategoryName();
                        if ( menuItemName.indexOf(categoryName) > 0 ) {
                            Intent i = new Intent(getActivity(), BbsSearchAgentActivity.class);
                            i.putExtra(DefineValue.CATEGORY_ID, shopCategories.get(x).getCategoryId());
                            i.putExtra(DefineValue.CATEGORY_NAME, shopCategories.get(x).getCategoryName());
                            i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                            i.putExtra(DefineValue.AMOUNT, "");
                            switchActivity(i, MainPage.ACTIVITY_RESULT);
                            break;
                        }
                    }
                    //switchMenu(NavigationDrawMenu.MCASHOUT,null);
                }

            }

        });

        RefreshSaldo();
        if(levelClass != null)
            levelClass.refreshData();
    }

    private ArrayList<String> SetupListMenu(){
        String[] _data = getResources().getStringArray(R.array.list_menu_frag_new_home);
        ArrayList<String> data = new ArrayList<>() ;
        Collections.addAll(data,_data);
        Boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);
        if(isAgent) {
            _data = getResources().getStringArray(R.array.list_menu_frag_new_home_agent);
            Collections.addAll(data,_data);
        } else {
            //_data = getResources().getStringArray(R.array.list_menu_frag_new_home_not_agent);
            String[] categories = new String[shopCategories.size()];
            for(int x =0 ; x < shopCategories.size(); x++ ) {
                categories[x] = getString(R.string.menu_item_search_agent_bbs) + " " + shopCategories.get(x).getCategoryName();
            }
            Collections.addAll(data,categories);
        }
        return data;
    }

    private int[] SetupListMenuIcons(){

        int totalIdx            = 0;
        int overallIdx          = 0;
        TypedArray ta           = getResources().obtainTypedArray(R.array.list_menu_icon_frag_new_home);
        TypedArray taAgent      = getResources().obtainTypedArray(R.array.list_menu_icon_frag_new_home_agent);
        TypedArray taNotAgent   = getResources().obtainTypedArray(R.array.list_menu_icon_frag_new_home_not_agent);

        totalIdx                = ta.length();
        Boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);
        if(isAgent) {
            totalIdx    += taAgent.length();
        } else {
            totalIdx    += shopCategories.size();
        }

        int[] data        = new int[totalIdx];

        for( int j = 0; j < ta.length(); j++) {
            data[j] = ta.getResourceId(j, -1);
            overallIdx++;
        }


        if(isAgent) {
            for( int j = 0; j < taAgent.length(); j++) {
                data[overallIdx] = taAgent.getResourceId(j, -1);
                overallIdx++;
            }
        } else {
            for(int x =0; x < shopCategories.size(); x++ ) {
                data[overallIdx] = R.drawable.ic_location_on_black;
                overallIdx++;
            }
            /*for( int j = 0; j < taNotAgent.length(); j++) {
                data[overallIdx] = taNotAgent.getResourceId(j, -1);
                overallIdx++;
            }*/
        }
        return data;
    }

    private void switchMenu(int idx_menu,Bundle data){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchMenu(idx_menu, data);
    }

    private void RefreshSaldo(){
        String balance = sp.getString(DefineValue.BALANCE_AMOUNT,"0");
        tv_saldo.setText(CurrencyFormat.format(balance));
    }


    @Override
    protected int getInflateFragmentLayout() {
        return 0;
    }

    @Override
    public boolean checkCanDoRefresh() {
        return false;
    }

    @Override
    public void refresh(PtrFrameLayout frameLayout) {

    }

    @Override
    public void goToTop() {

    }

    private IntentFilter filter = new IntentFilter(BalanceService.INTENT_ACTION_BALANCE);
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("receiver service balance");
            RefreshSaldo();
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    private void switchActivity(Intent mIntent, int j){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,j);
    }
}
