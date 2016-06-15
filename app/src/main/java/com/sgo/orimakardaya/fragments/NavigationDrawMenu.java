package com.sgo.orimakardaya.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.BalanceModel;
import com.sgo.orimakardaya.Beans.navdrawmainmenuModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.LevelFormRegisterActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.activities.MyProfileActivity;
import com.sgo.orimakardaya.adapter.NavDrawMainMenuAdapter;
import com.sgo.orimakardaya.coreclass.CurrencyFormat;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.MyPicasso;
import com.sgo.orimakardaya.coreclass.RoundImageTransformation;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogFrag;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.dialogs.ReportBillerDialog;
import com.sgo.orimakardaya.interfaces.OnLoadDataListener;
import com.sgo.orimakardaya.loader.UtilsLoader;
import com.sgo.orimakardaya.services.BalanceService;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

/*
  Created by Administrator on 12/8/2014.
 */
public class NavigationDrawMenu extends ListFragment{

    public static final int MTOPUP = 1;
    public static final int MPAYFRIENDS = 2;
    public static final int MASK4MONEY = 3;

    private ImageView headerCustImage;
    private TextView headerCustName,headerCustID,headerCurrency,balanceValue, currencyLimit, limitValue,periodeLimit;

    private Animation frameAnimation;
    private ImageView btn_refresh_balance;

    ListView mListView;
    private View v;
    public NavDrawMainMenuAdapter mAdapter;
    Boolean isLevel1,isRegisteredLevel,isAllowedLevel;
    Bundle _SaveInstance;
    SecurePreferences sp;
    String contactCenter,listContactPhone = "", listAddress="";
    Activity act;
    ProgressDialog progdialog;
    public String levelValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_navigation_draw_menu_main, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _SaveInstance = savedInstanceState;

        act = getActivity();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        mAdapter = new NavDrawMainMenuAdapter(getActivity(), generateData());
        mListView = (ListView) v.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        LinearLayout llHeaderProfile = (LinearLayout) v.findViewById(R.id.llHeaderProfile);
        headerCustImage = (ImageView) v.findViewById(R.id.header_cust_image);
        headerCurrency = (TextView) v.findViewById(R.id.currency_value);
        headerCustName = (TextView) v.findViewById(R.id.header_cust_name);
        headerCustID = (TextView) v.findViewById(R.id.header_cust_id);
        balanceValue = (TextView) v.findViewById(R.id.balance_value);
        currencyLimit = (TextView) v.findViewById(R.id.currency_limit_value);
        limitValue = (TextView) v.findViewById(R.id.limit_value);
        periodeLimit = (TextView) v.findViewById(R.id.periode_limit_value);

        refreshUINavDrawer();
        refreshDataNavDrawer();

        llHeaderProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MyProfileActivity.class);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
            }
        });


        btn_refresh_balance = (ImageView) v.findViewById(R.id.btn_refresh_balance);
        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        btn_refresh_balance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_refresh_balance.setEnabled(false);
                btn_refresh_balance.startAnimation(frameAnimation);
                getBalance();
            }
        });

        BalanceModel mBal = BalanceModel.load(BalanceModel.class, 1);
        if(mBal != null)
            setBalanceToUI(mBal);

    }

    public void setBalanceToUI(BalanceModel deData){
        headerCurrency.setText(deData.getCcy_id());
        balanceValue.setText(CurrencyFormat.format(deData.getAmount()));
        currencyLimit.setText(deData.getCcy_id());
        limitValue.setText(CurrencyFormat.format(deData.getRemain_limit()));

        if (deData.getPeriod_limit().equals("Monthly"))
            periodeLimit.setText(R.string.header_monthly_limit);
        else
            periodeLimit.setText(R.string.header_daily_limit);
    }

    public void getBalance(){
        new UtilsLoader(getActivity(),sp).getDataBalance(new OnLoadDataListener() {
            @Override
            public void onSuccess(Object deData) {
                setBalanceToUI((BalanceModel) deData);
                btn_refresh_balance.setEnabled(true);
                btn_refresh_balance.clearAnimation();

                Intent i = new Intent(BalanceService.INTENT_ACTION_BALANCE);
                i.putExtra(BalanceModel.BALANCE_PARCELABLE, (BalanceModel) deData);
                LocalBroadcastManager.getInstance(getActivity())
                        .sendBroadcast(i);
            }

            @Override
            public void onFail(String message) {

                btn_refresh_balance.setEnabled(true);
                btn_refresh_balance.clearAnimation();
            }

            @Override
            public void onFailure() {

                btn_refresh_balance.setEnabled(true);
                btn_refresh_balance.clearAnimation();

            }
        });
    }

    private void setImageProfPic(){
        float density = getResources().getDisplayMetrics().density;
        String _url_profpic;

        if(density <= 1) _url_profpic = sp.getString(DefineValue.IMG_SMALL_URL, null);
        else if(density < 2) _url_profpic = sp.getString(DefineValue.IMG_MEDIUM_URL, null);
        else _url_profpic = sp.getString(DefineValue.IMG_LARGE_URL, null);

        Timber.wtf("url prof pic:" + _url_profpic);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

        Picasso mPic;
        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getImageLoader(getActivity());
        else
            mPic= Picasso.with(getActivity());

        if(_url_profpic !=null && _url_profpic.isEmpty()){
            mPic.load(R.drawable.user_unknown_menu)
                    .error(roundedImage)
                    .fit().centerInside()
                    .placeholder(R.anim.progress_animation)
                    .transform(new RoundImageTransformation()).into(headerCustImage);
        }
        else {
            mPic.load(_url_profpic)
                    .error(roundedImage)
                    .fit().centerInside()
                    .placeholder(R.anim.progress_animation)
                    .transform(new RoundImageTransformation()).into(headerCustImage);
        }

    }

    public void initializeNavDrawer(){
       if(!getActivity().isFinishing()) {
           Fragment newFragment = new FragMainPage();
           switchFragment(newFragment, getString(R.string.toolbar_title_home));

           refreshDataNavDrawer();
       }
    }

    public void refreshUINavDrawer(){
        setImageProfPic();
        headerCustName.setText(sp.getString(DefineValue.CUST_NAME, getString(R.string.text_strip)));
        headerCustID.setText(sp.getString(DefineValue.CUST_ID, getString(R.string.text_strip)));
    }
    public void refreshDataNavDrawer(){
        if(sp.contains(DefineValue.LEVEL_VALUE)) {
//            int i = sp.getInt(DefineValue.LEVEL_VALUE, 0);
            levelValue = sp.getString(DefineValue.LEVEL_VALUE, "0");
            if(levelValue == null) {
                levelValue = "0";
            }
            Timber.d("refreshDataNavDrawer");
            isLevel1 = Integer.valueOf(levelValue) == 1;
            isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
            isAllowedLevel = sp.getBoolean(DefineValue.ALLOW_MEMBER_LEVEL, false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        selectItem(position, null);
    }

    private ArrayList<navdrawmainmenuModel> generateData(){
        ArrayList<navdrawmainmenuModel> models = new ArrayList<navdrawmainmenuModel>();
        models.add(new navdrawmainmenuModel(0,0,getString(R.string.menu_group_title_main_menu),true));                                        //0
//        models.add(new navdrawmainmenuModel(R.drawable.ic_home_icon_color,getString(R.string.menu_item_title_home),false));              //
//        models.add(new navdrawmainmenuModel(R.drawable.ic_accounts_icon_color,getString(R.string.menu_item_title_accounts),false));        //

        models.add(new navdrawmainmenuModel(R.drawable.ic_topup_icon_color,0,getString(R.string.menu_item_title_topup),false));              //1
        models.add(new navdrawmainmenuModel(R.drawable.ic_payfriends_icon_color,0,getString(R.string.menu_item_title_pay_friends),false));    //2
        models.add(new navdrawmainmenuModel(R.drawable.ic_ask_icon_color,0,getString(R.string.menu_item_title_ask_for_money),false));            //3
        models.add(new navdrawmainmenuModel(R.drawable.ic_topup_pulsa,0,getString(R.string.menu_item_title_pulsa_agent), false));
        models.add(new navdrawmainmenuModel(R.drawable.ic_buy_icon_color,0,getString(R.string.menu_item_title_buy),false));             //4
//        models.add(new navdrawmainmenuModel(R.drawable.ic_cashout_icon_color,0,getString(R.string.menu_item_title_cash_out),false));       //5


        models.add(new navdrawmainmenuModel(0,0,getString(R.string.menu_group_title_account),true));                                         //7
        models.add(new navdrawmainmenuModel(R.drawable.ic_friends_icon_color,0,getString(R.string.menu_item_title_my_friends),false));        //8
//        models.add(new navdrawmainmenuModel(R.drawable.ic_groups_icon_color,0,getString(R.string.menu_item_title_my_groups),false));          //9

        models.add(new navdrawmainmenuModel(0,0,getString(R.string.menu_group_title_supports),true));                                        //10
        models.add(new navdrawmainmenuModel(R.drawable.ic_report,0,getString(R.string.menu_item_title_report),false));              //6
        models.add(new navdrawmainmenuModel(R.drawable.ic_setting,0,getString(R.string.menu_item_title_setting),false));                    //11
        models.add(new navdrawmainmenuModel(R.drawable.ic_help,0,getString(R.string.menu_item_title_help),false));                          //12
        models.add(new navdrawmainmenuModel(0,0,getString(R.string.menu_group_title_logout),true));                                        //13
        models.add(new navdrawmainmenuModel(R.drawable.ic_logout_icon,0,getString(R.string.menu_item_title_logout),false));                 //14

        return models;
    }

    public void selectItem(int position, Bundle data){
        Fragment newFragment;
        Intent newIntent;
        mAdapter.setSelectedItem(position);
        mAdapter.notifyDataSetChanged();
        switch (position) {
            case MTOPUP:
                newFragment = new ListTopUp();
                switchFragment(newFragment, getString(R.string.toolbar_title_topup));
                break;
            case MPAYFRIENDS:
                if(isAllowedLevel && isLevel1) {
                    if(isRegisteredLevel) {
                        setListContact();
                    }
                    else
                        showDialogLevel();
                }
                else {
                    newFragment = new FragPayFriends();
                    if (data != null && !data.isEmpty()) newFragment.setArguments(data);
                    switchFragment(newFragment, getString(R.string.menu_item_title_pay_friends));
                }
                break;
            case MASK4MONEY:
                if(isAllowedLevel && isLevel1) {
                    if (isRegisteredLevel) {
                        setListContact();
                    }
                    else
                        showDialogLevel();
                }else {
                    newFragment = new FragAskForMoney();
                    if (data != null && !data.isEmpty()) newFragment.setArguments(data);
                    switchFragment(newFragment, getString(R.string.menu_item_title_ask_for_money));
                }
                break;
            case 4:
                newFragment = new FragPulsaAgent();
                switchFragment(newFragment, getString(R.string.toolbar_title_pulsa_agent));
                break;
            case 5:
                newFragment = new ListBuy();
                switchFragment(newFragment, getString(R.string.toolbar_title_purchase));
                break;
//            case 6:
//                if(isAllowedLevel && isLevel1) {
//                    if(isRegisteredLevel)
//                        showDialogLevelRegistered();
//                    else
//                        showDialogLevel();
//                }else {
//                    newFragment = new ListCashOut();
//                    switchFragment(newFragment, getString(R.string.menu_item_title_cash_out));
//                }
//                break;

            case 7:
                newFragment = new ListMyFriends();
                switchFragment(newFragment, getString(R.string.toolbar_title_myfriends));
                break;
//            case 8:
//                newFragment = new FragMyGroup();
//                switchFragment(newFragment, getString(R.string.toolbar_title_mygroup));
//                break;
            case 9:
                newFragment = new ReportTab();
                switchFragment(newFragment, getString(R.string.menu_item_title_report));
                break;
            case 10:
                newFragment = new ListSettings();
                switchFragment(newFragment, getString(R.string.menu_item_title_setting));
                break;
            case 11:
                newFragment = new ContactTab();
                switchFragment(newFragment, getString(R.string.menu_item_title_help));
                break;
            case 13:
                AlertDialog.Builder alertbox=new AlertDialog.Builder(getActivity());
                alertbox.setTitle(getString(R.string.warning));
                alertbox.setMessage(getString(R.string.exit_message));
                alertbox.setPositiveButton(getString(R.string.ok), new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                switchLogout();
                            }
                        });
                alertbox.setNegativeButton(getString(R.string.cancel), new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {}
                        });
                alertbox.show();
                break;
        }
    }

    private void showDialogLevel(){
        final AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getString(R.string.level_dialog_title),
                getString(R.string.level_dialog_message),getString(R.string.level_dialog_btn_ok),getString(R.string.cancel),false);
        dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent mI = new Intent(getActivity(), LevelFormRegisterActivity.class);
                switchActivity(mI, MainPage.ACTIVITY_RESULT);
            }
        });
        dialog_frag.setCancelListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog_frag.dismiss();
            }
        });
        dialog_frag.setTargetFragment(NavigationDrawMenu.this, 0);
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
                }
        );

        dialognya.show();
    }

    private void showMyCustomDialog() {
        ReportBillerDialog dialog = new ReportBillerDialog();
        dialog.show(getActivity().getSupportFragmentManager(), "asfasfaf");
    }

    private void switchFragment(Fragment i, String name){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchContent(i,name);
    }

    private void switchLogout(){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchLogout();
    }


    private void switchActivity(Intent mIntent,int j){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,j);
    }

    public void setPositionNull(){
        mAdapter.setDefault();
        mAdapter.notifyDataSetChanged();
    }

    public void getHelpList() {
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