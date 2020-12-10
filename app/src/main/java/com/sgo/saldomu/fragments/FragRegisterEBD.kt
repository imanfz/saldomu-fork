package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
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
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_register_ebd.*
import org.json.JSONObject
import timber.log.Timber

class FragRegisterEBD : BaseFragment() {

    var provinsiID = ""
    var kabupatenID = ""
    var kecamatanID = ""
    var kelurahanID = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_register_ebd, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        val tokoEBDActivity = activity as TokoEBDActivity
        tokoEBDActivity.initializeToolbar(getString(R.string.shop_registration))
        et_store_name.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_store_owner_name.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_id_no.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_delivery_address.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_province.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_district.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_sub_district.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_urban_village.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_postal_code.onRightDrawableRegisterEBDClicked { it.text.clear() }
        btn_submit.setOnClickListener { if (inputValidation()) submitRegisterEBD() }
        getLocationData()
    }

    private fun inputValidation(): Boolean {
        if (et_store_name.text!!.isEmpty()){
            et_store_name.requestFocus()
            et_store_name.error = getString(R.string.store_name_required)
            return false
        }
        if (et_store_owner_name.text!!.isEmpty()){
            et_store_owner_name.requestFocus()
            et_store_owner_name.error = getString(R.string.store_owner_name_required)
            return false
        }
        if (et_id_no.text!!.isEmpty()){
            et_id_no.requestFocus()
            et_id_no.error = getString(R.string.owner_id_no_required)
            return false
        }
        if (et_delivery_address.text!!.isEmpty()){
            et_delivery_address.requestFocus()
            et_delivery_address.error = getString(R.string.delivery_address_required)
            return false
        }
        if (et_postal_code.text!!.isEmpty()){
            et_postal_code.requestFocus()
            et_postal_code.error = getString(R.string.postal_code_required)
            return false
        }
        return true
    }

    private fun getLocationData() {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LOCATION_DATA)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.COMM_ID] = MyApiClient.COMM_ID
        if (provinsiID != "")
            params[WebParams.PROVINSI_ID] = provinsiID
        if (kabupatenID != "")
            params[WebParams.KABUPATEN_ID] = kabupatenID
        if (kecamatanID != "")
            params[WebParams.KECAMATAN_ID] = kecamatanID

        Timber.d("isi params get loc data :%s", params.toString())
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LOCATION_DATA, params, object : ObjListeners {
            override fun onResponses(response: JSONObject?) {

            }

            override fun onError(throwable: Throwable?) {
                dismissProgressDialog()
            }

            override fun onComplete() {
                dismissProgressDialog()
            }

        })
    }

    private fun submitRegisterEBD() {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REGISTER_EBD, "")
        params[WebParams.VERIFICATION_ID] = et_id_no.text.toString()
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.LATITUDE] = sp.getDouble(DefineValue.LATITUDE_UPDATED, 0.0)
        params[WebParams.LONGITUDE] = sp.getDouble(DefineValue.LONGITUDE_UPDATED, 0.0)
        params[WebParams.ADDRESS] = et_delivery_address.text.toString()
        params[WebParams.PROVINCE] = provinsiID
        params[WebParams.CITY] = kabupatenID
        params[WebParams.DISTRICT] = kecamatanID
        params[WebParams.VILLAGE] = kelurahanID
        params[WebParams.ZIP_CODE] = et_postal_code.text.toString()

        Timber.d("isi params register edb:%s", params.toString())
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REGISTER_EBD, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val code = response.getString(WebParams.ERROR_CODE)
                val message = response.getString(WebParams.ERROR_MESSAGE)
                when (code) {
                    WebParams.SUCCESS_CODE -> {
                        showDialog(response)
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
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
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

    @SuppressLint("SetTextI18n")
    private fun showDialog(response: JSONObject) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = resources.getString(R.string.shop_registration_success)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = getString(R.string.appname) + " " + getString(R.string.dialog_token_message_sms)

        dialog.btn_dialog_notification_ok.setOnClickListener {
            dialog.dismiss()
            fragmentManager!!.popBackStack()
        }
        dialog.show()
    }
}

@SuppressLint("ClickableViewAccessibility")
fun EditText.onRightDrawableRegisterEBDClicked(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is EditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}