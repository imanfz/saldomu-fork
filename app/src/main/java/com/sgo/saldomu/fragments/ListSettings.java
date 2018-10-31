package com.sgo.saldomu.fragments;

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
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.AboutAppsActivity;
import com.sgo.saldomu.activities.ChangePIN;
import com.sgo.saldomu.activities.ChangePassword;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.RegisterSMSBankingActivity;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.dialogs.PickLanguageDialog;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by thinkpad on 6/11/2015.
 */
public class ListSettings extends ListFragment {
    private View v;

    private SecurePreferences sp;
    private InformationDialog dialogI;
    private LevelClass levelClass;

    ArrayList<String> list = new ArrayList<>();
    private Boolean isLevel1;

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
        dialogI = InformationDialog.newInstance(11);
        dialogI.setTargetFragment(this,0);
        String[] _data;

        if(!isLevel1){
            _data = getResources().getStringArray(R.array.settings_list_pin_2);
            list.addAll(Arrays.asList(_data));
        }
//        else

        _data = getResources().getStringArray(R.array.settings_list_pin);
        list.addAll(Arrays.asList(_data));

        EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, list);

        ListView listView1 = v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i;

        switch (list.get(position)){
            case "Registrasi SMS Banking":
                i = new Intent(getActivity(), RegisterSMSBankingActivity.class);
                switchActivity(i);
                break;
            case "Ganti Password":
                i = new Intent(getActivity(), ChangePassword.class);
                switchActivity(i);
                break;
            case "Ganti PIN":
                i = new Intent(getActivity(), ChangePIN.class);
                switchActivity(i);
                break;
            case "Tentang App":
                i = new Intent(getActivity(), AboutAppsActivity.class);
                switchActivity(i);
                break;
            case "Bahasa":
                PickLanguageDialog dialog = PickLanguageDialog.Companion.initDialog(() -> {

                });
                dialog.show(getFragmentManager(), "asd");
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
