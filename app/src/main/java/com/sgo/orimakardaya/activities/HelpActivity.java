package com.sgo.orimakardaya.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.HelpModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.adapter.HelpAdapter;
import com.sgo.orimakardaya.coreclass.BaseActivity;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by thinkpad on 6/9/2015.
 */
public class HelpActivity extends BaseActivity {

    int RESULT;
    SecurePreferences sp;
    String ownerId,accessKey;

    ListView mListView;

    ArrayList<HelpModel> listHelp;
    HelpAdapter mAdapter;
    ProgressDialog progdialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeToolbar();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        ownerId = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        listHelp = new ArrayList<HelpModel>();
        mListView = (ListView) findViewById(R.id.lvHelpCenter);

        getHelpList();

        mAdapter = new HelpAdapter(getApplicationContext(), listHelp);
        mListView.setAdapter(mAdapter);

        RESULT = MainPage.RESULT_NORMAL;
    }

    public void getHelpList() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_USER_CONTACT_INSERT,
                    ownerId,accessKey);
            params.put(WebParams.USER_ID, ownerId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params help list", params.toString());

            MyApiClient.getHelpList(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params help list", response.toString());
                            String count = response.getString(WebParams.COUNT);
                            if(count.equals("0")) {
                                Timber.d("isi help list", "kosong");
                            }
                            else {
                                JSONArray mArrayContact = new JSONArray(response.getString(WebParams.CONTACT_DATA));

                                for (int i = 0; i < mArrayContact.length(); i++) {
                                    HelpModel helpModel = new HelpModel();
                                    helpModel.setId(mArrayContact.getJSONObject(i).getString(WebParams.ID));
                                    helpModel.setName(mArrayContact.getJSONObject(i).getString(WebParams.NAME));
                                    helpModel.setDesc(mArrayContact.getJSONObject(i).getString(WebParams.DESCRIPTION));
                                    helpModel.setPhone(mArrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE));
                                    helpModel.setMail(mArrayContact.getJSONObject(i).getString(WebParams.CONTACT_EMAIL));
                                    listHelp.add(helpModel);
                                }
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout", response.toString());
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(HelpActivity.this,message);
                        }
                        else {
                            Timber.d("isi error help list", response.toString());
                            Toast.makeText(HelpActivity.this, message, Toast.LENGTH_LONG).show();
                        }

                        progdialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    progdialog.dismiss();
                    Log.w("Error Koneksi Help List", throwable.toString());
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient", e.getMessage());
        }
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_help));
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
        return R.layout.activity_help_center;
    }
}
