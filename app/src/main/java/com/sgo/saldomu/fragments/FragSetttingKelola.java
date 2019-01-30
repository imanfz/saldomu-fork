package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsMemberLocationActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.TutorialActivity;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.ShopDetail;
import com.sgo.saldomu.services.AgentShopService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragSetttingKelola.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragSetttingKelola#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragSetttingKelola extends Fragment implements View.OnClickListener {
    public final static String TAG = "com.sgo.saldomu.fragments.Frag_Settting_Kelola";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    String[] _data;
    ArrayList<String> menu;
    ListView lvSetting;
    String shopId, memberId, shopName, memberType, category, agentName, commName, province, district, address, stepApprove, shopClosed, isMobility;
    ProgressDialog progdialog, progdialog2;
    String flagApprove, shopStatus;
    SecurePreferences sp;
    ArrayList<ShopDetail> shopDetails = new ArrayList<>();

    TextView tvDetailMemberName, tvCategoryName, tvCommName, tvAddress, tvTutupSekarangLabel;
    View llMemberDetail;
    Button btnSettingLokasi;
    Switch swTutupToko;
    ArrayList<String> selectedDates = new ArrayList<>();
    EasyAdapter lvSettingAdapter;
    String[] menuItems;
    FragmentManager fragmentManager;
    private static final int RC_GPS_REQUEST = 1;


    public FragSetttingKelola() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragSetttingKelola.
     */
    // TODO: Rename and change types and number of parameters
    public static FragSetttingKelola newInstance(String param1, String param2) {
        FragSetttingKelola fragment = new FragSetttingKelola();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        sp                      = CustomSecurePref.getInstance().getmSecurePrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.frag_settting_kelola, container, false);

        menuItems               = getResources().getStringArray(R.array.list_bbs_setting);

        lvSettingAdapter        = new EasyAdapter(getActivity(),R.layout.list_view_item_setting_with_arrow, menuItems);

        lvSetting               = v.findViewById(R.id.lvSetting);
        lvSetting.setOverscrollFooter(new ColorDrawable(Color.TRANSPARENT));
        lvSetting.setVisibility(View.GONE);

        llMemberDetail          =  v.findViewById(R.id.llMemberDetail);
        llMemberDetail.setVisibility(View.GONE);

        btnSettingLokasi        = v.findViewById(R.id.btnSettingLokasi);
        btnSettingLokasi.setVisibility(View.GONE);
        tvDetailMemberName      = v.findViewById(R.id.tvDetailMemberName);
        tvCategoryName          = v.findViewById(R.id.tvCategoryName);
        tvCommName              = v.findViewById(R.id.tvCommName);
        tvAddress               = v.findViewById(R.id.tvAddress);
        tvTutupSekarangLabel    = v.findViewById(R.id.tvTutupSekarangLabel);
        tvTutupSekarangLabel.setVisibility(View.GONE);

        swTutupToko             = v.findViewById(R.id.swTutupToko);

        lvSetting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tag = "";
                Fragment newFragment = null;
                String  menuName = ((TextView) view.findViewById(R.id.txtTitleList)).getText().toString();
                fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        InitializeTitle();
                    }
                });
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                if ( menuName.equals(getString(R.string.setup_agent_open_hour)) ) {
                    newFragment = new FragWaktuBeroperasi();
                    tag = FragWaktuBeroperasi.TAG;
                    setActionBarTitle(getString(R.string.setup_agent_open_hour));
                } else if ( menuName.equals(getString(R.string.setup_tutup_manual)) ) {
                    newFragment = new FragTutupManual();
                    tag = FragTutupManual.TAG;
                    setActionBarTitle(getString(R.string.setup_tutup_manual));
                }

                fragmentTransaction.replace(R.id.bbs_content, newFragment, tag).addToBackStack(tag);
                fragmentTransaction.commitAllowingStateLoss();


            }
        });

        btnSettingLokasi.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), BbsMemberLocationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("memberId", memberId);
                    intent.putExtra("shopId", shopId);
                    intent.putExtra("shopName", shopName);
                    intent.putExtra("memberType", memberType);
                    intent.putExtra("memberName", agentName);
                    intent.putExtra("commName", commName);
                    intent.putExtra("province", province);
                    intent.putExtra("district", district);
                    intent.putExtra("address", address);
                    intent.putExtra("category", category);
                    intent.putExtra("isMobility", isMobility);
                    getActivity().startActivityForResult(intent, MainPage.REQUEST_FINISH, null);
                    //getActivity().finish();

                }
            }
        );


        swTutupToko.setOnCheckedChangeListener (null);

        flagApprove             = DefineValue.STRING_BOTH;
        progdialog              = DefinedDialog.CreateProgressDialog(getContext(), "");

        String extraSignature = flagApprove;

        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_MEMBER_SHOP_LIST, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
        params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
        params.put(WebParams.FLAG_APPROVE, flagApprove);
        params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_MEMBER_SHOP_LIST, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            String code = response.getString(WebParams.ERROR_CODE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                llMemberDetail.setVisibility(View.VISIBLE);

                                JSONArray members = response.getJSONArray("member");

                                for (int i = 0; i < members.length(); i++) {
                                    JSONObject object = members.getJSONObject(i);

                                    ShopDetail shopDetail = new ShopDetail();
                                    shopDetail.setMemberId(object.getString("member_id"));
                                    shopDetail.setMemberCode(object.getString("member_code"));
                                    shopDetail.setMemberName(object.getString("member_name"));
                                    shopDetail.setMemberType(object.getString("member_type"));
                                    shopDetail.setCommName(object.getString("comm_name"));
                                    shopDetail.setCommCode(object.getString("comm_code"));
                                    shopDetail.setShopId(object.getString("shop_id"));
                                    shopDetail.setShopName(object.getString("shop_name"));
                                    shopDetail.setShopFirstAddress(object.getString("address1"));
                                    shopDetail.setShopDistrict(object.getString("district"));
                                    shopDetail.setShopProvince(object.getString("province"));
                                    shopDetail.setShopCountry(object.getString("country"));
                                    shopDetail.setStepApprove(object.getString("step_approve"));
                                    shopDetail.setSetupOpenHour(object.getString("setup_open_hour"));
                                    shopDetail.setIsMobility(object.getString("is_mobility"));

                                    memberId    = shopDetail.getMemberId();
                                    shopId      = shopDetail.getShopId();
                                    shopName      = shopDetail.getShopName();
                                    memberType      = shopDetail.getMemberType();
                                    agentName = object.getString("member_name");
                                    stepApprove = object.getString("step_approve");
                                    commName      = shopDetail.getCommName();
                                    province = shopDetail.getShopProvince();
                                    district = shopDetail.getShopDistrict();
                                    address = shopDetail.getShopFirstAddress();
                                    isMobility  = object.getString("is_mobility");

                                    if ( !object.getString("category").equals("") ) {
                                        JSONArray categories = object.getJSONArray("category");

                                        for (int j = 0; j < categories.length(); j++) {
                                            JSONObject object2 = categories.getJSONObject(j);
                                            shopDetail.setCategories(object2.getString("category_name"));
                                        }
                                        category = TextUtils.join(", ", shopDetail.getCategories());
                                    } else {
                                        category = "";
                                    }

                                    shopDetails.add(shopDetail);

                                    shopClosed = object.getString("shop_closed");
                                    tvDetailMemberName.setText(object.getString("member_name"));
                                    tvCategoryName.setText(category);
                                    tvCommName.setText(object.getString("shop_name"));
                                    tvAddress.setText(object.getString("address1"));

                                    if ( isMobility.equals(DefineValue.STRING_NO) && shopDetail.getStepApprove().equals(DefineValue.STRING_YES) && shopDetail.getSetupOpenHour().equals(DefineValue.STRING_NO) ) {

                                        FragWaktuBeroperasi fragWaktuBeroperasi = new FragWaktuBeroperasi();
                                        FragmentManager fragmentManager = getFragmentManager();
                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction.replace(R.id.bbs_content, fragWaktuBeroperasi, null);

                                        if ( getActivity() != null ) {
                                            BBSActivity bbc = (BBSActivity) getActivity();

                                            TextView title_detoolbar = getActivity().findViewById(R.id.main_toolbar_title);
                                            title_detoolbar.setText(getString(R.string.menu_item_title_waktu_beroperasi));
                                        }
                                        fragmentTransaction.commit();
                                    }

                                }

                                if ( stepApprove.equals(DefineValue.STRING_NO) ) {
                                    btnSettingLokasi.setVisibility(View.VISIBLE);
                                    tvTutupSekarangLabel.setVisibility(View.GONE);
                                    swTutupToko.setVisibility(View.GONE);
                                    lvSetting.setVisibility(View.GONE);
                                } else {
                                    btnSettingLokasi.setVisibility(View.GONE);
                                    tvTutupSekarangLabel.setVisibility(View.VISIBLE);
                                    lvSetting.setVisibility(View.VISIBLE);
                                    lvSetting.setAdapter(lvSettingAdapter);

                                    if ( shopClosed.equals(DefineValue.STRING_YES) ) {
                                        swTutupToko.setChecked(false);
                                    } else {
                                        swTutupToko.setChecked(true);
                                    }

                                    swTutupToko.setOnCheckedChangeListener(switchListener);
                                    swTutupToko.setVisibility(View.VISIBLE);
                                }

                        /*for(int i =0; i <= (_data.length-1); i++) {
                            String temp = _data[i];

                            if ( i == 0 ) {
                                temp += " : " + agentName;
                            } else if ( i == 1 ) {
                                temp += " : " + category;
                            }

                            if ( i == 2 ) {
                                if (stepApprove.equals(DefineValue.STRING_NO)) {
                                    menu.add(temp);
                                }
                            } else if ( i == 3 ) {
                                if (stepApprove.equals(DefineValue.STRING_YES)) {
                                    menu.add(temp);
                                }
                            } else {
                                menu.add(temp);
                            }
                        }
*/
                                //listSettingAdapter = new ListSettingAdapter(BbsListSettingKelolaActivity.this, menu, flagApprove, shopDetails);
                                //lvList.setAdapter(listSettingAdapter);

                            } else {

                                //redirect back to fragment - BBSActivity;
                                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext()).create();
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.setTitle(getString(R.string.alertbox_title_information));
                                alertDialog.setCancelable(false);

                                alertDialog.setMessage(getString(R.string.message_notif_not_registered_agent));



                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                getActivity().finish();
                                    /*FragmentManager fm = getFragmentManager();
                                    if (fm.getBackStackEntryCount() > 0) {
                                        fm.popBackStack();
                                    } else {

                                    }*/
                                            }
                                        });

                                alertDialog.show();

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

                        progdialog.dismiss();
                    }
                });

