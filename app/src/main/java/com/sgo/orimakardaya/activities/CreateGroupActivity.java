package com.sgo.orimakardaya.activities;

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
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/9/2015.
 */
public class CreateGroupActivity extends BaseActivity {

    SecurePreferences sp;
    private int RESULT;

    private EditText etGroupName, etDesc;
    private RecipientEditTextView phoneRetv;
    private Button btnSave, btnCancel;
    private DrawableRecipientChip[] chips;

    private List<String> listName;
    private ProgressDialog progdialog;

    private String _ownerID,accessKey;
    private String page = "0";

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_create_group;
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_my_groups));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeToolbar();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

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

    private class TempObjectData{

        private String user_id;

        public TempObjectData(String _user_id){
            this.user_id = _user_id;
        }

    }

    Button.OnClickListener btnCancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    Button.OnClickListener btnSaveListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!etGroupName.getText().toString().equals("") && !etGroupName.getText().toString().equals(" ")) {
                phoneRetv.requestFocus();
                ArrayList<TempObjectData> mTempObjectDataList = new ArrayList<TempObjectData>();

                String finalNumber;

                chips = phoneRetv.getSortedRecipients();

                listName = new ArrayList<String>();

                for (DrawableRecipientChip chip : chips) {
                    Timber.v("DrawableChip:"+chip.getEntry().getDisplayName() + " " + chip.getEntry().getDestination());
                    finalNumber = NoHPFormat.editNoHP(chip.getEntry().getDestination());
                    listName.add(chip.getEntry().getDisplayName());
                    mTempObjectDataList.add(new TempObjectData(finalNumber));
                }

                final GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setPrettyPrinting();
                final Gson gson = gsonBuilder.create();
                String members;
                if(mTempObjectDataList.size() > 0) {
                    members = gson.toJson(mTempObjectDataList);
                }
                else {
                    members = "";
                }

                Timber.d("test json:"+members);
                sentData(members);
            }
            else {
                Toast.makeText(getApplicationContext(), "Input Group Name!", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void sentData(String members) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            String desc = etDesc.getText().toString();
            final String groupName = etGroupName.getText().toString();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_ADD_GROUP,
                    _ownerID,accessKey);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.PAGE, page);
            params.put(WebParams.COUNT, DefineValue.COUNT);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.GROUP_NAME, groupName);
            params.put(WebParams.GROUP_DESC, desc);
            params.put(WebParams.MEMBERS, members);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sent add group:" + params.toString());

            MyApiClient.sentAddGroup(this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String count = response.getString(WebParams.COUNT);

                        if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                            Timber.d("isi params sent add group:"+response.toString());
                            Toast.makeText(getApplicationContext(), "Group " + groupName + " Created!", Toast.LENGTH_LONG).show();
                            finish();
                        }

                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(CreateGroupActivity.this,message);
                        }
                        else {
                            Timber.d("isi error sent add group:"+response.toString());
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(CreateGroupActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(CreateGroupActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    Timber.w("Error Koneksi Add Group:"+throwable.toString());
                }
            });

        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
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
