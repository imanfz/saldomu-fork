package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.Beans.PromoObject;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.AskForMoneyActivity;
import com.sgo.saldomu.activities.B2BActivity;
import com.sgo.saldomu.activities.B2BCanvasserActivity;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsNewSearchAgentActivity;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.activities.CashCollectionActivity;
import com.sgo.saldomu.activities.GridBillerActivity;
import com.sgo.saldomu.activities.GridLendingActivity;
import com.sgo.saldomu.activities.HistoryActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.MandiriLPActivity;
import com.sgo.saldomu.activities.ReportEBDActivity;
import com.sgo.saldomu.activities.ReportEBDListActivity;
import com.sgo.saldomu.activities.SearchMemberToVerifyActivity;
import com.sgo.saldomu.activities.TagihActivity;
import com.sgo.saldomu.activities.TokoEBDActivity;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.activities.TransferActivity;
import com.sgo.saldomu.adapter.AdapterHome;
import com.sgo.saldomu.adapter.GridMenu;
import com.sgo.saldomu.coreclass.BaseFragmentMainPage;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.EditMenuModel;
import com.sgo.saldomu.models.ShopCategory;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.CategoriesModel;
import com.sgo.saldomu.models.retrofit.CategoryListModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.services.AgentShopService;
import com.sgo.saldomu.services.BalanceService;
import com.sgo.saldomu.utils.UserUtils;
import com.synnapps.carouselview.CarouselView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import in.srain.cube.views.ptr.PtrFrameLayout;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/10/2017.
 */
public class FragHomeNew extends BaseFragmentMainPage {
    GridView gridView;
    RecyclerView recyclerView;
    TextView tv_balance;
    TextView tv_saldo;
    View v;
    ImageView refreshBtn;
    private Animation frameAnimation;
    private SecurePreferences sp;
    ArrayList<ShopCategory> shopCategories = new ArrayList<>();
    private Biller_Type_Data_Model mBillerTypeDataPLS;
    private Biller_Type_Data_Model mBillerTypeDataBPJS;
    private Biller_Type_Data_Model mBillerTypeDataTKN;
    private Biller_Type_Data_Model mBillerTypeDataEMoney;
    private Biller_Type_Data_Model mBillerTypeDataGame;
    private Biller_Type_Data_Model mBillerTypeDataVoucher;
    private Biller_Type_Data_Model mBillerTypeDataPDAM;
    private Biller_Type_Data_Model mBillerTypeDataDATA;
    private EditMenuModel editMenuModel;
    private Realm realm;
    private SwitchCompat swSettingOnline;
    private TextView tvSettingOnline;
    String shopStatus, isDormant, agentSchemeCode, memberSchemeCode, agentBillerCode, agentEBDCode, agentTrxCode;
    Boolean isAgent, isShowB2b = false, isAvailBiller = false, isB2BEratelToko = false, isB2BEratelCanvasser = false;
    ProgressBar gridview_progbar;
    ProgressBar progBanner;
    private CarouselView carouselView;
    private final ArrayList<PromoObject> listPromo = new ArrayList<>();

    private final ArrayList<String> menuStringsMain = new ArrayList<>();
    private final ArrayList<Drawable> menuDrawablesMain = new ArrayList<>();
    private final ArrayList<String> menuStrings = new ArrayList<>();
    private final ArrayList<Drawable> menuDrawables = new ArrayList<>();

    GridMenu gridMenuAdapter;
    AdapterHome adapterHomeMain;

    private JSONArray agentTrxCodeArray;
    private static final int RC_GPS_REQUEST = 1;
    private static final String BILLER_TYPE_CODE_PLS = "PLS";
    private static final String BILLER_TYPE_CODE_HP = "HP";
    private static final String BILLER_TYPE_CODE_TKN = "TKN";
    private static final String BILLER_TYPE_CODE_PLN = "PLN";
    private static final String BILLER_TYPE_CODE_BPJS = "BPJS";
    public static final String BILLER_TYPE_CODE_EMONEY = "EMON";
    private static final String BILLER_TYPE_CODE_GAME = "GAME";
    private static final String BILLER_TYPE_CODE_VOUCHER = "VCHR";
    private static final String BILLER_TYPE_CODE_PDAM = "AIR";
    private static final String BILLER_TYPE_CODE_DATA = "DATA";
    private boolean allowedTransfer;

    public FragHomeNew() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        memberSchemeCode = sp.getString(DefineValue.CATEGORY, "");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filter = new IntentFilter();
        filter.addAction(BalanceService.INTENT_ACTION_BALANCE);
        filter.addAction(AgentShopService.INTENT_ACTION_AGENT_SHOP);

//        getBalance(true);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag_home_new, container, false);

        gridView = v.findViewById(R.id.grid);
        recyclerView = v.findViewById(R.id.recycler_view);
        tv_balance = v.findViewById(R.id.tv_balance);
        tv_saldo = v.findViewById(R.id.tv_saldo);
        swSettingOnline = v.findViewById(R.id.switch_set_agent_online);
        tvSettingOnline = v.findViewById(R.id.tv_set_agent_online);
        gridview_progbar = v.findViewById(R.id.gridview_progbar);
        refreshBtn = v.findViewById(R.id.btn_refresh_balance);
        progBanner = v.findViewById(R.id.progressBarBanner);
        carouselView = v.findViewById(R.id.carouselView1);

