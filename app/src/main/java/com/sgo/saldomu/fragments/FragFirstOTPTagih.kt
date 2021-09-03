package com.sgo.saldomu.fragments

import android .os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TagihActivity
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_first_otp_tagih.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragFirstOTPTagih : BaseFragment() {

    var commCodeTagih : String= ""
    var memberCode : String= ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_first_otp_tagih, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val args = arguments!!

        commCodeTagih = args.getString(DefineValue.COMMUNITY_CODE,"")
        memberCode = args.getString(DefineValue.MEMBER_CODE,"")

        tv_otp.setText(args.getString(DefineValue.OTP))

        btn_next.setOnClickListener {
            sendDataTagih()
        }
    }

    fun sendDataTagih() {
        showProgressDialog()
        val extraSignature: String = commCodeTagih + memberCode
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LIST_INVOICE_DGI, extraSignature)
        params[WebParams.APP_ID] = BuildConfig.APP_ID
        params[WebParams.MEMBER_CODE] = memberCode
        params[WebParams.COMM_CODE] = commCodeTagih
        params[WebParams.USER_ID] = userPhoneID
        Timber.d("params list invoice DGI : $params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_LIST_INVOICE_DGI, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        try {
                            dismissProgressDialog()
                            val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                            Timber.d("response list invoice DGI : $response")
                            val code = response.getString(WebParams.ERROR_CODE)
                            val error_message = response.getString(WebParams.ERROR_MESSAGE)
                            if (code == WebParams.SUCCESS_CODE) {
                                val responseListInvoice = response.toString()
                                val newFrag: Fragment = FragListInvoiceIndomobil()
                                val bundle = Bundle()
                                bundle.putString(DefineValue.MEMBER_CODE, memberCode)
                                bundle.putString(DefineValue.COMMUNITY_CODE, commCodeTagih)
                                bundle.putString(DefineValue.RESPONSE, responseListInvoice)
                                bundle.putString(DefineValue.TXID_PG, arguments?.getString(DefineValue.TXID_PG, ""))
                                if (arguments?.getBoolean(DefineValue.IS_FAVORITE) == true) {
                                    bundle.putString(DefineValue.CUST_ID, memberCode)
                                    bundle.putString(DefineValue.NOTES, arguments!!.getString(DefineValue.NOTES))
                                    bundle.putString(DefineValue.TX_FAVORITE_TYPE, DefineValue.DGI)
                                    bundle.putString(DefineValue.PRODUCT_TYPE, DefineValue.DGI)
                                    bundle.putString(DefineValue.ANCHOR_ID, arguments?.getString(DefineValue.ANCHOR_ID))
                                }
                                val mEditor = sp.edit()
                                mEditor.putString(DefineValue.COMM_CODE_DGI, response.getString(WebParams.COMM_CODE_DGI))
                                mEditor.apply()
                                newFrag.arguments = bundle
                                if (activity == null) {
                                    return
                                }
                                val ftf = activity as TagihActivity?
                                ftf!!.switchContent(newFrag, "List Invoice", true)
                            } else if (code == DefineValue.ERROR_9333) {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) {
                                Timber.d("isi response maintenance:$response")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                            } else {
                                Toast.makeText(activity, error_message, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        dismissProgressDialog()
                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                    }
                })
    }
}