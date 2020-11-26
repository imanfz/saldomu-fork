package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_register_ebd.*
import org.json.JSONObject
import timber.log.Timber

class FragRegisterEBD : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_register_ebd, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        val tokoEBDActivity = activity as TokoEBDActivity
        tokoEBDActivity.initializeToolbar(getString(R.string.shop_registration))
        et_store_code.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_community_code.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_id_no.onRightDrawableRegisterEBDClicked { it.text.clear() }
        btn_submit.setOnClickListener { submitRegisterEBD() }
    }

    private fun submitRegisterEBD() {
        showProgressDialog()
        val memberCode = et_store_code.text.toString()
        val commCodeEspay = et_community_code.text.toString()
        val verificationId = et_id_no.text.toString()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REGISTER_EBD, memberCode + commCodeEspay)
        params[WebParams.MEMBER_CODE] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
        params[WebParams.VERIFICATION_ID] = verificationId
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.CUST_ID_ESPAY] = userPhoneID

        Timber.d("isi params register edb:%s", params.toString())
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REGISTER_EBD, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val code = response.getString(WebParams.ERROR_CODE)
                val message = response.getString(WebParams.ERROR_MESSAGE)
                when (code) {
                    WebParams.SUCCESS_CODE -> {
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                        fragmentManager!!.popBackStack()
                    }
                    WebParams.LOGOUT_CODE -> {
                        AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                    }
                    DefineValue.ERROR_9333 -> {
                        val model = gson.fromJson(response.toString(), jsonModel::class.java)
                        val appModel = model.app_data
                        AlertDialogUpdateApp.getInstance().showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                    }
                    DefineValue.ERROR_0066 -> {
                        AlertDialogMaintenance.getInstance().showDialogMaintenance(activity, message)
                    }
                    else -> {
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onError(throwable: Throwable?) {
                dismissProgressDialog()
            }

            override fun onComplete() {
                dismissProgressDialog()
            }

        })
    }
}

@SuppressLint("ClickableViewAccessibility")
fun EditText.onRightDrawableRegisterEBDClicked(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is EditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}