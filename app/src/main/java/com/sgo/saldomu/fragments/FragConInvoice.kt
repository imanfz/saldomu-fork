package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserInvoiceActivity
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.ListPOModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_order_confirm_toko.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragConInvoice : BaseFragment() {
    var memberCode: String? = null
    var commCode: String? = null

    var memberCodeEspay : String = ""
    var commCodeEspay : String = ""
    var custIdEspay : String = ""
    var docNo : String = ""
    var doc_detail : String = ""
    var type_id : String = ""

    var cust_id : String = ""
    var reff_id : String = ""
    var ccy_id : String = ""
    var cust_type : String = ""

    var obj: ListPOModel? = null;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_order_confirm_toko, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val tokoEBDActivity = activity as CanvasserInvoiceActivity
//        tokoEBDActivity.initializeToolbar(getString(R.string.purchase_order))

        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE, "")
            commCode = arguments!!.getString(DefineValue.COMMUNITY_CODE, "")
            commCodeEspay = arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            memberCodeEspay = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            obj = arguments!!.getParcelable(DefineValue.OBJ);

        }

        getDetail()
        member_code_field.text = memberCode
        comm_code_field.text = commCode
        submit_btn.setOnClickListener { submitOrder() }
    }


    private fun submitOrder(){

    }


    private fun getDetail()
    {
        Toast.makeText(context,"$memberCode $commCode",Toast.LENGTH_SHORT).show()

        try {


            showProgressDialog()
            extraSignature = obj!!.comm_code + obj!!.cust_id
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_DOCS, extraSignature)
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_CODE_ESPAY] =  obj!!.comm_code
            params[WebParams.MEMBER_CODE_ESPAY] =  obj!!.member_code
            params[WebParams.CUST_ID_ESPAY] =  obj!!.cust_id
            params[WebParams.CUST_ID] = userPhoneID
            params[WebParams.REFF_ID] = obj!!.reff_id
            params[WebParams.CCY_ID] =  MyApiClient.CCY_VALUE;
            params[WebParams.TYPE_ID] = obj!!.type_id
            params[WebParams.CUST_TYPE] = DefineValue.CANVASSER //
            params[WebParams.DOC_NO] = obj!!.doc_no
//            params[WebParams.DOC_DETAIL] = tempGson


            Timber.d("params inquiry doc detail:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_DOCS, params,
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
