package com.sgo.saldomu.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.SettingsAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.models.EditMenuModel;
import com.sgo.saldomu.utils.LocaleManager;
import com.sgo.saldomu.widgets.BaseActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 12/11/2017.
 */

public class ActivityListSettings extends BaseActivity implements SettingsAdapter.SettingsListener {
    private SecurePreferences sp;
    private InformationDialog dialogI;
    private Boolean isLevel1, isAgent;
    public static final int RESULT_CHANGE_LANGUAGE = 1200;

    ArrayList<String> list = new ArrayList<>();
    SettingsAdapter adapter;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

        dialogI = InformationDialog.newInstance(11);

        initData();
        initRecycleView();
    }

    private void initData() {
        String[] _data;

        _data = getResources().getStringArray(R.array.settings_list_pin);
        list.addAll(Arrays.asList(_data));

        if (isAgent) {
            _data = getResources().getStringArray(R.array.settings_is_agent);
            list.addAll(Arrays.asList(_data));
        } else {
            _data = getResources().getStringArray(R.array.settings_is_member);
            list.addAll(Arrays.asList(_data));
        }

        _data = getResources().getStringArray(R.array.settings_list_info);
        list.addAll(Arrays.asList(_data));
    }

    private void initRecycleView() {
        adapter = new SettingsAdapter(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        adapter.updateAdapter(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_information:
                if (!dialogI.isAdded())
                    dialogI.show(this.getSupportFragmentManager(), InformationDialog.TAG);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_setting));
    }

    @Override
    public void onClicked(@NotNull String model) {
        Intent i;

        if (model.equals(getResources().getString(R.string.menu_setting_profile))) {
            i = new Intent(ActivityListSettings.this, MyProfileNewActivity.class);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.menu_setting_info))) {
            dialogI.show(ActivityListSettings.this.getSupportFragmentManager(), InformationDialog.TAG);
        } else if (model.equals(getResources().getString(R.string.title_register_sms_banking))) {
            i = new Intent(ActivityListSettings.this, RegisterSMSBankingActivity.class);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.menu_setting_change_pass))) {
            i = new Intent(ActivityListSettings.this, ChangePassword.class);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.menu_setting_change_pin))) {
            i = new Intent(ActivityListSettings.this, ChangePIN.class);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.forgotpin))) {
            i = new Intent(ActivityListSettings.this, ForgotPin.class);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.menu_setting_change_email))) {
            i = new Intent(ActivityListSettings.this, ChangeEmail.class);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.about_apps))) {
            i = new Intent(ActivityListSettings.this, AboutAppsActivity.class);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.menu_item_title_kelola))) {
            i = new Intent(ActivityListSettings.this, BBSActivity.class);
            i.putExtra(DefineValue.INDEX, BBSActivity.BBSKELOLA);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.terms_and_condition))) {
            i = new Intent(ActivityListSettings.this, TermsAndCondition.class);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.menu_item_title_info_harga))) {
            i = new Intent(ActivityListSettings.this, InfoHargaWebActivity.class);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.menu_item_title_starterkit))) {
            i = new Intent(ActivityListSettings.this, StarterKitActivityKotlin.class);
            startActivity(i);
        } else if (model.equals(getResources().getString(R.string.logout))) {
            AlertDialog.Builder alertbox = new AlertDialog.Builder(ActivityListSettings.this);
            alertbox.setTitle(getString(R.string.warning));
            alertbox.setMessage(getString(R.string.exit_message));
            alertbox.setPositiveButton(getString(R.string.ok), (arg0, arg1) -> {
                if (Realm.getDefaultConfiguration() != null) {
                    Realm realm = Realm.getInstance(Realm.getDefaultConfiguration());
                    realm.beginTransaction();
                    realm.delete(EditMenuModel.class);
                    if (realm.isInTransaction())
                        realm.commitTransaction();
                    realm.close();
                }
                sp.edit().putBoolean(DefineValue.LOGOUT_FROM_SESSION_TIMEOUT, false).commit();
                setResult(MainPage.RESULT_LOGOUT);
                finish();
            });
            alertbox.setNegativeButton(getString(R.string.cancel), (arg0, arg1) -> {
            });
            alertbox.show();
        }
    }

    @Override
    public void onChangeLanguage(boolean isBahasa) {
        updateLanguage(isBahasa);
    }

    private void updateLanguage(boolean isBahasa) {
        String language = "";
        if (isBahasa) {
            language = DefineValue.LANGUAGE_CODE_IND;
        } else {
            language = DefineValue.LANGUAGE_CODE_ENG;
        }
        LocaleManager.setNewLocale(this, language);
        restartActivity();
    }

//    private void updateLanguage(boolean isBahasa){
//        Intent intent = new Intent();
//        intent.putExtra(DefineValue.IS_BAHASA, true);
//        setResult(RESULT_CHANGE_LANGUAGE, intent);
//        finish();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Timber.d("Masuk On Activity Result");
        if (resultCode == RESULT_CHANGE_LANGUAGE) {

            if (data != null) {

                boolean isBahasa = data.getBooleanExtra(DefineValue.IS_BAHASA, false);
                String language = "";
                if (isBahasa) {
                    language = DefineValue.LANGUAGE_CODE_IND;
                } else {
                    language = DefineValue.LANGUAGE_CODE_ENG;
                }
                LocaleManager.setNewLocale(this, language);
                restartActivity();
            }
        }
    }

    private void restartActivity() {
        Intent intent = new Intent(this, MainPage.class);
        finish();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
