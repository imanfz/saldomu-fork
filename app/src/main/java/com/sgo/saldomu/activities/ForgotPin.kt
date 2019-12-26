package com.sgo.saldomu.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.View
import android.view.Window
import android.widget.Toast
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
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_forgot_pin.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ForgotPin : BaseActivity() {

    private lateinit var viewLayout: View
    private lateinit var dpd: DatePickerDialog
    private var fromFormat: DateFormat? = null
    private var toFormat: DateFormat? = null
    private var toFormat2: DateFormat? = null
    private var dateNow: String? = null
    private var dedate: String? = null
    private var date_dob: String? = null

    override fun getLayoutResource(): Int {
        return R.layout.activity_forgot_pin
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "")!!.isEmpty() && sp.getString(DefineValue.IS_POS, "N").equals("N", ignoreCase = true)) {
            et_userid.setText(sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, ""))
            !et_userid.isEnabled
        }

        fromFormat = SimpleDateFormat("yyyy-MM-dd", Locale("ID", "INDONESIA"))
        toFormat = SimpleDateFormat("dd-MM-yyyy", Locale("ID", "INDONESIA"))
        toFormat2 = SimpleDateFormat("dd-M-yyyy", Locale("ID", "INDONESIA"))

        val c = Calendar.getInstance()
        dateNow = (fromFormat as SimpleDateFormat).format(c.time)
        Timber.d("date now profile:$dateNow")

        tv_dob_forgotpin.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                    DatePickerDialog(
                            this,
                            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                                dedate = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                                Timber.d("masuk date picker dob")
                                try {
                                    date_dob = (fromFormat as SimpleDateFormat).format((toFormat2 as SimpleDateFormat).parse(dedate))
                                    Timber.d("masuk date picker dob masuk tanggal : $date_dob")
                                } catch (e: ParseException) {
                                    e.printStackTrace()
                                }

                                tv_dob_forgotpin.text = dedate
                            },
                            year,
                            month,
                            day
                    )
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
            datePickerDialog.show()
        }

        btn_submit_forgot_pin.setOnClickListener {
            if (inputValidation()) {
                sentForgotPin()
            }
        }
    }

    fun inputValidation(): Boolean {
        var compare = 100
        if (date_dob != null) {
            var dob: Date? = null
            var now: Date? = null
            try {
                dob = fromFormat?.parse(date_dob)
                now = fromFormat?.parse(dateNow)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (dob != null) {
                if (now != null) {
                    compare = dob.compareTo(now)
                }
            }
            Timber.d("compare date:" + Integer.toString(compare))
        }
        if (et_userid.text.toString().isEmpty() || et_userid.text.toString().isEmpty()) {
            et_userid.requestFocus()
            et_userid.error = resources.getString(R.string.user_id_validation)
            return false
        } else if (tv_dob_forgotpin.text == getString(R.string.rsb_hint_dob)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date_empty))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (compare == 100) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date_empty))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (compare >= 0) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        }
        return true
    }

    fun sentForgotPin() {
        try {
            showProgressDialog()

            extraSignature = et_userid.text.toString() + date_dob

            val params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_FORGOT_PIN, extraSignature)

            params[WebParams.USER_ID] = et_userid.text.toString()
            params[WebParams.COMM_ID] = MyApiClient.COMM_ID
            params[WebParams.CUST_BIRTH_DATE] = date_dob
            Timber.d("isi params forgot pin:$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_FORGOT_PIN, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                                var code = response.getString(WebParams.ERROR_CODE)
                                Timber.d("isi response forgot pin: $response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        dismissProgressDialog()
                                        showDialog()
                                    }
                                    WebParams.LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
                                        val message = response.getString(WebParams.ERROR_MESSAGE)
                                        val test = AlertDialogLogout.getInstance()
                                        test.showDialoginActivity(this@ForgotPin, message)
                                    }
                                    DefineValue.ERROR_9333 -> {
                                        Timber.d("isi response app data:" + model.app_data)
                                        val appModel = model.app_data
                                        val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                        alertDialogUpdateApp.showDialogUpdate(this@ForgotPin, appModel.type, appModel.packageName, appModel.downloadUrl)
                                    }
                                    DefineValue.ERROR_0066 -> {
                                        Timber.d("isi response maintenance:$response")
                                        val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                        alertDialogMaintenance.showDialogMaintenance(this@ForgotPin, model.error_message)
                                    }
                                    else -> {
                                        Timber.d("Error forgot pin:$response")
                                        code = response.getString(WebParams.ERROR_MESSAGE)

                                        Toast.makeText(this@ForgotPin, code, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = getString(R.string.success)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = getString(R.string.success_forgot_pin)

        dialog.btn_dialog_notification_ok.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}
