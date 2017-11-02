package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.navdrawmainmenuModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsApprovalAgentActivity;
import com.sgo.saldomu.activities.BbsMapViewByAgentActivity;
import com.sgo.saldomu.activities.BbsMapViewByMemberActivity;
import com.sgo.saldomu.activities.BbsMemberShopActivity;
import com.sgo.saldomu.activities.BbsMerchantCommunityList;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.MyProfileActivity;
import com.sgo.saldomu.activities.MyProfileNewActivity;
import com.sgo.saldomu.adapter.NavDrawMainMenuAdapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GeneralizeImage;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.MyPicasso;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.services.AgentShopService;
import com.sgo.saldomu.services.BalanceService;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/*
  Created by Administrator on 12/8/2014.
 */
public class NavigationDrawMenu extends ListFragment{

    public static final int MHOME = 0;
    public static final int MTOPUP = 1;
    public static final int MPAYFRIENDS= 2;
    public static final int MASK4MONEY= 3;
    public static final int MBUY= 4;
    public static final int MCASHOUT= 5;
    private static final int MMYFRIENDS= 6;
    private static final int MMYGROUP= 7;
    public static final int MREPORT= 8;
    private static final int MSETTINGS= 9;
    private static final int MHELP= 10;
    private static final int MLOGOUT= 11;
    public static final int MDAP= 12;

    private static final int MREGISTERLOCATION = 13;
    private static final int MREGISTERTOKO = 14;
    private static final int MSEARCHAGENT = 15;
    private static final int MKELOLA=16;
    private static final int MLISTTOKO=17;
    private static final int MLISTAPPROVAL=18;

    public static final int MBBS= 19;
    public static final int MCATEGORYBBS=20;
    private static final int MLISTTRXAGENT = 21;

    private static final int MMAPVIEWBYAGENT = 22;  //temporary
    private static final int MMAPVIEWBYMEMBER = 23; //temporary

    public static final int MBBSCTA         = 24;
    public static final int MBBSATC         = 25;

    public static final int MTARIKDANA = 26;

    private ImageView headerCustImage;
    private TextView headerCustName,headerCustID,headerCurrency,balanceValue, currencyLimit, limitValue,periodeLimit, tvAgentDetailName;
    private Switch swSettingOnline;
    private LinearLayout llBalanceDetail, llAgentDetail;

    private Animation frameAnimation;
    private ImageView btn_refresh_balance;

    private View v;
    private NavDrawMainMenuAdapter mAdapter;
    private Bundle _SaveInstance;
    private SecurePreferences sp;
    ProgressDialog progdialog;
    private LevelClass levelClass;
    private IntentFilter filter;
    ProgressDialog progdialog2;
    String shopStatus;
    private String userID;
    private String accessKey;
    private int RESULT;
    private Uri mCapturedImageURI;
    private final int RESULT_GALERY = 100;
    private final int RESULT_CAMERA = 200;
    final int RC_CAMERA_STORAGE = 14;
    private String isRegisteredLevel; //saat antri untuk diverifikasi

    Boolean isLevel1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filter = new IntentFilter();
        filter.addAction(BalanceService.INTENT_ACTION_BALANCE);
        filter.addAction(AgentShopService.INTENT_ACTION_AGENT_SHOP);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_navigation_draw_menu_main, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _SaveInstance = savedInstanceState;

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        levelClass = new LevelClass(getActivity(),sp);
        isRegisteredLevel = sp.getString(DefineValue.IS_REGISTERED_LEVEL,"0");
        mAdapter = new NavDrawMainMenuAdapter(getActivity(), generateData());
        ListView mListView = (ListView) v.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        LinearLayout llHeaderProfile    = (LinearLayout) v.findViewById(R.id.llHeaderProfile);
        llBalanceDetail    = (LinearLayout) v.findViewById(R.id.llBalanceDetail);
        llAgentDetail      = (LinearLayout) v.findViewById(R.id.llAgentDetail);
        llAgentDetail.setVisibility(View.GONE);
        llBalanceDetail.setVisibility(View.GONE);

