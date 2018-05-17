package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.PromoObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SocialPromoActivity;
import com.sgo.saldomu.adapter.PromoAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;
import timber.log.Timber;

/*
  Created by Administrator on 4/16/2015.
 */
public class RightSideDrawMenu extends Fragment {

    private PtrFrameLayout ptrFrameLayout;
    private ArrayList<PromoObject> listPromo;

    private SecurePreferences sp;
    private String _ownerID;
    private String accessKey;
    private int page = 0;
    private String count = "5";

    private PromoAdapter promoAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_right_side_drawer_menu, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        ptrFrameLayout = (PtrFrameLayout) getActivity().findViewById(R.id.promo_ptr_frame);
        ListView lvPromo = (ListView) getActivity().findViewById(R.id.lvPromo);
        ImageView title = (ImageView) getActivity().findViewById(R.id.title);

        title.setImageResource(R.drawable.socialpromo_icon_bar);

        listPromo = new ArrayList<>();

        promoAdapter = new PromoAdapter(getActivity().getApplicationContext(), listPromo);
        lvPromo.setAdapter(promoAdapter);

        lvPromo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity().getApplicationContext(), SocialPromoActivity.class);
                i.putExtra("target_url", listPromo.get(position).getUrl());
                switchActivity(i);
            }
        });

        StoreHouseHeader header = new StoreHouseHeader(getActivity().getApplicationContext());
        header.setPadding(0, 20, 0, 20);
        header.setTextColor(Color.BLACK);
        header.initWithString(getString(R.string.updating));

        ptrFrameLayout.setDurationToCloseHeader(1500);
        ptrFrameLayout.setHeaderView(header);
        ptrFrameLayout.addPtrUIHandler(header);
        ptrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        page++;
                        getPromoList();
                        ptrFrameLayout.refreshComplete();
                    }
                }, 1800);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
//                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
                return !canScrollUp(content); // or cast with ListView
            }

            public boolean canScrollUp(View view) {
                if (android.os.Build.VERSION.SDK_INT < 14) {
                    if (view instanceof AbsListView) {
                        final AbsListView absListView = (AbsListView) view;
                        return absListView.getChildCount() > 0
                                && (absListView.getFirstVisiblePosition() > 0 || absListView
                                .getChildAt(0).getTop() < absListView.getPaddingTop());
                    } else {
                        return view.getScrollY() > 0;
                    }
                } else {
                    return ViewCompat.canScrollVertically(view, -1);
                }
            }

        });

    }

    public void autoRefreshList() {
        if (ptrFrameLayout != null)
            ptrFrameLayout.autoRefresh();
    }

    private void getPromoList() {
        try {

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_PROMO_LIST,
                    _ownerID, accessKey);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.PAGE, Integer.toString(page));
            params.put(WebParams.COUNT, count);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get promo list:" + params.toString());

            MyApiClient.getPromoList(getActivity(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
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
                                    if (listPromo.size() > 0) {
                                        for (int index = 0; index < listPromo.size(); index++) {
                                            if (listPromo.get(index).getId().equals(id)) {
                                                flagSame = true;
                                                break;
                                            } else {
                                                flagSame = false;
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

                                promoAdapter.notifyDataSetChanged();
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
                    if (getActivity() != null) {
                        if (MyApiClient.PROD_FAILURE_FLAG)
                            Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    }
                    Timber.w("Error Koneksi promo list promo righside:" + throwable.toString());
                }
            });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void switchActivity(Intent mIntent) {
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }
}