package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsNewSearchAgentActivity;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.GridHome;
import com.sgo.saldomu.coreclass.BaseFragmentMainPage;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.PoinFormat;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.models.ShopCategory;
import com.sgo.saldomu.services.AgentShopService;
import com.sgo.saldomu.services.BalanceService;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import in.srain.cube.views.ptr.PtrFrameLayout;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/10/2017.
 */
public class FragHomeNew extends BaseFragmentMainPage {
    GridView GridHome;
    Button btn_beli;
    TextView tv_saldo, tv_poin;
    EditText input;
    TextView tv_pulsa;
    TextView tv_bpjs;
    TextView tv_listrikPLN;
    View view_pulsa;
    View view_bpjs;
    View view_listrikPLN;
    View v;
    View BPJS;
    View PLS;
    View TKN;
    Boolean is_first_time=true;
    private LevelClass levelClass;
    private SecurePreferences sp;
    ProgressDialog progdialog;
    ArrayList<ShopCategory> shopCategories = new ArrayList<>();
    private String _biller_type_code;
    private Biller_Type_Data_Model mBillerTypeDataPLS;
    private Biller_Type_Data_Model mBillerTypeDataBPJS;
    private Biller_Type_Data_Model mBillerTypeDataTKN;
    private Realm realm;
    private Switch swSettingOnline;
    private LinearLayout llAgentDetail;
    ProgressDialog progdialog2;
    String shopStatus;

    private static final int RC_GPS_REQUEST = 1;

    int[] imageId = {
            R.drawable.ic_tariktunai,
            R.drawable.ic_tariktunai,
            R.drawable.ic_tambahsaldo,
            R.drawable.ic_bayarteman1,
            R.drawable.ic_mintauang,
            R.drawable.ic_belanja,
            R.drawable.ic_laporan,
            R.drawable.ic_location_on_black,

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

        filter = new IntentFilter();
        filter.addAction(BalanceService.INTENT_ACTION_BALANCE);
        filter.addAction(AgentShopService.INTENT_ACTION_AGENT_SHOP);
        filter.addAction(BalanceService.INTENT_ACTION_POIN);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag_home_new, container, false);
        GridHome=(GridView)v.findViewById(R.id.grid);
        tv_saldo = (TextView)v.findViewById(R.id.tv_saldo);
        tv_poin = (TextView)v.findViewById(R.id.tv_poin);
        swSettingOnline = (Switch) v.findViewById(R.id.swSettingOnline);
        llAgentDetail = (LinearLayout) v.findViewById(R.id.llAgentDetail);
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
        BPJS = v.findViewById(R.id.BPJS);
        PLS = v.findViewById(R.id.PLS);
        TKN = v.findViewById(R.id.TKN);

