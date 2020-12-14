package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_input_store_code.*
import org.json.JSONObject
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
            et_store_code.error = getString(R.string.store_code_validation)
            return false
        }
        return true
    }

    private fun submitRegMember() {
        showProgressDialog()
        val memberCode = et_store_code.text.toString()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REGISTER_EBD, memberCode)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.MEMBER_CODE] = memberCode
        params[WebParams.LATITUDE] = sp.getDouble(DefineValue.LATITUDE_UPDATED, 0.0)
        params[WebParams.LONGITUDE] = sp.getDouble(DefineValue.LONGITUDE_UPDATED, 0.0)
        Timber.d("isi params register community:%s", params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REGISTER_EBD, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                showDialog()
            }

            override fun onError(throwable: Throwable?) {
                dismissProgressDialog()
            }

            override fun onComplete() {
                dismissProgressDialog()
            }

        })
    }

    @SuppressLint("SetTextI18n")
    private fun showDialog() {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = resources.getString(R.string.remark)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = getString(R.string.join_community) + " " + getString(R.string.success)

        dialog.btn_dialog_notification_ok.setOnClickListener {
            dialog.dismiss()
            fragmentManager!!.popBackStack()
        }
        dialog.show()
    }
}