        return v;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (UserUtils.isLogin()) {
            isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

            isDormant = sp.getString(DefineValue.IS_DORMANT, DefineValue.STRING_NO);

            agentSchemeCode = sp.getString(DefineValue.AGENT_SCHEME_CODES, "");
            memberSchemeCode = sp.getString(DefineValue.CATEGORY, "");
            agentBillerCode = sp.getString(DefineValue.AGENT_BILLER_CODES, "");
            agentEBDCode = sp.getString(DefineValue.AGENT_EBD_CODES, "");
            agentTrxCode = sp.getString(DefineValue.AGENT_TRX_CODES, "");
            try {
                agentTrxCodeArray = new JSONArray(agentTrxCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            getRealmBillerData();
            getRealmCustomMenuData();
            setMenuAdapter();

            if (!sp.getBoolean(DefineValue.IS_AGENT, false)) {
                goneStatusOnline();
            } else {
                if (sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES)) {
                    showStatusOnline();
                } else {
                    goneStatusOnline();
                }
            }

            if (isAgent) {
                setupTitleMenu();
            } else {
                if (memberSchemeCode.equals("") || memberSchemeCode == null) {
                    getCategoryList();
                } else {
                    setupTitleMenu();
                }
            }

            gridView.setOnItemClickListener((parent, view1, position, id) -> {
                Timber.d("masuk gridhomeonitemclicklistener");

                String menuItemName = ((TextView) view1.findViewById(R.id.grid_text)).getText().toString();
                Timber.d("menuItemName : %s", menuItemName);
                onClickMenuItem(menuItemName);
            });

            if (sp.getBoolean(DefineValue.IS_AGENT, false)) {

                swSettingOnline.setOnCheckedChangeListener(null);
                swSettingOnline.setChecked(sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO));
                swSettingOnline.setOnCheckedChangeListener(switchListener);
            }

            RefreshSaldo();
            if (getLvlClass() != null)
                getLvlClass().refreshData();

            refreshBtn.setOnClickListener(v -> getBalance(true));

            if (!sp.getBoolean(DefineValue.SAME_BANNER, false))
                getPromoList();
            else
                populateBanner();
            if (isAgent)
                tv_balance.setText(getString(R.string.agent_balance));

            if (isAgent && agentTrxCodeArray.length() > 0)
                for (int i = 0; i < agentTrxCodeArray.length(); i++)
                    try {
                        if (agentTrxCodeArray.get(i).equals(DefineValue.P2P))
                            allowedTransfer = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            else allowedTransfer = !isAgent && sp.getInt(DefineValue.LEVEL_VALUE, 1) == 2;

            sp.edit().putBoolean(DefineValue.ALLOW_TRANSFER, allowedTransfer).commit();
        }
    }