        realm = Realm.getInstance(RealmManager.BillerConfiguration);
        mBillerTypeDataPLS = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "PLS")
                .findFirst();

        if (mBillerTypeDataPLS!=null)
        {
            PLS.setVisibility(View.VISIBLE);
        }
        else{
            PLS.setVisibility(View.GONE);
        }

        mBillerTypeDataBPJS = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "BPJS")
                .findFirst();

        if (mBillerTypeDataBPJS!=null)
        {
            BPJS.setVisibility(View.VISIBLE);
        }
        else{
            BPJS.setVisibility(View.GONE);
        }

        mBillerTypeDataTKN = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "TKN")
                .findFirst();

        if (mBillerTypeDataTKN!=null)
        {
            TKN.setVisibility(View.VISIBLE);
        }
        else{
            TKN.setVisibility(View.GONE);
        }

        if ( !sp.getBoolean(DefineValue.IS_AGENT, false) ) {
            llAgentDetail.setVisibility(View.GONE);

        } else {
            if ( sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES) ) {
                llAgentDetail.setVisibility(View.VISIBLE);
            } else {
                llAgentDetail.setVisibility(View.GONE);
            }

        }

        Boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);

        if ( isAgent ) {
            if(isAdded()) {
                GridHome adapter = new GridHome(getActivity(), SetupListMenu(), SetupListMenuIcons());
                GridHome.setAdapter(adapter);
            }
        } else {
            RequestParams params = new RequestParams();
            UUID rcUUID = UUID.randomUUID();
            String dtime = DateTimeFormat.getCurrentDateTime();

            params.put(WebParams.RC_UUID, rcUUID);
            params.put(WebParams.RC_DATETIME, dtime);
            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params.put(WebParams.SHOP_ID, "");

            String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                    DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.APP_ID));

            params.put(WebParams.SIGNATURE, signature);

            if(this.isVisible()) {
                progdialog              = DefinedDialog.CreateProgressDialog(getActivity(), "");
                MyApiClient.getCategoryList(getActivity().getApplicationContext(), params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                        try {
                            if (progdialog.isShowing())
                                progdialog.dismiss();

                            String code = response.getString(WebParams.ERROR_CODE);


                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                JSONArray categories = response.getJSONArray("category");

                                for (int i = 0; i < categories.length(); i++) {

                                    JSONObject object = categories.getJSONObject(i);
                                    ShopCategory shopCategory = new ShopCategory();
                                    shopCategory.setCategoryId(object.getString("category_id"));
                                    shopCategory.setSchemeCode(object.getString("scheme_code"));
                                    String tempCategory = object.getString("category_name").toLowerCase();

                                    String[] strArray = tempCategory.split(" ");
                                    StringBuilder builder = new StringBuilder();
                                    for (String s : strArray) {
                                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                        builder.append(cap + " ");
                                    }

                                    shopCategory.setCategoryName(builder.toString());
                                    shopCategories.add(shopCategory);
                                }


                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);
                            }

                            //gridBbsCategoryAdapter.notifyDataSetChanged();
                            if(isAdded()) {
                                GridHome adapter = new GridHome(getActivity(), SetupListMenu(), SetupListMenuIcons());
                                GridHome.setAdapter(adapter);
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
                        if (progdialog.isShowing())
                            progdialog.dismiss();

                        if (MyApiClient.PROD_FAILURE_FLAG)
                            Toast.makeText(getActivity().getApplication(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity().getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                        Timber.w("Error Koneksi login:" + throwable.toString());

                        //gridBbsCategoryAdapter.notifyDataSetChanged();
                        if(isAdded()) {
                            GridHome adapter = new GridHome(getActivity(), SetupListMenu(), SetupListMenuIcons());
                            GridHome.setAdapter(adapter);
                        }
                    }

                });
            }
        }




        btn_beli.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(view_pulsa.getVisibility()==View.VISIBLE)
                {
                    if (inputValidation()==true)
                    {
                        Intent intent = new Intent(getActivity(), BillerActivity.class);
                        intent.putExtra(DefineValue.BILLER_TYPE, "PLS");
                        intent.putExtra(DefineValue.BILLER_ID_NUMBER, input.getText().toString());
                        intent.putExtra(DefineValue.BILLER_NAME, "Voucher Pulsa Handphone");
                        startActivity(intent);
                    }
//                    Bundle bundle;
//                    bundle= new Bundle();
//                    bundle.putString(DefineValue.PHONE_NUMBER, input.getText().toString());
//                    switchMenu(NavigationDrawMenu.MDAP,bundle);
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
                input.setError(null);
                input.clearFocus();
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
                input.setError(null);
                input.clearFocus();
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
                    Intent i = new Intent(getActivity(), BBSActivity.class);
                    i.putExtra(DefineValue.INDEX, BBSActivity.TRANSACTION);
                    i.putExtra(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                    switchActivity(i,MainPage.ACTIVITY_RESULT);
                } else if ( menuItemName.equals(getString(R.string.cash_out)) ) {
                    Intent i = new Intent(getActivity(), BBSActivity.class);
                    i.putExtra(DefineValue.INDEX, BBSActivity.TRANSACTION);
                    i.putExtra(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                    switchActivity(i,MainPage.ACTIVITY_RESULT);
                } else if ( menuItemName.equals(getString(R.string.menu_item_title_trx_agent))) {
                    Intent i = new Intent(getActivity(), BBSActivity.class);
                    i.putExtra(DefineValue.INDEX, BBSActivity.BBSTRXAGENT);
                    switchActivity(i,MainPage.ACTIVITY_RESULT);
                } else if (menuItemName.equals(getString(R.string.menu_item_title_onprogress_agent)) ) {
                    Intent i = new Intent(getActivity(), BBSActivity.class);
                    i.putExtra(DefineValue.INDEX, BBSActivity.BBSONPROGRESSAGENT);
                    switchActivity(i,MainPage.ACTIVITY_RESULT);
                }else if (menuItemName.equals(getString(R.string.title_cash_out_member)) ) {
                    Intent i = new Intent(getActivity(), BBSActivity.class);
                    i.putExtra(DefineValue.INDEX, BBSActivity.CONFIRMCASHOUT);
                    switchActivity(i,MainPage.ACTIVITY_RESULT);
                }
                else
                {
                    for(int x=0;x<shopCategories.size();x++) {
                        String categoryName = shopCategories.get(x).getCategoryName();
                        if ( menuItemName.indexOf(categoryName) > 0 ) {
                            Intent i = new Intent(getActivity(), BbsNewSearchAgentActivity.class);
                            i.putExtra(DefineValue.CATEGORY_ID, shopCategories.get(x).getCategoryId());
                            i.putExtra(DefineValue.CATEGORY_NAME, shopCategories.get(x).getCategoryName());
                            i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                            i.putExtra(DefineValue.AMOUNT, "");
                            i.putExtra(DefineValue.BBS_SCHEME_CODE, shopCategories.get(x).getSchemeCode());
                            switchActivity(i, MainPage.ACTIVITY_RESULT);
                            break;
                        }
                    }
                    //switchMenu(NavigationDrawMenu.MCASHOUT,null);
                }

            }

        });

        if ( sp.getBoolean(DefineValue.IS_AGENT, false) ) {

            swSettingOnline.setOnCheckedChangeListener(null);
            if ( sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO) ) {
                swSettingOnline.setChecked(true);
            } else {
                swSettingOnline.setChecked(false);
            }
            swSettingOnline.setOnCheckedChangeListener(switchListener);
        }

        RefreshSaldo();
        RefreshPoin();
        if(levelClass != null)
            levelClass.refreshData();
    }

    private ArrayList<String> SetupListMenu(){
        String[] _data;
        ArrayList<String> data = new ArrayList<>() ;
        Boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);
        if(isAgent) {
            _data = getResources().getStringArray(R.array.list_menu_frag_new_home_agent);
            Collections.addAll(data,_data);

        } else {

            String[] categories = new String[shopCategories.size()];
            for(int x =0 ; x < shopCategories.size(); x++ ) {
                categories[x] = getString(R.string.menu_item_search_agent_bbs) + " " + shopCategories.get(x).getCategoryName();
            }
            Collections.addAll(data,categories);

            _data = getResources().getStringArray(R.array.list_menu_frag_new_home_not_agent);
            Collections.addAll(data,_data);
        }
        _data = getResources().getStringArray(R.array.list_menu_frag_new_home);
        Collections.addAll(data,_data);
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
            totalIdx    += taNotAgent.length();

        }

        int[] data        = new int[totalIdx];

        if(isAgent) {
            for( int j = 0; j < taAgent.length(); j++) {
                data[j] = taAgent.getResourceId(j, -1);
                overallIdx++;
            }


        } else {
            for(int x =0; x < shopCategories.size(); x++ ) {
                data[x] = R.drawable.ic_location_on_black;
                overallIdx++;
            }


            for( int j = 0; j < taNotAgent.length(); j++) {
                data[overallIdx] = taNotAgent.getResourceId(j, -1);
                overallIdx++;
            }
        }

        for( int j = 0; j < ta.length(); j++) {
            data[overallIdx] = ta.getResourceId(j, -1);
            overallIdx++;
        }

        return data;
    }

    private boolean inputValidation(){
        if(input.getText().toString().length()==0){
            input.requestFocus();
            input.setError(getString(R.string.validation_pulsa));
            return false;
        }
        if(input.getText().toString().charAt(0) == ' '){
            input.requestFocus();
            input.setError(getString(R.string.validation_pulsa));
            return false;
        }
        if(input.getText().toString().length()<5){
            input.requestFocus();
            input.setError(getString(R.string.validation_pulsa));
            return false;
        }
        return true;
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

    private void RefreshPoin(){
        String balance_poin = sp.getString(DefineValue.BALANCE_POIN,"0");
        tv_poin.setText(PoinFormat.format(balance_poin));
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
            RefreshPoin();

            String action = intent.getAction();
            if ( action.equals(AgentShopService.INTENT_ACTION_AGENT_SHOP) ) {

                if ( !sp.getBoolean(DefineValue.IS_AGENT, false) ) {
                    llAgentDetail.setVisibility(View.GONE);

                } else {
                    if ( sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES) ) {
                        llAgentDetail.setVisibility(View.VISIBLE);
                    } else {
                        llAgentDetail.setVisibility(View.GONE);
                    }

                }

                if ( sp.getBoolean(DefineValue.IS_AGENT, false) ) {
                    Timber.d("Receiver AgentShop " + sp.getString(DefineValue.AGENT_SHOP_CLOSED, "") );
                    swSettingOnline.setOnCheckedChangeListener(null);
                    if ( sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO) ) {
                        swSettingOnline.setChecked(true);
                    } else {
                        swSettingOnline.setChecked(false);
                    }
                    swSettingOnline.setOnCheckedChangeListener(switchListener);
                }
            }
        }
    };
    @Override
    public void onResume() {
        super.onResume();

        if ( sp.getBoolean(DefineValue.IS_AGENT, false) ) {

            swSettingOnline.setOnCheckedChangeListener(null);
            if ( sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO) ) {
                swSettingOnline.setChecked(true);
            } else {
                swSettingOnline.setChecked(false);
            }
            swSettingOnline.setOnCheckedChangeListener(switchListener);
        }

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

    Switch.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RequestParams params    = new RequestParams();
            shopStatus              = DefineValue.SHOP_OPEN;
            Boolean isCallWebservice    = false;

            if (!isChecked) {
                //buka
                shopStatus          = DefineValue.SHOP_CLOSE;

            }

            if (shopStatus.equals(DefineValue.SHOP_OPEN)) {
                if ( !sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO) ) {
                    isCallWebservice    = true;
                }

            } else {
                if ( !sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_YES) ) {
                    isCallWebservice    = true;
                }
            }

            if ( !GlobalSetting.isLocationEnabled(getActivity()) && shopStatus.equals(DefineValue.SHOP_OPEN) ) {
                showAlertEnabledGPS();
            } else {
                if (isCallWebservice) {

                    progdialog2 = DefinedDialog.CreateProgressDialog(getContext(), "");

                    UUID rcUUID = UUID.randomUUID();
                    String dtime = DateTimeFormat.getCurrentDateTime();

                    params.put(WebParams.RC_UUID, rcUUID);
                    params.put(WebParams.RC_DATETIME, dtime);
                    params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                    params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                    params.put(WebParams.SHOP_ID, sp.getString(DefineValue.BBS_SHOP_ID, ""));
                    params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.BBS_MEMBER_ID, ""));
                    params.put(WebParams.SHOP_STATUS, shopStatus);


                    String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + sp.getString(DefineValue.BBS_MEMBER_ID, "") + sp.getString(DefineValue.BBS_SHOP_ID, "") + BuildConfig.APP_ID + shopStatus));

                    params.put(WebParams.SIGNATURE, signature);

                    MyApiClient.updateCloseShopToday(getContext(), params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, org.apache.http.Header[] headers, JSONObject response) {
                            progdialog2.dismiss();

                            try {

                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    SecurePreferences.Editor mEditor = sp.edit();
                                    if (shopStatus.equals(DefineValue.SHOP_OPEN)) {
                                        Toast.makeText(getContext(), getString(R.string.process_update_online_success), Toast.LENGTH_SHORT).show();
                                        mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, DefineValue.STRING_NO);
                                    } else {
                                        Toast.makeText(getContext(), getString(R.string.process_update_offline_success), Toast.LENGTH_SHORT).show();
                                        mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, DefineValue.STRING_YES);
                                    }

                                    mEditor.apply();
                                } else {
                                    Toast.makeText(getContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                                }

                                Intent i = new Intent(AgentShopService.INTENT_ACTION_AGENT_SHOP);
                                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, org.apache.http.Header[] headers, String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers, responseString, throwable);
                            ifFailure(throwable);
                        }

                        @Override
                        public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            ifFailure(throwable);
                        }

                        private void ifFailure(Throwable throwable) {
                            Toast.makeText(getContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                            progdialog2.dismiss();
                            Timber.w("Error Koneksi login:" + throwable.toString());

                        }
                    });
                }
            }

        }
    };

    private void showAlertEnabledGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.alertbox_gps_warning))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(ilocation, RC_GPS_REQUEST);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();

                        swSettingOnline.setOnClickListener(null);
                        swSettingOnline.setChecked(false);
                        swSettingOnline.setOnCheckedChangeListener(switchListener);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
