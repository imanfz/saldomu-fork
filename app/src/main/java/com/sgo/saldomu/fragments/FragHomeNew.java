package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.Beans.PromoObject;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.AskForMoneyActivity;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsNewSearchAgentActivity;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.activities.HistoryActivity;
import com.sgo.saldomu.activities.ListBuyActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.ReportActivity;
import com.sgo.saldomu.activities.SearchMemberToVerifyActivity;
import com.sgo.saldomu.activities.TagihActivity;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.adapter.GridHome;
import com.sgo.saldomu.coreclass.BaseFragmentMainPage;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.ShopCategory;
import com.sgo.saldomu.models.retrofit.CategoriesModel;
import com.sgo.saldomu.models.retrofit.CategoryListModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.services.AgentShopService;
import com.sgo.saldomu.services.BalanceService;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import in.srain.cube.views.ptr.PtrFrameLayout;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/10/2017.
 */
public class FragHomeNew extends BaseFragmentMainPage {
    GridView GridView;
    Button btn_beli, btn_topup;
    TextView tv_saldo;
    EditText input;
    TextView tv_pulsa;
    TextView tv_bpjs;
    TextView tv_listrikPLN;
    TextView tv_greetings;
    View view_pulsa;
    View view_bpjs;
    View view_listrikPLN;
    View v;
    View BPJS;
    View PLS;
    View TKN;
    ImageView refreshBtn;
    ImageView img_greetings;
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
    private Realm realm;
    private Switch swSettingOnline;
    private LinearLayout llAgentDetail;
    String shopStatus, isMemberShopDGI, isDormant, agentSchemeCode, memberSchemeCode;
    Boolean isAgent;
    ProgressBar gridview_progbar;
    ProgressBar progBanner;
    private CarouselView carouselView;
    private ArrayList<PromoObject> listPromo = new ArrayList<>();

    private HashMap<String, Drawable> menuStringAndIconHashMap = new HashMap<>();
    private ArrayList<String> menuStrings = new ArrayList<>();
    private ArrayList<Drawable> menuDrawables = new ArrayList<>();

    private static final int RC_GPS_REQUEST = 1;

    public FragHomeNew() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filter = new IntentFilter();
        filter.addAction(BalanceService.INTENT_ACTION_BALANCE);
        filter.addAction(AgentShopService.INTENT_ACTION_AGENT_SHOP);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag_home_new, container, false);

        GridView = v.findViewById(R.id.grid);
        tv_saldo = v.findViewById(R.id.tv_saldo);
        swSettingOnline = v.findViewById(R.id.swSettingOnline);
        llAgentDetail = v.findViewById(R.id.llAgentDetail);
        gridview_progbar = v.findViewById(R.id.gridview_progbar);
        refreshBtn = v.findViewById(R.id.btn_refresh_balance);
        btn_topup = v.findViewById(R.id.btn_topup);
        progBanner = v.findViewById(R.id.progressBarBanner);
        carouselView = v.findViewById(R.id.carouselView1);

        return v;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isMemberShopDGI = sp.getString(DefineValue.IS_MEMBER_SHOP_DGI, "0");
        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

        isDormant = sp.getString(DefineValue.IS_DORMANT, "N");

        agentSchemeCode = sp.getString(DefineValue.AGENT_SCHEME_CODES, "");

        realm = RealmManager.getRealmBiller();

        getRealmData();

        if (!sp.getBoolean(DefineValue.IS_AGENT, false)) {
            llAgentDetail.setVisibility(View.GONE);
        } else {
            if (sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES)) {
                llAgentDetail.setVisibility(View.VISIBLE);
            } else {
                llAgentDetail.setVisibility(View.GONE);
            }
        }


        if (isAgent) {
            setupIconAndTitle();
            GridHome adapter = new GridHome(getActivity(), menuStrings, menuDrawables);
            GridView.setAdapter(adapter);
        } else {
            if (sp.getString(DefineValue.CATEGORY, null) == null) {
                HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CATEGORY_LIST);
                params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                params.put(WebParams.SHOP_ID, "");
                params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
                Timber.d("isi params shop category:" + params.toString());

                showView(gridview_progbar);

                RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_CATEGORY_LIST, params,
                        new ResponseListener() {
                            @Override
                            public void onResponses(JsonObject object) {
                                CategoryListModel model = getGson().fromJson(object, CategoryListModel.class);

                                String code = model.getError_code();

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    SecurePreferences.Editor mEditor = sp.edit();
                                    for (int i = 0; i < model.getCategories().size(); i++) {

                                        CategoriesModel obj = model.getCategories().get(i);


                                        String arrJson = toJson(model.getCategories()).toString();
                                        mEditor.putString(DefineValue.CATEGORY, arrJson);

                                        ShopCategory shopCategory = new ShopCategory();
                                        shopCategory.setCategoryId(obj.getCategory_id());
                                        if (shopCategory.getCategoryId().contains("SETOR"))
                                            mEditor.putString(DefineValue.CATEGORY_ID_CTA, shopCategory.getCategoryId());
                                        if (obj.getCategory_name().contains("Upgrade"))
                                            mEditor.putString(DefineValue.CATEGORY_ID_UPG, shopCategory.getCategoryId());
                                        shopCategory.setSchemeCode(obj.getScheme_code());
                                        String tempCategory = obj.getCategory_name().toLowerCase();
                                        String[] strArray = tempCategory.split(" ");
                                        StringBuilder builder = new StringBuilder();
                                        for (String s : strArray) {
                                            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                            builder.append(cap + " ");
                                        }

                                        shopCategory.setCategoryName(builder.toString());
                                        shopCategories.add(shopCategory);

                                    }
                                    mEditor.apply();
                                } else {
                                    Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_LONG).show();
                                }

                                setupIconAndTitle();
                                GridHome adapter = new GridHome(getActivity(), menuStrings, menuDrawables);
                                GridView.setAdapter(adapter);
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onComplete() {
                                dismissProgressDialog();
                                hideView(gridview_progbar);
                                Timber.d("hide view");
                            }
                        });
            } else {
                setupIconAndTitle();
                GridHome adapter = new GridHome(getActivity(), menuStrings, menuDrawables);
                GridView.setAdapter(adapter);
            }

        }


        btn_topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), TopUpActivity.class);
                i.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                switchActivity(i, MainPage.ACTIVITY_RESULT);
            }
        });

