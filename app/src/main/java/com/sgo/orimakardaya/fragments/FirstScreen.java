package com.sgo.orimakardaya.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.LoginActivity;
import com.sgo.orimakardaya.activities.Registration;
import com.sgo.orimakardaya.adapter.FirstScreenSlideAdapter;
import com.viewpagerindicator.CirclePageIndicator;

/**
 Created by Administrator on 7/15/2014.
 */
public class FirstScreen extends Fragment {

    Button btnDaftar,btnLogin;
    Fragment mFragment;
    View v;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_firstscreen, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnDaftar = (Button) v.findViewById(R.id.btn_daftar);
        btnLogin = (Button) v.findViewById(R.id.btn_login);

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.firstscreen_pager);
        FirstScreenSlideAdapter adapter = new FirstScreenSlideAdapter(getChildFragmentManager(),getActivity());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        CirclePageIndicator circleIndicator = (CirclePageIndicator) v.findViewById(R.id.firstscreen_indicator);
        circleIndicator.setViewPager(viewPager);

        btnDaftar.setOnClickListener(bukaReg);
        btnLogin.setOnClickListener(bukaLogin);
    }

    Button.OnClickListener bukaReg = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            mFragment = new Regist1();
            switchFragment(mFragment,"reg1",true);
        }
    };

    Button.OnClickListener bukaLogin = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            Intent i = new Intent(getActivity(),LoginActivity.class);
            startActivity(i);
        }
    };

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        Registration fca = (Registration) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}