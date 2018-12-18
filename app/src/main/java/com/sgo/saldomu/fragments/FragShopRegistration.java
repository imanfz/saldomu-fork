package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.widgets.BaseFragment;

public class FragShopRegistration extends BaseFragment{

    View v;
    Bundle bundle;
    EditText et_address;
    Spinner sp_city;
    Button bt_regist, bt_back;
    String memberCode, commCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_regist_shop_location, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bundle = getArguments();
        Bundle bundle = getArguments();
        if (bundle != null) {
            memberCode = bundle.getString(DefineValue.MEMBER_CODE, "");
            commCode = bundle.getString(DefineValue.COMMUNITY_CODE, "");
        }

        et_address = v.findViewById(R.id.et_address);
        sp_city = v.findViewById(R.id.sp_city);
        bt_back = v.findViewById(R.id.btn_cancel);
        bt_regist = v.findViewById(R.id.bt_registTokoDGI);


    }
}
