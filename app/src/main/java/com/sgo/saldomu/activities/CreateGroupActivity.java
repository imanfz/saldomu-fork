package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/9/2015.
 */
public class CreateGroupActivity extends BaseActivity {

    private SecurePreferences sp;
    private int RESULT;

    private EditText etGroupName, etDesc;
    private RecipientEditTextView phoneRetv;
    private Button btnSave, btnCancel;
    private DrawableRecipientChip[] chips;

    private List<String> listName;
    private ProgressDialog progdialog;

    private String _ownerID;
    private String page = "0";

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_create_group;
    }

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_my_groups));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeToolbar();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE, "");

        etGroupName = (EditText) findViewById(R.id.mygroup_name_create);
        etDesc = (EditText) findViewById(R.id.mygroup_desc_create);
        phoneRetv = (RecipientEditTextView) findViewById(R.id.phone_retv);
        btnSave = (Button) findViewById(R.id.btn_save_create_group);
        btnCancel = (Button) findViewById(R.id.btn_cancel_create_group);

        phoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, this);
        phoneRetv.setAdapter(adapter);
        phoneRetv.dismissDropDownOnItemSelected(true);

        btnSave.setOnClickListener(btnSaveListener);
        btnCancel.setOnClickListener(btnCancelListener);

        RESULT = MainPage.RESULT_NORMAL;
    }

    private static class TempObjectData {

        private String user_id;

        public TempObjectData(String _user_id) {
            this.user_id = _user_id;
        }

    }

    private Button.OnClickListener btnCancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private Button.OnClickListener btnSaveListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!etGroupName.getText().toString().equals("") && !etGroupName.getText().toString().equals(" ")) {
                phoneRetv.requestFocus();
                ArrayList<TempObjectData> mTempObjectDataList = new ArrayList<>();

                String finalNumber;

                chips = phoneRetv.getSortedRecipients();

                listName = new ArrayList<>();

                for (DrawableRecipientChip chip : chips) {
                    Timber.v("DrawableChip:" + chip.getEntry().getDisplayName() + " " + chip.getEntry().getDestination());
                    finalNumber = NoHPFormat.formatTo62(chip.getEntry().getDestination());
                    listName.add(chip.getEntry().getDisplayName());
                    mTempObjectDataList.add(new TempObjectData(finalNumber));
                }

                final GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setPrettyPrinting();
                final Gson gson = gsonBuilder.create();
                String members;
                if (mTempObjectDataList.size() > 0) {
                    members = gson.toJson(mTempObjectDataList);
                } else {
                    members = "";
                }

                Timber.d("test json:" + members);
                sentData(members);
            } else {
                Toast.makeText(getApplicationContext(), "Input Group Name!", Toast.LENGTH_LONG).show();
            }
        }
    };

    private void sentData(String members) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            String desc = etDesc.getText().toString();
            final String groupName = etGroupName.getText().toString();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_ADD_GROUP);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.PAGE, page);
            params.put(WebParams.COUNT, DefineValue.COUNT);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.GROUP_NAME, groupName);
            params.put(WebParams.GROUP_DESC, desc);
            params.put(WebParams.MEMBERS, members);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sent add group:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_ADD_GROUP, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                String count = response.getString(WebParams.COUNT);

                                if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                                    Timber.d("isi params sent add group:" + response.toString());
                                    Toast.makeText(getApplicationContext(), "Group " + groupName + " Created!", Toast.LENGTH_LONG).show();
                                    finish();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(CreateGroupActivity.this, message);
                                } else {
                                    Timber.d("isi error sent add group:" + response.toString());
                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
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
}
