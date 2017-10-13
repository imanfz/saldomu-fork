package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.adapter.FirstScreenSlideAdapter;
import com.viewpagerindicator.CirclePageIndicator;

/**
 Created by Administrator on 7/15/2014.
 */
public class FirstScreen extends Fragment {

    private Button btnLogin;
    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_firstscreen, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button btnDaftar = (Button) v.findViewById(R.id.btn_daftar);
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

    private Button.OnClickListener bukaReg = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            Fragment mFragment = new Regist1();
            switchFragment(mFragment,"reg1",true);
        }
    };

    private Button.OnClickListener bukaLogin = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            Intent i = new Intent(getActivity(),LoginActivity.class);
            startActivity(i);
        }
    };

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
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