//        memberId        = getIntent().getStringExtra("memberId");
//        shopId          = getIntent().getStringExtra("shopId");
//        shopName        = getIntent().getStringExtra("shopName");
//        memberType      = getIntent().getStringExtra("memberType");
//        category        = getIntent().getStringExtra("category");
//        agentName       = getIntent().getStringExtra("memberName");
//        commName        = getIntent().getStringExtra("commName");
//        province        = getIntent().getStringExtra("province");
//        district        = getIntent().getStringExtra("district");
//        address         = getIntent().getStringExtra("address");

        // Inflate the layout for this fragment
        return v;
    }

    protected void setActionBarTitle(String _title) {

        TextView title_detoolbar = getActivity().findViewById(R.id.main_toolbar_title);
        title_detoolbar.setText(_title);
    }

    private void InitializeTitle(){
        Fragment fragment = fragmentManager.findFragmentById(R.id.bbs_content);
        if(fragment instanceof FragWaktuBeroperasi) {
            setActionBarTitle(getString(R.string.setup_agent_open_hour));
        }else if(fragment instanceof FragTutupManual)
            setActionBarTitle(getString(R.string.setup_tutup_manual));

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                showTutorial();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        validasiTutorial();
    }

    private void validasiTutorial()
    {
        if(sp.contains(DefineValue.TUTORIAL_KELOLA_AGENT))
        {
            Boolean is_first_time = sp.getBoolean(DefineValue.TUTORIAL_KELOLA_AGENT,false);
            if(is_first_time)
                showTutorial();
        }
        else {
            showTutorial();
        }
    }

    private void showTutorial()
    {
        Intent intent = new Intent(getActivity(), TutorialActivity.class);
        intent.putExtra(DefineValue.TYPE, TutorialActivity.tutorial_kelola_agent);
        startActivity(intent);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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

                        swTutupToko.setOnClickListener(null);
                        swTutupToko.setChecked(false);
                        swTutupToko.setOnCheckedChangeListener(switchListener);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    Switch.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            shopStatus              = DefineValue.SHOP_OPEN;
            Boolean isCallWebservice    = false;

            if (!isChecked) {
                //buka
                shopStatus          = DefineValue.SHOP_CLOSE;

            }

            if (shopStatus.equals(DefineValue.SHOP_OPEN)) {
                if ( !sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_NO) ) {
                    isCallWebservice    = true;
                }

            } else {
                if ( !sp.getString(DefineValue.AGENT_SHOP_CLOSED, "").equals(DefineValue.STRING_YES) ) {
                    isCallWebservice    = true;
                }
            }

            String extraSignature   = sp.getString(DefineValue.BBS_MEMBER_ID, "") + sp.getString(DefineValue.BBS_SHOP_ID, "");

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY, extraSignature);

            if ( !GlobalSetting.isLocationEnabled(getActivity()) && shopStatus.equals(DefineValue.SHOP_OPEN) ) {
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

                                            getActivity().setResult(MainPage.RESULT_REFRESH_NAVDRAW);

                                            Intent i = new Intent(AgentShopService.INTENT_ACTION_AGENT_SHOP);
                                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);

                                        } else {

                                            Toast.makeText(getContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
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
                                    progdialog2.dismiss();
                                }
                            });
                }
            }
        }
    };
}
