package com.sgo.hpku.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;

import com.sgo.hpku.activities.BBSActivity;
import com.sgo.hpku.activities.BillerActivity;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.activities.TopUpActivity;
import com.sgo.hpku.adapter.GridHome;
import com.sgo.hpku.coreclass.BaseFragmentMainPage;
import com.sgo.hpku.coreclass.CurrencyFormat;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.services.BalanceService;

import java.util.ArrayList;
import java.util.Collections;

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
    private SecurePreferences sp;
    int[] imageId = {
            R.drawable.ic_tambahsaldo,
            R.drawable.ic_bayarteman1,
            R.drawable.ic_mintauang,
            R.drawable.ic_belanja,
            R.drawable.ic_laporan,
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
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
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

        btn_beli = (Button) v.findViewById(R.id.btn_beli);
        input = (EditText) v.findViewById(R.id.input);
        tv_pulsa = (TextView) v.findViewById(R.id.tv_pulsa);
        tv_bpjs =(TextView) v.findViewById(R.id.tv_bpjs);
        tv_listrikPLN = (TextView) v.findViewById(R.id.tv_listrikPLN);
        view_pulsa = v.findViewById(R.id.view_pulsa);
        view_bpjs = v.findViewById(R.id.view_bpjs);
        view_listrikPLN = v.findViewById(R.id.view_listrikPLN);
        GridHome adapter = new GridHome(getActivity(),SetupListMenu(),imageId);
        GridHome.setAdapter(adapter);

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
                if (position == 0) {
                    switchMenu(NavigationDrawMenu.MTOPUP,null);
                }
                else if (position == 1) {
                    switchMenu(NavigationDrawMenu.MPAYFRIENDS,null);
                }
                else if (position == 2) {
                    switchMenu(NavigationDrawMenu.MASK4MONEY,null);
                }
                else if (position == 3) {
                    switchMenu(NavigationDrawMenu.MBUY,null);
                }
                else if (position == 4) {
                    switchMenu(NavigationDrawMenu.MREPORT,null);
                }
                else if (position == 5) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                    bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                    switchMenu(NavigationDrawMenu.MBBS,bundle);
                }
                else if (position == 6) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                    bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                    switchMenu(NavigationDrawMenu.MBBS,bundle);
                }
                else
                {
                    switchMenu(NavigationDrawMenu.MCASHOUT,null);
                }

            }

        });

        RefreshSaldo();
    }

    private ArrayList<String> SetupListMenu(){
        String[] _data = getResources().getStringArray(R.array.list_menu_frag_new_home);
        ArrayList<String> data = new ArrayList<>() ;
        Collections.addAll(data,_data);
        Boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);
        if(isAgent) {
            _data = getResources().getStringArray(R.array.list_menu_frag_new_home_agent);
            Collections.addAll(data,_data);
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
}
