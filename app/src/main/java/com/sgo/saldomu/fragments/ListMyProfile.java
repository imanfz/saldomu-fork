package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.*;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;

/*
  Created by Administrator on 1/18/2015.
 */
public class ListMyProfile extends ListFragment {

    private View v;

    private SecurePreferences sp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_list_my_profile, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        String authType = sp.getString(DefineValue.AUTHENTICATION_TYPE, "");

        String[] _data = null;
        if(authType.equalsIgnoreCase("OTP")) {
            _data = getResources().getStringArray(R.array.myprofilelist_list_item);
        }
        else if(authType.equalsIgnoreCase("PIN")) {
            _data = getResources().getStringArray(R.array.myprofilelist_list_item_pin);
        }

        EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i;
        switch (position) {
            case 0:
                i = new Intent(getActivity(), MyProfileActivity.class);
                switchActivity(i);
                break;
            case 1:
                i = new Intent(getActivity(),ChangePassword.class);
                switchActivity(i);
                break;
            case 2:
                i = new Intent(getActivity(), ChangePIN.class);
                switchActivity(i);
                break;
        }
    }

    private void switchFragment(android.support.v4.app.Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }
}