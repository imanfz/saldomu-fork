package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.BillerActivity
import com.sgo.saldomu.activities.CanvasserGoodReceiptActivity
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
import kotlinx.android.synthetic.main.frag_input_store_code.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragInputStoreCode : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_input_store_code, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        iv_clear.setOnClickListener(View.OnClickListener { v: View? -> et_store_code.setText("") })

        btn_submit.setOnClickListener {
            if (inputValidation()) {
                inquiryDocList()
            }
        }
    }

    fun inputValidation(): Boolean {
        if (et_store_code == null || et_store_code.getText().toString().isEmpty()) {
            et_store_code.requestFocus()
            et_store_code.error = getString(R.string.store_code_validation)
            return false
        }
        return true
    }

    fun inquiryDocList() {
        try {
            showProgressDialog()
            extraSignature = et_store_code!!.text.toString()
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_LIST, extraSignature)
            params[WebParams.COMM_ID] = commIDLogin
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.MEMBER_ID] = memberIDLogin
            params[WebParams.CUST_ID_ESPAY] = userPhoneID
            params[WebParams.MEMBER_CODE_ESPAY] = et_store_code!!.text.toString()
            params[WebParams.TYPE_ID] = DefineValue.PO
            Timber.d("params inquiry doc list:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_LIST, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response inquiry doc list:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        val bundle = Bundle()
                                        bundle.putString(DefineValue.DOC_LIST, response.optString(WebParams.DOC_LIST))
                                        val frag: Fragment = FragListPOfromGR()
                                        frag.setArguments(bundle)

                                        switchFragment(frag,"","",true, "")
                                    }
                                    WebParams.LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
                                        val message = response.getString(WebParams.ERROR_MESSAGE)
                                        val test = AlertDialogLogout.getInstance()
                                        test.showDialoginActivity(activity, message)
                                    }
                                    DefineValue.ERROR_9333 -> {
                                        Timber.d("isi response app data:%s", model.app_data)
                                        val appModel = model.app_data
                                        val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                        alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                    }
                                    DefineValue.ERROR_0066 -> {
                                        Timber.d("isi response maintenance:$response")
                                        val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                        alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                                    }
                                    else -> {
                                        Timber.d("isi error inquiry doc list:$response")
                                        Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {}
                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: java.lang.Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserGoodReceiptActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }
}

