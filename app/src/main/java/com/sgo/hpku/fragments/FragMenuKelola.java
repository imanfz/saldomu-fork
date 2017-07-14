package com.sgo.hpku.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.Beans.Denom_Data_Model;
import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.adapter.CustomTabPagerAdapter;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DateTimeFormat;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.HashMessage;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.RealmManager;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.entityRealm.MerchantCommunityList;
import com.sgo.hpku.models.ShopDetail;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;
import timber.log.Timber;

import static io.realm.Realm.getDefaultInstance;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragMenuKelola#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragMenuKelola extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String[] menuItems;
    SecurePreferences sp;
    ProgressDialog progdialog;
    private Realm myRealm;
    CustomTabPagerAdapter tabPageAdapter;
    ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    ViewPager viewPager;

    public FragMenuKelola() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragMenuKelola.
     */
    // TODO: Rename and change types and number of parameters
    public static FragMenuKelola newInstance(String param1, String param2) {
        FragMenuKelola fragment = new FragMenuKelola();
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

        myRealm                 = Realm.getDefaultInstance();
        sp                      = CustomSecurePref.getInstance().getmSecurePrefs();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.frag_menu_kelola, container, false);

        menuItems = getResources().getStringArray(R.array.list_menu_kelola);

        tabPageAdapter = new CustomTabPagerAdapter(getChildFragmentManager(), getContext(), menuItems, shopDetails);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        viewPager.setAdapter(tabPageAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        progdialog              = DefinedDialog.CreateProgressDialog(getContext(), "");

        String flagApprove      = DefineValue.STRING_YES;
        RequestParams params    = new RequestParams();
        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();
        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
        params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
        params.put(WebParams.FLAG_APPROVE, flagApprove);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID +
                sp.getString(DefineValue.USERID_PHONE, "") + BuildConfig.AppID + flagApprove));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.getMemberShopList(getContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progdialog.dismiss();

                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        JSONArray members = response.getJSONArray("member");

                        myRealm.beginTransaction();
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject object = members.getJSONObject(i);

                            myRealm.where(MerchantCommunityList.class).equalTo("memberId", object.getString("member_id"))
                                    .findAll().deleteFirstFromRealm();

                            MerchantCommunityList agentDetailModel = myRealm.createObject(MerchantCommunityList.class, object.getString("member_id") );
                            agentDetailModel.setMemberName( object.getString("member_name") );
                            agentDetailModel.setMemberCode(object.getString("member_code"));
                            agentDetailModel.setMemberType(object.getString("member_type"));
                            agentDetailModel.setCommName(object.getString("comm_name"));
                            agentDetailModel.setCommCode(object.getString("comm_code"));
                            agentDetailModel.setShopId(object.getString("shop_id"));
                            agentDetailModel.setShopName(object.getString("shop_name"));
                            agentDetailModel.setAddress1(object.getString("address1"));
                            agentDetailModel.setDistrict(object.getString("district"));
                            agentDetailModel.setProvince(object.getString("province"));
                            agentDetailModel.setCountry(object.getString("country"));
                            agentDetailModel.setMemberCust(sp.getString(DefineValue.USERID_PHONE, ""));

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
                            shopDetails.add(shopDetail);


                        }
                        myRealm.commitTransaction();

                        viewPager.getAdapter().notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                ifFailure(throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                ifFailure(throwable);
            }

            private void ifFailure(Throwable throwable) {
                //if (MyApiClient.PROD_FAILURE_FLAG)
                //Toast.makeText(getApplication(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                //else
                Toast.makeText(getContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                progdialog.dismiss();
                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });

        // Inflate the layout for this fragment
        return v;
    }

}
