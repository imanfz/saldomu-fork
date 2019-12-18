package com.sgo.saldomu.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.NoHPFormat
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.*
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_forgot_pin.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FragForgotPIN : BaseFragment() {
    private lateinit var viewLayout: View
    private lateinit var dpd: DatePickerDialog
    private var fromFormat: DateFormat? = null
    private var toFormat: DateFormat? = null
    private var toFormat2: DateFormat? = null
    private var dateNow: String? = null
    private var dedate: String? = null
    private var date_dob: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewLayout = inflater.inflate(R.layout.frag_forgot_pin, container, false)
        return viewLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (!sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "")!!.isEmpty() && sp.getString(DefineValue.IS_POS, "N").equals("N", ignoreCase = true)) {
            et_userid.setText(sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, ""))
            et_userid.isEnabled == false
        }

        fromFormat = SimpleDateFormat("yyyy-MM-dd", Locale("ID", "INDONESIA"))
        toFormat = SimpleDateFormat("dd-MM-yyyy", Locale("ID", "INDONESIA"))
        toFormat2 = SimpleDateFormat("dd-M-yyyy", Locale("ID", "INDONESIA"))

        val c = Calendar.getInstance()
        dateNow = (fromFormat as SimpleDateFormat).format(c.time)
        Timber.d("date now profile:$dateNow")

        tv_dob_forgotpin.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                    DatePickerDialog(
                            activity,
                            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                                dedate = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                                Timber.d("masuk date picker dob")
//                                tv_dob_forgotpin.text = "$dayOfMonth / $monthOfYear/ $year"
                                try {
                                    date_dob = (fromFormat as SimpleDateFormat).format((toFormat2 as SimpleDateFormat).parse(dedate))
                                    Timber.d("masuk date picker dob masuk tanggal : $date_dob")
                                } catch (e: ParseException) {
                                    e.printStackTrace()
                                }

                                tv_dob_forgotpin.setText(dedate)
//                                date_dob = dedate
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

    private val dobPickerSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        dedate = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
        Timber.d("masuk date picker dob")
        try {
            date_dob = fromFormat?.format(toFormat2?.parse(dedate))
            Timber.d("masuk date picker dob masuk tanggal : $date_dob")
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        tv_dob_forgotpin.text = dedate
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
        if (et_userid.text.toString().length == 0 || et_userid.text.toString().isNullOrEmpty()) {
            et_userid.requestFocus()
            et_userid.setError(resources.getString(R.string.user_id_validation))
            return false
        } else if (tv_dob_forgotpin.text.equals(getString(R.string.rsb_hint_dob))) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date_empty))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (compare == 100) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date_empty))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (compare >= 0) {
            val builder = AlertDialog.Builder(activity)
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

            params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_FORGOT_PIN, extraSignature)

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
                                        test.showDialoginActivity(activity, message)
                                    }
                                    DefineValue.ERROR_9333 -> {
                                        Timber.d("isi response app data:" + model.app_data)
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
                                        Timber.d("Error forgot pin:$response")
                                        code = response.getString(WebParams.ERROR_MESSAGE)

                                        Toast.makeText(activity, code, Toast.LENGTH_SHORT).show()
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
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = getString(R.string.success)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = getString(R.string.success_forgot_pin)

        dialog.btn_dialog_notification_ok.setOnClickListener {
            dialog.dismiss()
            fragmentManager!!.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        dialog.show()
    }
}
