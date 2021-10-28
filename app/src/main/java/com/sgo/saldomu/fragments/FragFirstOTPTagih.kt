package com.sgo.saldomu.fragments

import android.os.Bundle
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

    var commCodeTagih: String = ""
    var memberCode: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.frag_first_otp_tagih, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val args = arguments!!

        commCodeTagih = args.getString(DefineValue.COMMUNITY_CODE, "")
        memberCode = args.getString(DefineValue.MEMBER_CODE, "")

        btn_refresh.setOnClickListener { requestOTP() }
        btn_next.setOnClickListener {
            sendDataTagih()
        }
        requestOTP()
    }

    private fun requestOTP() {
        showProgressDialog()

        extraSignature = commCodeTagih + memberCode
        params = RetrofitService.getInstance()
            .getSignature(MyApiClient.LINK_REQ_FIRST_OTP, extraSignature)
        params[WebParams.APP_ID] = BuildConfig.APP_ID
        params[WebParams.MEMBER_CODE] = memberCode
        params[WebParams.COMM_CODE] = commCodeTagih
        params[WebParams.USER_ID] = userPhoneID

        Timber.d("params first OTP tagih DGI : %s", params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_FIRST_OTP, params,
            object : ObjListeners {
                override fun onResponses(response: JSONObject) {
                    try {
                        dismissProgressDialog()
                        val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                        Timber.d("response first OTP tagih DGI  : %s", response.toString())
                        val code = response.getString(WebParams.ERROR_CODE)
                        val errorMessage = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                layout_refresh_code.visibility = View.INVISIBLE
                                layout_success_code.visibility = View.VISIBLE
                                tv_otp.text = response.getString(WebParams.otp)
                            }
                            DefineValue.ERROR_9333 -> {
                                Timber.d("isi response app data:%s", model.app_data)
                                val appModel = model.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                    activity,
                                    appModel.type,
                                    appModel.packageName,
                                    appModel.downloadUrl
                                )
                            }
                            DefineValue.ERROR_0066 -> {
                                Timber.d("isi response maintenance:%s", response.toString())
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
                            }
                            else -> {
                                Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
                                layout_refresh_code.visibility = View.VISIBLE
                                layout_success_code.visibility = View.INVISIBLE
                            }
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

    private fun sendDataTagih() {
        val newFrag: Fragment = FragListInvoiceIndomobil()
        val bundle = Bundle()
        bundle.putString(DefineValue.MEMBER_CODE, memberCode)
        bundle.putString(DefineValue.COMMUNITY_CODE, commCodeTagih)
        bundle.putString(DefineValue.TXID_PG, arguments?.getString(DefineValue.TXID_PG, ""))
        if (arguments?.getBoolean(DefineValue.IS_FAVORITE) == true) {
            bundle.putString(DefineValue.CUST_ID, memberCode)
            bundle.putString(DefineValue.NOTES, arguments!!.getString(DefineValue.NOTES))
            bundle.putString(DefineValue.TX_FAVORITE_TYPE, DefineValue.DGI)
            bundle.putString(DefineValue.PRODUCT_TYPE, DefineValue.DGI)
            bundle.putString(DefineValue.ANCHOR_ID, arguments?.getString(DefineValue.ANCHOR_ID))
        }
        newFrag.arguments = bundle
        if (activity == null) {
            return
        }
        val ftf = activity as TagihActivity?
        ftf!!.switchContent(newFrag, "List Invoice", true)
    }
}