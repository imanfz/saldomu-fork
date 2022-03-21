package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.TagihActivity;
import com.sgo.saldomu.adapter.GridBbsMenu;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.services.AgentShopService;
import com.sgo.saldomu.services.UpdateBBSData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 1/25/2017.
 */

public class ListBBS extends Fragment {

    private View v;
    String[] _data;
    Boolean isAgent;
    SecurePreferences sp;
    private IntentFilter filter;
    private LinearLayout llAgentDetail;
    private SwitchCompat swSettingOnline;
    String shopStatus;
    ProgressDialog progdialog2;
    ProgressDialog progDialog;
    private static final int RC_GPS_REQUEST = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        filter = new IntentFilter();
        filter.addAction(AgentShopService.INTENT_ACTION_AGENT_SHOP);
        filter.addAction(UpdateBBSData.INTENT_ACTION_BBS_DATA);

        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);
        progDialog = DefinedDialog.CreateProgressDialog(getContext());
        progDialog.dismiss();
        if (isAgent) {
            _data = getResources().getStringArray(R.array.list_bbs_agent);
            boolean isUpdatingData = sp.getBoolean(DefineValue.IS_UPDATING_BBS_DATA, false);
            if (isUpdatingData)
                progDialog.show();
            else
                checkAndRunServiceBBS();
        } else
            _data = getResources().getStringArray(R.array.list_bbs_member);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_bbs, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        llAgentDetail = v.findViewById(R.id.llAgentDetail);
        swSettingOnline = v.findViewById(R.id.swSettingOnline);
        llAgentDetail.setVisibility(View.GONE);
        setAgentDetailToUI();

        GridView gvListBbs = v.findViewById(R.id.gvListBbs);

        GridBbsMenu gridBbsMenuAdapter = new GridBbsMenu(getActivity(), SetupMenuItems(), SetupMenuIcons());
        gvListBbs.setAdapter(gridBbsMenuAdapter);

        gvListBbs.setOnItemClickListener((parent, view, position, id) -> {
            String menuItemName = ((TextView) view.findViewById(R.id.tvMenuName)).getText().toString();
            String trxType = "";
            int posIdx;
            if (isAgent) {
                if (menuItemName.equalsIgnoreCase(getString(R.string.title_bbs_list_account_bbs)))
                    posIdx = BBSActivity.LISTACCBBS;
                else if (menuItemName.equalsIgnoreCase(getString(R.string.transaction)))
                    posIdx = BBSActivity.TRANSACTION;
                else if (menuItemName.equalsIgnoreCase(getString(R.string.title_cash_out_member)))
                    posIdx = BBSActivity.CONFIRMCASHOUT;
                else if (menuItemName.equalsIgnoreCase(getString(R.string.menu_item_title_kelola)))
                    posIdx = BBSActivity.BBSKELOLA;
                    //else if (_data[position].equalsIgnoreCase(getString(R.string.menu_item_title_list_approval)))
                    //posIdx = BBSActivity.BBSAPPROVALAGENT;
                else if (menuItemName.equalsIgnoreCase(getString(R.string.menu_item_title_trx_agent)))
                    posIdx = BBSActivity.BBSTRXAGENT;
                else if (menuItemName.equalsIgnoreCase(getString(R.string.menu_item_title_waktu_beroperasi)))
                    posIdx = BBSActivity.BBSWAKTUBEROPERASI;
                else if (menuItemName.equalsIgnoreCase(getString(R.string.menu_item_title_tutup_manual)))
                    posIdx = BBSActivity.BBSTUTUPMANUAL;
                else if (menuItemName.equalsIgnoreCase(getString(R.string.cash_in))) {
                    posIdx = BBSActivity.TRANSACTION;
                    trxType = DefineValue.BBS_CASHIN;
                } else if (menuItemName.equalsIgnoreCase(getString(R.string.cash_out))) {
                    posIdx = BBSActivity.TRANSACTION;
                    trxType = DefineValue.BBS_CASHOUT;
                } else if (menuItemName.equalsIgnoreCase(getString(R.string.transfer_funds))) {
                    posIdx = BBSActivity.TRANSACTION;
                    trxType = DefineValue.BBS_TRANSFER_FUND;
                } else if (menuItemName.equals(getString(R.string.menu_item_title_onprogress_agent))) {
                    posIdx = BBSActivity.BBSONPROGRESSAGENT;
                    trxType = DefineValue.INDEX;
                } else if (menuItemName.equals(getString(R.string.menu_item_title_tagih_agent))) {
                    posIdx = -1;
                    startActivity(new Intent(getActivity(), TagihActivity.class));
                } else {
                    posIdx = -1;
                }
            } else {
                if (menuItemName.equalsIgnoreCase(getString(R.string.title_cash_out_member)))
                    posIdx = BBSActivity.CONFIRMCASHOUT;
                else if (menuItemName.equalsIgnoreCase(getString(R.string.title_rating_by_member)))
                    posIdx = BBSActivity.BBSRATINGBYMEMBER;
                else if (menuItemName.equalsIgnoreCase(getString(R.string.title_bbs_my_orders)))
                    posIdx = BBSActivity.BBSMYORDERS;
                else {
                    posIdx = -1;
                }

            }
            if (posIdx != -1) {
                Intent i = new Intent(getActivity(), BBSActivity.class);
                i.putExtra(DefineValue.INDEX, posIdx);

                if (!trxType.equals(""))
                    i.putExtra(DefineValue.TYPE, trxType);

                switchActivity(i, MainPage.ACTIVITY_RESULT);
            }

        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            int posIdx = bundle.getInt(DefineValue.INDEX, -1);
            if (posIdx != -1) {
                Intent i = new Intent(getActivity(), BBSActivity.class);
                i.putExtras(bundle);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
            }
        }
    }

    void checkAndRunServiceBBS() {
        BBSDataManager bbsDataManager = new BBSDataManager();
        if (!bbsDataManager.isDataUpdated()) {
            progDialog.show();
            bbsDataManager.runServiceUpdateData(getContext());
            Timber.d("Run Service update data BBS");
        }
    }


    /*@Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        int posIdx;
        if(isAgent) {
            if (_data[position].equalsIgnoreCase(getString(R.string.title_bbs_list_account_bbs)))
                posIdx = BBSActivity.LISTACCBBS;
            else if (_data[position].equalsIgnoreCase(getString(R.string.transaction)))
                posIdx = BBSActivity.TRANSACTION;
            else if (_data[position].equalsIgnoreCase(getString(R.string.title_cash_out_member)))
                posIdx = BBSActivity.CONFIRMCASHOUT;
            else if (_data[position].equalsIgnoreCase(getString(R.string.menu_item_title_kelola)))
                posIdx = BBSActivity.BBSKELOLA;
            //else if (_data[position].equalsIgnoreCase(getString(R.string.menu_item_title_list_approval)))
                //posIdx = BBSActivity.BBSAPPROVALAGENT;
            else if (_data[position].equalsIgnoreCase(getString(R.string.menu_item_title_trx_agent)))
                posIdx = BBSActivity.BBSTRXAGENT;
            else if (_data[position].equalsIgnoreCase(getString(R.string.menu_item_title_waktu_beroperasi)))
                posIdx = BBSActivity.BBSWAKTUBEROPERASI;
            else if (_data[position].equalsIgnoreCase(getString(R.string.menu_item_title_tutup_manual)))
                posIdx = BBSActivity.BBSTUTUPMANUAL;
            else {
                posIdx = -1;
            }
        } else {
            if (_data[position].equalsIgnoreCase(getString(R.string.title_cash_out_member)))
                posIdx = BBSActivity.CONFIRMCASHOUT;
            else if (_data[position].equalsIgnoreCase(getString(R.string.title_rating_by_member)))
                posIdx = BBSActivity.BBSRATINGBYMEMBER;
            else if (_data[position].equalsIgnoreCase(getString(R.string.title_bbs_my_orders)))
                posIdx = BBSActivity.BBSMYORDERS;
            else {
                posIdx = -1;
            }

        }
        if(posIdx !=-1){
            Intent i = new Intent(getActivity(), BBSActivity.class);
            i.putExtra(DefineValue.INDEX, posIdx);
            switchActivity(i,MainPage.ACTIVITY_RESULT);
        }
    }
    */

    private void switchActivity(Intent mIntent, int j) {
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, j);
    }

    private ArrayList<String> SetupMenuItems() {
        ArrayList<String> menuItems = new ArrayList<>();

        Collections.addAll(menuItems, _data);

        checkSchemeCode(menuItems);

        return menuItems;
    }

    void checkSchemeCode(ArrayList<String> menuItems) {
        String string = sp.getString(DefineValue.AGENT_SCHEME_CODES, "");
        try {
            JSONArray arr = new JSONArray(string);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String objs = obj.optString(WebParams.SCHEME_CODE, "");

                switch (objs) {
                    case "ATC":
                        menuItems.add(0, getResources().getString(R.string.cash_out));
                        break;
                    case "CTA":
                        menuItems.add(0, getResources().getString(R.string.cash_in));
                        break;
                    case "TFD":
                        menuItems.add(0, getResources().getString(R.string.transfer_funds));
                        break;
                    case "DGI":
                        menuItems.add(0, getResources().getString(R.string.menu_item_title_tagih_agent));
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int[] SetupMenuIcons() {
        int totalIdx = 0;

        TypedArray taAgent = getResources().obtainTypedArray(R.array.list_icon_bbs_agent);
        TypedArray taMember = getResources().obtainTypedArray(R.array.list_icon_bbs_member);

        if (isAgent) {
            totalIdx += taAgent.length();
        } else {
            totalIdx += taMember.length();
        }

        int[] data = new int[totalIdx];


        if (isAgent) {
            for (int j = 0; j < taAgent.length(); j++) {
                data[j] = taAgent.getResourceId(j, -1);
            }
        } else {

            for (int j = 0; j < taMember.length(); j++) {
                data[j] = taMember.getResourceId(j, -1);
            }
        }
        return data;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    private void switchMenu(int menuIdx) {
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchMenu(menuIdx, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    public void setAgentDetailToUI() {
        if (sp.getBoolean(DefineValue.IS_AGENT, false) && sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES)) {
            llAgentDetail.setVisibility(View.VISIBLE);
        } else {
            llAgentDetail.setVisibility(View.GONE);
        }

        if (sp.getBoolean(DefineValue.IS_AGENT, false)) {
            swSettingOnline.setOnCheckedChangeListener(null);
            if (sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO)) {
                swSettingOnline.setChecked(true);
            } else {
                swSettingOnline.setChecked(false);
            }
            swSettingOnline.setOnCheckedChangeListener(switchListener);
        }
    }

    Switch.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            shopStatus = DefineValue.SHOP_OPEN;
            Boolean isCallWebservice = false;

            if (!isChecked) {
                //buka
                shopStatus = DefineValue.SHOP_CLOSE;

            }

            if (shopStatus.equals(DefineValue.SHOP_OPEN)) {
                if (!sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO)) {
                    isCallWebservice = true;
                }

            } else {
                if (!sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_YES)) {
                    isCallWebservice = true;
                }
            }

            String extraSignature = sp.getString(DefineValue.BBS_MEMBER_ID, "") + sp.getString(DefineValue.BBS_SHOP_ID, "");
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY,
                    extraSignature);

            if (!GlobalSetting.isLocationEnabled(getActivity()) && shopStatus.equals(DefineValue.SHOP_OPEN)) {
                showAlertEnabledGPS();
            } else {
                if (isCallWebservice) {

                    progdialog2 = DefinedDialog.CreateProgressDialog(getContext(), "");

                    params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                    params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                    params.put(WebParams.SHOP_ID, sp.getString(DefineValue.BBS_SHOP_ID, ""));
                    params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.BBS_MEMBER_ID, ""));
                    params.put(WebParams.SHOP_STATUS, shopStatus);
                    params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));

                    RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY, params,
                            new ObjListeners() {
                                @Override
                                public void onResponses(JSONObject response) {
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

                                            getActivity().setResult(MainPage.RESULT_REFRESH_NAVDRAW);

                                            Intent i = new Intent(AgentShopService.INTENT_ACTION_AGENT_SHOP);
                                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);

                                        } else {
                                            setAgentDetailToUI();
                                            Toast.makeText(getContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
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
                                    progdialog2.dismiss();
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
                .setPositiveButton(R.string.yes, (dialog, id) -> {

                    Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(ilocation, RC_GPS_REQUEST);

                })
                .setNegativeButton(R.string.no, (dialog, id) -> {
                    dialog.cancel();

                    swSettingOnline.setOnClickListener(null);
                    swSettingOnline.setChecked(false);
                    swSettingOnline.setOnCheckedChangeListener(switchListener);
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UpdateBBSData.INTENT_ACTION_BBS_DATA)) {
                if (progDialog.isShowing())
                    progDialog.dismiss();
                if (!intent.getBooleanExtra(DefineValue.IS_SUCCESS, false)) {
                    if (BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("development")) {
                        Toast.makeText(getContext(), getString(R.string.error_message), Toast.LENGTH_LONG).show();
                        switchMenu(NavigationDrawMenu.MHOME);
                    }
                }
            } else if (action.equals(AgentShopService.INTENT_ACTION_AGENT_SHOP)) {
                setAgentDetailToUI();
            }
        }
    };

}
