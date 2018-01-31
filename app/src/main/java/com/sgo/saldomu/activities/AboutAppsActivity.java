package com.sgo.saldomu.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.entityRealm.BBSBankModel;
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
        Realm realmBBSMember = RealmManager.getRealmBBSMemberBank();
        TextView versionDetail = (TextView) findViewById(R.id.version_detail);
        try
        {
            StringBuilder stringBuilder = new StringBuilder();
            String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            String bbs_ver = "";
            BBSCommModel bbsCommModel = realmBBS.where(BBSCommModel.class).findFirst();
            if (bbsCommModel!=null)
            {
                bbs_ver = bbsCommModel.getLast_update();
            }
            bbs_ver = bbs_ver.replace("-","");
            String biller_ver = "";
            Biller_Type_Data_Model biller_type_data_model = realmBiller.where(Biller_Type_Data_Model.class).findFirst();
            if (biller_type_data_model!=null){
                biller_ver = biller_type_data_model.getLast_update();
            }
            biller_ver = biller_ver.replace("-","");
            String bbs_member_ver = "";
            BBSBankModel bbsBankModel = realmBBSMember.where(BBSBankModel.class).findFirst();
            if (bbsBankModel!=null)
            {
                bbs_member_ver = bbsBankModel.getLast_update();
            }
            bbs_member_ver = bbs_member_ver.replace("-","");

            stringBuilder.append("V ");
            if (app_ver!=null && !app_ver.isEmpty())
            {
                stringBuilder.append(app_ver);
                stringBuilder.append("-");
            }
            if (bbs_ver!=null && !bbs_ver.isEmpty())
            {
                stringBuilder.append("BBS");
                stringBuilder.append(bbs_ver);
                stringBuilder.append("-");
            }
            if (biller_ver!=null && !biller_ver.isEmpty())
            {
                stringBuilder.append("BIL");
                stringBuilder.append(biller_ver);
                stringBuilder.append("-");
            }
            if (bbs_member_ver!=null && !bbs_member_ver.isEmpty()) {
                stringBuilder.append("MEM");
                stringBuilder.append(bbs_member_ver);
            }

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