    private void getCategoryList() {
        showProgressDialog();
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CATEGORY_LIST);
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.SHOP_ID, "");
        params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
        Timber.d("isi params shop category:%s", params.toString());

        showView(gridview_progbar);

        RetrofitService.getInstance().PostObjectRequestDebounce(MyApiClient.LINK_CATEGORY_LIST, params,
                new ResponseListener() {
                    @Override
                    public void onResponses(JsonObject object) {
                        CategoryListModel model = getGson().fromJson(object, CategoryListModel.class);

                        String code = model.getError_code();
                        String message = model.getError_message();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            SecurePreferences.Editor mEditor = sp.edit();
                            for (int i = 0; i < model.getCategories().size(); i++) {

                                CategoriesModel obj = model.getCategories().get(i);

                                String arrJson = toJson(model.getCategories()).toString();
                                mEditor.putString(DefineValue.CATEGORY, arrJson);
                                memberSchemeCode = arrJson;

                                ShopCategory shopCategory = new ShopCategory();
                                shopCategory.setCategoryId(obj.getCategory_id());
                                if (obj.getCategory_name().contains("Setor"))
                                    mEditor.putString(DefineValue.CATEGORY_ID_CTA, shopCategory.getCategoryId());
                                if (obj.getCategory_name().contains("Upgrade"))
                                    mEditor.putString(DefineValue.CATEGORY_ID_UPG, shopCategory.getCategoryId());
                                shopCategory.setSchemeCode(obj.getScheme_code());
                                String tempCategory = obj.getCategory_name().toLowerCase();
                                String[] strArray = tempCategory.split(" ");
                                StringBuilder builder = new StringBuilder();
                                for (String s : strArray) {
                                    String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                    builder.append(cap).append(" ");
                                }

                                shopCategory.setCategoryName(builder.toString());
                                shopCategories.add(shopCategory);

                            }
                            mEditor.apply();
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            AlertDialogLogout.getInstance().showDialoginMain(getActivity(), message);
                        } else if (code.equals(DefineValue.ERROR_9333)) {
                            Timber.d("isi response app data:%s", model.getApp_data());
                            final AppDataModel appModel = model.getApp_data();
                            AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                        } else if (code.equals(DefineValue.ERROR_0066)) {
                            Timber.d("isi response shop category: %s", model.toString());
                            AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                        }
                        setupTitleMenu();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        getCategoryList();
                    }

                    @Override
                    public void onComplete() {
                        dismissProgressDialog();
                        hideView(gridview_progbar);
                        Timber.d("hide view");
                    }
                });
    }

    private void showStatusOnline() {
        swSettingOnline.setVisibility(View.VISIBLE);
        tvSettingOnline.setVisibility(View.VISIBLE);
    }

    private void goneStatusOnline() {
        swSettingOnline.setVisibility(View.GONE);
        tvSettingOnline.setVisibility(View.GONE);
    }

    private void getRealmBillerData() {
        realm = RealmManager.getRealmBiller();
        mBillerTypeDataPLS = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "PLS")
                .findFirst();

        mBillerTypeDataBPJS = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "BPJS")
                .findFirst();

        mBillerTypeDataTKN = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "TKN")
                .findFirst();

        mBillerTypeDataEMoney = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "EMON")
                .findFirst();

        mBillerTypeDataGame = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "GAME")
                .findFirst();

        mBillerTypeDataVoucher = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "VCHR")
                .findFirst();

        mBillerTypeDataPDAM = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "AIR")
                .findFirst();

        mBillerTypeDataDATA = realm.where(Biller_Type_Data_Model.class)
                .equalTo(WebParams.BILLER_TYPE_CODE, "DATA")
                .findFirst();

    }

    private void getRealmCustomMenuData() {
        setRealmToDefault();
        editMenuModel = realm.where(EditMenuModel.class).findFirst();
    }

    public void getBalance(Boolean isAuto) {
        animateRefreshBtn(true);

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(MainPage.HOME_BALANCE_ANIMATE));

        new UtilsLoader(getActivity(), sp).getDataBalance(isAuto, new OnLoadDataListener() {
            @Override
            public void onSuccess(Object deData) {
//                setBalanceToUI();
                RefreshSaldo();
                refreshBtn.setEnabled(true);
                refreshBtn.clearAnimation();
            }

            @Override
            public void onFail(Bundle message) {
                refreshBtn.setEnabled(true);
                refreshBtn.clearAnimation();
            }

            @Override
            public void onFailure(String message) {
                refreshBtn.setEnabled(true);
                refreshBtn.clearAnimation();
            }
        });
    }

    private void getPromoList() {
        try {

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_PROMO_LIST);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.PAGE, Integer.toString(0));
            params.put(WebParams.COUNT, "5");
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.ACCESS_KEY, sp.getString(DefineValue.ACCESS_KEY, ""));

            Timber.d("isi params get promo list:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequestDebounce(MyApiClient.LINK_PROMO_LIST, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = getGson().fromJson(object, jsonModel.class);
                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                String count = object.get(WebParams.COUNT).toString();
                                if (!count.equals("0")) {
                                    try {
                                        String promoData = object.get(WebParams.PROMO_DATA).toString();
                                        JSONArray mArrayPromo = new JSONArray(promoData);

                                        for (int i = 0; i < mArrayPromo.length(); i++) {
                                            String id = mArrayPromo.getJSONObject(i).getString(WebParams.ID);
                                            boolean flagSame = false;

                                            // cek apakah ada id yang sama.. kalau ada tidak dimasukan ke array
                                            if (mArrayPromo.length() > 0) {
                                                for (int index = 0; index < mArrayPromo.length(); index++) {
                                                    if (mArrayPromo.getJSONObject(i).getString(WebParams.ID).equals(id)) {
                                                        flagSame = true;
                                                        sp.edit().putBoolean(DefineValue.SAME_BANNER, true).commit();
                                                        sp.edit().putString(DefineValue.DATA_BANNER, promoData).commit();
                                                        break;
                                                    } else {
                                                        flagSame = false;
                                                        sp.edit().putBoolean(DefineValue.SAME_BANNER, false).commit();
                                                    }
                                                }
                                            }

                                            if (!flagSame) {
                                                String name = mArrayPromo.getJSONObject(i).getString(WebParams.NAME);
                                                String description = mArrayPromo.getJSONObject(i).getString(WebParams.DESCRIPTION);
                                                String banner_pic = mArrayPromo.getJSONObject(i).getString(WebParams.BANNER_PIC);
                                                String target_url = mArrayPromo.getJSONObject(i).getString(WebParams.TARGET_URL);
                                                String type = mArrayPromo.getJSONObject(i).getString(WebParams.TYPE);

                                                PromoObject promoObject = new PromoObject();
                                                promoObject.setId(id);
                                                promoObject.setName(name);
                                                promoObject.setDesc(description);
                                                promoObject.setImage(banner_pic);
                                                promoObject.setUrl(target_url);
                                                promoObject.setType(type);

                                                listPromo.add(promoObject);
                                            }
                                        }
                                        populateBanner();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout.getInstance().showDialoginMain(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            getPromoList();
                        }

                        @Override
                        public void onComplete() {
                            getBalance(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void populateBanner() {

        if (sp.getBoolean(DefineValue.SAME_BANNER, false)) {
            try {
                JSONArray mArrayPromo_ = new JSONArray(sp.getString(DefineValue.DATA_BANNER, ""));
                for (int i = 0; i < mArrayPromo_.length(); i++) {
                    try {
                        String id = mArrayPromo_.getJSONObject(i).getString(WebParams.ID);
                        String name = mArrayPromo_.getJSONObject(i).getString(WebParams.NAME);
                        String description = mArrayPromo_.getJSONObject(i).getString(WebParams.DESCRIPTION);
                        String banner_pic = mArrayPromo_.getJSONObject(i).getString(WebParams.BANNER_PIC);
                        String target_url = mArrayPromo_.getJSONObject(i).getString(WebParams.TARGET_URL);
                        String type = mArrayPromo_.getJSONObject(i).getString(WebParams.TYPE);

                        PromoObject promoObject = new PromoObject();
                        promoObject.setId(id);
                        promoObject.setName(name);
                        promoObject.setDesc(description);
                        promoObject.setImage(banner_pic);
                        promoObject.setUrl(target_url);
                        promoObject.setType(type);

                        listPromo.add(promoObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        carouselView.setImageListener((position, imageView) -> Glide.with(getActivity())
                .load(listPromo.get(position).getImage())
                .into(imageView));
        carouselView.setPageCount(listPromo.size());
        carouselView.setImageClickListener(position -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(listPromo.get(position).getUrl()));
            startActivity(browserIntent);
        });
        carouselView.setVisibility(View.VISIBLE);
        progBanner.setVisibility(View.GONE);
    }

    void animateRefreshBtn(boolean isLoad) {
        if (isLoad) {
            refreshBtn.setEnabled(false);
            refreshBtn.startAnimation(frameAnimation);
        } else {
            refreshBtn.setEnabled(true);
            refreshBtn.clearAnimation();
        }
    }

    private void setupTitleMenu() {
        if (getActivity() != null && isAdded()) {
            menuStringsMain.clear();
            menuStringsMain.add(getResources().getString(R.string.toolbar_title_topup));
            menuStringsMain.add(getResources().getString(R.string.menu_item_title_send));
            menuStringsMain.add(getResources().getString(R.string.menu_item_title_ask_for_money));
            menuStringsMain.add(getResources().getString(R.string.menu_item_title_biller));
            if (editMenuModel == null) {
                if (isAgent) {
                    if (((sp.getString(DefineValue.IS_AGENT_TRX_CTA_MANDIRI_LP, DefineValue.STRING_NO).equalsIgnoreCase(DefineValue.STRING_YES))
                            || sp.getString(DefineValue.IS_AGENT_TRX_ATC_MANDIRI_LP, DefineValue.STRING_NO).equalsIgnoreCase(DefineValue.STRING_YES)) &&
                            sp.getString(DefineValue.COMPANY_TYPE, "").equals(getString(R.string.LP)))
                        menuStrings.add(getResources().getString(R.string.menu_item_title_mandiri_lkd));

                    checkSchemeCodeAgent();

                    if (sp.getString(DefineValue.IS_AGENT_TRX_REQ, "").equalsIgnoreCase(DefineValue.STRING_YES))
                        menuStrings.add(getResources().getString(R.string.menu_item_title_trx_agent));

                    menuStrings.add(getResources().getString(R.string.title_bbs_list_account_bbs));

                    menuStrings.add(getResources().getString(R.string.menu_item_title_onprogress_agent));


                } else {
                    checkSchemeCodeMember();

                    menuStrings.add(getResources().getString(R.string.title_cash_out_member));

                    if (mBillerTypeDataPLS != null)
                        menuStrings.add(getResources().getString(R.string.menu_item_title_pulsa_agent));

                    if (mBillerTypeDataDATA != null)
                        menuStrings.add(getResources().getString(R.string.newhome_data));

                    if (mBillerTypeDataBPJS != null)
                        menuStrings.add(getResources().getString(R.string.newhome_bpjs));

                    if (mBillerTypeDataTKN != null)
                        menuStrings.add(getResources().getString(R.string.newhome_listrik_pln));

                    if (mBillerTypeDataEMoney != null)
                        menuStrings.add(getResources().getString(R.string.newhome_emoney));

                    if (mBillerTypeDataGame != null)
                        menuStrings.add(getResources().getString(R.string.newhome_game));

                    if (mBillerTypeDataVoucher != null)
                        menuStrings.add(getResources().getString(R.string.newhome_voucher));

                    if (mBillerTypeDataPDAM != null)
                        menuStrings.add(getResources().getString(R.string.newhome_pam));

                    menuStrings.add(getResources().getString(R.string.menu_item_title_b2b_eratel) + " " + getResources().getString(R.string.menu_item_title_ebd_toko));
                    isB2BEratelToko = true;

                    menuStrings.add(getResources().getString(R.string.menu_item_title_report_ebd));
                }

                menuStrings.add(getResources().getString(R.string.menu_item_title_mutation));

//                menuStrings.add(getResources().getString(R.string.menu_item_lending));
            } else
                getTitleMenu();

            menuStrings.add(getResources().getString(R.string.more));
            setupIconMenu();
        }
    }

    void checkBillerCodeAgent() {
        try {
            JSONArray arr = new JSONArray(agentBillerCode);
            for (int i = 0; i < arr.length(); i++) {
                String obj = arr.getString(i);
                switch (obj) {
                    case BILLER_TYPE_CODE_PLS:
                    case BILLER_TYPE_CODE_HP:
                        if (!menuStrings.contains(getResources().getString(R.string.menu_item_title_pulsa_agent)))
                            menuStrings.add(getResources().getString(R.string.menu_item_title_pulsa_agent));
                        break;
                    case BILLER_TYPE_CODE_TKN:
                    case BILLER_TYPE_CODE_PLN:
                        if (!menuStrings.contains(getResources().getString(R.string.newhome_listrik_pln))) {
                            menuStrings.add(getResources().getString(R.string.newhome_listrik_pln));
                        }
                        break;
                    case BILLER_TYPE_CODE_DATA:
                        menuStrings.add(getResources().getString(R.string.newhome_data));
                        break;
                    case BILLER_TYPE_CODE_BPJS:
                        menuStrings.add(getResources().getString(R.string.newhome_bpjs));
                        break;
                    case BILLER_TYPE_CODE_EMONEY:
                        menuStrings.add(getResources().getString(R.string.newhome_emoney));
                        break;
                    case BILLER_TYPE_CODE_GAME:
                        menuStrings.add(getResources().getString(R.string.newhome_game));
                        break;
                    case BILLER_TYPE_CODE_VOUCHER:
                        menuStrings.add(getResources().getString(R.string.newhome_voucher));
                        break;
                    case BILLER_TYPE_CODE_PDAM:
                        menuStrings.add(getResources().getString(R.string.newhome_pam));
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void checkEBDCodeAgent() {
        try {
            JSONArray arr = new JSONArray(agentEBDCode);
            String ebd = getResources().getString(R.string.menu_item_title_b2b_eratel);
            String code = "";
            for (int i = 0; i < arr.length(); i++) {
                String obj = arr.getString(i);
                switch (obj) {
                    case DefineValue.TOKO:
                        code = getResources().getString(R.string.menu_item_title_ebd_toko);
                        isB2BEratelToko = true;
                        break;
                    case DefineValue.CANVASSER:
                        code = getResources().getString(R.string.menu_item_title_ebd_canvasser);
                        isB2BEratelCanvasser = true;
                        break;
                }
            }
            menuStrings.add(ebd + " " + code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void checkSchemeCodeAgent() {
        try {
            JSONArray arr = new JSONArray(agentSchemeCode);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String objs = obj.optString(WebParams.SCHEME_CODE, "");

                switch (objs) {
                    case "ATC":
                        menuStrings.add(getResources().getString(R.string.cash_out));
                        break;
                    case "CTA":
                        menuStrings.add(getResources().getString(R.string.cash_in));
                        break;
                    case "DGI":
                        sp.edit().putBoolean(DefineValue.IS_AGENT_DGI, true).commit();
                        menuStrings.add(getResources().getString(R.string.menu_item_title_tagih_agent));
//                        if (sp.getString(DefineValue.AGENT_TYPE, "").equalsIgnoreCase(getString(R.string.agent_type_col))) {
                        if (sp.getString(DefineValue.USE_DEPOSIT_COL, "").equalsIgnoreCase("LIMIT"))
                            menuStrings.add(getResources().getString(R.string.menu_item_title_collector_history));
                        break;
                    case "UPG":
                        menuStrings.add(getResources().getString(R.string.menu_item_title_upgrade_member));
                        break;
                    case "CTR":
                        sp.edit().putBoolean(DefineValue.IS_AGENT_CTR, true).commit();
                        menuStrings.add(getResources().getString(R.string.menu_title_cash_collection));
                        if (sp.getString(DefineValue.USE_DEPOSIT_CCOL, "").equalsIgnoreCase("LIMIT"))
                            menuStrings.add(getResources().getString(R.string.menu_item_title_cash_collector_history));
                        break;
                    case "BIL":
                        checkBillerCodeAgent();
                        isAvailBiller = true;
//                        menuStrings.add(getResources().getString(R.string.menu_item_title_biller));
                        break;
                    case "TOP":
                        sp.edit().putBoolean(DefineValue.IS_AGENT_TOP, true).commit();
                        if (!isShowB2b) {
                            menuStrings.add(getResources().getString(R.string.menu_item_title_scadm));
                            isShowB2b = true;
                        }
                        break;
                    case "BDK":
                        sp.edit().putBoolean(DefineValue.IS_AGENT_BDK, true).commit();
                        if (!isShowB2b) {
                            menuStrings.add(getResources().getString(R.string.menu_item_title_scadm));
                            isShowB2b = true;
                        }
                        break;
                    case "EBD":
                        checkEBDCodeAgent();
                        menuStrings.add(getResources().getString(R.string.menu_item_title_report_ebd));
                        break;
                    case "TFD":
                        menuStrings.add(getResources().getString(R.string.transfer_funds));
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void checkSchemeCodeMember() {
        try {
            JSONArray arr = new JSONArray(memberSchemeCode);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String objs = obj.optString(WebParams.SCHEME_CODE, "");
                switch (objs) {
                    case "ATC":
                        menuStrings.add(getString(R.string.menu_item_search_agent_bbs) + " " + getString(R.string.cash_in));
                        break;
                    case "CTA":
                        menuStrings.add(getString(R.string.menu_item_search_agent_bbs) + " " + getString(R.string.cash_out));
                        break;
                    case "BIL":
                        break;
//                    case "CTR":
//                        menuStrings.add(getString(R.string.menu_item_search_agent_bbs) + " " + obj.optString(WebParams.CATEGORY_NAME));
//                        menuDrawables.add(getResources().getDrawable(R.drawable.ic_search_agent_ctr));
//                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void RefreshSaldo() {
        String balance = sp.getString(DefineValue.BALANCE_AMOUNT, "0");
        tv_saldo.setText(CurrencyFormat.format(balance));
    }

    @Override
    protected int getInflateFragmentLayout() {
        return 0;
    }

    @Override
    public boolean checkCanDoRefresh() {
        return false;
    }

    @Override
    public void refresh(PtrFrameLayout frameLayout) {

    }

    @Override
    public void goToTop() {

    }

    private IntentFilter filter = new IntentFilter(BalanceService.INTENT_ACTION_BALANCE);
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("receiver service balance");
            animateRefreshBtn(false);
            RefreshSaldo();

            String action = intent.getAction();
            if (action.equals(AgentShopService.INTENT_ACTION_AGENT_SHOP)) {

                if (!sp.getBoolean(DefineValue.IS_AGENT, false)) {
                    goneStatusOnline();
                } else {
                    if (sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES)) {
                        showStatusOnline();
                    } else {
                        goneStatusOnline();
                    }

                }

                if (sp.getBoolean(DefineValue.IS_AGENT, false)) {
                    Timber.d("Receiver AgentShop %s", sp.getString(DefineValue.AGENT_SHOP_CLOSED, ""));
                    swSettingOnline.setOnCheckedChangeListener(null);
                    swSettingOnline.setChecked(sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO));
                    swSettingOnline.setOnCheckedChangeListener(switchListener);
                }
            }
        }
    };

    private final BroadcastReceiver refBtnReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            animateRefreshBtn(true);
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if (sp.getBoolean(DefineValue.IS_AGENT, false)) {

            swSettingOnline.setOnCheckedChangeListener(null);
            swSettingOnline.setChecked(sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO));
            swSettingOnline.setOnCheckedChangeListener(switchListener);
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refBtnReciever,
                new IntentFilter(MainPage.HOME_BALANCE_ANIMATE));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(refBtnReciever);
    }

    private void switchActivity(Intent mIntent, int j) {
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, j);
    }

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

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY, extraSignature);

            if (!GlobalSetting.isLocationEnabled(getActivity()) && shopStatus.equals(DefineValue.SHOP_OPEN)) {
                showAlertEnabledGPS();
            } else {
                if (isCallWebservice) {

                    showProgressDialog();

                    params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                    params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                    params.put(WebParams.SHOP_ID, sp.getString(DefineValue.BBS_SHOP_ID, ""));
                    params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.BBS_MEMBER_ID, ""));
                    params.put(WebParams.SHOP_STATUS, shopStatus);
                    params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));

                    RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY, params,
                            new ResponseListener() {
                                @Override
                                public void onResponses(JsonObject object) {
                                    jsonModel model = getGson().fromJson(object, jsonModel.class);

                                    String code = model.getError_code();
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
                                        Toast.makeText(getContext(), model.getError_message(), Toast.LENGTH_SHORT).show();
                                    }

                                    Intent i = new Intent(AgentShopService.INTENT_ACTION_AGENT_SHOP);
                                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);
                                }

                                @Override
                                public void onError(Throwable throwable) {

                                }

                                @Override
                                public void onComplete() {
                                    dismissProgressDialog();
                                }
                            });
                }
            }

        }
    };

    private void showAlertEnabledGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.alertbox_gps_warning))
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialog, id) -> {

                    Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(ilocation, RC_GPS_REQUEST);

                })
                .setNegativeButton(R.string.no, (dialog, id) -> {
                    dialog.cancel();

                    swSettingOnline.setOnClickListener(null);
                    swSettingOnline.setChecked(false);
                    swSettingOnline.setOnCheckedChangeListener(switchListener);
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void dialogDormant() {
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getActivity().getString(R.string.title_dialog_dormant),
                getActivity().getString(R.string.message_dialog_dormant),
                () -> {
                }
        );

        dialognya.show();
    }

    private void dialogUnavailable() {
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getString(R.string.alertbox_title_information),
                getString(R.string.cashout_dialog_message),
                () -> {
                }
        );

        dialognya.show();
    }

    private void openCustomizeMenu() {
        if (editMenuModel == null) {
            setRealmToDefault();
            realm.beginTransaction();
            for (int i = 0; i < menuStrings.size() - 1; i++) {
                editMenuModel = realm.createObject(EditMenuModel.class);
                editMenuModel.setTitle(menuStrings.get(i));
                editMenuModel.setIsShow(true);
            }

            if (realm.isInTransaction())
                realm.commitTransaction();
        }
        CustomizeMenuFragment customizeMenuFragment = new CustomizeMenuFragment(this::setupTitleMenu);
        customizeMenuFragment.show(getFragManager(), "CustomizeMenuFragment");
    }

    private void setRealmToDefault() {
        if (Realm.getDefaultConfiguration() != null) {
            realm = Realm.getInstance(Realm.getDefaultConfiguration());
        }
    }

    private void setMenuAdapter() {
        gridMenuAdapter = new GridMenu(getContext(), menuStringsMain, menuDrawablesMain);
        adapterHomeMain = new AdapterHome(getContext(), menuStrings, menuDrawables, this::onClickMenuItem);
        gridView.setAdapter(gridMenuAdapter);
        recyclerView.setAdapter(adapterHomeMain);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
    }

    private void getTitleMenu() {
        setRealmToDefault();
        RealmResults<EditMenuModel> realmResults = realm.where(EditMenuModel.class).equalTo(DefineValue.IS_SHOW, true).findAll();
        if (realmResults != null) {
            menuStrings.clear();
            for (int i = 0; i < realmResults.size(); i++) {
                menuStrings.add(realmResults.get(i).getTitle());
            }
        }
        setupIconMenu();
    }

    private void setupIconMenu() {
        menuDrawables.clear();
        menuDrawablesMain.clear();
        for (int i = 0; i < menuStrings.size(); i++) {
            menuDrawables.add(ResourcesCompat.getDrawable(getResources(), getImageMenu(menuStrings.get(i)), null));
        }
        for (int i = 0; i < menuStringsMain.size(); i++) {
            menuDrawablesMain.add(ResourcesCompat.getDrawable(getResources(), getImageMenu(menuStringsMain.get(i)), null));
        }
        gridMenuAdapter.notifyDataSetChanged();
        adapterHomeMain.notifyDataSetChanged();
    }

    private int getImageMenu(String titleMenu) {
        if (titleMenu.equalsIgnoreCase(getString(R.string.toolbar_title_topup)))
            return R.drawable.ic_top_up;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_send)))
            return R.drawable.ic_transfer;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_ask_for_money)))
            return R.drawable.ic_request;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_mandiri_lkd)))
            return R.drawable.ic_mandiri;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_trx_agent)))
            return R.drawable.ic_permintaan_transaksi;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.title_bbs_list_account_bbs)))
            return R.drawable.ic_rekening_saya;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_onprogress_agent)))
            return R.drawable.ic_dalam_proses;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.cash_out)))
            return R.drawable.ic_tarik_tunai;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.cash_in)))
            return R.drawable.ic_setor_tunai;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_tagih_agent)))
            return R.drawable.ic_biller;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_collector_history)))
            return R.drawable.ic_history_collector;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_upgrade_member)))
            return R.drawable.ic_upgrade;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_title_cash_collection)))
            return R.drawable.ic_cash_collection;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_cash_collector_history)))
            return R.drawable.ic_history_collector;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_pulsa_agent)))
            return R.drawable.ic_pulsa;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.newhome_listrik_pln)))
            return R.drawable.ic_listrik_pln;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.newhome_data)))
            return R.drawable.ic_paket_data;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.newhome_bpjs)))
            return R.drawable.ic_bpjs;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.newhome_emoney)))
            return R.drawable.ic_emoney;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.newhome_game)))
            return R.drawable.ic_game;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.newhome_voucher)))
            return R.drawable.ic_voucher;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.newhome_pam)))
            return R.drawable.ic_pdam;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_biller)))
            return R.drawable.ic_biller;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_scadm)))
            return R.drawable.ic_menu_b2b;
        else if (titleMenu.contains(getResources().getString(R.string.menu_item_title_b2b_eratel)))
