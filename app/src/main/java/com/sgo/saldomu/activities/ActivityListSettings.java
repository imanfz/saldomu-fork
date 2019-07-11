package com.sgo.saldomu.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.widgets.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Lenovo Thinkpad on 12/11/2017.
 */

public class ActivityListSettings extends BaseActivity {
    private SecurePreferences sp;
    private InformationDialog dialogI;
    private Boolean isLevel1, isAgent;
    private LevelClass levelClass;

    ArrayList<String> list = new ArrayList<>();

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

//        authType = sp.getString(DefineValue.AUTHENTICATION_TYPE,"");

        levelClass = new LevelClass(this,sp);
        levelClass.refreshData();
        isLevel1 = levelClass.isLevel1QAC();
        dialogI = InformationDialog.newInstance(11);

        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

        String[] _data;

        _data = getResources().getStringArray(R.array.settings_list_pin);
        list.addAll(Arrays.asList(_data));

        if (isAgent)
        {
            _data = getResources().getStringArray(R.array.settings_is_agent);
            list.addAll(Arrays.asList(_data));
        }
        else{
        _data = getResources().getStringArray(R.array.settings_is_member);
        list.addAll(Arrays.asList(_data));
        }

        _data = getResources().getStringArray(R.array.settings_list_info);
        list.addAll(Arrays.asList(_data));
        EasyAdapter adapter = new EasyAdapter(this, R.layout.list_view_item_with_arrow, list);

        ListView listView1 = findViewById(R.id.list_setting);
        listView1.setAdapter(adapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i;

                switch (list.get(position)) {
                    case "Profil Saya":
                        i = new Intent(ActivityListSettings.this, MyProfileNewActivity.class);
                        startActivity(i);
                        break;
                    case "Informasi":
                        dialogI.show(ActivityListSettings.this.getSupportFragmentManager(), InformationDialog.TAG);
                        break;
                    case "Registrasi SMS Banking":
                        i = new Intent(ActivityListSettings.this, RegisterSMSBankingActivity.class);
                        startActivity(i);
                        break;
                    case "Ganti Password":
                        i = new Intent(ActivityListSettings.this, ChangePassword.class);
                        startActivity(i);
                        break;
                    case "Ganti PIN":
                        i = new Intent(ActivityListSettings.this, ChangePIN.class);
                        startActivity(i);
                        break;
                    case "Ganti Email":
                        i = new Intent(ActivityListSettings.this, ChangeEmail.class);
                        startActivity(i);
                        break;
                    case "Tentang App":
                        i = new Intent(ActivityListSettings.this, AboutAppsActivity.class);
                        startActivity(i);
                        break;
                    case "Pengaturan Agen":
                        i = new Intent(ActivityListSettings.this, BBSActivity.class);
                        i.putExtra(DefineValue.INDEX, BBSActivity.BBSKELOLA);
                        startActivity(i);
                        break;
                    case "Syarat dan Ketentuan" :
                        i = new Intent(ActivityListSettings.this, PrivacyPolicyActivity.class);
                        startActivity(i);
                        break;
//                    case "Bahasa":
//                        PickLanguageDialog dialog = PickLanguageDialog.Companion.initDialog(() -> {
//
//                        });
//                        dialog.show(getFragmentManager(), "asd");
//                        break;
                    case "Info Harga":
                        i=new Intent(ActivityListSettings.this,InfoHargaWebActivity.class);
                        startActivity(i);
                        break;
                    case "Starter Kit":
                        i=new Intent(ActivityListSettings.this,StarterKitActivityKotlin.class);
                        startActivity(i);
                        break;
                    case "Keluar":
                        AlertDialog.Builder alertbox = new AlertDialog.Builder(ActivityListSettings.this);
                        alertbox.setTitle(getString(R.string.warning));
                        alertbox.setMessage(getString(R.string.exit_message));
                        alertbox.setPositiveButton(getString(R.string.ok), (arg0, arg1) -> switchLogout());
                        alertbox.setNegativeButton(getString(R.string.cancel), (arg0, arg1) -> {
                        });
                        alertbox.show();
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(this.getSupportFragmentManager(), InformationDialog.TAG);
                return true;
            case android.R.id.home :
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_setting));
    }
    private void switchLogout() {
        setResult(MainPage.RESULT_LOGOUT);
        finish();
    }
}
