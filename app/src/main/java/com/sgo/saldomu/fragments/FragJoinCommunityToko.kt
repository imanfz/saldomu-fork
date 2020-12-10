package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_input_store_code.*
import timber.log.Timber

class FragJoinCommunityToko : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_input_store_code, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        val tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity.initializeToolbar(getString(R.string.join_community))

        iv_clear.setOnClickListener { et_store_code.setText("") }
        btn_submit.setOnClickListener {
            if (inputValidation())
                submitRegMember()
        }
    }

    private fun inputValidation(): Boolean {
        if (et_store_code.text.isEmpty()) {
            et_store_code.requestFocus()
            et_store_code.error = getString(R.string.store_phone_vaidation)
            return false
        }
        return true
    }

    private fun submitRegMember() {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REGISTER_EBD)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.MEMBER_CODE] = et_store_code.text.toString()
        params[WebParams.LATITUDE] = sp.getDouble(DefineValue.LATITUDE_UPDATED, 0.0)
        params[WebParams.LONGITUDE] = sp.getDouble(DefineValue.LONGITUDE_UPDATED, 0.0)
        Timber.d("isi params register community:%s", params.toString())
    }
}