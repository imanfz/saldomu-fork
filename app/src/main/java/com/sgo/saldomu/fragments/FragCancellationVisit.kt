package com.sgo.saldomu.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.sgo.saldomu.BuildConfig
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
import com.sgo.saldomu.widgets.BaseFragment
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_cancelation_visit.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FragCancellationVisit : BaseFragment() {

    private var memberCode: String = ""
    private var commCode: String = ""
    private var fromFormat: DateFormat? = null
    private var toFormat: DateFormat? = null
    private var date_visit: String? = null
    private var dpd: DatePickerDialog? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        v = inflater.inflate(R.layout.frag_cancelation_visit, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val args = arguments!!
        memberCode = args.getString(DefineValue.MEMBER_CODE)!!
        commCode = args.getString(DefineValue.COMMUNITY_CODE)!!

        fromFormat = SimpleDateFormat("yyyy-MM-dd", Locale("ID", "INDONESIA"))
        toFormat = SimpleDateFormat("dd-MM-yyyy", Locale("ID", "INDONESIA"))
        toFormat = SimpleDateFormat("dd-M-yyyy", Locale("ID", "INDONESIA"))

        val c = Calendar.getInstance()
        val dateNow: String = (fromFormat as SimpleDateFormat).format(c.time)
        Timber.d("date now profile:%s", dateNow)

        dpd = DatePickerDialog.newInstance(
                dobPickerSetListener,
                c[Calendar.YEAR],
                c[Calendar.MONTH],
                c[Calendar.DAY_OF_MONTH]
        )

        tv_nextVisit.setOnClickListener { view: View? ->
            if (fragmentManager != null) {
                dpd!!.show(fragmentManager!!, "asd")
            }
        }

        btnProses.setOnClickListener { view: View? ->
            if (inputValidation())
            {
                cancelTrx()
            }
        }

    }

    private fun cancelTrx()
    {
        try {
            showProgressDialog()


            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CANCEL_VISIT_DGI,extraSignature)

            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_CODE] = commCode
            params[WebParams.MEMBER_CODE] = memberCode
            params[WebParams.NEXT_VISIT_DATE] = date_visit
            params[WebParams.LATITUDE] = sp.getDouble(DefineValue.LATITUDE_UPDATED, 0.0)
            params[WebParams.LONGITUDE] = sp.getDouble(DefineValue.LONGITUDE_UPDATED, 0.0)
            params[WebParams.REASON] = et_reason.text.toString()
            params[WebParams.APP_ID] = BuildConfig.APP_ID
            Timber.d("isi params cancel visit:$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CANCEL_VISIT_DGI, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val message = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response cancel visit: $response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        showDialog()
                                    }
                                    WebParams.LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
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
                                        alertDialogMaintenance.showDialogMaintenance(activity)
                                    }
                                    else -> {
                                        Timber.d("Error cancel visit:$response")

                                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
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

    private fun inputValidation(): Boolean
    {
        if (et_reason.text.toString().equals("") || et_reason.text.toString().length==0){
            Toast.makeText(context, Objects.requireNonNull(context)!!.getString(R.string.please_fill_reason), Toast.LENGTH_SHORT).show()
            return false
        }
        else if (tv_nextVisit.text.toString() == Objects.requireNonNull(activity)!!.getString(R.string.choose_date)) {
            Toast.makeText(context, Objects.requireNonNull(context)!!.getString(R.string.please_choose_date), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private val dobPickerSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        val dedate = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
        Timber.d("masuk date picker")
        try {
            date_visit = fromFormat!!.format(Objects.requireNonNull(toFormat!!.parse(dedate)))
            Timber.d("masuk date picker tanggal : %s", date_visit)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        tv_nextVisit.text = dedate
    }

    private fun showDialog() {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = getString(R.string.success)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = getString(R.string.cancelation_success)

        dialog.btn_dialog_notification_ok.setOnClickListener {
            dialog.dismiss()
            activity!!.finish()
        }
        dialog.show()
    }
}