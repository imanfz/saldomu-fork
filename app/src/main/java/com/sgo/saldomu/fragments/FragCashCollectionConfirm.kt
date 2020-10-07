package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.google.gson.JsonObject
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.InsertPIN
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DateTimeFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.*
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.interfaces.OnLoadDataListener
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.loader.UtilsLoader
import com.sgo.saldomu.models.retrofit.FailedPinModel
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_cash_collection.*
import kotlinx.android.synthetic.main.dialog_notification.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragCashCollectionConfirm : BaseFragment(), ReportBillerDialog.OnDialogOkCallback {
    private lateinit var viewLayout: View
    private var txId: String = ""
    private var commId: String = ""
    private var commCode: String = ""
    private var memberCode: String = ""
    private var productCode: String = ""
    private var benefProductValueCode: String = ""
    private var benefProductValueName: String = ""
    private var otp: String = ""
    private var amount: String = ""
    private var fee: String = ""
    private var cust_id: String = ""
    private var notes: String = ""
    private var product_type: String = ""
    private var tx_favorite_type: String = ""
    private var isPIN: Boolean? = false
    private var isFav: Boolean? = false
    private var attempt = 0
    private var failed = 0
    private var countResend = 0
    private var maxResend = 0
    private lateinit var dialogReport: ReportBillerDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewLayout = inflater.inflate(R.layout.dialog_cash_collection, container, false)
        return viewLayout
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val args = arguments!!
        txId = args.getString(DefineValue.TX_ID)!!
        commId = args.getString(DefineValue.COMMUNITY_ID)!!
        benefProductValueCode = args.getString(DefineValue.BENEF_PRODUCT_VALUE_CODE)!!
        benefProductValueName = args.getString(DefineValue.BENEF_PRODUCT_VALUE_NAME)!!
        commCode = args.getString(DefineValue.COMMUNITY_CODE)!!
        memberCode = args.getString(DefineValue.MEMBER_CODE)!!
        productCode = args.getString(DefineValue.PRODUCT_CODE)!!
        amount = args.getString(DefineValue.AMOUNT)!!
        fee = args.getString(DefineValue.FEE)!!

        if (args.getBoolean(DefineValue.IS_FAVORITE) == true) {
            isFav = true
            notes = args.getString(DefineValue.NOTES, "")
            cust_id = args.getString(DefineValue.CUST_ID, "")
            tx_favorite_type = args.getString(DefineValue.TX_FAVORITE_TYPE, "")
            product_type = args.getString(DefineValue.PRODUCT_TYPE, "")
        }

        dialog_cash_collection_tv_name.text = benefProductValueName
        dialog_cash_collection_tv_acc_no.text = benefProductValueCode
        dialog_cash_collection_tv_amount_deposit.text = getString(R.string.rp_) + " " + CurrencyFormat.format(amount)
        dialog_cash_collection_tv_fee.text = getString(R.string.rp_) + " " + CurrencyFormat.format(fee)
        tv_resend_otp.text = getString(R.string.resend_confirmation_code) + "(" + countResend + "/3)"

        dialog_cash_collection_btn_ok.setOnClickListener {
            if (inputValidation()) {
                otp = et_otp_cashcollection.text.toString()
                if (isFav==true) {
                    onSaveToFavorite()
                } else
                    confirmTokenC2R()
            }
        }

        dialog_cash_collection_btn_cancel.setOnClickListener { fragmentManager!!.popBackStack() }

        tv_resend_otp.setOnClickListener {
            if (countResend < 3)
                resendTokenC2R()
        }
    }

    private fun confirmTokenC2R() {
        try {
            showProgressDialog()

            val link = MyApiClient.LINK_CONFIRM_TOKEN_C2R
            val subStringLink = link.substring(link.indexOf("saldomu/"))
            extraSignature = txId + commId + otp
            params = RetrofitService.getInstance().getSignature(link, extraSignature)
            val uuid: String = params[WebParams.RC_UUID].toString()
            val dateTime: String = params[WebParams.RC_DTIME].toString()
            params[WebParams.TX_ID] = txId
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID] = commId
            params[WebParams.TOKEN_ID] = RSA.opensslEncryptCommID(commId, uuid, dateTime, userPhoneID, otp, subStringLink)

            Timber.d("isi params confirmTokenC2R:$params")

            RetrofitService.getInstance().PostJsonObjRequest(link, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) = try {
                            val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                            val code = response.getString(WebParams.ERROR_CODE)
                            val msg = response.getString(WebParams.ERROR_MESSAGE)
                            Timber.d("isi response confirmTokenC2R: $response")
                            when (code) {
                                WebParams.SUCCESS_CODE -> {
                                    inputPIN()
                                }
                                WebParams.LOGOUT_CODE -> {
                                    Timber.d("isi response autologout:$response")
                                    val message = response.getString(WebParams.ERROR_MESSAGE)
                                    val test = AlertDialogLogout.getInstance()
                                    test.showDialoginActivity(activity, message)
                                }
                                DefineValue.ERROR_0061 -> {
                                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                                    et_otp_cashcollection.setText("")
                                }
                                DefineValue.ERROR_0135 -> {
                                    showDialog(msg)
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
                                    Timber.d("Error confirmTokenC2R:$response")

                                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:%s", e.message)
        }

    }

    private fun resendTokenC2R() {
        try {
            showProgressDialog()

            extraSignature = txId + commId

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_RESEND_TOKEN_C2R, extraSignature)

            params[WebParams.TX_ID] = txId
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID] = commId

            Timber.d("isi params resendTokenC2R:$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_RESEND_TOKEN_C2R, params,
                    object : ObjListeners {
                        @SuppressLint("SetTextI18n")
                        override fun onResponses(response: JSONObject) = try {
                            val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                            val code = response.getString(WebParams.ERROR_CODE)
                            val msg = response.getString(WebParams.ERROR_MESSAGE)
                            Timber.d("isi response resendTokenC2R: $response")
                            when (code) {
                                WebParams.SUCCESS_CODE -> {
                                    countResend = response.opt(WebParams.COUNT_RESEND) as Int
                                    maxResend = response.opt(WebParams.MAX_RESEND) as Int
                                    if (countResend == 3) {
                                        !tv_resend_otp.isEnabled
                                        tv_resend_otp.setTextColor(resources.getColor(R.color.colorSecondaryDark))
                                    }
                                    tv_resend_otp.text = getString(R.string.resend_confirmation_code) + "(" + countResend + "/" + maxResend + ")"
                                }
                                WebParams.LOGOUT_CODE -> {
                                    Timber.d("isi response autologout:$response")
                                    val message = response.getString(WebParams.ERROR_MESSAGE)
                                    val test = AlertDialogLogout.getInstance()
                                    test.showDialoginActivity(activity, message)
                                }
                                DefineValue.ERROR_0135 -> {
                                    showDialog(msg)
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
                                    Timber.d("Error resendTokenC2R:$response")

                                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun onSaveToFavorite() {
        extraSignature = cust_id + product_type + tx_favorite_type
        Log.e("extraSignature params ", extraSignature)
        val url = MyApiClient.LINK_TRX_FAVORITE_SAVE
        val params = RetrofitService.getInstance().getSignature(url, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.PRODUCT_TYPE] = product_type
        params[WebParams.CUSTOMER_ID] = cust_id
        params[WebParams.TX_FAVORITE_TYPE] = tx_favorite_type
        params[WebParams.COMM_ID] = MyApiClient.COMM_ID_TAGIH
        params[WebParams.NOTES] = notes
        params[WebParams.DENOM_ITEM_ID] = ""
        Log.e("params fav CTR :", params.toString())
        RetrofitService.getInstance().PostJsonObjRequest(url, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        try {
                            val model = RetrofitService.getInstance().gson.fromJson(response.toString(), jsonModel::class.java)
                            Log.e("onResponse fav CTR", response.toString())
                            val code = response.getString(WebParams.ERROR_CODE)
                            val message = response.getString(WebParams.ERROR_MESSAGE)
                            if (code == WebParams.SUCCESS_CODE) {
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
                                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        Log.e("onResponse fav CTR", throwable.localizedMessage)
                        throwable.printStackTrace()
                    }

                    override fun onComplete() {
                        confirmTokenC2R()
                    }
                })
    }

    private fun inputPIN() {
        isPIN = true
        UtilsLoader(activity, sp).getFailedPIN(userPhoneID, object : OnLoadDataListener {
            override fun onSuccess(deData: Any) {

            }

            override fun onFail(message: Bundle) {

            }

            override fun onFailure(message: String) {

            }
        })

        if (isPIN!!) {
            callPINInput()
        }
    }


    private fun callPINInput() {
        val i = Intent(activity, InsertPIN::class.java)
        startActivityForResult(i, MainPage.REQUEST_FINISH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE)
                sentInsertTransTopUp(data?.getStringExtra(DefineValue.PIN_VALUE))
        }
    }

    private fun sentInsertTransTopUp(tokenValue: String?) {
        try {
            showProgressDialog()

            val link = MyApiClient.LINK_INSERT_TRANS_TOPUP
            val subStringLink = link.substring(link.indexOf("saldomu/"))
            extraSignature = txId + commCode + productCode + tokenValue
            val params = RetrofitService.getInstance().getSignature(link, extraSignature)
            val uuid: String = params[WebParams.RC_UUID].toString()
            val dateTime: String = params[WebParams.RC_DTIME].toString()
            params[WebParams.TX_ID] = txId
            params[WebParams.PRODUCT_CODE] = productCode
            params[WebParams.COMM_CODE] = commCode
            params[WebParams.COMM_ID] = commId
            params[WebParams.MEMBER_ID] = memberIDLogin
            params[WebParams.PRODUCT_VALUE] = RSA.opensslEncryptCommID(commId, uuid, dateTime, userPhoneID, tokenValue, subStringLink)
            params[WebParams.USER_ID] = userPhoneID

            Timber.d("params insert trx : $params")
            RetrofitService.getInstance().PostObjectRequest(link, params,
                    object : ResponseListener {
                        override fun onResponses(response: JsonObject) {
                            val model = getGson().fromJson(response, FailedPinModel::class.java)

                            val code = model.error_code
                            val message = model.error_message
                            if (code == WebParams.SUCCESS_CODE) {
                                activity!!.setResult(MainPage.RESULT_BALANCE)
                                getTrxStatus(sp.getString(DefineValue.USER_NAME, ""), txId, userPhoneID)
                            } else if (code == WebParams.LOGOUT_CODE) {
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(activity, message)
                            } else if (code == "0061") {
                                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                            } else if (code == DefineValue.ERROR_9333) {
                                Timber.d("isi response app data:%s", model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) {
                                Timber.d("isi response maintenance:$response")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                            } else {
                                if (isPIN!!) {
                                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                                    //pin tidak sesuai errorcode 0097
                                    if (code == "0097") {
                                        val i = Intent(activity, InsertPIN::class.java)

                                        attempt = model.failed_attempt
                                        failed = model.max_failed

                                        if (attempt != -1)
                                            i.putExtra(DefineValue.ATTEMPT, failed - attempt)

                                        startActivityForResult(i, MainPage.REQUEST_FINISH)
                                    } else {
                                        activity!!.setResult(MainPage.RESULT_BALANCE)
                                        getTrxStatus(sp.getString(DefineValue.USER_NAME, ""), txId, userPhoneID)
                                    }
                                } else {
                                    activity!!.setResult(MainPage.RESULT_BALANCE)
                                    getTrxStatus(sp.getString(DefineValue.USER_NAME, ""), txId, userPhoneID)
                                }
                            }
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun getTrxStatus(userName: String?, txId: String?, userId: String?) {
        try {
            val progressDialog = DefinedDialog.CreateProgressDialog(activity, getString(R.string.check_status))
            progressDialog.show()

            extraSignature = txId + commCode
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TRX_STATUS_BBS, extraSignature)
            params[WebParams.TX_ID] = txId
            params[WebParams.COMM_ID] = commId
            params[WebParams.COMM_CODE] = commCode
            params[WebParams.USER_ID] = userId

            Timber.d("isi params sent get Trx Status bbs:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_TRX_STATUS_BBS, params,
                    object : ResponseListener {
                        override fun onResponses(`object`: JsonObject) {
                            val model = getGson().fromJson(`object`, GetTrxStatusReportModel::class.java)

                            val code = model.error_code
                            if (code == WebParams.SUCCESS_CODE || code == "0003") {
                                showReportBillerDialog(userName, txId, userId, model)
                            } else if (code == "0288") {
                                val codeMsg = model.error_message
                                Toast.makeText(activity, codeMsg, Toast.LENGTH_LONG).show()
                            } else if (code == WebParams.LOGOUT_CODE) {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(activity, message)
                            } else if (code == DefineValue.ERROR_9333) {
                                Timber.d("isi response app data:%s", model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) {
                                Timber.d("isi response maintenance:$`object`")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                            } else {
                                val msg = model.error_message
                                showDialog(msg)
                            }

                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            if (progressDialog.isShowing)
                                progressDialog.dismiss()
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun showReportBillerDialog(userName: String?, txId: String?, userId: String?, response: GetTrxStatusReportModel?) {
        val args = Bundle()
        dialogReport = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.USER_NAME, userName)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response?.created))
        args.putString(DefineValue.TX_ID, txId)
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHIN)
        args.putString(DefineValue.USERID_PHONE, userId)
        args.putString(DefineValue.BANK_NAME, response?.tx_bank_name)
        args.putString(DefineValue.BANK_PRODUCT, response?.product_name)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response?.admin_fee))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response?.tx_amount))
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response?.additional_fee))
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response?.total_amount))

        var txStat: Boolean? = false
        val txStatus = response?.tx_status
        when {
            txStatus == DefineValue.SUCCESS -> {
                txStat = true
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
            }
            txStatus == DefineValue.ONRECONCILED -> {
                txStat = true
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
            }
//            txStatus == DefineValue.SUSPECT -> args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//            txStatus != DefineValue.FAILED -> args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//            else -> args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response?.tx_remark)
        args.putString(DefineValue.MEMBER_NAME, response?.member_name)
        args.putString(DefineValue.TRX_STATUS_REMARK, response?.tx_status_remark)
        args.putString(DefineValue.SOURCE_ACCT, response?.source_bank_name)
        args.putString(DefineValue.SOURCE_ACCT_NO, response?.source_acct_no)
        args.putString(DefineValue.SOURCE_ACCT_NAME, response?.source_acct_name)
        args.putString(DefineValue.BANK_BENEF, response?.benef_bank_name)
        args.putString(DefineValue.TYPE_BENEF, response?.benef_acct_type)
        args.putString(DefineValue.NO_BENEF, response?.benef_acct_no)
        args.putString(DefineValue.NAME_BENEF, response?.benef_acct_name)
        args.putString(DefineValue.PRODUCT_NAME, response?.product_name)
        args.putString(DefineValue.MEMBER_SHOP_PHONE, response?.member_shop_phone)
        args.putString(DefineValue.BUSS_SCHEME_CODE, response?.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response?.buss_scheme_name)
        args.putString(DefineValue.BENEF_PRODUCT_CODE, benefProductValueCode)
        args.putString(DefineValue.MEMBER_SHOP_NAME, response?.member_shop_name)
        args.putString(DefineValue.MEMBER_SHOP_PHONE, response?.member_shop_phone)

        dialogReport.arguments = args
        dialogReport.show(activity!!.supportFragmentManager, ReportBillerDialog.TAG)
    }

    override fun onOkButton() {
        dialogReport.dismiss()
        activity!!.finish()
    }


    private fun inputValidation(): Boolean {
        if (et_otp_cashcollection.text.isNullOrEmpty()) {
            et_otp_cashcollection.requestFocus()
            et_otp_cashcollection.error = getString(R.string.regist3_validation_otp)
            return false
        }

        return true
    }

    private fun showDialog(msg: String?) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = getString(R.string.error)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = msg

        dialog.btn_dialog_notification_ok.setOnClickListener {
            //            dialog.dismiss()
            activity!!.finish()
        }

        dialog.show()
    }

}