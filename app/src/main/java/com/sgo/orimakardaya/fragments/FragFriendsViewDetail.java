package com.sgo.orimakardaya.fragments;

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

import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.FriendsViewDetailActivity;
import com.sgo.orimakardaya.activities.LevelFormRegisterActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.MyPicasso;
import com.sgo.orimakardaya.coreclass.RoundedQuickContactBadge;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogFrag;
import com.sgo.orimakardaya.dialogs.MessageDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by thinkpad on 5/5/2015.
 */
public class FragFriendsViewDetail extends Fragment {

    View v;
    RoundedQuickContactBadge friendsPicContent;
    TextView tvName, tvID, tvPhone, tvEmail;
    Button btnAsk, btnPay;
    int RESULT;
    String imgUrl, name, id, phone, email;
    Boolean isLevel1,isRegisteredLevel;
    String contactCenter,listContactPhone = "",listAddress = "";
    SecurePreferences sp;
    static boolean successUpgrade = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_friends_view_detail, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        successUpgrade = false;
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        int i = sp.getInt(DefineValue.LEVEL_VALUE,0);
        isLevel1 = i == 1;
        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL,false);
        contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER, "");

        try {
            JSONArray arrayContact = new JSONArray(contactCenter);
            for(i=0 ; i<arrayContact.length() ; i++) {
//                String contactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
//                if(i == arrayContact.length()-1) {
//                    listContactPhone += contactPhone;
//                }
//                else {
//                    listContactPhone += contactPhone + " atau ";
//                }

                if(i == 0) {
                    listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
                    listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        friendsPicContent = (RoundedQuickContactBadge) getActivity().findViewById(R.id.friends_pic_content);
        tvName = (TextView) getActivity().findViewById(R.id.friends_value_name);
        tvID = (TextView) getActivity().findViewById(R.id.friends_value_id);
        tvPhone = (TextView) getActivity().findViewById(R.id.friends_value_phone);
        tvEmail = (TextView) getActivity().findViewById(R.id.friends_value_email);
        btnAsk = (Button) getActivity().findViewById(R.id.btn_ask_for_money);
        btnPay = (Button) getActivity().findViewById(R.id.btn_pay);

        Picasso mPic;
        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getImageLoader(getActivity());
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
                    .placeholder(R.anim.progress_animation)
                    .fit()
                    .centerCrop()
                    .into(friendsPicContent);

            tvName.setText(name);
            tvID.setText(id);
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
                    if (isRegisteredLevel)
                        showDialogLevelRegistered();
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
                    if (isRegisteredLevel)
                        showDialogLevelRegistered();
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
        MessageDialog dialognya;
        dialognya = new MessageDialog(getActivity(), getString(R.string.level_dialog_finish_title), getString(R.string.level_dialog_finish_message) + "\n" + listAddress + "\n" +
                getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone);
        dialognya.setDialogButtonClickListener(new MessageDialog.DialogButtonListener() {
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

    @Override
    public void onStart() {
        super.onStart();

        if(successUpgrade) {
            getActivity().setResult(MainPage.RESULT_REFRESH_NAVDRAW);
            getActivity().finish();
        }
    }
}
