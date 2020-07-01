package com.sgo.saldomu.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.NoHPFormat
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_otp.*
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class OTPActivity : BaseActivity() {

    private var countDownTimer: CountDownTimer? = null
    private var dateDOB: String? = null
    private var tokenID: String? = null
    private var userID: String? = null
    override fun getLayoutResource(): Int {
        return R.layout.activity_otp
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tv_email_value.text = intent.getStringExtra(DefineValue.USER_EMAIL)
        dateDOB = intent.getStringExtra(DefineValue.PROFILE_DOB)
        userID = NoHPFormat.formatTo62(intent.getStringExtra(DefineValue.CURR_USERID))

        initiateCountDownTimerForResendOTP()

        btnSend.setOnClickListener {
            if (inputValidation()) {
                tokenID = pin_view.text.toString()
                confirmOTP()
            }
        }

        btnResend.setOnClickListener {
            getOTP()
        }
    }

    private fun initiateCountDownTimerForResendOTP() {
        tv_countdown.visibility = View.VISIBLE
        countDownTimer = object : CountDownTimer(300000, 1000) {
            var sisa: String? = null
            override fun onTick(l: Long) {
                sisa = String.format(getString(R.string.time_left) + " %02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(l) % 60)
                tv_countdown.text = sisa

                btnResend.isEnabled = false
                btnResend.background = resources.getDrawable(R.drawable.rounded_background_button_disabled)
            }

            override fun onFinish() {
                btnResend.isEnabled = true
                btnResend.background = resources.getDrawable(R.drawable.rounded_background_blue)
                tv_countdown.visibility = View.GONE
            }
        }.start()
    }

    fun inputValidation(): Boolean {
        if (pin_view.text.toString().length != 6) {
            Toast.makeText(this, resources.getString(R.string.otp_validation), Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun confirmOTP() {
        showProgressDialog()

        val link = MyApiClient.LINK_VALIDATE_OTP_RESET_PIN
        val subStringLink = link.substring(link.indexOf("saldomu/"))
        extraSignature = userID + tokenID
        val params = RetrofitService.getInstance().getSignatureSecretKey(link, extraSignature)
        val uuid: String = params[WebParams.RC_UUID].toString()
        val dateTime: String = params[WebParams.RC_DTIME].toString()
        params[WebParams.USER_ID] = userID
        params[WebParams.COMM_ID] = MyApiClient.COMM_ID
        params[WebParams.TOKEN_ID] = RSA.opensslEncrypt(uuid, dateTime, userID, tokenID, subStringLink)

        Timber.d("isi param validate otp reset pin:$params")
        RetrofitService.getInstance().PostJsonObjRequest(link, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        dismissProgressDialog()
                        val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                        var code = response.getString(WebParams.ERROR_CODE)
                        var message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                val i = Intent(applicationContext, CreatePIN::class.java)
                                i.putExtra(DefineValue.RESET_PIN, true)
                                i.putExtra(DefineValue.TOKEN_ID, tokenID)
                                i.putExtra(DefineValue.CURR_USERID, userID)
                                startActivity(i)
                                finish()
                            }
                            WebParams.LOGOUT_CODE -> {
                                Timber.d("isi response autologout:$response")
                                val message = response.getString(WebParams.ERROR_MESSAGE)
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(this@OTPActivity, message)
                            }
                            DefineValue.ERROR_9333 -> {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(this@OTPActivity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            }
                            DefineValue.ERROR_0066 -> {
                                Timber.d("isi response maintenance:$response")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(this@OTPActivity, model.error_message)
                            }
                            else -> {
                                Timber.d("Error forgot pin:$response")
                                if (message.contains("Kode Verifikasi"))
                                    Toast.makeText(this@OTPActivity, message, Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(this@OTPActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onError(throwable: Throwable?) {
                        dismissProgressDialog()
                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                    }

                })
    }

    private fun getOTP() {
        showProgressDialog()

        extraSignature = userID+ dateDOB

        val params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_REQUEST_RESET_PIN, extraSignature)

        params[WebParams.USER_ID] = userID
        params[WebParams.COMM_ID] = MyApiClient.COMM_ID
        params[WebParams.CUST_BIRTH_DATE] = dateDOB

        Timber.d("isi params reset pin:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQUEST_RESET_PIN, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                        var code = response.getString(WebParams.ERROR_CODE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                Toast.makeText(this@OTPActivity, getString(R.string.success_resend_otp), Toast.LENGTH_SHORT).show()
                                initiateCountDownTimerForResendOTP()
                                pin_view.setText("")
                            }
                            WebParams.LOGOUT_CODE -> {
                                Timber.d("isi response autologout:$response")
                                val message = response.getString(WebParams.ERROR_MESSAGE)
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(this@OTPActivity, message)
                            }
                            DefineValue.ERROR_9333 -> {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(this@OTPActivity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            }
                            DefineValue.ERROR_0066 -> {
                                Timber.d("isi response maintenance:$response")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(this@OTPActivity, model.error_message)
                            }
                            else -> {
                                Timber.d("Error forgot pin:$response")
                                code = response.getString(WebParams.ERROR_MESSAGE)

                                Toast.makeText(this@OTPActivity, code, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onError(throwable: Throwable?) {

                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                    }
                })
    }
}
