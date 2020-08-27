package com.sgo.saldomu.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.dialogs.DefinedDialog;


public class ErrorActivity extends AppCompatActivity {

    public final static int GOOGLE_SERVICE_TYPE = 1;
    public final static int DEVICE_ROOTED = 2;
    public final static int SIM_CARD_ABSENT = 3;
    String msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        int type = getIntent().getIntExtra(DefineValue.TYPE,0);

        switch (type){
            case GOOGLE_SERVICE_TYPE:
                msg = getString(R.string.error_msg_googleplayservices, getString(R.string.appname));
                showError(msg);
                break;
            case DEVICE_ROOTED:
                msg = getString(R.string.error_msg_rooteddevice, getString(R.string.appname));
                showError(msg);
                break;
            case SIM_CARD_ABSENT:
                msg = getString(R.string.error_msg_no_simcard);
                showError(msg);
                break;
            default:
                msg = getString(R.string.error_message);
                showError(msg);
        }

    }

    void showError(String message){
//        DefinedDialog.showErrorDialog(this, message, new DefinedDialog.DialogButtonListener() {
//            @Override
//            public void onClickButton(View v, boolean isLongClick) {
//                finish();
//            }
//        });
        DefinedDialog.showErrorDialog(this, message, new DefinedDialog.DialogButtonListener() {
            @Override
            public void onClickButton(View v, boolean isLongClick) {
                finish();
            }
        });
    }

}
