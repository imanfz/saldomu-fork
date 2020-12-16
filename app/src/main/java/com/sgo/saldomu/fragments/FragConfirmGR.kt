package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CurrencyFormat
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
import kotlinx.android.synthetic.main.confirm_create_gr.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragConfirmGR : BaseFragment() {
    var memberCodeEspay : String =""
    var commCodeEspay : String =""
    var custIdEspay : String =""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.confirm_create_gr, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val bundle = arguments

        memberCodeEspay = bundle!!.getString(DefineValue.MEMBER_CODE_ESPAY,"")
        commCodeEspay = bundle!!.getString(DefineValue.COMMUNITY_CODE_ESPAY,"")
        custIdEspay= bundle!!.getString(DefineValue.CUST_ID_ESPAY,"")

        frag_gr_confirm_store_code.setText(memberCodeEspay)
        frag_gr_confirm_comm_code.setText(commCodeEspay)
        frag_gr_confirm_amount.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(bundle!!.getString(DefineValue.AMOUNT)))
        frag_gr_confirm_discount.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(bundle!!.getString(DefineValue.TOTAL_DISC)))
        frag_gr_confirm_total_amount.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(bundle!!.getString(DefineValue.TOTAL_AMOUNT)))

        frag_gr_confirm_submit_btn.setOnClickListener { reqOTP() }
    }

    fun reqOTP()
    {
        try {
            showProgressDialog()
            extraSignature = memberCodeEspay + commCodeEspay
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_DETAIL, extraSignature)
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            params[WebParams.CUST_ID_ESPAY] = custIdEspay
            params[WebParams.USER_ID] = userPhoneID
            Timber.d("params inquiry doc detail:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_DETAIL, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response inquiry doc detail:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {

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
                                        Timber.d("isi error inquiry doc detail:$response")
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

}