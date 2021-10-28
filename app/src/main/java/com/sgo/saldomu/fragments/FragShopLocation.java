package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sgo.saldomu.Beans.CustomAdapterModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MapsActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class FragShopLocation extends BaseFragment {

    View v;
    EditText et_address;
    Button bt_regist, bt_back;
    TextView setCoordinate, codeStore, commNameText, changeLoc;
    LinearLayout linearLayoutSetLocation;
    AutoCompleteTextView cityLocField;
    ArrayAdapter<String> adapters;

    String memberCode, commCode, commName;
    List<CustomAdapterModel> locList;
    List<String> locLists;
    Double latitude, longitude;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_regist_shop_location, container, false);

        setCoordinate = v.findViewById(R.id.regis_shop_showmap);
        codeStore = v.findViewById(R.id.regis_shop_store_code);
        cityLocField = v.findViewById(R.id.get_shop_location_list);
        commNameText = v.findViewById(R.id.regis_shop_community);
        changeLoc = v.findViewById(R.id.regis_shop_change_location);

        et_address = v.findViewById(R.id.et_address);
        bt_back = v.findViewById(R.id.btn_cancel);
        bt_regist = v.findViewById(R.id.btn_shop_register);
        linearLayoutSetLocation = v.findViewById(R.id.ll_setLocation);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            memberCode = bundle.getString(DefineValue.MEMBER_CODE, "");
            commCode = bundle.getString(DefineValue.COMMUNITY_CODE, "");
            commName = bundle.getString(DefineValue.COMMUNITY_NAME, "");

            codeStore.setText(memberCode);
            commNameText.setText(commName);
        }

        locList = new ArrayList<>();
        locLists = new ArrayList<>();
        adapters = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, locLists);
        cityLocField.setAdapter(adapters);
//        cityLocField.setDropDownBackgroundResource(R.color.white);
        cityLocField.setThreshold(2);

        Realm realm = Realm.getInstance(RealmManager.BBSConfiguration);
        RealmResults<List_BBS_Birth_Place> results = realm.where(List_BBS_Birth_Place.class).findAll();
        List<List_BBS_Birth_Place> list_bbs_birth_place = new ArrayList<>(realm.copyFromRealm(results));

        for (List_BBS_Birth_Place model : list_bbs_birth_place
        ) {
            locList.add(new CustomAdapterModel(model));
            locLists.add(model.getBirthPlace_city());
        }

        adapters.notifyDataSetChanged();

        final View.OnClickListener openMap = v -> startActivityForResult(new Intent(getActivity(), MapsActivity.class), 100);
        linearLayoutSetLocation.setOnClickListener(openMap);

        changeLoc.setOnClickListener(openMap);

        bt_regist.setOnClickListener(v -> {
            if (checkInput()) {
                setMemberLocation();
            }
        });

        bt_back.setOnClickListener(v -> getFragmentManager().popBackStack());

    }

    boolean checkInput() {
        if (et_address.getText().toString().trim().length() == 0) {
            et_address.setError("Alamat kosong");
            et_address.requestFocus();
            return false;
        } else if (!locLists.contains(cityLocField.getText().toString())) {
            cityLocField.requestFocus();
            cityLocField.setError("Nama kota tidak ditemukan!");
            return false;
        } else if (cityLocField.getText().toString().trim().length() == 0) {
            cityLocField.setError("Kota kosong");
            cityLocField.requestFocus();
            return false;
        } else if (setCoordinate.getText().toString().equalsIgnoreCase("Koordinat belum terpasang")) {
            Toast.makeText(getActivity(), "Koordinat letak belum siap", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void setMemberLocation() {
        try {
            showProgressDialog();
            extraSignature = commCode + memberCode;

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_SET_MEMBER_LOC, extraSignature);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_CODE, memberCode);
            params.put(WebParams.ADDRESS, et_address.getText().toString());
            params.put(WebParams.LATITUDE, latitude);
            params.put(WebParams.LONGITUDE, longitude);
            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
            params.put(WebParams.CITY, cityLocField.getText().toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_SET_MEMBER_LOC, params,
                    new ObjListeners() {

                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("Isi response getBalance Collector:%s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Toast.makeText(getActivity(), "Sukses memperbaharui lokasi", Toast.LENGTH_SHORT).show();
                                    getFragmentManager().popBackStack();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", response.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                                } else {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
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
                            dismissProgressDialog();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        switch (requestCode) {
            case 100:
                if (resultCode == 201) {
                    if (data != null && data.getExtras() != null) {
                        String address = data.getStringExtra("address");
                        changeLoc.setVisibility(View.VISIBLE);
                        setCoordinate.setVisibility(View.VISIBLE);
                        setCoordinate.setText(address);
                        linearLayoutSetLocation.setVisibility(View.GONE);
                        longitude = data.getDoubleExtra("longitude", 0);
                        latitude = data.getDoubleExtra("latitude", 0);
                    }
                }
                break;
        }
    }

}
