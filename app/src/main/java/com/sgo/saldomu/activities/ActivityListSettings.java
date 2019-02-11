package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
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
    private Boolean isLevel1;
    private LevelClass levelClass;
    ListView list_setting;

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

        String[] _data;

        _data = getResources().getStringArray(R.array.settings_list_pin);
        list.addAll(Arrays.asList(_data));

        if (!isLevel1) {
            _data = getResources().getStringArray(R.array.settings_isnot_lvl1);
            list.addAll(Arrays.asList(_data));
        }
//        else{
        _data = getResources().getStringArray(R.array.settings_is_lvl1);
        list.addAll(Arrays.asList(_data));
//        }

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
//                    case "Bahasa":
//                        PickLanguageDialog dialog = PickLanguageDialog.Companion.initDialog(() -> {
//
//                        });
//                        dialog.show(getFragmentManager(), "asd");
//                        break;
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
}
