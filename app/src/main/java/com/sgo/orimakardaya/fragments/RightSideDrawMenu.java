package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.PromoObject;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.activities.SocialPromoActivity;
import com.sgo.orimakardaya.adapter.PromoAdapter;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import timber.log.Timber;

/*
  Created by Administrator on 4/16/2015.
 */
public class RightSideDrawMenu extends Fragment {

    PtrFrameLayout ptrFrameLayout;
    ListView lvPromo;
    ImageView title;
    ArrayList<PromoObject> listPromo;

    SecurePreferences sp;
    String _ownerID,accessKey;
    int page = 0;
    String count = "5";

    PromoAdapter promoAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_right_side_drawer_menu, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        ptrFrameLayout = (PtrFrameLayout) getActivity().findViewById(R.id.promo_ptr_frame);
        lvPromo = (ListView) getActivity().findViewById(R.id.lvPromo);
        title = (ImageView) getActivity().findViewById(R.id.title);

        title.setImageResource(R.drawable.socialpromo_icon_bar);

        listPromo = new ArrayList<PromoObject>();

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
        header.initWithString("Updating...");

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
                return !canScrollUp(((ListView) content)); // or cast with ListView
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

    public void autoRefreshList(){
        if(ptrFrameLayout != null)
            ptrFrameLayout.autoRefresh();
    }

    public void getPromoList() {
        try {

            RequestParams params =  MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_PROMO_LIST,
                    _ownerID,accessKey);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.PAGE, Integer.toString(page));
            params.put(WebParams.COUNT, count);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get promo list:" + params.toString());

            MyApiClient.getPromoList(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);

                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params promo list:"+response.toString());
                            String count = response.getString(WebParams.COUNT);
                            if(count.equals("0")) {
                            }
                            else {
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

                                    if (flagSame == false) {
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
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout", response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);

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

                private void failure(Throwable throwable){
                    if(getActivity() != null) {
                        if (MyApiClient.PROD_FAILURE_FLAG)
                            Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    }
                    Timber.w("Error Koneksi promo list promo righside:"+throwable.toString());
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }
}