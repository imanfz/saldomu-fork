package com.sgo.saldomu.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.entityRealm.BBSCommModel;

import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by sgo on 6/3/2015.
 */
public class AboutAppsActivity extends BaseActivity {

    private int RESULT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeToolbar();

        Realm realmBBS = RealmManager.getRealmBBS();
        Realm realmBiller = RealmManager.getRealmBiller();
        TextView versionDetail = (TextView) findViewById(R.id.version_detail);
        try
        {
            StringBuilder stringBuilder = new StringBuilder();
            String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            String bbs_ver = realmBBS.where(BBSCommModel.class).findFirst().getLast_update();
            bbs_ver = bbs_ver.replace("-","");
            String biller_ver = realmBiller.where(Biller_Type_Data_Model.class).findFirst().getLast_update();
            biller_ver = biller_ver.replace("-","");

            stringBuilder.append("V ");
            stringBuilder.append(app_ver);
            stringBuilder.append("-");
            stringBuilder.append(bbs_ver);
            stringBuilder.append("-");
            stringBuilder.append(biller_ver);

            versionDetail.setText(stringBuilder.toString());
                    //" last update (" + getResources().getString(R.string.last_update) + ")");
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Timber.e("error app ver = " + e.getMessage());
        }

        RESULT = MainPage.RESULT_NORMAL;
    }

    private void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.title_about_app));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_about_apps;
    }

}
