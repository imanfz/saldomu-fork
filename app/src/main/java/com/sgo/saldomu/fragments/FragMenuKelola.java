package com.sgo.saldomu.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.CustomTabPagerAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.ShopDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FragMenuKelola extends Fragment {
    SecurePreferences sp;
    ProgressDialog progDialog;
    CustomTabPagerAdapter tabPageAdapter;
    ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    ViewPager viewPager;

    public FragMenuKelola() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.frag_menu_kelola, container, false);

        tabPageAdapter = new CustomTabPagerAdapter(getChildFragmentManager(), shopDetails);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        viewPager.setAdapter(tabPageAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        progDialog = DefinedDialog.CreateProgressDialog(getContext(), "");

        String flagApprove = DefineValue.STRING_YES;

        String extraSignature = flagApprove;

        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_MEMBER_SHOP_LIST, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
        params.put(WebParams.FLAG_APPROVE, flagApprove);
        params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_MEMBER_SHOP_LIST, params, new ObjListeners() {
            @Override
            public void onResponses(JSONObject response) {
                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        JSONArray members = response.getJSONArray(WebParams.MEMBER);

                        //myRealm.beginTransaction();
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject object = members.getJSONObject(i);

                            ShopDetail shopDetail = new ShopDetail();
                            shopDetail.setMemberId(object.getString(WebParams.MEMBER_ID));
                            shopDetail.setMemberCode(object.getString(WebParams.MEMBER_CODE));
                            shopDetail.setMemberName(object.getString(WebParams.MEMBER_NAME));
                            shopDetail.setMemberType(object.getString(WebParams.MEMBER_TYPE));
                            shopDetail.setCommName(object.getString(WebParams.COMM_NAME));
                            shopDetail.setCommCode(object.getString(WebParams.COMM_CODE));
                            shopDetail.setShopId(object.getString(WebParams.SHOP_ID));
                            shopDetail.setShopName(object.getString(WebParams.SHOP_NAME));
                            shopDetail.setShopFirstAddress(object.getString(WebParams.ADDRESS1));
                            shopDetail.setShopDistrict(object.getString(WebParams.DISTRICT));
                            shopDetail.setShopProvince(object.getString(WebParams.PROVINCE));
                            shopDetail.setShopCountry(object.getString(WebParams.COUNTRY));

                            JSONArray categories = object.getJSONArray(WebParams.CATEGORY);

                            for (int j = 0; j < categories.length(); j++) {
                                JSONObject object2 = categories.getJSONObject(j);
                                shopDetail.setCategories(object2.getString(WebParams.CATEGORY));
                            }

                            shopDetails.add(shopDetail);


                        }
                        //myRealm.commitTransaction();

                        viewPager.getAdapter().notifyDataSetChanged();
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
                progDialog.dismiss();
            }
        });

        // Inflate the layout for this fragment
        return v;
    }

}
