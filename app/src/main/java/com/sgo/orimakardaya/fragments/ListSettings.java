package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.AboutAppsActivity;
import com.sgo.orimakardaya.activities.ChangePIN;
import com.sgo.orimakardaya.activities.ChangePassword;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.activities.RegisterSMSBankingActivity;
import com.sgo.orimakardaya.adapter.EasyAdapter;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.dialogs.InformationDialog;
import com.sgo.orimakardaya.coreclass.LevelClass;

/**
 * Created by thinkpad on 6/11/2015.
 */
public class ListSettings extends ListFragment {
    private View v;

    private SecurePreferences sp;
    private InformationDialog dialogI;
    private Boolean isLevel1;
    private LevelClass levelClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_settings, container, false);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
//        authType = sp.getString(DefineValue.AUTHENTICATION_TYPE,"");

        levelClass = new LevelClass(getActivity(),sp);
        levelClass.refreshData();
        isLevel1 = levelClass.isLevel1QAC();
        dialogI = InformationDialog.newInstance(this,11);
        String[] _data;

        if(isLevel1)
            _data = getResources().getStringArray(R.array.settings_list_pin);
        else
            _data = getResources().getStringArray(R.array.settings_list_pin_2);

        EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i;

        if(isLevel1){
            switch (position) {
                case 0:
                    i = new Intent(getActivity(), ChangePassword.class);
                    switchActivity(i);
                    break;

                case 1:
                    i = new Intent(getActivity(), ChangePIN.class);
                    switchActivity(i);
                    break;

                case 2:
                    i = new Intent(getActivity(), AboutAppsActivity.class);
                    switchActivity(i);
                    break;
            }
        }
        else {
            switch (position) {
                case 0:
                    i = new Intent(getActivity(), RegisterSMSBankingActivity.class);
                    switchActivity(i);
                    break;
                case 1:
                    i = new Intent(getActivity(), ChangePassword.class);
                    switchActivity(i);
                    break;

                case 2:
                    i = new Intent(getActivity(), ChangePIN.class);
                    switchActivity(i);
                    break;

                case 3:
                    i = new Intent(getActivity(), AboutAppsActivity.class);
                    switchActivity(i);
                    break;
            }
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
