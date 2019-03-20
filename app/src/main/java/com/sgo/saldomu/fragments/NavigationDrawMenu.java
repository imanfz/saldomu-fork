package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.navdrawmainmenuModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.ActivityListSettings;
import com.sgo.saldomu.activities.ActivityProfileQr;
import com.sgo.saldomu.activities.ActivitySCADM;
import com.sgo.saldomu.activities.AskForMoneyActivity;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsApprovalAgentActivity;
import com.sgo.saldomu.activities.BbsMapViewByAgentActivity;
import com.sgo.saldomu.activities.BbsMapViewByMemberActivity;
import com.sgo.saldomu.activities.BbsMemberShopActivity;
import com.sgo.saldomu.activities.BbsMerchantCommunityList;
import com.sgo.saldomu.activities.BbsNewSearchAgentActivity;
import com.sgo.saldomu.activities.ContactActivity;
import com.sgo.saldomu.activities.InfoHargaWebActivity;
import com.sgo.saldomu.activities.ListBuyActivity;
import com.sgo.saldomu.activities.ListContactActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.MyProfileNewActivity;
import com.sgo.saldomu.activities.PayFriendsActivity;
import com.sgo.saldomu.activities.ReportActivity;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.adapter.NavDrawMainMenuAdapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.ShopCategory;
import com.sgo.saldomu.models.retrofit.UploadPPModel;
import com.sgo.saldomu.services.AgentShopService;
import com.sgo.saldomu.services.BalanceService;
import com.sgo.saldomu.utils.PickAndCameraUtil;
import com.sgo.saldomu.widgets.ProgressRequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/*
  Created by Administrator on 12/8/2014.
 */
public class NavigationDrawMenu extends ListFragment implements ProgressRequestBody.UploadCallbacks {

    public static final String TAG = "com.sgo.saldomu.fragments.NavigationDrawMenu";
    public static final int MHOME = 0;
    public static final int MTOPUP = 1;
    public static final int MPAYFRIENDS = 2;
    public static final int MASK4MONEY = 3;
    public static final int MBUY = 4;
    public static final int MCASHOUT = 5;
    private static final int MMYFRIENDS = 6;
    private static final int MMYGROUP = 7;
    public static final int MREPORT = 8;
    private static final int MSETTINGS = 9;
    private static final int MHELP = 10;
    private static final int MLOGOUT = 11;
    public static final int MDAP = 12;

    private static final int MREGISTERLOCATION = 13;
    private static final int MREGISTERTOKO = 14;
    private static final int MSEARCHAGENT = 15;
    private static final int MKELOLA = 16;
    private static final int MLISTTOKO = 17;
    private static final int MLISTAPPROVAL = 18;

    public static final int MBBS = 19;
    public static final int MCATEGORYBBS = 20;
    private static final int MLISTTRXAGENT = 21;

    private static final int MMAPVIEWBYAGENT = 22;  //temporary
    private static final int MMAPVIEWBYMEMBER = 23; //temporary

    public static final int MBBSCTA = 24;
    public static final int MBBSATC = 25;

    public static final int MTARIKDANA = 26;
    public static final int MSCADM = 27;
    public static final int MINFO = 28;
    public static final int MTAGIH = 29;

    private static final int RC_GPS_REQUEST = 1;

    private ImageView headerCustImage;
    private TextView headerCustName, headerCustID, headerCurrency, balanceValue, currencyLimit, limitValue, periodeLimit, tvAgentDetailName;
    private Switch swSettingOnline;
    private LinearLayout llBalanceDetail, llAgentDetail;

    private Animation frameAnimation;
    private ImageView btn_refresh_balance;
    Gson gson;

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
    private final int RESULT_GALERY = 100;
    private final int RESULT_CAMERA = 200;
    final int RC_CAMERA_STORAGE = 14;
    private PickAndCameraUtil pickAndCameraUtil;
    Boolean isAgent;
    private String isRegisteredLevel; //saat antri untuk diverifikasi
    String categoryIdcta;
    ArrayList<ShopCategory> shopCategories = new ArrayList<>();
    private String isDormant;

    Intent i;

    Boolean isLevel1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filter = new IntentFilter();
        filter.addAction(BalanceService.INTENT_ACTION_BALANCE);
        filter.addAction(AgentShopService.INTENT_ACTION_AGENT_SHOP);
        pickAndCameraUtil = new PickAndCameraUtil(getActivity(), this);

