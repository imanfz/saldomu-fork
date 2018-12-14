package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.activeandroid.util.Log;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.TagihModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.TagihActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.TagihCommunityModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class FragTagihInput extends BaseFragment {
    Spinner sp_mitra, sp_communtiy;
    SecurePreferences sp;
    EditText et_memberCode;
    Button btn_submit;
    View v;
    private ArrayAdapter<String> mitraAdapter;
    private ArrayList<String> mitraNameArrayList = new ArrayList<>();
    private ArrayList<TagihModel> mitraNameData = new ArrayList<>();
    private ArrayAdapter<String> communityAdapter;
    private ArrayList<String> communityNameArrayList = new ArrayList<>();
    String commCodeTagih;
    ProgressDialog progdialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_tagih_input, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getSecurePrefsInstance();
        initiatizeView();

        InitializeData();

        btn_submit.setOnClickListener(submitListener);
    }

    Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (inputValidation()) {
                sendDataTagih();
            }
        }
    };

    private void initiatizeView() {
        sp_mitra = v.findViewById(R.id.sp_mitra);
        sp_communtiy = v.findViewById(R.id.sp_community);
        et_memberCode = v.findViewById(R.id.et_memberCode);
        btn_submit = v.findViewById(R.id.btn_submit);

        mitraAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, mitraNameArrayList);
        sp_mitra.setAdapter(mitraAdapter);
    }

    public void InitializeData() {
        Realm _realm = RealmManager.getRealmTagih();
        RealmResults<TagihModel> list = _realm.where(TagihModel.class).findAll();
        mitraNameData.addAll(list);

        for (int i = 0; i < list.size(); i++) {
            mitraNameArrayList.add(list.get(i).getAnchor_name());
        }
        mitraAdapter.notifyDataSetChanged();

        communityAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, communityNameArrayList);
        sp_communtiy.setAdapter(communityAdapter);

        sp_mitra.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initializeCommunity(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void initializeCommunity(int pos) {
        Realm _realm = RealmManager.getRealmTagih();
        final ArrayList<TagihCommunityModel> listTagih = new ArrayList<>();
        listTagih.addAll(mitraNameData.get(pos).getListCommunity());
//                _realm.where(TagihCommunityModel.class).findAll();
        Log.d("mainpage", "id : " + listTagih.get(0).getId());

        communityNameArrayList.clear();

        for (int i = 0; i < listTagih.size(); i++) {
            communityNameArrayList.add(listTagih.get(i).getComm_name());
            Timber.d("comm code tagih : "+listTagih.get(i).getComm_code());
        }
        communityAdapter.notifyDataSetChanged();

        if(listTagih != null && listTagih.size() > 0){
            commCodeTagih = listTagih.get(0).getComm_code();
        }else
            commCodeTagih ="";


        sp_communtiy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                commCodeTagih = listTagih.get(position).getComm_code();

                Timber.d("TEST comm code tagih selected: "+listTagih.get(position).getComm_code()+" pos:"+position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    public Boolean inputValidation() {
        if (et_memberCode.getText().toString().equalsIgnoreCase("")) {
            et_memberCode.requestFocus();
            et_memberCode.setError(getString(R.string.error_input_tagih));
            return false;
        }
            return true;
    }

    public void sendDataTagih() {
        showProgressDialog();

        String extraSignature = commCodeTagih + et_memberCode.getText().toString();
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LIST_INVOICE_DGI, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.MEMBER_CODE, et_memberCode.getText().toString());
        params.put(WebParams.COMM_CODE,commCodeTagih);
        params.put(WebParams.USER_ID, userPhoneID);
        Timber.d("params list invoice DGI : " + params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_LIST_INVOICE_DGI, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            dismissProgressDialog();

                            Timber.d("response list invoice DGI : " + response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String error_message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                String responseListInvoice = response.toString();
                                Fragment newFrag = new FragListInvoiceTagih();
                                Bundle bundle = new Bundle();
                                bundle.putString(DefineValue.MEMBER_CODE, et_memberCode.getText().toString());
                                bundle.putString(DefineValue.COMMUNITY_CODE, commCodeTagih);
                                bundle.putString(DefineValue.RESPONSE, responseListInvoice);

                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.COMM_CODE_DGI, response.getString(WebParams.COMM_CODE_DGI));
                                mEditor.apply();

                                newFrag.setArguments(bundle);
                                if(getActivity() == null){
                                    return;
                                }
                                TagihActivity ftf = (TagihActivity) getActivity();
                                ftf.switchContent(newFrag,"List Invoice",true);
                            } else {
                                Toast.makeText(getActivity(), error_message, Toast.LENGTH_LONG).show();
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
    }


}