//            return R.drawable.ic_b2b_eratel;
            return R.drawable.ic_menu_b2b;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_search_agent_bbs) + " " + getString(R.string.cash_in)))
            return R.drawable.ic_tarik_tunai;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_search_agent_bbs) + " " + getString(R.string.cash_out)))
            return R.drawable.ic_setor_tunai;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.title_cash_out_member)))
            return R.drawable.ic_permintaan_transaksi;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_mutation)))
            return R.drawable.ic_mutasi;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_title_report_ebd)))
            return R.drawable.ic_laporan;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.menu_item_lending)))
            return R.drawable.ic_lending;
        else if (titleMenu.equalsIgnoreCase(getString(R.string.more)))
            return R.drawable.ic_more;
        else if (titleMenu.equalsIgnoreCase(getResources().getString(R.string.transfer_funds))) {
            return R.drawable.ic_transfer;
        }
        else
            return R.drawable.ic_home_default;
    }

    private void onClickMenuItem(String menuItemName) {
        isDormant = sp.getString(DefineValue.IS_DORMANT, DefineValue.STRING_NO);
        String trxType = "";
        int posIdx = -1;
        if (menuItemName.equals(getString(R.string.toolbar_title_topup))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent i = new Intent(getActivity(), TopUpActivity.class);
                i.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
            }
        } else if (menuItemName.equals(getString(R.string.menu_item_title_send))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent intent = new Intent(getActivity(), TransferActivity.class);
                if (allowedTransfer)
                    switchActivity(intent, MainPage.ACTIVITY_RESULT);
                else
                    dialogUnavailable();
            }
        } else if (menuItemName.equals(getString(R.string.menu_item_title_ask_for_money))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                if (getLvlClass().isLevel1QAC()) {
                    dialogUnavailable();
                } else {
                    Intent i = new Intent(getActivity(), AskForMoneyActivity.class);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                }
            }
        } else if (menuItemName.equals(getString(R.string.menu_item_title_upgrade_member))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent i = new Intent(getActivity(), SearchMemberToVerifyActivity.class);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
            }
        } else if (menuItemName.equals(getString(R.string.menu_item_title_biller))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else if (isAgent && !isAvailBiller) {
                dialogUnavailable();
            } else {
                Intent i = new Intent(getActivity(), GridBillerActivity.class);
                i.putExtra(DefineValue.BILLER_TYPE, DefineValue.BIL_TYPE_PAY);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
            }
        } else if (menuItemName.equals(getString(R.string.menu_item_title_mutation))) {
            Intent i = new Intent(getActivity(), HistoryActivity.class);
            switchActivity(i, MainPage.ACTIVITY_RESULT);
        } else if (menuItemName.equals(getString(R.string.menu_item_title_report_ebd))) {
            Intent i = new Intent();
            if (isB2BEratelToko) {
                i = new Intent(getActivity(), ReportEBDActivity.class);
                i.putExtra(DefineValue.EBD, DefineValue.TOKO);
            } else if (isB2BEratelCanvasser) {
                i = new Intent(getActivity(), ReportEBDListActivity.class);
                i.putExtra(DefineValue.EBD, DefineValue.CANVASSER);
            }
            switchActivity(i, MainPage.ACTIVITY_RESULT);
        } else if (menuItemName.equals(getString(R.string.menu_item_title_pulsa_agent))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent intent = new Intent(getActivity(), BillerActivity.class);
                intent.putExtra(DefineValue.BILLER_TYPE, BILLER_TYPE_CODE_PLS);
                intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.prepaid_title));
                startActivity(intent);
            }
        } else if (menuItemName.equals(getString(R.string.newhome_data))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent intent = new Intent(getActivity(), BillerActivity.class);
                intent.putExtra(DefineValue.BILLER_TYPE, BILLER_TYPE_CODE_DATA);
                intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.data_title));
                startActivity(intent);
            }
        } else if (menuItemName.equals(getString(R.string.newhome_listrik_pln))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent intent = new Intent(getActivity(), BillerActivity.class);
                intent.putExtra(DefineValue.BILLER_TYPE, BILLER_TYPE_CODE_TKN);
                intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.pln_title));
                startActivity(intent);
            }
        } else if (menuItemName.equals(getString(R.string.newhome_emoney))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else if (sp.getInt(DefineValue.LEVEL_VALUE, 1) == 1) {
                LevelClass levelClass = new LevelClass(getActivity());
                levelClass.showDialogLevel();
            } else {
                Intent intent = new Intent(getActivity(), BillerActivity.class);
                intent.putExtra(DefineValue.BILLER_TYPE, BILLER_TYPE_CODE_EMONEY);
                intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.newhome_emoney));
                startActivity(intent);
            }
        } else if (menuItemName.equals(getString(R.string.newhome_bpjs))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent intent = new Intent(getActivity(), BillerActivity.class);
                intent.putExtra(DefineValue.BILLER_TYPE, BILLER_TYPE_CODE_BPJS);
                intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.newhome_bpjs));
                startActivity(intent);
            }
        } else if (menuItemName.equals(getString(R.string.newhome_game))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent intent = new Intent(getActivity(), BillerActivity.class);
                intent.putExtra(DefineValue.BILLER_TYPE, BILLER_TYPE_CODE_GAME);
                intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.newhome_game));
                startActivity(intent);
            }
        } else if (menuItemName.equals(getString(R.string.newhome_voucher))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent intent = new Intent(getActivity(), BillerActivity.class);
                intent.putExtra(DefineValue.BILLER_TYPE, BILLER_TYPE_CODE_VOUCHER);
                intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.newhome_voucher));
                startActivity(intent);
            }
        } else if (menuItemName.equals(getString(R.string.newhome_pam))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent intent = new Intent(getActivity(), BillerActivity.class);
                intent.putExtra(DefineValue.BILLER_TYPE, BILLER_TYPE_CODE_PDAM);
                intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.newhome_pam));
                startActivity(intent);
            }
        } else if (menuItemName.equals(getString(R.string.menu_item_title_scadm))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES))
                dialogDormant();
            else
                startActivity(new Intent(getActivity(), B2BActivity.class));
        } else if (menuItemName.equals(getString(R.string.menu_item_title_b2b_eratel) + " " + getString(R.string.menu_item_title_ebd_toko))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES))
                dialogDormant();
            else
                startActivity(new Intent(getActivity(), TokoEBDActivity.class));
        } else if (menuItemName.equals(getString(R.string.menu_item_title_b2b_eratel) + " " + getString(R.string.menu_item_title_ebd_canvasser))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES))
                dialogDormant();
            else
                startActivity(new Intent(getActivity(), B2BCanvasserActivity.class));
        } else if (menuItemName.equals(getString(R.string.menu_item_title_mandiri_lkd))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES))
                dialogDormant();
            else
                startActivity(new Intent(getActivity(), MandiriLPActivity.class));
        } else if (menuItemName.equals(getString(R.string.menu_item_lending))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES))
                dialogDormant();
            else
                startActivity(new Intent(getActivity(), GridLendingActivity.class));
        } else if (menuItemName.equals(getString(R.string.more))) {
            openCustomizeMenu();
        } else if (menuItemName.equals(getString(R.string.menu_item_title_tagih_agent))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else
                startActivity(new Intent(getActivity(), TagihActivity.class));
        } else if (menuItemName.equals(getString(R.string.menu_title_cash_collection))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else
                startActivity(new Intent(getActivity(), CashCollectionActivity.class));
        } else if (menuItemName.equals(getString(R.string.menu_item_title_collector_history))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent intent = new Intent(getActivity(), HistoryActivity.class);
                intent.putExtra(DefineValue.IS_AGENT_DGI, true);
                sp.edit().putBoolean(DefineValue.IS_AGENT_DGI, true).commit();
                intent.putExtra(DefineValue.HISTORY_TITLE, getString(R.string.menu_item_title_collector_history));
                startActivity(intent);
            }
        } else if (menuItemName.equals(getString(R.string.menu_item_title_cash_collector_history))) {
            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                dialogDormant();
            } else {
                Intent intent = new Intent(getActivity(), HistoryActivity.class);
                intent.putExtra(DefineValue.IS_AGENT_CTR, true);
                sp.edit().putBoolean(DefineValue.IS_AGENT_CTR, true).commit();
                intent.putExtra(DefineValue.HISTORY_TITLE, getString(R.string.menu_item_title_cash_collector_history));
                startActivity(intent);
            }
        }

        if (isAgent) {
            if (menuItemName.equalsIgnoreCase(getString(R.string.title_bbs_list_account_bbs)))
                if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                    dialogDormant();
                } else
                    posIdx = BBSActivity.LISTACCBBS;
            else if (menuItemName.equalsIgnoreCase(getString(R.string.title_cash_out_member)))
                if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                    dialogDormant();
                } else
                    posIdx = BBSActivity.CONFIRMCASHOUT;
            else if (menuItemName.equalsIgnoreCase(getString(R.string.menu_item_title_trx_agent)))
                if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                    dialogDormant();
                } else
                    posIdx = BBSActivity.BBSTRXAGENT;
            else if (menuItemName.equalsIgnoreCase(getString(R.string.cash_in))) {
                if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                    dialogDormant();
                } else {
                    posIdx = BBSActivity.TRANSACTION;
                    trxType = DefineValue.BBS_CASHIN;
                }
            } else if (menuItemName.equalsIgnoreCase(getString(R.string.cash_out))) {
                if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                    dialogDormant();
                } else {
                    posIdx = BBSActivity.TRANSACTION;
                    trxType = DefineValue.BBS_CASHOUT;
                }
            } else if (menuItemName.equals(getString(R.string.menu_item_title_onprogress_agent))) {
                if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                    dialogDormant();
                } else {
                    posIdx = BBSActivity.BBSONPROGRESSAGENT;
                    trxType = DefineValue.INDEX;
                }
            } else if (menuItemName.equalsIgnoreCase(getString(R.string.transfer_funds))) {
                if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                    dialogDormant();
                } else {
                    posIdx = BBSActivity.TRANSACTION;
                    trxType = DefineValue.BBS_TRANSFER_FUND;
                }
            }
        } else {
            if (menuItemName.equalsIgnoreCase(getString(R.string.title_cash_out_member))) {
                if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES))
                    dialogDormant();
                else
                    posIdx = BBSActivity.CONFIRMCASHOUT;
            } else {
                try {
                    if (memberSchemeCode.equals("") || memberSchemeCode == null) {
                        getCategoryList();
                    }
                    JSONArray jsonArray = new JSONArray(memberSchemeCode);
                    for (int index = 0; index < jsonArray.length(); index++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        String objs = jsonObject.optString(WebParams.CATEGORY_NAME, "");
                        if (objs.equals("Tarik Tunai"))
                            objs = getString(R.string.cash_out);
                        if (objs.equals("Setor Tunai"))
                            objs = getString(R.string.cash_in);
                        String categoryNameModified = getString(R.string.menu_item_search_agent_bbs) + " " + objs;
                        if (menuItemName.equalsIgnoreCase(categoryNameModified)) {
                            if (isDormant.equalsIgnoreCase(DefineValue.STRING_YES)) {
                                dialogDormant();
                            } else {
                                Intent i = new Intent(getActivity(), BbsNewSearchAgentActivity.class);
                                i.putExtra(DefineValue.CATEGORY_ID, jsonObject.optString(WebParams.CATEGORY_ID));
                                sp.edit().putString(DefineValue.CATEGORY_ID, jsonObject.optString(WebParams.CATEGORY_ID));
                                i.putExtra(DefineValue.CATEGORY_NAME, objs);
                                i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                                i.putExtra(DefineValue.AMOUNT, "");
                                i.putExtra(DefineValue.BBS_SCHEME_CODE, jsonObject.optString(WebParams.SCHEME_CODE));
                                switchActivity(i, MainPage.ACTIVITY_RESULT);
                                break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (posIdx != -1) {
            Intent i = new Intent(getActivity(), BBSActivity.class);
            i.putExtra(DefineValue.INDEX, posIdx);

            if (!trxType.equals(""))
                i.putExtra(DefineValue.TYPE, trxType);

            switchActivity(i, MainPage.ACTIVITY_RESULT);
        }
    }
}
