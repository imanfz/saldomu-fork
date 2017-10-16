package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.FriendsViewDetailActivity;
import com.sgo.saldomu.activities.LevelFormRegisterActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.MyPicasso;
import com.sgo.saldomu.coreclass.RoundedQuickContactBadge;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by thinkpad on 5/5/2015.
 */
public class FragFriendsViewDetail extends Fragment {

    private RoundedQuickContactBadge friendsPicContent;
    private TextView tvName;
    private TextView tvPhone;
    private TextView tvEmail;
//    TextView tvID;
private Button btnAsk;
    private Button btnPay;
    private int RESULT;
    private String imgUrl;
    private String name;
    private String id;
    private String phone;
    private String email;
    private Boolean isLevel1;
    private Boolean isRegisteredLevel;
    private String contactCenter;
    private String listContactPhone = "";
    private String listAddress = "";
    private SecurePreferences sp;
    static boolean successUpgrade = false;
    private ProgressDialog progdialog;
    private Activity act;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_friends_view_detail, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        act = getActivity();
        successUpgrade = false;
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        int i = sp.getInt(DefineValue.LEVEL_VALUE,0);
        isLevel1 = i == 1;
        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL,false);
//        contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER, "");
//
//        try {
//            JSONArray arrayContact = new JSONArray(contactCenter);
//            for(i=0 ; i<arrayContact.length() ; i++) {
////                String contactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
////                if(i == arrayContact.length()-1) {
////                    listContactPhone += contactPhone;
////                }
////                else {
////                    listContactPhone += contactPhone + " atau ";
////                }
//
//                if(i == 0) {
//                    listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
//                    listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        friendsPicContent = (RoundedQuickContactBadge) getActivity().findViewById(R.id.friends_pic_content);
        tvName = (TextView) getActivity().findViewById(R.id.friends_value_name);
//        tvID = (TextView) getActivity().findViewById(R.id.friends_value_id);
        tvPhone = (TextView) getActivity().findViewById(R.id.friends_value_phone);
        tvEmail = (TextView) getActivity().findViewById(R.id.friends_value_email);
        btnAsk = (Button) getActivity().findViewById(R.id.btn_ask_for_money);
        btnPay = (Button) getActivity().findViewById(R.id.btn_pay);

        Picasso mPic;
        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getUnsafeImageLoader(getActivity());
        else
            mPic= Picasso.with(getActivity());

        Intent intent = getActivity().getIntent();
        if(intent != null) {
            imgUrl = intent.getStringExtra("image");
            name = intent.getStringExtra("name");
            id = intent.getStringExtra("id");
            phone = intent.getStringExtra("phone");
            email = intent.getStringExtra("email");

            if(imgUrl.equals(""))
                mPic.load(R.drawable.user_unknown_menu)
                    .fit()
                    .centerCrop()
                    .into(friendsPicContent);
            else
                mPic.load(imgUrl)
                    .error(R.drawable.user_unknown_menu)
                    .placeholder(R.drawable.progress_animation)
                    .fit()
                    .centerCrop()
                    .into(friendsPicContent);

            tvName.setText(name);
//            tvID.setText(id);
            tvPhone.setText(phone);
            tvEmail.setText(email);
        }
        else {
            mPic.load(R.drawable.user_unknown_menu)
                .fit()
                .centerCrop()
                .into(friendsPicContent);
        }

        btnAsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLevel1) {
                    if (isRegisteredLevel) {
                        setListContact();
//                        showDialogLevelRegistered();
                    }
                    else
                        showDialogLevel();
                } else {
                    Fragment newFragment = new FragAskForMoney();
                    Bundle args = new Bundle();
                    args.putString("image", imgUrl);
                    args.putString("name", name);
                    args.putString("id", id);
                    args.putString("phone", phone);
                    args.putString("email", email);
                    newFragment.setArguments(args);
                    switchFragment(newFragment, getResources().getString(R.string.menu_item_title_ask_for_money), true);
                }
            }
        });

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLevel1) {
                    if (isRegisteredLevel) {
                        setListContact();
//                        showDialogLevelRegistered();
                    }
                    else
                        showDialogLevel();
                } else {
                    Fragment newFragment = new FragPayFriends();
                    Bundle args = new Bundle();
                    args.putString("image", imgUrl);
                    args.putString("name", name);
                    args.putString("id", id);
                    args.putString("phone", phone);
                    args.putString("email", email);
                    newFragment.setArguments(args);
                    switchFragment(newFragment, getResources().getString(R.string.menu_item_title_pay_friends), true);
                }
            }
        });
        RESULT = MainPage.RESULT_NORMAL;

        setActionBarTitle(name);
    }

    private void showDialogLevel(){
        final AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getString(R.string.level_dialog_title),
                getString(R.string.level_dialog_message),getString(R.string.level_dialog_btn_ok),getString(R.string.cancel), false);
        dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent mI = new Intent(getActivity(), LevelFormRegisterActivity.class);
                startActivity(mI);
            }
        });
        dialog_frag.setCancelListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog_frag.dismiss();
            }
        });
        dialog_frag.setTargetFragment(FragFriendsViewDetail.this, 0);
//        dialog_frag.show(getActivity().getSupportFragmentManager(), AlertDialogFrag.TAG);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(dialog_frag, null);
        ft.commitAllowingStateLoss();
    }

    private void showDialogLevelRegistered(){
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getString(R.string.level_dialog_finish_title),
                getString(R.string.level_dialog_finish_message) + "\n" + listAddress + "\n" +
                        getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone,
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {

                    }
                });

        dialognya.show();
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        FriendsViewDetailActivity fca = (FriendsViewDetailActivity) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    private void setActionBarTitle(String _title){
        if (getActivity() == null)
            return;

        FriendsViewDetailActivity fca = (FriendsViewDetailActivity) getActivity();
        fca.setToolbarTitle(_title);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(successUpgrade) {
            getActivity().setResult(MainPage.RESULT_REFRESH_NAVDRAW);
            getActivity().finish();
        }
    }

    private void getHelpList() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(act, "");
            progdialog.show();
            String ownerId = sp.getString(DefineValue.USERID_PHONE,"");
            String accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_USER_CONTACT_INSERT,
                    ownerId,accessKey);
            params.put(WebParams.USER_ID, ownerId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params help list:" + params.toString());

            MyApiClient.getHelpList(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params help list:"+response.toString());

                            contactCenter = response.getString(WebParams.CONTACT_DATA);

                            SecurePreferences.Editor mEditor = sp.edit();
                            mEditor.putString(DefineValue.LIST_CONTACT_CENTER, response.getString(WebParams.CONTACT_DATA));
                            mEditor.apply();

                            try {
                                JSONArray arrayContact = new JSONArray(contactCenter);
                                for(int i=0 ; i<arrayContact.length() ; i++) {
                                    if(i == 0) {
                                        listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
                                        listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            showDialogLevelRegistered();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(act,message);
                        }
                        else {
                            Timber.d("isi error help list:"+response.toString());
                            Toast.makeText(act, message, Toast.LENGTH_LONG).show();
                        }

                        progdialog.dismiss();

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
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi help list help:"+throwable.toString());
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void setListContact() {
        contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER, "");

        if(contactCenter.equals("")) {
            getHelpList();
        }
        else {
            try {
                JSONArray arrayContact = new JSONArray(contactCenter);
                for (int i = 0; i < arrayContact.length(); i++) {
                    if (i == 0) {
                        listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
                        listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            showDialogLevelRegistered();
        }
    }
}