        headerCustImage = (ImageView) v.findViewById(R.id.header_cust_image);
        headerCurrency = (TextView) v.findViewById(R.id.currency_value);
        headerCustName = (TextView) v.findViewById(R.id.header_cust_name);
        headerCustID = (TextView) v.findViewById(R.id.header_cust_id);
        balanceValue = (TextView) v.findViewById(R.id.balance_value);
        currencyLimit = (TextView) v.findViewById(R.id.currency_limit_value);
        limitValue = (TextView) v.findViewById(R.id.limit_value);
        periodeLimit = (TextView) v.findViewById(R.id.periode_limit_value);
        swSettingOnline = (Switch) v.findViewById(R.id.swSettingOnline);
        tvAgentDetailName = (TextView) v.findViewById(R.id.tvAgentDetailName);

        if ( !sp.getBoolean(DefineValue.IS_AGENT, false) ) {
            llAgentDetail.setVisibility(View.GONE);
            llBalanceDetail.setVisibility(View.VISIBLE);
        } else {
            if ( sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES) ) {
                llAgentDetail.setVisibility(View.VISIBLE);
            } else {
                llAgentDetail.setVisibility(View.GONE);
            }
            llBalanceDetail.setVisibility(View.GONE);
        }

        refreshUINavDrawer();