//        btn_beli.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (view_pulsa.getVisibility() == View.VISIBLE) {
//                    if (inputValidation() == true) {
//                        Intent intent = new Intent(getActivity(), BillerActivity.class);
//                        intent.putExtra(DefineValue.BILLER_TYPE, "PLS");
//                        intent.putExtra(DefineValue.BILLER_ID_NUMBER, input.getText().toString());
//                        intent.putExtra(DefineValue.BILLER_NAME, "Voucher Pulsa Handphone");
//                        startActivity(intent);
//                    }
//                }
//                if (view_bpjs.getVisibility() == View.VISIBLE) {
//                    Intent intent = new Intent(getActivity(), BillerActivity.class);
//                    intent.putExtra(DefineValue.BILLER_TYPE, "BPJS");
//                    intent.putExtra(DefineValue.BILLER_ID_NUMBER, input.getText().toString());
//                    intent.putExtra(DefineValue.BILLER_NAME, "BPJS");
//                    startActivity(intent);
//                }
//                if (view_listrikPLN.getVisibility() == View.VISIBLE) {
//                    Intent intent = new Intent(getActivity(), BillerActivity.class);
//                    intent.putExtra(DefineValue.BILLER_TYPE, "TKN");
//                    intent.putExtra(DefineValue.BILLER_ID_NUMBER, input.getText().toString());
//                    intent.putExtra(DefineValue.BILLER_NAME, "Voucher Token Listrik");
//                    startActivity(intent);
//                }
//            }
//        });
//        tv_pulsa.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                input.setText("");
//                view_pulsa.setVisibility(View.VISIBLE);
//                view_bpjs.setVisibility(View.INVISIBLE);
//                view_listrikPLN.setVisibility(View.INVISIBLE);
//                input.setHint("Masukkan No. Hp");
//            }
//        });
//        tv_bpjs.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                input.setText("");
//                input.setError(null);
//                input.clearFocus();
//                view_pulsa.setVisibility(View.INVISIBLE);
//                view_bpjs.setVisibility(View.VISIBLE);
//                view_listrikPLN.setVisibility(View.INVISIBLE);
//                input.setHint("Masukkan No. BPJS");
//            }
//        });
//        tv_listrikPLN.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                input.setText("");
//                input.setError(null);
//                input.clearFocus();
//                view_pulsa.setVisibility(View.INVISIBLE);
//                view_bpjs.setVisibility(View.INVISIBLE);
//                view_listrikPLN.setVisibility(View.VISIBLE);
//                input.setHint("Masukkan No. Listrik");
//            }
//        });


        GridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("masuk gridhomeonitemclicklistener");
                Fragment newFragment;

                isDormant = sp.getString(DefineValue.IS_DORMANT, "N");

                String menuItemName = ((TextView) view.findViewById(R.id.grid_text)).getText().toString();
                String trxType = "";
                int posIdx = -1;

                if (menuItemName.equals(getString(R.string.newhome_title_topup))) {
                    Intent i = new Intent(getActivity(), TopUpActivity.class);
                    i.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                } else if (menuItemName.equals(getString(R.string.menu_item_title_ask_for_money))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        if (getLvlClass().isLevel1QAC()) {
                            getLvlClass().showDialogLevel();
                        } else {
                            Intent i = new Intent(getActivity(), AskForMoneyActivity.class);
                            switchActivity(i, MainPage.ACTIVITY_RESULT);
                        }
                    }
                }
                // upgrade Member AGENT
                 else if (menuItemName.equals(getString(R.string.menu_item_title_upgrade_member))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Intent i = new Intent(getActivity(), SearchMemberToVerifyActivity.class);
                        switchActivity(i, MainPage.ACTIVITY_RESULT);
                    }
                } else if (menuItemName.equals(getString(R.string.menu_item_title_buy))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Intent i = new Intent(getActivity(), ListBuyActivity.class);
                        switchActivity(i, MainPage.ACTIVITY_RESULT);
                    }
                } else if (menuItemName.equals(getString(R.string.menu_item_title_report))) {
                    Intent i = new Intent(getActivity(), ReportActivity.class);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                } else if (menuItemName.equals(getString(R.string.menu_item_title_scadm))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else
                        switchMenu(NavigationDrawMenu.MSCADM, null);
                } else if (menuItemName.equals(getString(R.string.menu_item_search_agent))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Bundle bundle = new Bundle();
                        switchMenu(NavigationDrawMenu.MCATEGORYBBS, bundle);
                    }
                } else if (menuItemName.equals(getString(R.string.menu_item_title_pulsa_agent))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Intent intent = new Intent(getActivity(), BillerActivity.class);
                        intent.putExtra(DefineValue.BILLER_TYPE, "PLS");
                        intent.putExtra(DefineValue.BILLER_NAME, "Voucher Pulsa Handphone");
                        startActivity(intent);
                    }
                } else if (menuItemName.equals(getString(R.string.newhome_data))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Intent intent = new Intent(getActivity(), BillerActivity.class);
                        intent.putExtra(DefineValue.BILLER_TYPE, "DATA");
                        intent.putExtra(DefineValue.BILLER_NAME, "Paket Data Handphone");
                        startActivity(intent);
                    }
                } else if (menuItemName.equals(getString(R.string.newhome_listrik_pln))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Intent intent = new Intent(getActivity(), BillerActivity.class);
                        intent.putExtra(DefineValue.BILLER_TYPE, "TKN");
                        intent.putExtra(DefineValue.BILLER_NAME, "Voucher Token Listrik");
                        startActivity(intent);
                    }
                } else if (menuItemName.equals(getString(R.string.newhome_emoney))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Intent intent = new Intent(getActivity(), BillerActivity.class);
                        intent.putExtra(DefineValue.BILLER_TYPE, "EMON");
                        intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.newhome_emoney));
                        startActivity(intent);
                    }
                } else if (menuItemName.equals(getString(R.string.newhome_bpjs))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Intent intent = new Intent(getActivity(), BillerActivity.class);
                        intent.putExtra(DefineValue.BILLER_TYPE, "BPJS");
                        intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.newhome_bpjs));
                        startActivity(intent);
                    }
                } else if (menuItemName.equals(getString(R.string.newhome_game))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Intent intent = new Intent(getActivity(), BillerActivity.class);
                        intent.putExtra(DefineValue.BILLER_TYPE, "GAME");
                        intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.newhome_game));
                        startActivity(intent);
                    }
                } else if (menuItemName.equals(getString(R.string.newhome_voucher))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Intent intent = new Intent(getActivity(), BillerActivity.class);
                        intent.putExtra(DefineValue.BILLER_TYPE, "VCHR");
                        intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.newhome_voucher));
                        startActivity(intent);
                    }
                } else if (menuItemName.equals(getString(R.string.newhome_pam))) {
                    if (isDormant.equalsIgnoreCase("Y")) {
                        dialogDormant();
                    } else {
                        Intent intent = new Intent(getActivity(), BillerActivity.class);
                        intent.putExtra(DefineValue.BILLER_TYPE, "AIR");
                        intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.pam));
                        startActivity(intent);
                    }
                } else if (menuItemName.equals(getString(R.string.menu_item_history_detail))) {
                    Intent intent = new Intent(getActivity(), HistoryActivity.class);
                    startActivity(intent);
                }

                if (isAgent) {
                    if (menuItemName.equalsIgnoreCase(getString(R.string.title_bbs_list_account_bbs)))
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            posIdx = BBSActivity.LISTACCBBS;
                    else if (menuItemName.equalsIgnoreCase(getString(R.string.transaction)))
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            posIdx = BBSActivity.TRANSACTION;
                    else if (menuItemName.equalsIgnoreCase(getString(R.string.title_cash_out_member)))
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            posIdx = BBSActivity.CONFIRMCASHOUT;
                    else if (menuItemName.equalsIgnoreCase(getString(R.string.menu_item_title_kelola)))
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            posIdx = BBSActivity.BBSKELOLA;
                    else if (menuItemName.equalsIgnoreCase(getString(R.string.menu_item_title_trx_agent)))
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            posIdx = BBSActivity.BBSTRXAGENT;
                    else if (menuItemName.equalsIgnoreCase(getString(R.string.menu_item_title_waktu_beroperasi)))
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            posIdx = BBSActivity.BBSWAKTUBEROPERASI;
                    else if (menuItemName.equalsIgnoreCase(getString(R.string.menu_item_title_tutup_manual)))
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            posIdx = BBSActivity.BBSTUTUPMANUAL;
                    else if (menuItemName.equalsIgnoreCase(getString(R.string.cash_in))) {
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else {
                            posIdx = BBSActivity.TRANSACTION;
                            trxType = DefineValue.BBS_CASHIN;
                        }
                    } else if (menuItemName.equalsIgnoreCase(getString(R.string.cash_out))) {
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else {
                            posIdx = BBSActivity.TRANSACTION;
                            trxType = DefineValue.BBS_CASHOUT;
                        }
                    } else if (menuItemName.equals(getString(R.string.menu_item_title_onprogress_agent))) {
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else {
                            posIdx = BBSActivity.BBSONPROGRESSAGENT;
                            trxType = DefineValue.INDEX;
                        }
                    } else if (menuItemName.equals(getString(R.string.menu_item_title_tagih_agent))) {
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            startActivity(new Intent(getActivity(), TagihActivity.class));
                    } else {
                        posIdx = -1;
                    }
                } else {
                    if (menuItemName.equalsIgnoreCase(getString(R.string.title_cash_out_member)))
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            posIdx = BBSActivity.CONFIRMCASHOUT;
                    else if (menuItemName.equalsIgnoreCase(getString(R.string.title_rating_by_member)))
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            posIdx = BBSActivity.BBSRATINGBYMEMBER;
                    else if (menuItemName.equalsIgnoreCase(getString(R.string.title_bbs_my_orders)))
                        if (isDormant.equalsIgnoreCase("Y")) {
                            dialogDormant();
                        } else
                            posIdx = BBSActivity.BBSMYORDERS;
                    else {
//                        posIdx = -1;
                        try {
                            JSONArray jsonArray = new JSONArray(memberSchemeCode);
                            for (int index = 0; index < jsonArray.length(); index++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(index);
                                String objs = jsonObject.optString(WebParams.CATEGORY_NAME, "");
                                String categoryNameModified = "Panggil Agen " + objs;
                                if (menuItemName.equalsIgnoreCase(categoryNameModified)) {
                                    if (isDormant.equalsIgnoreCase("Y")) {
                                        dialogDormant();
                                    } else {
                                        Intent i = new Intent(getActivity(), BbsNewSearchAgentActivity.class);
                                        i.putExtra(DefineValue.CATEGORY_ID, jsonObject.optString(WebParams.CATEGORY_ID));
                                        sp.edit().putString(DefineValue.CATEGORY_ID, jsonObject.optString(WebParams.CATEGORY_ID));
                                        i.putExtra(DefineValue.CATEGORY_NAME, jsonObject.optString(WebParams.CATEGORY_NAME));
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

        });


        if (sp.getBoolean(DefineValue.IS_AGENT, false)) {

            swSettingOnline.setOnCheckedChangeListener(null);
            if (sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO)) {
                swSettingOnline.setChecked(true);
            } else {
                swSettingOnline.setChecked(false);
            }
            swSettingOnline.setOnCheckedChangeListener(switchListener);
        }

        RefreshSaldo();
        if (getLvlClass() != null)
            getLvlClass().refreshData();

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                animateRefrestBtn(true);
//                sp.edit().putString(DefineValue.IS_MANUAL, "Y").commit();
//                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(MainPage.RESULT_HOME_BALANCE));

                getBalance(true);
            }
        });

        if (sp.getBoolean(DefineValue.SAME_BANNER, false) == false)
            getPromoList();
        else
            populateBanner();
    }

    private void getRealmData() {
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

    public void getBalance(Boolean isAuto) {


        animateRefrestBtn(true);

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

            Timber.d("isi params get promo list:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_PROMO_LIST, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("isi params promo list:" + response.toString());
                                    String count = response.getString(WebParams.COUNT);
                                    if (!count.equals("0")) {
                                        JSONArray mArrayPromo = new JSONArray(response.getString(WebParams.PROMO_DATA));

                                        for (int i = 0; i < mArrayPromo.length(); i++) {
                                            String id = mArrayPromo.getJSONObject(i).getString(WebParams.ID);
                                            boolean flagSame = false;

                                            // cek apakah ada id yang sama.. kalau ada tidak dimasukan ke array
                                            if (mArrayPromo.length() > 0) {
                                                for (int index = 0; index < mArrayPromo.length(); index++) {
                                                    if (mArrayPromo.getJSONObject(i).getString(WebParams.ID).equals(id)) {
                                                        flagSame = true;
                                                        sp.edit().putBoolean(DefineValue.SAME_BANNER, true).commit();
                                                        sp.edit().putString(DefineValue.DATA_BANNER, response.getString(WebParams.PROMO_DATA)).commit();
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
                                    }
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout", response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginMain(getActivity(), message);
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

                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void populateBanner() {

        if (sp.getBoolean(DefineValue.SAME_BANNER,false)==true) {
            try {
                JSONArray mArrayPromo_ = new JSONArray(sp.getString(DefineValue.DATA_BANNER,""));
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
        carouselView.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                Glide.with(getActivity())
                        .load(listPromo.get(position).getImage())
                        .into(imageView);
            }
        });
        carouselView.setPageCount(listPromo.size());
        carouselView.setImageClickListener(position -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(listPromo.get(position).getUrl()));
            startActivity(browserIntent);
        });
        carouselView.setVisibility(View.VISIBLE);
        progBanner.setVisibility(View.GONE);
    }

    void animateRefrestBtn(boolean isLoad) {
        if (isLoad) {
            refreshBtn.setEnabled(false);
            refreshBtn.startAnimation(frameAnimation);
        } else {
            refreshBtn.setEnabled(true);
            refreshBtn.clearAnimation();
        }
    }

    private void setupIconAndTitle() {
        if (getActivity() != null && isAdded()) {
            if (isAgent) {
                checkSchemeCodeAgent();
                if (sp.getString(DefineValue.IS_AGENT_TRX_REQ, "").equalsIgnoreCase("Y")) {
                    menuStrings.add(getResources().getString(R.string.menu_item_title_trx_agent));
                    menuDrawables.add(getResources().getDrawable(R.drawable.ic_permintaan_transaksi));
                }
                menuStrings.add(getResources().getString(R.string.title_bbs_list_account_bbs));
                menuDrawables.add(getResources().getDrawable(R.drawable.ic_rekening_saya));

                menuStrings.add(getResources().getString(R.string.menu_item_title_onprogress_agent));
                menuDrawables.add(getResources().getDrawable(R.drawable.ic_dalam_proses));
            } else {
                checkSchemeCodeMember();

                menuStrings.add(getResources().getString(R.string.title_cash_out_member));
                menuDrawables.add(getResources().getDrawable(R.drawable.ic_permintaan_transaksi));

                menuStrings.add(getResources().getString(R.string.menu_item_title_buy));
                menuDrawables.add(getResources().getDrawable(R.drawable.ic_belanja));

                if (mBillerTypeDataPLS != null) {
                    menuStrings.add(getResources().getString(R.string.menu_item_title_pulsa_agent));
                    menuDrawables.add(getResources().getDrawable(R.drawable.ic_pulsa));
                }

                if (mBillerTypeDataDATA != null) {
                    menuStrings.add(getResources().getString(R.string.newhome_data));
                    menuDrawables.add(getResources().getDrawable(R.drawable.ic_paket_data));
                }

                if (mBillerTypeDataBPJS != null) {
                    menuStrings.add(getResources().getString(R.string.newhome_bpjs));
                    menuDrawables.add(getResources().getDrawable(R.drawable.ic_bpjs));
                }

                if (mBillerTypeDataTKN != null) {
                    menuStrings.add(getResources().getString(R.string.newhome_listrik_pln));
                    menuDrawables.add(getResources().getDrawable(R.drawable.ic_listrik_pln));
                }

                if (mBillerTypeDataEMoney != null) {
                    menuStrings.add(getResources().getString(R.string.newhome_emoney));
                    menuDrawables.add(getResources().getDrawable(R.drawable.ic_emoney));
                }

                if (mBillerTypeDataGame != null) {
                    menuStrings.add(getResources().getString(R.string.newhome_game));
                    menuDrawables.add(getResources().getDrawable(R.drawable.game));
                }

                if (mBillerTypeDataVoucher != null) {
                    menuStrings.add(getResources().getString(R.string.newhome_voucher));
                    menuDrawables.add(getResources().getDrawable(R.drawable.voucher));
                }

                if (mBillerTypeDataPDAM != null) {
                    menuStrings.add(getResources().getString(R.string.newhome_pam));
                    menuDrawables.add(getResources().getDrawable(R.drawable.ic_pdam));
                }

            }
            menuStrings.add(getResources().getString(R.string.menu_item_title_ask_for_money));
            menuDrawables.add(getResources().getDrawable(R.drawable.ic_minta_saldo));

            menuStrings.add(getResources().getString(R.string.menu_item_title_report));
            menuDrawables.add(getResources().getDrawable(R.drawable.ic_laporan));

            menuStrings.add(getResources().getString(R.string.menu_item_history_detail));
            menuDrawables.add(getResources().getDrawable(R.drawable.group));
        }

//        menuStrings.add(getResources().getString(R.string.menu_item_title_pay_friends));
//        menuDrawables.add(getResources().getDrawable(R.drawable.ic_transfer_saldo));
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
                        menuDrawables.add(getResources().getDrawable(R.drawable.ic_tarik_tunai));
                        break;
                    case "CTA":
                        menuStrings.add(getResources().getString(R.string.cash_in));
                        menuDrawables.add(getResources().getDrawable(R.drawable.ic_setor_tunai));
                        break;
                    case "DGI":
                        menuStrings.add(getResources().getString(R.string.menu_item_title_tagih_agent));
                        menuDrawables.add(getResources().getDrawable(R.drawable.tagih_id));
                        break;
                    case "UPG":
                        menuStrings.add(getResources().getString(R.string.menu_item_title_upgrade_member));
                        menuDrawables.add(getResources().getDrawable(R.drawable.ic_upgrade));
                        break;
                    case "BIL":
                        menuStrings.add(getResources().getString(R.string.menu_item_title_buy));
                        menuDrawables.add(getResources().getDrawable(R.drawable.ic_belanja));

                        if (mBillerTypeDataPLS != null) {
                            menuStrings.add(getResources().getString(R.string.menu_item_title_pulsa_agent));
                            menuDrawables.add(getResources().getDrawable(R.drawable.ic_pulsa));
                        }

                        if (mBillerTypeDataDATA != null) {
                            menuStrings.add(getResources().getString(R.string.newhome_data));
                            menuDrawables.add(getResources().getDrawable(R.drawable.ic_paket_data));
                        }

                        if (mBillerTypeDataBPJS != null) {
                            menuStrings.add(getResources().getString(R.string.newhome_bpjs));
                            menuDrawables.add(getResources().getDrawable(R.drawable.ic_bpjs));
                        }

                        if (mBillerTypeDataTKN != null) {
                            menuStrings.add(getResources().getString(R.string.newhome_listrik_pln));
                            menuDrawables.add(getResources().getDrawable(R.drawable.ic_listrik_pln));
                        }

                        if (mBillerTypeDataEMoney != null) {
                            menuStrings.add(getResources().getString(R.string.newhome_emoney));
                            menuDrawables.add(getResources().getDrawable(R.drawable.ic_emoney));
                        }

                        if (mBillerTypeDataGame != null) {
                            menuStrings.add(getResources().getString(R.string.newhome_game));
                            menuDrawables.add(getResources().getDrawable(R.drawable.game));
                        }

                        if (mBillerTypeDataVoucher != null) {
                            menuStrings.add(getResources().getString(R.string.newhome_voucher));
                            menuDrawables.add(getResources().getDrawable(R.drawable.voucher));
                        }

                        if (mBillerTypeDataPDAM != null) {
                            menuStrings.add(getResources().getString(R.string.newhome_pam));
                            menuDrawables.add(getResources().getDrawable(R.drawable.ic_pdam));
                        }
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void checkSchemeCodeMember() {
        memberSchemeCode = sp.getString(DefineValue.CATEGORY, "");
        try {
            JSONArray arr = new JSONArray(memberSchemeCode);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String objs = obj.optString(WebParams.SCHEME_CODE, "");
                switch (objs) {
                    case "ATC":
                        menuStrings.add(getString(R.string.menu_item_search_agent_bbs) + " " + obj.optString(WebParams.CATEGORY_NAME));
                        menuDrawables.add(getResources().getDrawable(R.drawable.ic_tarik_tunai));
                        break;
                    case "CTA":
                        menuStrings.add(getString(R.string.menu_item_search_agent_bbs) + " " + obj.optString(WebParams.CATEGORY_NAME));
                        menuDrawables.add(getResources().getDrawable(R.drawable.ic_setor_tunai));
                        break;
                    case "BIL":
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean inputValidation() {
        if (input.getText().toString().length() == 0) {
            input.requestFocus();
            input.setError(getString(R.string.validation_pulsa));
            return false;
        }
        if (input.getText().toString().charAt(0) == ' ') {
            input.requestFocus();
            input.setError(getString(R.string.validation_pulsa));
            return false;
        }
        if (input.getText().toString().length() < 5) {
            input.requestFocus();
            input.setError(getString(R.string.validation_pulsa));
            return false;
        }
        return true;
    }

    private void switchMenu(int idx_menu, Bundle data) {
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchMenu(idx_menu, data);
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
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("receiver service balance");
            animateRefrestBtn(false);
            RefreshSaldo();

            String action = intent.getAction();
            if (action.equals(AgentShopService.INTENT_ACTION_AGENT_SHOP)) {

                if (!sp.getBoolean(DefineValue.IS_AGENT, false)) {
                    llAgentDetail.setVisibility(View.GONE);

                } else {
                    if (sp.getString(DefineValue.IS_AGENT_APPROVE, "").equals(DefineValue.STRING_YES)) {
                        llAgentDetail.setVisibility(View.VISIBLE);
                    } else {
                        llAgentDetail.setVisibility(View.GONE);
                    }

                }

                if (sp.getBoolean(DefineValue.IS_AGENT, false)) {
                    Timber.d("Receiver AgentShop " + sp.getString(DefineValue.AGENT_SHOP_CLOSED, ""));
                    swSettingOnline.setOnCheckedChangeListener(null);
                    if (sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO)) {
                        swSettingOnline.setChecked(true);
                    } else {
                        swSettingOnline.setChecked(false);
                    }
                    swSettingOnline.setOnCheckedChangeListener(switchListener);
                }
            }
        }
    };

    private BroadcastReceiver refBtnReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            animateRefrestBtn(true);
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if (sp.getBoolean(DefineValue.IS_AGENT, false)) {

            swSettingOnline.setOnCheckedChangeListener(null);
            if (sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO)) {
                swSettingOnline.setChecked(true);
            } else {
                swSettingOnline.setChecked(false);
            }
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

    private void switchFragment(Fragment i, String name) {
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchContent(i, name);
    }
}
