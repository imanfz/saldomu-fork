package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.navdrawmainmenuModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsApprovalAgentActivity;
import com.sgo.saldomu.activities.BbsMapViewByAgentActivity;
import com.sgo.saldomu.activities.BbsMapViewByMemberActivity;
import com.sgo.saldomu.activities.BbsMemberShopActivity;
import com.sgo.saldomu.activities.BbsMerchantCommunityList;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.MyProfileActivity;
import com.sgo.saldomu.adapter.NavDrawMainMenuAdapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.MyPicasso;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.services.BalanceService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import timber.log.Timber;

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

    private ImageView headerCustImage;
    private TextView headerCustName,headerCustID,headerCurrency,balanceValue, currencyLimit, limitValue,periodeLimit;

    private Animation frameAnimation;
    private ImageView btn_refresh_balance;

    private View v;
    private NavDrawMainMenuAdapter mAdapter;
    private Bundle _SaveInstance;
    private SecurePreferences sp;
    ProgressDialog progdialog;
    private LevelClass levelClass;
    private IntentFilter filter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filter = new IntentFilter();
        filter.addAction(BalanceService.INTENT_ACTION_BALANCE);
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
        mAdapter = new NavDrawMainMenuAdapter(getActivity(), generateData());
        ListView mListView = (ListView) v.findViewById(android.R.id.list);
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
//        refreshDataNavDrawer();

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

    public void getBalance(Boolean isAuto){
        new UtilsLoader(getActivity(),sp).getDataBalance(isAuto,new OnLoadDataListener() {
            @Override
            public void onSuccess(Object deData) {
                setBalanceToUI();
                btn_refresh_balance.setEnabled(true);
                btn_refresh_balance.clearAnimation();
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
        models.add(new navdrawmainmenuModel(R.drawable.ic_topup_pulsa,R.drawable.ic_topup_pulsa,getString(R.string.menu_item_title_pulsa_agent), MDAP));
        models.add(new navdrawmainmenuModel(R.drawable.ic_buy_icon_color,R.drawable.ic_buy_icon_color,getString(R.string.menu_item_title_buy),MBUY));             //4
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
            }
        }
    };

}