        gson = new Gson();
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
        levelClass = new LevelClass(getActivity(), sp);
        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);
        isDormant = sp.getString(DefineValue.IS_DORMANT, "N");
        isRegisteredLevel = sp.getString(DefineValue.IS_REGISTERED_LEVEL, "0");
        categoryIdcta = sp.getString(DefineValue.CATEGORY_ID_CTA, "");
        mAdapter = new NavDrawMainMenuAdapter(getActivity(), generateData());
        ListView mListView = v.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        LinearLayout llHeaderProfile = v.findViewById(R.id.llHeaderProfile);
        llBalanceDetail = v.findViewById(R.id.llBalanceDetail);
        llAgentDetail = v.findViewById(R.id.llAgentDetail);
        llAgentDetail.setVisibility(View.GONE);
        llBalanceDetail.setVisibility(View.GONE);

        headerCustImage = v.findViewById(R.id.header_cust_image);
        headerCurrency = v.findViewById(R.id.currency_value);
        headerCustName = v.findViewById(R.id.header_cust_name);
        headerCustID = v.findViewById(R.id.header_cust_id);
        balanceValue = v.findViewById(R.id.balance_value);
        currencyLimit = v.findViewById(R.id.currency_limit_value);
        limitValue = v.findViewById(R.id.limit_value);
        periodeLimit = v.findViewById(R.id.periode_limit_value);
        swSettingOnline = v.findViewById(R.id.swSettingOnline);
        tvAgentDetailName = v.findViewById(R.id.tvAgentDetailName);

        if (!sp.getBoolean(DefineValue.IS_AGENT, false)) {
            llAgentDetail.setVisibility(View.GONE);
            llBalanceDetail.setVisibility(View.VISIBLE);
        } else {
            if (sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES)) {
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
                final String[] items = {"Choose from Gallery", "Take a Photo"};

                AlertDialog.Builder a = new AlertDialog.Builder(getActivity());
                a.setCancelable(true);
                a.setTitle("Choose Profile Picture");
                a.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Timber.wtf("masuk gallery");
                                    pickAndCameraUtil.chooseGallery(RESULT_GALERY);
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

                Intent i = new Intent(getActivity(), ActivityProfileQr.class);
                startActivity(i);

//                Intent i = new Intent(getActivity(), MyProfileNewActivity.class);
//                switchActivity(i, MainPage.ACTIVITY_RESULT);
            }
        });

        btn_refresh_balance = v.findViewById(R.id.btn_refresh_balance);
        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);
        btn_refresh_balance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBalance(false);
            }
        });

        setBalanceToUI();
    }

    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    private void chooseCamera() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            pickAndCameraUtil.runCamera(RESULT_CAMERA);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage),
                    RC_CAMERA_STORAGE, perms);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        refreshDataNavDrawer();
    }

    public void setBalanceToUI() {
        headerCurrency.setText(sp.getString(DefineValue.BALANCE_CCYID, ""));
        balanceValue.setText(CurrencyFormat.format(sp.getString(DefineValue.BALANCE_AMOUNT, "")));
        currencyLimit.setText(sp.getString(DefineValue.BALANCE_CCYID, ""));
        limitValue.setText(CurrencyFormat.format(sp.getString(DefineValue.BALANCE_REMAIN_LIMIT, "")));

        if (sp.getString(DefineValue.BALANCE_PERIOD_LIMIT, "").equals("Monthly"))
            periodeLimit.setText(R.string.header_monthly_limit);
        else
            periodeLimit.setText(R.string.header_daily_limit);
    }

    public void setAgentDetailToUI() {
        if (sp.getBoolean(DefineValue.IS_AGENT, false) && sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES)) {
            llAgentDetail.setVisibility(View.VISIBLE);
        } else {
            llAgentDetail.setVisibility(View.GONE);
        }

        if (sp.getBoolean(DefineValue.IS_AGENT, false)) {
            llBalanceDetail.setVisibility(View.GONE);
        } else {
            llBalanceDetail.setVisibility(View.VISIBLE);
        }

        if (sp.getBoolean(DefineValue.IS_AGENT, false)) {
            tvAgentDetailName.setText(sp.getString(DefineValue.AGENT_NAME, ""));

            swSettingOnline.setOnCheckedChangeListener(null);
            if (sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO)) {
                swSettingOnline.setChecked(true);
            } else {
                swSettingOnline.setChecked(false);
            }
            swSettingOnline.setOnCheckedChangeListener(switchListener);
        }
    }

    public void getBalance(Boolean isAuto) {

        btn_refresh_balance.setEnabled(false);
        btn_refresh_balance.startAnimation(frameAnimation);

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(MainPage.HOME_BALANCE_ANIMATE));

        new UtilsLoader(getActivity(), sp).getDataBalance(isAuto, new OnLoadDataListener() {
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

    private void setImageProfPic() {
        float density = getResources().getDisplayMetrics().density;
        String _url_profpic;

//        if(density <= 1) _url_profpic = sp.getString(DefineValue.IMG_SMALL_URL, null);
//        else if(density < 2) _url_profpic = sp.getString(DefineValue.IMG_MEDIUM_URL, null);
//        else _url_profpic = sp.getString(DefineValue.IMG_LARGE_URL, null);
        _url_profpic = sp.getString(DefineValue.IMG_URL, null);

        Timber.wtf("url prof pic:" + _url_profpic);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

//        Picasso mPic;
//        if(MyApiClient.PROD_FLAG_ADDRESS)
//            mPic = MyPicasso.getUnsafeImageLoader(getActivity());
//        else
//            mPic= Picasso.with(getActivity());

        if (_url_profpic != null && _url_profpic.isEmpty()) {
            GlideManager.sharedInstance().initializeGlide(getActivity(), R.drawable.user_unknown_menu, roundedImage, headerCustImage);
        } else {
            GlideManager.sharedInstance().initializeGlide(getActivity(), _url_profpic, roundedImage, headerCustImage);
        }

    }

    public void initializeNavDrawer() {
        if (!getActivity().isFinishing()) {
            Fragment newFragment = new FragMainPage();
            switchFragment(newFragment, getString(R.string.appname).toUpperCase());
            refreshDataNavDrawer();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_GALERY:
                if (resultCode == RESULT_OK) {
                    new ImageCompressionAsyncTask().execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA:
                if (resultCode == RESULT_OK && pickAndCameraUtil.getCaptureImageUri() != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        new ImageCompressionAsyncTask().execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                    } else {
                        new ImageCompressionAsyncTask().execute(pickAndCameraUtil.getCurrentPhotoPath());
                    }
                } else {
                    Toast.makeText(getActivity(), "Try Again", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    public void refreshUINavDrawer() {
        setImageProfPic();
        headerCustName.setText(sp.getString(DefineValue.CUST_NAME, getString(R.string.text_strip)));
        headerCustID.setText(sp.getString(DefineValue.CUST_ID, getString(R.string.text_strip)));

        setAgentDetailToUI();
    }

    public void refreshDataNavDrawer() {
        if (levelClass != null)
            levelClass.refreshData();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mAdapter.setSelectedItem(position);
        mAdapter.notifyDataSetChanged();
        selectItem(mAdapter.getItem(position).getNavItemId(), null);
    }

    private ArrayList<navdrawmainmenuModel> generateData() {
        ArrayList<navdrawmainmenuModel> models = new ArrayList<>();
        models.add(new navdrawmainmenuModel(getString(R.string.menu_group_title_main_menu)));
        if (!levelClass.isLevel1QAC()) {
            models.add(new navdrawmainmenuModel(R.drawable.ic_agent, R.drawable.ic_agent, getString(R.string.menu_item_title_bbs), MBBS));
        }
        models.add(new navdrawmainmenuModel(R.drawable.ic_topup, R.drawable.ic_topup, getString(R.string.menu_item_title_topup), MTOPUP));              //1
        models.add(new navdrawmainmenuModel(R.drawable.ic_transfer_saldo, R.drawable.ic_transfer_saldo, getString(R.string.menu_item_title_pay_friends), MPAYFRIENDS));    //2
        models.add(new navdrawmainmenuModel(R.drawable.ic_minta_saldo, R.drawable.ic_minta_saldo, getString(R.string.menu_item_title_ask_for_money), MASK4MONEY));            //3
        models.add(new navdrawmainmenuModel(R.drawable.ic_belanja, R.drawable.ic_belanja, getString(R.string.menu_item_title_buy), MBUY));//4
        models.add(new navdrawmainmenuModel(R.drawable.ic_report, R.drawable.ic_report, getString(R.string.menu_item_title_scadm), MSCADM));              //6
        models.add(new navdrawmainmenuModel(getString(R.string.menu_group_title_supports)));                                        //10
        models.add(new navdrawmainmenuModel(R.drawable.ic_report, R.drawable.ic_report, getString(R.string.menu_item_title_report), MREPORT));              //6
        models.add(new navdrawmainmenuModel(R.drawable.ic_setting, R.drawable.ic_setting, getString(R.string.menu_item_title_setting), MSETTINGS));                    //11
        models.add(new navdrawmainmenuModel(R.drawable.ic_user, R.drawable.ic_user, getString(R.string.menu_item_title_help1), MHELP));                          //12
        models.add(new navdrawmainmenuModel(R.drawable.ic_belanja, R.drawable.ic_belanja, getString(R.string.menu_item_title_info_harga), MINFO)); //28                         //15
        models.add(new navdrawmainmenuModel(getString(R.string.menu_group_title_logout)));                                        //13
        models.add(new navdrawmainmenuModel(R.drawable.ic_logout_icon, R.drawable.ic_logout_icon, getString(R.string.menu_item_title_logout), MLOGOUT));                 //14

        return models;
    }

    public void selectItem(int itemId, Bundle data) {
        Fragment newFragment;

        switch (itemId) {
            case MTOPUP:
//                newFragment = new ListBankTopUpFragment();
//                switchFragment(newFragment, getString(R.string.toolbar_title_topup));
                i = new Intent(getActivity(), TopUpActivity.class);
                i.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
                break;
            case MPAYFRIENDS:
                if (isDormant.equalsIgnoreCase("Y")) {
                    dialogDormant();
                } else {
                    if (levelClass.isLevel1QAC()) {
                        levelClass.showDialogLevel();
                    } else {
//                    newFragment = new FragPayFriends();
//                    if (data != null && !data.isEmpty()) newFragment.setArguments(data);
//                    switchFragment(newFragment, getString(R.string.menu_item_title_pay_friends));
                        i = new Intent(getActivity(), PayFriendsActivity.class);
                        i.putExtra("data", data);
                        switchActivity(i, MainPage.ACTIVITY_RESULT);
                    }
                }
                break;
            case MASK4MONEY:
                if (isDormant.equalsIgnoreCase("Y")) {
                    dialogDormant();
                } else {
                    if (levelClass.isLevel1QAC()) {
                        levelClass.showDialogLevel();
                    } else {
//                    newFragment = new FragAskForMoney();
//                    if (data != null && !data.isEmpty()) newFragment.setArguments(data);
//                    switchFragment(newFragment, getString(R.string.menu_item_title_ask_for_money));
                        Intent i = new Intent(getActivity(), AskForMoneyActivity.class);
                        startActivity(i);
                        break;
                    }
                }
                break;
            case MDAP:
                if (isDormant.equalsIgnoreCase("Y")) {
                    dialogDormant();
                } else {
                    newFragment = new FragPulsaAgent();
                    if (data != null && !data.isEmpty()) newFragment.setArguments(data);
                    switchFragment(newFragment, getString(R.string.toolbar_title_pulsa_agent));
                }
                break;
            case MBUY:
//                newFragment = new ListBuy();
//                switchFragment(newFragment, getString(R.string.toolbar_title_purchase));
                if (isDormant.equalsIgnoreCase("Y")) {
                    dialogDormant();
                } else {
                    Intent i = new Intent(getActivity(), ListBuyActivity.class);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                }
                break;

            case MMYFRIENDS:
                newFragment = new ListMyFriends();
                switchFragment(newFragment, getString(R.string.toolbar_title_myfriends));
                break;
            case MTARIKDANA:
                if (levelClass.isLevel1QAC()) {
                    levelClass.showDialogLevel();
                } else if (!levelClass.isLevel1QAC() && !isAgent) {
                    i = new Intent(getActivity(), BbsNewSearchAgentActivity.class);
                    i.putExtra(DefineValue.CATEGORY_ID, categoryIdcta);
                    i.putExtra(DefineValue.CATEGORY_NAME, "Setor Tunai");
                    i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                    i.putExtra(DefineValue.AMOUNT, "");
                    i.putExtra(DefineValue.BBS_SCHEME_CODE, "CTA");
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                    break;
                } else if (isAgent) {
                    i = new Intent(getActivity(), BBSActivity.class);
                    i.putExtra(DefineValue.INDEX, BBSActivity.TRANSACTION);
                    i.putExtra(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                }
                break;
            case MSCADM:
                if (isDormant.equalsIgnoreCase("Y")) {
                    dialogDormant();
                } else {
                    i = new Intent(getActivity(), ActivitySCADM.class);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                }
                break;
            case MMYGROUP:
                newFragment = new FragMyGroup();
                switchFragment(newFragment, getString(R.string.toolbar_title_mygroup));
                break;
            case MREPORT:
//                newFragment = new ReportTab();
//                switchFragment(newFragment, getString(R.string.menu_item_title_report));
                i = new Intent(getActivity(), ReportActivity.class);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
                break;
            case MSETTINGS:
//                newFragment = new ListSettings();
//                switchFragment(newFragment, getString(R.string.menu_item_title_setting));
                i = new Intent(getActivity(), ActivityListSettings.class);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
                break;
            case MHELP:
//                newFragment = new ContactTab();
//                switchFragment(newFragment, getString(R.string.menu_item_title_help1));
                i = new Intent(getActivity(), ContactActivity.class);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
                break;
            case MBBS:
                if (isDormant.equalsIgnoreCase("Y")) {
                    dialogDormant();
                } else {
                    if (isAgent) {
                        newFragment = new ListBBS();
                        if (data != null)
                            newFragment.setArguments(data);
                        switchFragment(newFragment, getString(R.string.menu_item_title_bbs));
                    } else showDialogNotAgent();
                }

                break;
            case MLOGOUT:
                AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());
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
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        });
                alertbox.show();
                break;
            case MREGISTERLOCATION:
                i = new Intent(getActivity(), BbsMerchantCommunityList.class);
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
                switchFragment(newFragment, getString(R.string.menu_item_search_agent));
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
            case MINFO:
                startActivity(new Intent(getActivity(), InfoHargaWebActivity.class));
                break;
        }
    }

    private void switchFragment(Fragment i, String name) {
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchContent(i, name);
    }

    private void switchLogout() {
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchLogout();
    }


    private void switchActivity(Intent mIntent, int j) {
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, j);
    }

    public void setPositionNull() {
        mAdapter.setDefault();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
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

            if (action.equals(BalanceService.INTENT_ACTION_BALANCE)) {
                setBalanceToUI();
            } else if (action.equals(AgentShopService.INTENT_ACTION_AGENT_SHOP)) {
                setAgentDetailToUI();
            }
        }
    };

    Switch.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            shopStatus = DefineValue.SHOP_OPEN;
            Boolean isCallWebservice = false;

            if (!isChecked) {
                //buka
                shopStatus = DefineValue.SHOP_CLOSE;
            }

            if (shopStatus.equals(DefineValue.SHOP_OPEN)) {
                if (!sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO)) {
                    isCallWebservice = true;
                }

            } else {
                if (!sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_YES)) {
                    isCallWebservice = true;
                }
            }


            String extraSignature = sp.getString(DefineValue.BBS_MEMBER_ID, "") + sp.getString(DefineValue.BBS_SHOP_ID, "");
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY,
                    extraSignature);

            if (!GlobalSetting.isLocationEnabled(getActivity()) && shopStatus.equals(DefineValue.SHOP_OPEN)) {
                showAlertEnabledGPS();
            } else {
                if (isCallWebservice) {
                    progdialog2 = DefinedDialog.CreateProgressDialog(getContext(), "");

                    params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                    params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                    params.put(WebParams.SHOP_ID, sp.getString(DefineValue.BBS_SHOP_ID, ""));
                    params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.BBS_MEMBER_ID, ""));
                    params.put(WebParams.SHOP_STATUS, shopStatus);
                    params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));

                    RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY, params,
                            new ObjListeners() {
                                @Override
                                public void onResponses(JSONObject response) {
                                    try {

                                        String code = response.getString(WebParams.ERROR_CODE);
                                        if (code.equals(WebParams.SUCCESS_CODE)) {
                                            SecurePreferences.Editor mEditor = sp.edit();
                                            if (shopStatus.equals(DefineValue.SHOP_OPEN)) {
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
                                public void onError(Throwable throwable) {

                                }

                                @Override
                                public void onComplete() {
                                    progdialog2.dismiss();
                                }
                            });
                }

            }

        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private class ImageCompressionAsyncTask extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... params) {
            return pickAndCameraUtil.compressImage(params[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            uploadFileToServer(file);
        }
    }

    @Override
    public void onProgressUpdate(int percentage) {
        Log.d("okhttp", "percentage :" + String.valueOf(percentage));
        if (progdialog2.isShowing())
            progdialog2.setProgress(percentage);
    }

    private void uploadFileToServer(File photoFile) {

        progdialog2 = DefinedDialog.CreateProgressDialog(getContext(), "");

        if (accessKey == null)
            accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        if (userID == null)
            userID = sp.getString(DefineValue.USERID_PHONE, "");

        HashMap<String, RequestBody> params2 = RetrofitService.getInstance()
                .getSignature2(MyApiClient.LINK_UPLOAD_PROFILE_PIC, "");

        RequestBody requestFile =
                new ProgressRequestBody(photoFile, this);
//                RequestBody.create(MediaType.parse("image/*"), photoFile);

        MultipartBody.Part filePart = MultipartBody.Part.createFormData(WebParams.USER_FILE, photoFile.getName(),
                requestFile);
        RequestBody req1 = RequestBody.create(MediaType.parse("text/plain"),
                headerCustID.getText().toString());
        RequestBody req2 = RequestBody.create(MediaType.parse("text/plain"),
                MyApiClient.COMM_ID);

        params2.put(WebParams.USER_ID, req1);
        params2.put(WebParams.COMM_ID, req2);

        RetrofitService.getInstance().MultiPartRequest(MyApiClient.LINK_UPLOAD_PROFILE_PIC, params2, filePart,
                new ObjListener() {
                    @Override
                    public void onResponses(JsonObject object) {

                        UploadPPModel model = gson.fromJson(object, UploadPPModel.class);

                        String error_code = model.getError_code();
                        String error_message = model.getError_message();
//                            Timber.d("response upload profile picture:" + response.toString());
                        if (error_code.equalsIgnoreCase("0000")) {
                            SecurePreferences.Editor mEditor = sp.edit();

                            mEditor.putString(DefineValue.IMG_URL, model.getImg_url());
                            mEditor.putString(DefineValue.IMG_SMALL_URL, model.getImg_small_url());
                            mEditor.putString(DefineValue.IMG_MEDIUM_URL, model.getImg_medium_url());
                            mEditor.putString(DefineValue.IMG_LARGE_URL, model.getImg_large_url());

                            mEditor.apply();

                            setImageProfPic();

                            RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
                        } else if (error_code.equals(WebParams.LOGOUT_CODE)) {
//                                Timber.d("isi response autologout:" + response.toString());
//                                String message = response.getString(WebParams.ERROR_MESSAGE);

                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), error_message);
                        } else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("Upload Image");
                            alert.setMessage("Upload Image : " + error_message);
                            alert.setPositiveButton("OK", null);
                            alert.show();

                        }

                        progdialog2.dismiss();

                    }
                });
    }

    private void showAlertEnabledGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.alertbox_gps_warning))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(ilocation, RC_GPS_REQUEST);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();

                        swSettingOnline.setOnClickListener(null);
                        swSettingOnline.setChecked(false);
                        swSettingOnline.setOnCheckedChangeListener(switchListener);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void showDialogNotAgent() {
        final AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getActivity().getString(R.string.level_dialog_title),
                getActivity().getString(R.string.level_dialog_message_agent), getActivity().getString(R.string.level_dialog_btn_ok),
                getActivity().getString(R.string.cancel), false);
        dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent mI = new Intent(getActivity(), MyProfileNewActivity.class);
                getActivity().startActivityForResult(mI, MainPage.ACTIVITY_RESULT);
            }
        });
        dialog_frag.setCancelListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog_frag.dismiss();
            }
        });

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.add(dialog_frag, null);
        ft.commitAllowingStateLoss();
    }

    private void dialogDormant() {
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getActivity().getString(R.string.title_dialog_dormant),
                getActivity().getString(R.string.message_dialog_dormant_),
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {
                        Intent i = new Intent(getActivity(), TopUpActivity.class);
                        i.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                        switchActivity(i, MainPage.ACTIVITY_RESULT);
                    }
                }
        );

        dialognya.show();
    }
}