   package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserGoodReceiptActivity
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
import kotlinx.android.synthetic.main.frag_create_gr.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragCreateGR : BaseFragment() {
    var memberCodeEspay : String =""
    var commCodeEspay : String =""
    var custIdEspay : String =""
    var docNo : String =""
    var amount : String =""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_create_gr, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var bundle = arguments!!
        memberCodeEspay = bundle!!.getString(DefineValue.MEMBER_CODE_ESPAY,"")
        commCodeEspay = bundle!!.getString(DefineValue.COMMUNITY_CODE_ESPAY,"")
        custIdEspay= bundle!!.getString(DefineValue.CUST_ID_ESPAY,"")
        docNo = bundle!!.getString(DefineValue.DOC_NO, "")
        amount = bundle!!.getString(DefineValue.AMOUNT, "")

        frag_gr_confirm_store_code.setText(memberCodeEspay)
        frag_gr_confirm_comm_code.setText(commCodeEspay)
        frag_gr_confirm_amount.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount))
        frag_gr_confirm_discount.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(bundle!!.getString(DefineValue.TOTAL_DISC)))
        frag_gr_confirm_total_amount.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(bundle!!.getString(DefineValue.TOTAL_AMOUNT)))

        frag_gr_confirm_submit_btn.setOnClickListener { createGR() }
    }

    fun createGR()
    {
        try {
            showProgressDialog()
            extraSignature = memberCodeEspay + commCodeEspay + amount
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CREATE_GR, extraSignature)
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            params[WebParams.CUST_ID_ESPAY] = custIdEspay
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.DOC_NO] = docNo
            params[WebParams.INVOICE_NOTE] = ""
            params[WebParams.NOTES_NO] = ""
            params[WebParams.NOTES_ID] = ""
            params[WebParams.TYPE_ID] = DefineValue.GR
            params[WebParams.CUST_TYPE] = DefineValue.CANVASSER
            params[WebParams.DOC_DETAIL] = ""
            Timber.d("params create GR:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CREATE_GR, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response create GR:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        var bundle = arguments!!
                                        bundle.putString(DefineValue.TX_ID, response.optString(DefineValue.TX_ID))
                                        val frag: Fragment = FragConfirmCreateGR()
                                        frag.arguments = bundle
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
                                        Timber.d("isi error create GR:$response")
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