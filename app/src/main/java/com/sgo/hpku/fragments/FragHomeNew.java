package com.sgo.hpku.fragments;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import com.sgo.hpku.R;

import com.sgo.hpku.activities.BillerActivity;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.activities.TopUpActivity;
import com.sgo.hpku.adapter.GridHome;
import com.sgo.hpku.coreclass.BaseFragmentMainPage;
import com.sgo.hpku.coreclass.DefineValue;

import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Created by Lenovo Thinkpad on 5/10/2017.
 */
public class FragHomeNew extends BaseFragmentMainPage {
    GridView GridHome;
    Button btn_beli;
    EditText input;
    TextView tv_pulsa;
    TextView tv_bpjs;
    TextView tv_listrikPLN;
    View view_pulsa;
    View view_bpjs;
    View view_listrikPLN;
    View v;
    int[] imageId = {
            R.drawable.ic_tambahsaldo,
            R.drawable.ic_bayarteman1,
            R.drawable.ic_mintauang,
            R.drawable.ic_belanja,
            R.drawable.ic_laporan,
            R.drawable.ic_tariktunai,
            R.drawable.ic_tariktunai,
    };
    String[] text = {
            "TAMBAH SALDO",
            "BAYAR TEMAN",
            "MINTA UANG",
            "BELANJA",
            "LAPORAN",
            "CASH IN",
            "CASH OUT",
    } ;

    public FragHomeNew() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag_home_new, container, false);
        GridHome=(GridView)v.findViewById(R.id.grid);
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
        view_pulsa = (View) v.findViewById(R.id.view_pulsa);
        view_bpjs = (View) v.findViewById(R.id.view_bpjs);
        view_listrikPLN = (View) v.findViewById(R.id.view_listrikPLN);
        GridHome adapter = new GridHome(getActivity(),text,imageId);
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

                }
                else
                {
                    switchMenu(NavigationDrawMenu.MCASHOUT,null);
                }

            }

        });
    }
    private void switchMenu(int idx_menu,Bundle data){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchMenu(idx_menu, data);
    }

//    Button.OnClickListener backListener = new Button.OnClickListener(){
//
//        @Override
//        public void onClick(View v) {
//            switchMenu();
//        }
//    }

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
}
