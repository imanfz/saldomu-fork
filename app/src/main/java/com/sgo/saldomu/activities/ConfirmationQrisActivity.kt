package com.sgo.saldomu.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonObject
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
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
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_confirmation_qris.*
import kotlinx.android.synthetic.main.dialog_top_up.*
import org.json.JSONObject
import timber.log.Timber

class ConfirmationQrisActivity : BaseActivity(), ReportBillerDialog.OnDialogOkCallback {

    private var txId = ""
    private var commCode = ""
    private var commId = ""
    private var productCode = ""
    private var percentage = 0.0

    override fun getLayoutResource(): Int {
        return R.layout.activity_confirmation_qris
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                ToggleKeyboard.hide_keyboard(this)
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBarTitle = getString(R.string.transaction_confirmation)
        setActionBarIcon(R.drawable.ic_arrow_left)

        val qrisParsingModel = getGson().fromJson(
            intent.getStringExtra(DefineValue.RESPONSE),
            QrisParsingModel::class.java
        )
        tv_acquire_name_value.text = qrisParsingModel.nnsMemberName
        tv_payment_destination_name_value.text = qrisParsingModel.merchantName
        tv_payment_destination_city_value.text = qrisParsingModel.merchantCity
        val transactionAmount = qrisParsingModel.transactionAmount
        if (transactionAmount != "") {
            edit_text_amount_transfer.setText(transactionAmount)
            edit_text_amount_transfer.isEnabled = false
        } else {
            if (qrisParsingModel.percentage!!.toInt() != 0)
                percentage = qrisParsingModel.percentage.toDouble() / 100

            edit_text_amount_transfer.requestFocus()
            ToggleKeyboard.show_keyboard(this)
        }

        edit_text_amount_transfer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                edit_text_amount_transfer.removeTextChangedListener(this)
                val value = edit_text_amount_transfer.text.toString()
                if (value != "") {
                    if (value.startsWith("0") && !value.startsWith("0.")) {
                        edit_text_amount_transfer.setText("")
                        edit_text_fee_amount.setText("")
                    } else {
                        val str = NumberTextWatcherForThousand.trimCommaOfString(value)
                        edit_text_amount_transfer.setText(
                            NumberTextWatcherForThousand.getDecimalFormattedString(
                                str
                            )
                        )
                        if (percentage > 0) {
                            val feeAmount = str.toInt() * percentage
                            if (feeAmount >= 1)
                                edit_text_fee_amount.setText(feeAmount.toInt().toString())
                        }
                    }
                    edit_text_amount_transfer.setSelection(edit_text_amount_transfer.text.toString().length)
                }
                edit_text_amount_transfer.addTextChangedListener(this)
            }

        })
        edit_text_fee_amount.addTextChangedListener(
            NumberTextWatcherForThousand(
                edit_text_fee_amount
            )
        )

        edit_text_fee_amount.setText(qrisParsingModel.feeAmount)
        if (qrisParsingModel.indicatorType != "01")
            edit_text_fee_amount.isEnabled = false

        cancel_btn.setOnClickListener { finish() }
        proses_btn.setOnClickListener {
            if (edit_text_amount_transfer.text!!.isNotEmpty())
                paymentReqQris(
                    NumberTextWatcherForThousand.trimCommaOfString(
                        edit_text_amount_transfer.text.toString()
                    ), qrisParsingModel
                )
            else
                edit_text_amount_transfer.error = getString(R.string.payment_nominal_required)
        }
    }

    private fun paymentReqQris(amount: String, qrisParsingModel: QrisParsingModel) {
        showProgressDialog()

        var feeAmount = "0"
        if (edit_text_fee_amount.text!!.isNotEmpty())
            feeAmount =
                NumberTextWatcherForThousand.trimCommaOfString(edit_text_fee_amount.text.toString())

        val params = RetrofitService.getInstance().getSignature(
            MyApiClient.LINK_QRIS_PAYMENT_REQUEST,
            qrisParsingModel.commId + qrisParsingModel.memberId
        )
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.AMOUNT] = amount
        params[WebParams.ADMIN_FEE] = qrisParsingModel.adminFee
        params[WebParams.FEE_AMOUNT] = feeAmount
        params[WebParams.TOTAL_AMOUNT] =
            Integer.parseInt(amount) + Integer.parseInt(feeAmount) + Integer.parseInt(
                qrisParsingModel.adminFee!!
            )
        params[WebParams.INDICATOR_TYPE] = qrisParsingModel.indicatorType
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
        RetrofitService.getInstance()
            .PostJsonObjRequest(MyApiClient.LINK_QRIS_PAYMENT_REQUEST, params,
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
                                AlertDialogLogout.getInstance()
                                    .showDialoginActivity(this@ConfirmationQrisActivity, message)
                            }
                            DefineValue.ERROR_9333 -> {
                                val model =
                                    gson.fromJson(response.toString(), jsonModel::class.java)
                                val appModel = model.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                    this@ConfirmationQrisActivity,
                                    appModel.type,
                                    appModel.packageName,
                                    appModel.downloadUrl
                                )
                            }
                            DefineValue.ERROR_0066 -> {
                                AlertDialogMaintenance.getInstance()
                                    .showDialogMaintenance(this@ConfirmationQrisActivity, message)
                            }
                            DefineValue.ERROR_0338 -> {
                                showDialog(message)
                            }
                            DefineValue.ERROR_57 -> {
                                showDialogTopup(amount)
                            }
                            else -> {
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
                                    .show()
                                finish()
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
        UtilsLoader(this, sp).getFailedPIN(userPhoneID, object : OnLoadDataListener {
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
        val i = Intent(this, InsertPIN::class.java)
        if (attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, attempt)
        startActivityForResult(i, MainPage.REQUEST_FINISH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
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
        params[WebParams.PRODUCT_VALUE] =
            RSA.opensslEncryptCommID(commId, uuid, dateTime, userPhoneID, valuePin, subStringLink)
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
                            AlertDialogLogout.getInstance()
                                .showDialoginActivity(this@ConfirmationQrisActivity, message)
                        }
                        DefineValue.ERROR_9333 -> {
                            val model = gson.fromJson(response.toString(), jsonModel::class.java)
                            val appModel = model.app_data
                            AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                this@ConfirmationQrisActivity,
                                appModel.type,
                                appModel.packageName,
                                appModel.downloadUrl
                            )
                        }
                        DefineValue.ERROR_0066 -> {
                            AlertDialogMaintenance.getInstance()
                                .showDialogMaintenance(this@ConfirmationQrisActivity, message)
                        }
                        else -> {
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
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
        val params = RetrofitService.getInstance()
            .getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature)

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
                            AlertDialogLogout.getInstance()
                                .showDialoginActivity(this@ConfirmationQrisActivity, message)
                        } else if (code == DefineValue.ERROR_9333) {
                            val appModel = model.app_data
                            AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                this@ConfirmationQrisActivity,
                                appModel.type,
                                appModel.packageName,
                                appModel.downloadUrl
                            )
                        } else if (code == DefineValue.ERROR_0066) {
                            AlertDialogMaintenance.getInstance().showDialogMaintenance(
                                this@ConfirmationQrisActivity,
                                model.error_message
                            )
                        } else {
                            showDialog(message)
                        }
                    } else {
                        Toast.makeText(applicationContext, model.error_message, Toast.LENGTH_SHORT)
                            .show()
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
        args.putString(
            DefineValue.FEE,
            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.admin_fee)
        )
        args.putString(
            DefineValue.AMOUNT,
            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tx_amount)
        )

        val dAmount = response.tx_amount!!.toDouble()
        val dFee = response.admin_fee!!.toDouble()
        val totalAmount = dAmount + dFee

        args.putString(
            DefineValue.TOTAL_AMOUNT,
            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(totalAmount)
        )

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
        args.putString(DefineValue.MERCHANT_CITY, response.merchant_city)
        args.putString(DefineValue.MERCHANT_PAN, response.merchant_pan)
        args.putString(DefineValue.TERMINAL_ID, response.terminal_id)
        args.putString(DefineValue.TRX_ID_REF, response.trx_id_ref)

        dialog.arguments = args
        dialog.show(supportFragmentManager, ReportBillerDialog.TAG)
    }


    private fun showDialogTopup(amount: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_top_up)

        dialog.tv_remaining_balance.text =
            CurrencyFormat.format(sp.getString(DefineValue.BALANCE_AMOUNT, "0"))
        dialog.tv_paid_amount.text = CurrencyFormat.format(amount)
        dialog.btn_dialog_cancel.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.btn_dialog_top_up.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, TopUpActivity::class.java)
            intent.putExtra(DefineValue.IS_ACTIVITY_FULL, true)
            startActivity(intent)
        }
        dialog.show()
    }

    private fun showDialog(msg: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        val btnDialog = dialog.findViewById<Button>(R.id.btn_dialog_notification_ok)
        val title = dialog.findViewById<TextView>(R.id.title_dialog)
        val message = dialog.findViewById<TextView>(R.id.message_dialog)

        message.visibility = View.VISIBLE
        title.text = getString(R.string.error)
        message.text = msg

        btnDialog.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    override fun onOkButton() {
        finish()
    }
}