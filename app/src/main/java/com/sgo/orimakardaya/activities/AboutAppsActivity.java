package com.sgo.orimakardaya.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.BaseActivity;

/**
 * Created by sgo on 6/3/2015.
 */
public class AboutAppsActivity extends BaseActivity {

    private int RESULT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeToolbar();

        TextView versionDetail = (TextView) findViewById(R.id.version_detail);
        try
        {
            String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            versionDetail.setText("V " + app_ver );
                    //" last update (" + getResources().getString(R.string.last_update) + ")");
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.e("error app ver", e.getMessage());
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
