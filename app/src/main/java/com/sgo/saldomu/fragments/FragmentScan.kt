package com.sgo.saldomu.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonObject
import com.google.zxing.Result
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.InsertPIN
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DateTimeFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.dialogs.ReportBillerDialog
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.interfaces.OnLoadDataListener
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.loader.UtilsLoader
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel
import com.sgo.saldomu.models.retrofit.QrisParsingModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.utils.NumberTextWatcherForThousand
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_input_amount_qris.*
import kotlinx.android.synthetic.main.frag_scan.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class FragmentScan : BaseFragment(), ZXingScannerView.ResultHandler, ReportBillerDialog.OnDialogOkCallback {

    private var txId = ""
    private var commCode = ""
    private var commId = ""
    private var productCode = ""

    private var mScannerView: ZXingScannerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_scan, container, false)
        return v
    }

    override fun onStart() {
//        startScanner()
        doRequestPermission()
        super.onStart()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initScannerView()
    }

    private fun resumeScanner() {
        startScanner()
        mScannerView?.resumeCameraPreview(this)
    }

    private fun startScanner() {
        mScannerView?.startCamera()
    }

    private fun stopScanner() {
        mScannerView?.stopCamera()
    }

    private fun initScannerView() {
        mScannerView = ZXingScannerView(activity)
        mScannerView?.setAutoFocus(true)
        mScannerView?.setResultHandler(this)
        frame_layout_camera.addView(mScannerView)
        startScanner()
    }

    private fun doRequestPermission() {
        if (!EasyPermissions.hasPermissions(context!!, Manifest.permission.CAMERA)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage), 123, Manifest.permission.CAMERA);
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            100 -> {
                initScannerView()
            }
            else -> {
                /* nothing to do in here */
            }
        }
    }

    override fun onPause() {
        stopScanner()
        super.onPause()
    }

    override fun handleResult(rawResult: Result?) {
        parsingQR(rawResult?.text.toString())
    }

    private fun parsingQR(qrisString: String?) {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_QRIS_PARSING, qrisString)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.QRIS_STRING] = qrisString

        Timber.d("isi params qris parsing:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_QRIS_PARSING, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                val qrisParsingModel = getGson().fromJson(response.toString(), QrisParsingModel::class.java)
                                showDialogConfirmation(qrisParsingModel)
                            }
                            WebParams.LOGOUT_CODE -> {
                                AlertDialogLogout.getInstance().showDialoginActivity2(activity, message)
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
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                resumeScanner()
                            }
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

    private fun showDialogConfirmation(qrisParsingModel: QrisParsingModel) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_input_amount_qris)

        val transactionAmount = qrisParsingModel.transactionAmount
        dialog.text_view_merchant_name.text = qrisParsingModel.merchantName
        dialog.text_view_merchant_city.text = qrisParsingModel.merchantCity
        dialog.edit_text_payment_nominal.addTextChangedListener(NumberTextWatcherForThousand(dialog.edit_text_payment_nominal))
        dialog.edit_text_payment_nominal.requestFocus()
        val imm = context!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        if (transactionAmount != "") {
            dialog.edit_text_payment_nominal.setText(qrisParsingModel.transactionAmount)
            dialog.edit_text_payment_nominal.isEnabled = false
        }

        dialog.btn_dialog_ok.setOnClickListener {
            if (dialog.edit_text_payment_nominal.text!!.isNotEmpty()) {
                paymentReqQris(NumberTextWatcherForThousand.trimCommaOfString(dialog.edit_text_payment_nominal.text.toString()), qrisParsingModel)
                dialog.dismiss()
            } else
                dialog.edit_text_payment_nominal.error = getString(R.string.payment_nominal_required)
        }
        dialog.show()
    }

    private fun paymentReqQris(amount: String, qrisParsingModel: QrisParsingModel) {
        showProgressDialog()

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_QRIS_PAYMENT_REQUEST, qrisParsingModel.commId + qrisParsingModel.memberId)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.AMOUNT] = amount
        params[WebParams.ADMIN_FEE] = "0"
        params[WebParams.MERCHANT_NAME] = qrisParsingModel.merchantName
        params[WebParams.MERCHANT_CITY] = qrisParsingModel.merchantCity
        params[WebParams.MERCHANT_QRIS_TYPE] = qrisParsingModel.merchantQrisType
        params[WebParams.POSTAL_CODE] = qrisParsingModel.postalCode
        params[WebParams.ADDITIONAL_FIELD] = qrisParsingModel.additionalField
        params[WebParams.COMM_ID] = qrisParsingModel.commId
        params[WebParams.MEMBER_ID] = qrisParsingModel.memberId
        params[WebParams.MERCHANT_TYPE] = qrisParsingModel.merchantType
        params[WebParams.NMID] = qrisParsingModel.nmid
        params[WebParams.MERCHANT_CRITERIA] = qrisParsingModel.merchantCriteria
        params[WebParams.MERCHANT_ID] = qrisParsingModel.merchantId
        params[WebParams.MERCHANT_COUNTRY] = qrisParsingModel.merchantCountry
        params[WebParams.MERCHANT_PAN] = qrisParsingModel.merchantPan
        params[WebParams.TERMINAL_ID] = qrisParsingModel.terminalId
        params[WebParams.MERCHANT_STORE_ID] = qrisParsingModel.merchantStoreId

        Timber.d("isi params qris payment request:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_QRIS_PAYMENT_REQUEST, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                txId = response.getString(WebParams.TX_ID)
                                commCode = response.getString(WebParams.COMM_CODE)
                                commId = response.getString(WebParams.COMM_ID)
                                productCode = response.getString(WebParams.PRODUCT_CODE)
                                inputPIN(-1)
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
                            DefineValue.ERROR_0338 -> {
                                showDialog(message)
                            }
                            DefineValue.ERROR_57 -> {
                                showDialog(message)
                            }
                            else -> {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                resumeScanner()
                            }
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

    private fun inputPIN(attempt: Int) {
        UtilsLoader(activity, sp).getFailedPIN(userPhoneID, object : OnLoadDataListener {
            override fun onSuccess(deData: Any) {

            }

            override fun onFail(message: Bundle) {

            }

            override fun onFailure(message: String) {

            }
        })
        callPINinput(attempt)
    }

    private fun callPINinput(attempt: Int) {
        val i = Intent(activity, InsertPIN::class.java)
        if (attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, attempt)
        startActivityForResult(i, MainPage.REQUEST_FINISH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                stopScanner()
                val valuePin = data!!.getStringExtra(DefineValue.PIN_VALUE)!!
                sentInsertTransTopup(valuePin)
            }
        }
    }

    private fun sentInsertTransTopup(valuePin: String) {
        showProgressDialog()

        val link = MyApiClient.LINK_INSERT_TRANS_TOPUP
        val subStringLink = link.substring(link.indexOf("saldomu/"))
        extraSignature = txId + commCode + productCode + valuePin
        val params = RetrofitService.getInstance().getSignature(link, extraSignature)
        val uuid: String = params[WebParams.RC_UUID].toString()
        val dateTime: String = params[WebParams.RC_DTIME].toString()
        params[WebParams.TX_ID] = txId
        params[WebParams.PRODUCT_CODE] = productCode
        params[WebParams.COMM_CODE] = commCode
        params[WebParams.COMM_ID] = commId
        params[WebParams.MEMBER_ID] = sp.getString(DefineValue.MEMBER_ID, "")
        params[WebParams.PRODUCT_VALUE] = RSA.opensslEncryptCommID(commId, uuid, dateTime, userPhoneID, valuePin, subStringLink)
        params[WebParams.USER_ID] = userPhoneID

        Timber.d("isi params insertTrx:$params")
        RetrofitService.getInstance().PostJsonObjRequest(link, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                getTrxStatus()
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
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                resumeScanner()
                            }
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

    private fun getTrxStatus() {
        showProgressDialog()
        extraSignature = txId + commId
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature)

        params[WebParams.TX_ID] = txId
        params[WebParams.COMM_ID] = commId
        params[WebParams.PRIVACY] = ""
        params[WebParams.TX_TYPE] = DefineValue.ESPAY
        params[WebParams.USER_ID] = userPhoneID

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                object : ResponseListener {
                    override fun onResponses(response: JsonObject) {
                        val model = getGson().fromJson(response, GetTrxStatusReportModel::class.java)

                        val code = model.error_code
                        val message = model.error_message

                        if (!model.on_error) {
                            if (code == WebParams.SUCCESS_CODE || code == "0003") {
                                showReportQRSDialog(model)
                            } else if (code == WebParams.LOGOUT_CODE) {
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(activity, message)
                            } else if (code == DefineValue.ERROR_9333) {
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) {
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                            } else {
                                showDialog(message)
                            }
                        } else {
                            Toast.makeText(activity, model.error_message, Toast.LENGTH_SHORT).show()
                            resumeScanner()
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

    private fun showReportQRSDialog(response: GetTrxStatusReportModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.created))
        args.putString(DefineValue.TX_ID, response.tx_id)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.admin_fee))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tx_amount))

        val dAmount = response.tx_amount!!.toDouble()
        val dFee = response.admin_fee!!.toDouble()
        val totalAmount = dAmount + dFee

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(totalAmount))

        var txStat = false
        val txStatus = response.tx_status
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat)
        args.putString(DefineValue.TRX_STATUS_REMARK, response.tx_status_remark)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)

        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)
        args.putString(DefineValue.MERCHANT_NAME, response.merchant_name)

        dialog.arguments = args
        dialog.show(activity!!.supportFragmentManager, ReportBillerDialog.TAG)
    }

    private fun showDialog(msg: String) {
        // Create custom dialog object
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification)

        // set values for custom dialog components - text, image and button
        val btnDialog = dialog.findViewById<Button>(R.id.btn_dialog_notification_ok)
        val title = dialog.findViewById<TextView>(R.id.title_dialog)
        val message = dialog.findViewById<TextView>(R.id.message_dialog)

        message.visibility = View.VISIBLE
        title.text = getString(R.string.error)
        message.text = msg

        btnDialog.setOnClickListener {
            dialog.dismiss()
            resumeScanner()
        }

        dialog.show()
    }

    override fun onOkButton() {
        startScanner()
    }
}