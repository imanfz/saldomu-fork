package com.sgo.orimakardaya.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.TopupATMObject;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.adapter.TopupATMAdapter;
import com.sgo.orimakardaya.coreclass.BaseActivity;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.dialogs.InformationDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thinkpad on 5/28/2015.
 */
public class TopUpAtmActivity extends BaseActivity implements InformationDialog.OnDialogOkCallback {

    SecurePreferences sp;
    String bankCode, bankName, noVA, maxTopupValue;
    ListView lvAtm;
    TopupATMAdapter mAdapter;
    List<TopupATMObject> mList;
    private InformationDialog dialogI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        bankCode = sp.getString(DefineValue.BANK_ATM_CODE, "");
        bankName = sp.getString(DefineValue.BANK_ATM_NAME, "");
        noVA = sp.getString(DefineValue.NO_VA,"");
        maxTopupValue = sp.getString(DefineValue.MAX_TOPUP,"");

        lvAtm = (ListView) findViewById(R.id.lvAtm);

        mList = new ArrayList<TopupATMObject>();
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

        mAdapter = new TopupATMAdapter(getApplicationContext(), mList, maxTopupValue);
        lvAtm.setAdapter(mAdapter);

        dialogI = InformationDialog.newInstance(this,1);
    }

    public void InitializeToolbar() {
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

    @Override
    public void onOkButton() {

    }
}

