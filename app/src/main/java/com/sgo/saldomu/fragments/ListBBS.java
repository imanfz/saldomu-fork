package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.services.UpdateBBSData;

import timber.log.Timber;

/**
 * Created by thinkpad on 1/25/2017.
 */

public class ListBBS extends ListFragment{

    private View v;
    private boolean isJoin = false;
    String[] _data;
    Boolean isAgent;
    SecurePreferences sp;
    ProgressDialog progDialog;
    IntentFilter intentFilter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);
        progDialog = DefinedDialog.CreateProgressDialog(getContext());
        progDialog.dismiss();
        if(isAgent) {
            _data = getResources().getStringArray(R.array.list_bbs_agent);
            boolean isUpdatingData = sp.getBoolean(DefineValue.IS_UPDATING_BBS_DATA,false);
            if(isUpdatingData)
                progDialog.show();
            else
                checkAndRunServiceBBS();
        }
        else
            _data = getResources().getStringArray(R.array.list_bbs_member);

        intentFilter = new IntentFilter();
        intentFilter.addAction(UpdateBBSData.INTENT_ACTION_BBS_DATA);
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

        EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

        Bundle bundle = getArguments();
        if(bundle != null){
            int posIdx = bundle.getInt(DefineValue.INDEX,-1);
            if(posIdx != -1){
                Intent i = new Intent(getActivity(), BBSActivity.class);
                i.putExtras(bundle);
                switchActivity(i,MainPage.ACTIVITY_RESULT);
            }
        }
    }

    void checkAndRunServiceBBS(){
        BBSDataManager bbsDataManager = new BBSDataManager();
        if(!bbsDataManager.isDataUpdated()) {
            progDialog.show();
            bbsDataManager.runServiceUpdateData(getContext());
            Timber.d("Run Service update data BBS");
        }
    }


    @Override
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
            else {
                posIdx = -1;
            }
        } else
            posIdx = BBSActivity.CONFIRMCASHOUT;

        if(posIdx !=-1){
            Intent i = new Intent(getActivity(), BBSActivity.class);
            i.putExtra(DefineValue.INDEX, posIdx);
            switchActivity(i,MainPage.ACTIVITY_RESULT);
        }
    }

    private void switchActivity(Intent mIntent, int j){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,j);
    }

    private void switchMenu(int menuIdx){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchMenu(menuIdx,null);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(UpdateBBSData.INTENT_ACTION_BBS_DATA)){
                if(progDialog.isShowing())
                    progDialog.dismiss();
                if(!intent.getBooleanExtra(DefineValue.IS_SUCCESS,false)){
                    Toast.makeText(getContext(),getString(R.string.error_message),Toast.LENGTH_LONG).show();
                    switchMenu(NavigationDrawMenu.MHOME);
                }
            }
        }
    };


}
