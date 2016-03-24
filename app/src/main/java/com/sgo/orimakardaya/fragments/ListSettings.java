package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.dialogs.InformationDialog;

/**
 * Created by thinkpad on 6/11/2015.
 */
public class ListSettings extends ListFragment implements InformationDialog.OnDialogOkCallback {
    View v;

    SecurePreferences sp;
    private InformationDialog dialogI;
    String authType;

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
        authType = sp.getString(DefineValue.AUTHENTICATION_TYPE,"");


        dialogI = InformationDialog.newInstance(this,11);
        String[] _data = null;
        if(authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP)) {
            _data = getResources().getStringArray(R.array.settings_list);
        }
        else if(authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_PIN)) {
            _data = getResources().getStringArray(R.array.settings_list_pin);
        }


        EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i;
        Fragment f;
        if(authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP)) {
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
                    i = new Intent(getActivity(), AboutAppsActivity.class);
                    switchActivity(i);
                    break;
            }
        }
        else if(authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_PIN)) {
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

    @Override
    public void onOkButton() {

    }
}
