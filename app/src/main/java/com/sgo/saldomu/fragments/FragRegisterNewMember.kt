package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.MapsActivity
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.databinding.FragRegisterEbdBinding
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.AnchorListItem
import com.sgo.saldomu.models.retrofit.*
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_notification.*
import org.json.JSONObject
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class FragRegisterNewMember : BaseFragment() {

    var provinceID = ""
    var districtID = ""
    var subDistrictID = ""
    var anchorCodeEspay = ""
    var memberDOB = ""
    var latitude = 0.0
    var longitude = 0.0

    val provinceList: ArrayList<ProvinceModel> = arrayListOf()
    val provincesNameList: ArrayList<String> = arrayListOf()
    val districtList: ArrayList<DistrictModel> = arrayListOf()
    val districtNameList: ArrayList<String> = arrayListOf()
    val subDistrictList: ArrayList<SubDistrictModel> = arrayListOf()
    val subDistrictNameList: ArrayList<String> = arrayListOf()
    val urbanVillageList: ArrayList<UrbanVillageModel> = arrayListOf()
    val urbanVillageNameList: ArrayList<String> = arrayListOf()
    val anchorList = ArrayList<AnchorListItem>()

    @SuppressLint("SimpleDateFormat")
    var fromFormat: DateFormat = SimpleDateFormat()

    private var binding: FragRegisterEbdBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragRegisterEbdBinding.inflate(inflater, container, false)
        v = binding!!.root
        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        val openMap = View.OnClickListener { v: View? ->
            startActivityForResult(
                Intent(
                    activity,
                    MapsActivity::class.java
                ), 100
            )
        }
        val tokoEBDActivity = activity as TokoEBDActivity
        tokoEBDActivity.initializeToolbar(getString(R.string.shop_registration))

        binding!!.etStoreName.onRightDrawableRegisterEBDClicked { it.text.clear() }
        binding!!.etStorePhone.onRightDrawableRegisterEBDClicked { it.text.clear() }
        binding!!.etStorePhone.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(13))
        binding!!.etStoreOwnerName.onRightDrawableRegisterEBDClicked { it.text.clear() }
        binding!!.etIdNo.onRightDrawableRegisterEBDClicked { it.text.clear() }
        binding!!.etDeliveryAddress.onRightDrawableRegisterEBDClicked { it.text.clear() }
        binding!!.etPostalCode.onRightDrawableRegisterEBDClicked { it.text.clear() }
        binding!!.llSetLocation.setOnClickListener(openMap)
        binding!!.changeLocation.setOnClickListener(openMap)
        binding!!.cbTermsncond.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                binding!!.btnSubmit.isEnabled = true
                binding!!.btnSubmit.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.rounded_background_blue, null)
            } else {
                binding!!.btnSubmit.isEnabled = false
                binding!!.btnSubmit.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.rounded_background_button_disabled,
                    null
                )
            }
        }
        binding!!.tvTermsncond.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(MyApiClient.LINK_TERMS_AND_CONDITION)
                )
            )
        }
        binding!!.btnSubmit.setOnClickListener { if (inputValidation()) submitRegisterEBD() }

        binding!!.districtAutoText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) binding!!.districtAutoText.showDropDown() }
        binding!!.subDistrictAutoText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) binding!!.subDistrictAutoText.showDropDown() }
        binding!!.urbanVillageAutoText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) binding!!.urbanVillageAutoText.showDropDown() }
        binding!!.provinceAutoText.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                for (i in 0 until provinceList.size) {
                    if (provinceList[i].provinceName == binding!!.provinceAutoText.text.toString()) {
                        provinceID = provinceList[i].provinceCode
                        binding!!.districtAutoText.setText("")
                        binding!!.subDistrictAutoText.setText("")
                        binding!!.urbanVillageAutoText.setText("")
                    }
                }
                getLocationData()
            }
        binding!!.districtAutoText.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                for (i in 0 until districtList.size) {
                    if (districtList[i].districtName == binding!!.districtAutoText.text.toString()) {
                        districtID = districtList[i].districtCode
                        binding!!.subDistrictAutoText.setText("")
                        binding!!.urbanVillageAutoText.setText("")
                    }
                }
                getLocationData()
            }
        binding!!.subDistrictAutoText.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                for (i in 0 until subDistrictList.size) {
                    if (subDistrictList[i].subDistrictName == binding!!.subDistrictAutoText.text.toString()) {
                        subDistrictID = subDistrictList[i].subDistrictCode
                        binding!!.urbanVillageAutoText.setText("")
                    }
                }
                getLocationData()
            }
        getLocationData()
        getListAnchor()

        binding!!.tvDob.setOnClickListener {
            val c = Calendar.getInstance()
            val yearNow = c.get(Calendar.YEAR)
            val monthNow = c.get(Calendar.MONTH)
            val dayNow = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(context!!, { _, year, monthOfYear, dayOfMonth ->
                val calendar: Calendar = Calendar.getInstance()
                calendar.set(year, monthOfYear, dayOfMonth)

                val monthDisplay = monthOfYear + 1
                binding!!.tvDob.text = "$dayOfMonth - $monthDisplay - $year"

                fromFormat = SimpleDateFormat("yyyy-MM-dd", Locale("ID", "INDONESIA"))
                memberDOB = fromFormat.format(calendar.time)
            }, yearNow, monthNow, dayNow)
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
            datePickerDialog.show()
        }
    }

    private fun inputValidation(): Boolean {
        if (binding!!.etStoreName.text!!.isEmpty()) {
            binding!!.etStoreName.requestFocus()
            binding!!.etStoreName.error = getString(R.string.store_name_required)
            return false
        }
        if (binding!!.etStorePhone.text!!.isEmpty() || binding!!.etStorePhone.text!!.length < 10) {
            binding!!.etStorePhone.requestFocus()
            binding!!.etStorePhone.error = getString(R.string.store_phone_required)
            return false
        }
        if (binding!!.etStoreOwnerName.text!!.isEmpty()) {
            binding!!.etStoreOwnerName.requestFocus()
            binding!!.etStoreOwnerName.error = getString(R.string.store_owner_name_required)
            return false
        }
        if (binding!!.etIdNo.text!!.isEmpty()) {
            binding!!.etIdNo.requestFocus()
            binding!!.etIdNo.error = getString(R.string.owner_id_no_required)
            return false
        }
        if (binding!!.etIdNo.text!!.length != 16) {
            binding!!.etIdNo.requestFocus()
            binding!!.etIdNo.error = getString(R.string.owner_id_no_length)
            return false
        }
        if (binding!!.etDeliveryAddress.text!!.isEmpty()) {
            binding!!.etDeliveryAddress.requestFocus()
            binding!!.etDeliveryAddress.error = getString(R.string.delivery_address_required)
            return false
        }
        if (binding!!.provinceAutoText.text!!.isEmpty()) {
            binding!!.provinceAutoText.requestFocus()
            binding!!.provinceAutoText.error = getString(R.string.province_validation)
            return false
        }
        if (binding!!.districtAutoText.text!!.isEmpty()) {
            binding!!.districtAutoText.requestFocus()
            binding!!.districtAutoText.error = getString(R.string.district_validation)
            return false
        }
        if (binding!!.subDistrictAutoText.text!!.isEmpty()) {
            binding!!.subDistrictAutoText.requestFocus()
            binding!!.subDistrictAutoText.error = getString(R.string.sub_district_validation)
            return false
        }
        if (binding!!.urbanVillageAutoText.text!!.isEmpty()) {
            binding!!.urbanVillageAutoText.requestFocus()
            binding!!.urbanVillageAutoText.error = getString(R.string.urban_village_validation)
            return false
        }
        if (binding!!.etPostalCode.text!!.isEmpty()) {
            binding!!.etPostalCode.requestFocus()
            binding!!.etPostalCode.error = getString(R.string.postal_code_required)
            return false
        }
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(context, getString(R.string.location_required), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (memberDOB == "") {
            Toast.makeText(context, getString(R.string.rsb_validation_dob), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun getLocationData() {
        showProgressDialog()
        val params = RetrofitService.getInstance()
            .getSignatureSecretKey(MyApiClient.LINK_GET_LOCATION_DATA, "")
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.COMM_ID] = MyApiClient.COMM_ID
        if (provinceID != "")
            params[WebParams.PROVINSI_ID] = provinceID
        if (districtID != "")
            params[WebParams.KABUPATEN_ID] = districtID
        if (subDistrictID != "")
            params[WebParams.KECAMATAN_ID] = subDistrictID

        Timber.d("isi params get loc data :%s", params.toString())
        RetrofitService.getInstance()
            .PostJsonObjRequest(MyApiClient.LINK_GET_LOCATION_DATA, params, object : ObjListeners {
                override fun onResponses(response: JSONObject) {
                    val jsonArray = response.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = JSONObject(jsonArray[i].toString())
                        when {
                            provinceID == "" -> {
                                provinceList.clear()
                                provinceList.add(
                                    ProvinceModel(
                                        jsonObject.optString(WebParams.KODE_PROVINSI),
                                        jsonObject.optString(WebParams.NAMA_PROVINSI)
                                    )
                                )
                                provincesNameList.clear()
                                provincesNameList.add(provinceList[i].provinceName)
                            }
                            districtID == "" -> {
                                districtList.clear()
                                districtList.add(
                                    DistrictModel(
                                        jsonObject.optString(WebParams.KODE_KOT_KAB),
                                        jsonObject.optString(WebParams.NAMA_KOT_KAB)
                                    )
                                )
                                districtNameList.clear()
                                districtNameList.add(districtList[i].districtName)
                            }
                            subDistrictID == "" -> {
                                subDistrictList.clear()
                                subDistrictList.add(
                                    SubDistrictModel(
                                        jsonObject.optString(WebParams.KODE_KECAMATAN),
                                        jsonObject.optString(WebParams.NAMA_KECAMATAN)
                                    )
                                )
                                subDistrictNameList.clear()
                                subDistrictNameList.add(subDistrictList[i].subDistrictName)
                            }
                            else -> {
                                urbanVillageList.clear()
                                urbanVillageList.add(
                                    UrbanVillageModel(
                                        jsonObject.optString(
                                            WebParams.KODE_LUR_DES
                                        ), jsonObject.optString(WebParams.NAMA_LUR_DES)
                                    )
                                )
                                urbanVillageNameList.clear()
                                urbanVillageNameList.add(urbanVillageList[i].urbanVillageName)
                            }
                        }

                    }

                    val provincesAdapter = ArrayAdapter(
                        context!!,
                        android.R.layout.simple_spinner_dropdown_item,
                        provincesNameList
                    )
                    binding!!.provinceAutoText.setAdapter(provincesAdapter)
                    val districtAdapter = ArrayAdapter(
                        context!!,
                        android.R.layout.simple_spinner_dropdown_item,
                        districtNameList
                    )
                    binding!!.districtAutoText.setAdapter(districtAdapter)
                    val subDistrictAdapter = ArrayAdapter(
                        context!!,
                        android.R.layout.simple_spinner_dropdown_item,
                        subDistrictNameList
                    )
                    binding!!.subDistrictAutoText.setAdapter(subDistrictAdapter)
                    val urbanVillageAdapter = ArrayAdapter(
                        context!!,
                        android.R.layout.simple_spinner_dropdown_item,
                        urbanVillageNameList
                    )
                    binding!!.urbanVillageAutoText.setAdapter(urbanVillageAdapter)
                }

                override fun onError(throwable: Throwable?) {
                    dismissProgressDialog()
                }

                override fun onComplete() {
                    dismissProgressDialog()
                }

            })
    }

    private fun getListAnchor() {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_ANCHOR)
        params[WebParams.USER_ID] = userPhoneID
        Timber.d("isi params list anchor:%s", params.toString())

        RetrofitService.getInstance()
            .PostJsonObjRequest(MyApiClient.LINK_GET_LIST_ANCHOR, params, object : ObjListeners {
                override fun onResponses(response: JSONObject) {
                    val jsonArray = response.getJSONArray(WebParams.ANCHOR_LIST)
                    for (i in 0 until jsonArray.length()) {
                        val anchorListItem = getGson().fromJson(
                            jsonArray.getJSONObject(i).toString(),
                            AnchorListItem::class.java
                        )
                        anchorList.add(anchorListItem)
                    }
                    val anchorListOption = ArrayList<String>()
                    for (i in anchorList.indices) {
                        anchorListOption.add(anchorList[i].anchorName)
                    }
                    val anchorOptionsAdapter = ArrayAdapter(
                        requireActivity(),
                        android.R.layout.simple_spinner_item,
                        anchorListOption
                    )
                    anchorOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding!!.spinnerAnchor.adapter = anchorOptionsAdapter
                    binding!!.spinnerAnchor.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                anchorCodeEspay = anchorList[p2].anchorCode
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {

                            }

                        }

                }

                override fun onError(throwable: Throwable?) {
                    getListAnchor()
                    dismissProgressDialog()
                }

                override fun onComplete() {
                    dismissProgressDialog()
                }

            })
    }

    private fun submitRegisterEBD() {
        showProgressDialog()
        val custName = binding!!.etStoreOwnerName.text.toString()
        val shopName = binding!!.etStoreName.text.toString()
        val params = RetrofitService.getInstance()
            .getSignature(MyApiClient.LINK_REGISTER_NEW_EBD, userPhoneID + shopName)
        params[WebParams.VERIFICATION_ID] = binding!!.etIdNo.text.toString()
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.LATITUDE] = latitude
        params[WebParams.LONGITUDE] = longitude
        params[WebParams.ADDRESS] = binding!!.etDeliveryAddress.text.toString().replace("\n", " ")
        params[WebParams.PROVINCE] = binding!!.provinceAutoText.text.toString()
        params[WebParams.CITY] = binding!!.districtAutoText.text.toString()
        params[WebParams.DISTRICT] = binding!!.subDistrictAutoText.text.toString()
        params[WebParams.VILLAGE] = binding!!.urbanVillageAutoText.text.toString()
        params[WebParams.ZIP_CODE] = binding!!.etPostalCode.text.toString()
        params[WebParams.CUST_NAME] = custName
        params[WebParams.SHOP_NAME] = shopName
        params[WebParams.CONTACT_NUMBER] = binding!!.etStorePhone.text.toString()
        params[WebParams.ANCHOR_CODE_ESPAY] = anchorCodeEspay
        params[WebParams.BIRTH_DATE] = memberDOB

        Timber.d("isi params register store:%s", params.toString())
        RetrofitService.getInstance()
            .PostJsonObjRequest(MyApiClient.LINK_REGISTER_NEW_EBD, params, object : ObjListeners {
                override fun onResponses(response: JSONObject) {
                    val code = response.getString(WebParams.ERROR_CODE)
                    val message = response.getString(WebParams.ERROR_MESSAGE)
                    when (code) {
                        WebParams.SUCCESS_CODE -> {
                            showDialog(message)
                        }
                        WebParams.LOGOUT_CODE -> {
                            AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                        }
                        DefineValue.ERROR_9333 -> {
                            val model = gson.fromJson(response.toString(), jsonModel::class.java)
                            val appModel = model.app_data
                            AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                activity,
                                appModel.type,
                                appModel.packageName,
                                appModel.downloadUrl
                            )
                        }
                        DefineValue.ERROR_0066 -> {
                            AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
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
    private fun showDialog(message: String) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = resources.getString(R.string.shop_registration_success)
        dialog.message_dialog.visibility = View.VISIBLE
//        dialog.message_dialog.text = getString(R.string.register_success_wait_for_verification)
        dialog.message_dialog.text = message

        dialog.btn_dialog_notification_ok.setOnClickListener {
            dialog.dismiss()
            requireFragmentManager().popBackStack()
        }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> if (resultCode == 201) {
                if (data != null && data.extras != null) {
                    val address = data.getStringExtra("address")
                    binding!!.changeLocation.visibility = View.VISIBLE
                    binding!!.tvAddress.visibility = View.VISIBLE
                    binding!!.tvAddress.text = address
                    binding!!.llSetLocation.visibility = View.GONE
                    longitude = data.getDoubleExtra("longitude", 0.0)
                    latitude = data.getDoubleExtra("latitude", 0.0)
                }
            }
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
}