package com.sgo.saldomu.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.widgets.BaseActivity;

public class TagihActivity extends BaseActivity {
    Spinner sp_mitra, sp_communtiy;
    SecurePreferences sp;
    EditText et_memberCode;
    Button btn_submit;
    View v;


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_tagih;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp_mitra = findViewById(R.id.sp_mitra);
        sp_communtiy = findViewById(R.id.sp_community);
        et_memberCode = findViewById(R.id.et_memberCode);
        btn_submit = findViewById(R.id.btn_submit);

        InitializeToolbar();

        btn_submit.setOnClickListener(submitListener);
    }

    Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_tagih_agent));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
