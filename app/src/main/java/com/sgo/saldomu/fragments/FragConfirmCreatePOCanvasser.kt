package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_confirm_gr.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragConfirmCreatePOCanvasser : BaseFragment(){
    var txId : String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_confirm_gr, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val bundle = arguments
        txId = bundle!!.getString(DefineValue.TX_ID, "")

        frag_gr_confirm_submit_btn.setOnClickListener {
            if(inputValidation())
                confirmOTP() }


        frag_gr_resend_otp.setOnClickListener {
            et_otp_confirm_gr.setText("")
            resendOTP()
        }
    }

    fun inputValidation(): Boolean {
        if (et_otp_confirm_gr == null || et_otp_confirm_gr.getText().toString().isEmpty()) {
            et_otp_confirm_gr.requestFocus()
            et_otp_confirm_gr.error = getString(R.string.validation_confirmation_code)
            return false
        }
        return true
    }

    fun confirmOTP()
    {
        try {
            showProgressDialog()
            val link = MyApiClient.LINK_CONFIRM_OTP_DOC
            val subStringLink = link.substring(link.indexOf("saldomu/"))
            val tokenId = et_otp_confirm_gr.text.toString()
            extraSignature = txId + tokenId
            val params = RetrofitService.getInstance().getSignature(link, extraSignature)
            val uuid: String = params[WebParams.RC_UUID].toString()
            val dateTime: String = params[WebParams.RC_DTIME].toString()
            val encryptedOtp = RSA.opensslEncrypt(uuid, dateTime, userPhoneID, tokenId, subStringLink)
            params[WebParams.TX_ID] = txId
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.TOKEN_ID] = encryptedOtp
            params[WebParams.COMM_ID] = MyApiClient.COMM_ID
            Timber.d("params GR confirm OTP:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_OTP_DOC, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response PO confirm OTP:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        showDialog(response)
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
                                        Timber.d("isi error GR confirm OTP:$response")
                                        Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
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
        } catch (e: java.lang.Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }


    private fun resendOTP()
    {
        try {
            showProgressDialog()
            extraSignature = txId + userPhoneID
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_RESEND_OTP_DOC, extraSignature)
            params[WebParams.CUST_ID] = userPhoneID
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.TX_ID] = txId

            Timber.d("params resend OTP:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_RESEND_OTP_DOC, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response resend OTP:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        et_otp_confirm_gr.setText("")
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
                                        Timber.d("isi error resend OTP:$response")
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

    @SuppressLint("SetTextI18n")
    private fun showDialog(response: JSONObject) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = resources.getString(R.string.success)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = response.getString(WebParams.ERROR_MESSAGE)

        dialog.btn_dialog_notification_ok.setOnClickListener {
            dialog.dismiss()
            activity!!.finish()
        }
        dialog.show()
    }
}