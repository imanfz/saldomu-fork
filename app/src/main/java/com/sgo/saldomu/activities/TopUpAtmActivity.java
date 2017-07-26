package com.sgo.saldomu.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.TopupATMObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.TopupATMAdapter;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.dialogs.InformationDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thinkpad on 5/28/2015.
 */
public class TopUpAtmActivity extends BaseActivity {

    private InformationDialog dialogI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        String bankCode = sp.getString(DefineValue.BANK_ATM_CODE, "");
        String bankName = sp.getString(DefineValue.BANK_ATM_NAME, "");
        String noVA = sp.getString(DefineValue.NO_VA, "");
        String maxTopupValue = sp.getString(DefineValue.MAX_TOPUP, "");

        ListView lvAtm = (ListView) findViewById(R.id.lvAtm);

        List<TopupATMObject> mList = new ArrayList<>();
        String[] arrayBankCode = bankCode.split(",");
        String[] arrayNoVA = noVA.split(",");
        String[] arrayBankName = bankName.split(",");

        for(int i=0 ; i<arrayBankCode.length ; i++) {
            TopupATMObject mObject = new TopupATMObject();
            mObject.setBank_code(arrayBankCode[i]);
            mObject.setNo_va(arrayNoVA[i]);
            mObject.setBank_name(arrayBankName[i]);
            mList.add(mObject);
        }

        TopupATMAdapter mAdapter = new TopupATMAdapter(getApplicationContext(), mList, maxTopupValue);
        lvAtm.setAdapter(mAdapter);

        dialogI = InformationDialog.newInstance(1);
    }

    private void InitializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.atm));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(MainPage.RESULT_NORMAL);
                finish();
                return true;
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(getSupportFragmentManager(), InformationDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_topup_atm;
    }

}