//        refreshDataNavDrawer();

        headerCustImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = {"Choose from Gallery" , "Take a Photo"};

                AlertDialog.Builder a = new AlertDialog.Builder(getActivity());
                a.setCancelable(true);
                a.setTitle("Choose Profile Picture");
                a.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Timber.wtf("masuk gallery");
                                    chooseGallery();
                                } else if (which == 1) {
                                    chooseCamera();
                                }

                            }
                        }
                );
                a.create();
                a.show();
            }
        });

        llHeaderProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MyProfileNewActivity.class);
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
                getBalance(false);
            }
        });

        setBalanceToUI();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        refreshDataNavDrawer();
    }

    public void setBalanceToUI(){
        headerCurrency.setText(sp.getString(DefineValue.BALANCE_CCYID,""));
        balanceValue.setText(CurrencyFormat.format(sp.getString(DefineValue.BALANCE_AMOUNT,"")));
        currencyLimit.setText(sp.getString(DefineValue.BALANCE_CCYID,""));
        limitValue.setText(CurrencyFormat.format(sp.getString(DefineValue.BALANCE_REMAIN_LIMIT,"")));

        if (sp.getString(DefineValue.BALANCE_PERIOD_LIMIT,"").equals("Monthly"))
            periodeLimit.setText(R.string.header_monthly_limit);
        else
            periodeLimit.setText(R.string.header_daily_limit);
    }

    public void setAgentDetailToUI(){
        if ( sp.getBoolean(DefineValue.IS_AGENT, false) && sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES) ) {
            llAgentDetail.setVisibility(View.VISIBLE);
        } else {
            llAgentDetail.setVisibility(View.GONE);
        }

        if ( sp.getBoolean(DefineValue.IS_AGENT, false) ) {
            llBalanceDetail.setVisibility(View.GONE);
        } else {
            llBalanceDetail.setVisibility(View.VISIBLE);
        }

        if ( sp.getBoolean(DefineValue.IS_AGENT, false) ) {
            tvAgentDetailName.setText(sp.getString(DefineValue.AGENT_NAME, ""));

            swSettingOnline.setOnCheckedChangeListener(null);
            if ( sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO) ) {
                swSettingOnline.setChecked(true);
            } else {
                swSettingOnline.setChecked(false);
            }
            swSettingOnline.setOnCheckedChangeListener(switchListener);
        }
    }

    public void getBalance(Boolean isAuto){
        new UtilsLoader(getActivity(),sp).getDataBalance(isAuto,new OnLoadDataListener() {
            @Override
            public void onSuccess(Object deData) {
                setBalanceToUI();
                btn_refresh_balance.setEnabled(true);
                btn_refresh_balance.clearAnimation();
            }

            @Override
            public void onFail(Bundle message) {
                btn_refresh_balance.setEnabled(true);
                btn_refresh_balance.clearAnimation();
            }

            @Override
            public void onFailure(String message) {
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
            mPic = MyPicasso.getUnsafeImageLoader(getActivity());
        else
            mPic= Picasso.with(getActivity());

        if(_url_profpic !=null && _url_profpic.isEmpty()){
            mPic.load(R.drawable.user_unknown_menu)
                    .error(roundedImage)
                    .fit().centerInside()
                    .placeholder(R.drawable.progress_animation)
                    .transform(new RoundImageTransformation()).into(headerCustImage);
        }
        else {
            mPic.load(_url_profpic)
                    .error(roundedImage)
                    .fit().centerInside()
                    .placeholder(R.drawable.progress_animation)
                    .transform(new RoundImageTransformation()).into(headerCustImage);
        }

    }

    public void initializeNavDrawer(){
       if(!getActivity().isFinishing()) {
           Fragment newFragment = new FragMainPage();
           switchFragment(newFragment, getString(R.string.appname).toUpperCase());
           refreshDataNavDrawer();
       }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case RESULT_GALERY:
                if(resultCode == RESULT_OK){
//                    Picasso.with(getActivity()).load(setmGalleryImage(data)).transform(new RoundImageTransformation()).centerCrop().fit().into(headerCustImage);
                    Bitmap photo = null;
                    Uri _urinya = data.getData();
                    if(data.getData() == null) {
                        photo = (Bitmap)data.getExtras().get("data");
                    } else {
                        try {
                            photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    GeneralizeImage mGI = new GeneralizeImage(getActivity(),photo,_urinya);
                    uploadFileToServer(mGI.Convert());
                }
                break;
            case RESULT_CAMERA:
                if(resultCode == RESULT_OK && mCapturedImageURI!=null){
//                    Picasso.with(getActivity()).load(setmCapturedImage(data)).transform(new RoundImageTransformation()).centerCrop().fit().into(headerCustImage);
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(mCapturedImageURI, projection, null, null, null);
                    String filePath;
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        filePath = cursor.getString(column_index_data);
                    }
                    else
                        filePath = data.getData().getPath();

                    GeneralizeImage mGI = new GeneralizeImage(getActivity(),filePath);
                    uploadFileToServer(mGI.Convert());

                    assert cursor != null;
                    cursor.close();
                }
                else{
                    Toast.makeText(getActivity(), "Try Again", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    public void refreshUINavDrawer(){
        setImageProfPic();
        headerCustName.setText(sp.getString(DefineValue.CUST_NAME, getString(R.string.text_strip)));
        headerCustID.setText(sp.getString(DefineValue.CUST_ID, getString(R.string.text_strip)));

        setAgentDetailToUI();
    }
    public void refreshDataNavDrawer(){
        if(levelClass != null)
            levelClass.refreshData();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mAdapter.setSelectedItem(position);
        mAdapter.notifyDataSetChanged();
        selectItem(mAdapter.getItem(position).getNavItemId(), null);
    }

    private ArrayList<navdrawmainmenuModel> generateData(){
        ArrayList<navdrawmainmenuModel> models = new ArrayList<>();
        models.add(new navdrawmainmenuModel(getString(R.string.menu_group_title_main_menu)));
        models.add(new navdrawmainmenuModel(R.drawable.ic_topup_icon_color, R.drawable.ic_topup_icon_color, getString(R.string.menu_item_title_bbs), MBBS));

        models.add(new navdrawmainmenuModel(R.drawable.ic_topup_icon_color, R.drawable.ic_topup_icon_color, getString(R.string.menu_item_title_topup),MTOPUP));              //1
        models.add(new navdrawmainmenuModel(R.drawable.ic_payfriends_icon_color,R.drawable.ic_payfriends_icon_color,getString(R.string.menu_item_title_pay_friends),MPAYFRIENDS));    //2
        models.add(new navdrawmainmenuModel(R.drawable.ic_ask_icon_color,R.drawable.ic_ask_icon_color,getString(R.string.menu_item_title_ask_for_money),MASK4MONEY));            //3
//        models.add(new navdrawmainmenuModel(R.drawable.ic_topup_pulsa,R.drawable.ic_topup_pulsa,getString(R.string.menu_item_title_pulsa_agent), MDAP));
        models.add(new navdrawmainmenuModel(R.drawable.ic_buy_icon_color,R.drawable.ic_buy_icon_color,getString(R.string.menu_item_title_buy),MBUY));//4
        models.add(new navdrawmainmenuModel(R.drawable.ic_cashout_icon_color,R.drawable.ic_cashout_icon_color,getString(R.string.menu_item_title_cash_out),MTARIKDANA));
//        models.add(new navdrawmainmenuModel(R.drawable.ic_cashout_icon_color,0,getString(R.string.menu_item_title_cash_out),false));       //5


//        models.add(new navdrawmainmenuModel(getString(R.string.menu_group_title_account)));                                         //7
//        models.add(new navdrawmainmenuModel(R.drawable.ic_friends_icon_color,0,getString(R.string.menu_item_title_my_friends),MMYFRIENDS));        //8
//        models.add(new navdrawmainmenuModel(R.drawable.ic_groups_icon_color,0,getString(R.string.menu_item_title_my_groups),false));          //9


        if ( !sp.getBoolean(DefineValue.IS_AGENT, false) ) {
            //models.add(new navdrawmainmenuModel(R.drawable.ic_location_on_dark_blue,R.drawable.ic_location_on_dark_blue,getString(R.string.menu_item_search_agent),MCATEGORYBBS));
            //models.add(new navdrawmainmenuModel(R.drawable.ic_location_on_dark_blue,R.drawable.ic_location_on_dark_blue,getString(R.string.menu_item_title_map_member),MMAPVIEWBYMEMBER));
        } else {

            //models.add(new navdrawmainmenuModel(R.drawable.ic_list_black_36dp, R.drawable.ic_list_black_36dp, getString(R.string.menu_item_title_kelola), MKELOLA));
            //models.add(new navdrawmainmenuModel(R.drawable.ic_list_black_36dp, R.drawable.ic_list_black_36dp, getString(R.string.menu_item_title_list_approval), MLISTAPPROVAL));
            //models.add(new navdrawmainmenuModel(R.drawable.ic_list_black_36dp, R.drawable.ic_list_black_36dp, getString(R.string.menu_item_title_list_toko), MLISTTOKO));
            //models.add(new navdrawmainmenuModel(R.drawable.ic_location_on_dark_blue,R.drawable.ic_location_on_dark_blue,getString(R.string.menu_item_bbs_register_location),MREGISTERLOCATION));

            //models.add(new navdrawmainmenuModel(R.drawable.ic_list_black_36dp,R.drawable.ic_list_black_36dp,getString(R.string.menu_item_title_trx_agent),MLISTTRXAGENT));
            //models.add(new navdrawmainmenuModel(R.drawable.ic_location_on_dark_blue,R.drawable.ic_location_on_dark_blue,getString(R.string.menu_item_title_map_agent),MMAPVIEWBYAGENT));
        }
        //models.add(new navdrawmainmenuModel(R.drawable.map_white,R.drawable.map,getString(R.string.menu_item_bbs_search_toko),MREGISTERTOKO));
        //models.add(new navdrawmainmenuModel(R.drawable.map_white,R.drawable.map,getString(R.string.menu_item_search_agent),MSEARCHAGENT));


        models.add(new navdrawmainmenuModel(getString(R.string.menu_group_title_supports)));                                        //10
        models.add(new navdrawmainmenuModel(R.drawable.ic_report,R.drawable.ic_report,getString(R.string.menu_item_title_report),MREPORT));              //6
        models.add(new navdrawmainmenuModel(R.drawable.ic_setting,R.drawable.ic_setting,getString(R.string.menu_item_title_setting),MSETTINGS));                    //11
        models.add(new navdrawmainmenuModel(R.drawable.ic_help,R.drawable.ic_help,getString(R.string.menu_item_title_help),MHELP));                          //12
        models.add(new navdrawmainmenuModel(getString(R.string.menu_group_title_logout)));                                        //13
        models.add(new navdrawmainmenuModel(R.drawable.ic_logout_icon,R.drawable.ic_logout_icon,getString(R.string.menu_item_title_logout),MLOGOUT));                 //14

        return models;
    }

    public void selectItem(int itemId, Bundle data){
        Fragment newFragment;

        switch (itemId) {
            case MTOPUP:
                newFragment = new ListTopUp();
                switchFragment(newFragment, getString(R.string.toolbar_title_topup));
                break;
            case MPAYFRIENDS:
                if(levelClass.isLevel1QAC()) {
                   levelClass.showDialogLevel();
                }
                else {
                    newFragment = new FragPayFriends();
                    if (data != null && !data.isEmpty()) newFragment.setArguments(data);
                    switchFragment(newFragment, getString(R.string.menu_item_title_pay_friends));
                }
                break;
            case MASK4MONEY:
                if(levelClass.isLevel1QAC()) {
                   levelClass.showDialogLevel();
                }else {
                    newFragment = new FragAskForMoney();
                    if (data != null && !data.isEmpty()) newFragment.setArguments(data);
                    switchFragment(newFragment, getString(R.string.menu_item_title_ask_for_money));
                }
                break;
            case MDAP:
                newFragment = new FragPulsaAgent();
                if (data != null && !data.isEmpty()) newFragment.setArguments(data);
                switchFragment(newFragment, getString(R.string.toolbar_title_pulsa_agent));
                break;
            case MBUY:
                newFragment = new ListBuy();
                switchFragment(newFragment, getString(R.string.toolbar_title_purchase));
                break;

            case MMYFRIENDS:
                newFragment = new ListMyFriends();
                switchFragment(newFragment, getString(R.string.toolbar_title_myfriends));
                break;
            case MTARIKDANA:
                if(levelClass.isLevel1QAC()) {
                    levelClass.showDialogLevel();
                }
                else {
                    newFragment = new ListCashOut();
                    switchFragment(newFragment, getString(R.string.menu_item_title_cash_out));
                }
                break;
            case MMYGROUP:
                newFragment = new FragMyGroup();
                switchFragment(newFragment, getString(R.string.toolbar_title_mygroup));
                break;
            case MREPORT:
                newFragment = new ReportTab();
                switchFragment(newFragment, getString(R.string.menu_item_title_report));
                break;
            case MSETTINGS:
                newFragment = new ListSettings();
                switchFragment(newFragment, getString(R.string.menu_item_title_setting));
                break;
            case MHELP:
                newFragment = new ContactTab();
                switchFragment(newFragment, getString(R.string.menu_item_title_help));
                break;
            case MBBS:
                newFragment = new ListBBS();
                if(data != null)
                    newFragment.setArguments(data);
                switchFragment(newFragment,getString(R.string.menu_item_title_bbs));
                break;
            case MLOGOUT:
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
            case MREGISTERLOCATION:
                Intent i = new Intent(getActivity(), BbsMerchantCommunityList.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                getActivity().finish();
                //startActivity(new Intent(getActivity(), BbsMerchantSetupHourActivity.class));
                break;
            /*case MREGISTERTOKO:
                startActivity(new Intent(getActivity(), BbsSearchTokoActivity.class));
                break;
            case MSEARCHAGENT:
                //startActivity(new Intent(getActivity(), SearchAgentActivity.class));
                startActivity(new Intent(getActivity(), BbsSearchByLocationActivity.class));
                break;*/
            case MKELOLA:
                newFragment = new FragMenuKelola();
                switchFragment(newFragment, getString(R.string.menu_item_title_kelola));
                break;

            case MLISTAPPROVAL:
                Intent intentApproval = new Intent(getActivity(), BbsMemberShopActivity.class);
                intentApproval.putExtra("flagApprove", DefineValue.STRING_NO);
                startActivity(intentApproval);
                break;
            case MLISTTOKO:
                Intent intentListToko = new Intent(getActivity(), BbsMemberShopActivity.class);
                intentListToko.putExtra("flagApprove", DefineValue.STRING_YES);
                startActivity(intentListToko);
                break;
            case MCATEGORYBBS:
                newFragment = new FragListCategoryBbs();
                switchFragment(newFragment,getString(R.string.menu_item_search_agent));
                break;
            case MLISTTRXAGENT:
                Intent intentTrxAgent = new Intent(getActivity(), BbsApprovalAgentActivity.class);
                startActivity(intentTrxAgent);
                break;
            case MMAPVIEWBYAGENT:
                startActivity(new Intent(getActivity(), BbsMapViewByAgentActivity.class));
                break;
            case MMAPVIEWBYMEMBER:
                startActivity(new Intent(getActivity(), BbsMapViewByMemberActivity.class));
                break;
        }
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

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BalanceService.INTENT_ACTION_BALANCE)){
                setBalanceToUI();
            } else if ( action.equals(AgentShopService.INTENT_ACTION_AGENT_SHOP) ) {
                setAgentDetailToUI();
            }
        }
    };

    Switch.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RequestParams params    = new RequestParams();
            shopStatus              = DefineValue.SHOP_OPEN;

            if (!isChecked) {
                //buka
                shopStatus          = DefineValue.SHOP_CLOSE;

            }

            progdialog2 = DefinedDialog.CreateProgressDialog(getContext(), "");

            UUID rcUUID = UUID.randomUUID();
            String dtime = DateTimeFormat.getCurrentDateTime();

            params.put(WebParams.RC_UUID, rcUUID);
            params.put(WebParams.RC_DATETIME, dtime);
            params.put(WebParams.APP_ID, BuildConfig.AppID);
            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params.put(WebParams.SHOP_ID, sp.getString(DefineValue.BBS_SHOP_ID, ""));
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.BBS_MEMBER_ID, ""));
            params.put(WebParams.SHOP_STATUS, shopStatus);


            String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + sp.getString(DefineValue.BBS_MEMBER_ID, "") + sp.getString(DefineValue.BBS_SHOP_ID, "") + BuildConfig.AppID + shopStatus));

            params.put(WebParams.SIGNATURE, signature);

            MyApiClient.updateCloseShopToday(getContext(), params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, org.apache.http.Header[] headers, JSONObject response) {
                    progdialog2.dismiss();

                    try {

                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            SecurePreferences.Editor mEditor = sp.edit();
                            if ( shopStatus.equals(DefineValue.SHOP_OPEN) ) {
                                Toast.makeText(getContext(), getString(R.string.process_update_online_success), Toast.LENGTH_SHORT).show();
                                mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, DefineValue.STRING_NO);
                            } else {
                                Toast.makeText(getContext(), getString(R.string.process_update_offline_success), Toast.LENGTH_SHORT).show();
                                mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, DefineValue.STRING_YES);
                            }

                            mEditor.apply();
                        } else {
                            Toast.makeText(getContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                        }

                        Intent i = new Intent(AgentShopService.INTENT_ACTION_AGENT_SHOP);
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    ifFailure(throwable);
                }

                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    ifFailure(throwable);
                }

                private void ifFailure(Throwable throwable) {
                    Toast.makeText(getContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    progdialog2.dismiss();
                    Timber.w("Error Koneksi login:" + throwable.toString());

                }
            });
        }
    };

    private void chooseGallery() {
        Timber.wtf("masuk gallery");
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_GALERY);
    }

    private void chooseCamera() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(getActivity(),perms)) {
            runCamera();
        }
        else {
            EasyPermissions.requestPermissions(this,getString(R.string.rationale_camera_and_storage),
                    RC_CAMERA_STORAGE,perms);
        }
    }

    private void runCamera(){
        String fileName = "temp.jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);

        mCapturedImageURI = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
        startActivityForResult(takePictureIntent, RESULT_CAMERA);
    }

    private void uploadFileToServer(File photoFile) {

        progdialog2 = DefinedDialog.CreateProgressDialog(getContext(), "");

        RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_UPLOAD_PROFILE_PIC,
                userID,accessKey);

        try {
            params.put(WebParams.USER_ID,headerCustID.getText().toString());
            params.put(WebParams.USER_FILE, photoFile);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Timber.d("params upload profile picture: " + params.toString());

        MyApiClient.sentProfilePicture(getActivity(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    progdialog2.dismiss();
                    String error_code = response.getString("error_code");
                    String error_message = response.getString("error_message");
                    Timber.d("response Listbank:" + response.toString());
                    if (error_code.equalsIgnoreCase("0000")) {
                        SecurePreferences.Editor mEditor = sp.edit();

                        mEditor.putString(DefineValue.IMG_URL, response.getString(WebParams.IMG_URL));
                        mEditor.putString(DefineValue.IMG_SMALL_URL, response.getString(WebParams.IMG_SMALL_URL));
                        mEditor.putString(DefineValue.IMG_MEDIUM_URL, response.getString(WebParams.IMG_MEDIUM_URL));
                        mEditor.putString(DefineValue.IMG_LARGE_URL, response.getString(WebParams.IMG_LARGE_URL));

                        mEditor.apply();

                        setImageProfPic();

                        RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
                    } else if (error_code.equals(WebParams.LOGOUT_CODE)) {
                        Timber.d("isi response autologout:" + response.toString());
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        AlertDialogLogout test = AlertDialogLogout.getInstance();
                        test.showDialoginActivity(getActivity(), message);
                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Upload Image");
                        alert.setMessage("Upload Image : " + error_message);
                        alert.setPositiveButton("OK", null);
                        alert.show();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
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

            private void failure(Throwable throwable) {
                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                setImageProfPic();
                progdialog2.dismiss();
                Timber.w("Error Koneksi data upload foto myprofile:" + throwable.toString());
            }

        });
    }
}