package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.MapsActivity
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
import com.sgo.saldomu.models.AnchorListItem
import com.sgo.saldomu.models.retrofit.*
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_register_ebd.*
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragRegisterNewMember : BaseFragment() {

    var provinceID = ""
    var districtID = ""
    var subDistrictID = ""
    var anchorCodeEspay = ""
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_register_ebd, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        val openMap = View.OnClickListener { v: View? -> startActivityForResult(Intent(activity, MapsActivity::class.java), 100) }
        val tokoEBDActivity = activity as TokoEBDActivity
        tokoEBDActivity.initializeToolbar(getString(R.string.shop_registration))
        et_store_name.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_store_owner_name.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_id_no.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_delivery_address.onRightDrawableRegisterEBDClicked { it.text.clear() }
        et_postal_code.onRightDrawableRegisterEBDClicked { it.text.clear() }
        ll_setLocation.setOnClickListener(openMap)
        change_location.setOnClickListener(openMap)
        btn_submit.setOnClickListener { if (inputValidation()) submitRegisterEBD() }


        province_auto_text.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            for (i in 0 until provinceList.size) {
                if (provinceList[i].provinceName == province_auto_text.text.toString()) {
                    provinceID = provinceList[i].provinceCode
                    district_auto_text.setText("")
                    sub_district_auto_text.setText("")
                    urban_village_auto_text.setText("")
                }
            }
            getLocationData()
        }
        district_auto_text.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            for (i in 0 until districtList.size) {
                if (districtList[i].districtName == district_auto_text.text.toString()) {
                    districtID = districtList[i].districtCode
                    sub_district_auto_text.setText("")
                    urban_village_auto_text.setText("")
                }
            }
            getLocationData()
        }
        sub_district_auto_text.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            for (i in 0 until subDistrictList.size) {
                if (subDistrictList[i].subDistrictName == sub_district_auto_text.text.toString()) {
                    subDistrictID = subDistrictList[i].subDistrictCode
                    urban_village_auto_text.setText("")
                }
            }
            getLocationData()
        }
        getLocationData()
        getListAnchor()
    }

    private fun inputValidation(): Boolean {
        if (et_store_name.text!!.isEmpty()) {
            et_store_name.requestFocus()
            et_store_name.error = getString(R.string.store_name_required)
            return false
        }
        if (et_store_owner_name.text!!.isEmpty()) {
            et_store_owner_name.requestFocus()
            et_store_owner_name.error = getString(R.string.store_owner_name_required)
            return false
        }
        if (et_id_no.text!!.isEmpty()) {
            et_id_no.requestFocus()
            et_id_no.error = getString(R.string.owner_id_no_required)
            return false
        }
        if (et_id_no.text!!.length != 16) {
            et_id_no.requestFocus()
            et_id_no.error = getString(R.string.owner_id_no_length)
            return false
        }
        if (et_delivery_address.text!!.isEmpty()) {
            et_delivery_address.requestFocus()
            et_delivery_address.error = getString(R.string.delivery_address_required)
            return false
        }
        if (province_auto_text.text!!.isEmpty()) {
            province_auto_text.requestFocus()
            province_auto_text.error = getString(R.string.province_validation)
            return false
        }
        if (district_auto_text.text!!.isEmpty()) {
            district_auto_text.requestFocus()
            district_auto_text.error = getString(R.string.district_validation)
            return false
        }
        if (sub_district_auto_text.text!!.isEmpty()) {
            sub_district_auto_text.requestFocus()
            sub_district_auto_text.error = getString(R.string.sub_district_validation)
            return false
        }
        if (urban_village_auto_text.text!!.isEmpty()) {
            urban_village_auto_text.requestFocus()
            urban_village_auto_text.error = getString(R.string.urban_village_validation)
            return false
        }
        if (et_postal_code.text!!.isEmpty()) {
            et_postal_code.requestFocus()
            et_postal_code.error = getString(R.string.postal_code_required)
            return false
        }
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(context, getString(R.string.location_required), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun getLocationData() {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_GET_LOCATION_DATA, "")
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.COMM_ID] = MyApiClient.COMM_ID
        if (provinceID != "")
            params[WebParams.PROVINSI_ID] = provinceID
        if (districtID != "")
            params[WebParams.KABUPATEN_ID] = districtID
        if (subDistrictID != "")
            params[WebParams.KECAMATAN_ID] = subDistrictID

        Timber.d("isi params get loc data :%s", params.toString())
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LOCATION_DATA, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val jsonArray = response.getJSONArray("data")
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = JSONObject(jsonArray[i].toString())
                    when {
                        provinceID == "" -> {
                            provinceList.add(ProvinceModel(jsonObject.optString(WebParams.KODE_PROVINSI), jsonObject.optString(WebParams.NAMA_PROVINSI)))
                            provincesNameList.add(provinceList[i].provinceName)
                        }
                        districtID == "" -> {
                            districtList.add(DistrictModel(jsonObject.optString(WebParams.KODE_KOT_KAB), jsonObject.optString(WebParams.NAMA_KOT_KAB)))
                            districtNameList.add(districtList[i].districtName)
                        }
                        subDistrictID == "" -> {
                            subDistrictList.add(SubDistrictModel(jsonObject.optString(WebParams.KODE_KECAMATAN), jsonObject.optString(WebParams.NAMA_KECAMATAN)))
                            subDistrictNameList.add(subDistrictList[i].subDistrictName)
                        }
                        else -> {
                            urbanVillageList.add(UrbanVillageModel(jsonObject.optString(WebParams.KODE_LUR_DES), jsonObject.optString(WebParams.NAMA_LUR_DES)))
                            urbanVillageNameList.add(urbanVillageList[i].urbanVillageName)
                        }
                    }

                }

                val provincesAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, provincesNameList)
                province_auto_text.setAdapter(provincesAdapter)
                val districtAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, districtNameList)
                district_auto_text.setAdapter(districtAdapter)
                val subDistrictAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, subDistrictNameList)
                sub_district_auto_text.setAdapter(subDistrictAdapter)
                val urbanVillageAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, urbanVillageNameList)
                urban_village_auto_text.setAdapter(urbanVillageAdapter)
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

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_ANCHOR, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val jsonArray = response.getJSONArray(WebParams.ANCHOR_LIST)
                for (i in 0 until jsonArray.length()) {
                    val anchorListItem = getGson().fromJson(jsonArray.getJSONObject(i).toString(), AnchorListItem::class.java)
                    anchorList.add(anchorListItem)
                }
                val anchorListOption = ArrayList<String>()
                for (i in anchorList.indices) {
                    anchorListOption.add(anchorList[i].anchorName)
                }
                val anchorOptionsAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, anchorListOption)
                anchorOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner_anchor.adapter = anchorOptionsAdapter
                spinner_anchor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
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
        val custName = et_store_owner_name.text.toString()
        val shopName = et_store_name.text.toString()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REGISTER_NEW_EBD, userPhoneID + shopName)
        params[WebParams.VERIFICATION_ID] = et_id_no.text.toString()
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.LATITUDE] = latitude
        params[WebParams.LONGITUDE] = longitude
        params[WebParams.ADDRESS] = et_delivery_address.text.toString().replace("\n", " ")
        params[WebParams.PROVINCE] = province_auto_text.text.toString()
        params[WebParams.CITY] = district_auto_text.text.toString()
        params[WebParams.DISTRICT] = sub_district_auto_text.text.toString()
        params[WebParams.VILLAGE] = urban_village_auto_text.text.toString()
        params[WebParams.ZIP_CODE] = et_postal_code.text.toString()
        params[WebParams.CUST_NAME] = custName
        params[WebParams.SHOP_NAME] = shopName
        params[WebParams.ANCHOR_CODE_ESPAY] = anchorCodeEspay

        Timber.d("isi params register store:%s", params.toString())
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REGISTER_NEW_EBD, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val code = response.getString(WebParams.ERROR_CODE)
                val message = response.getString(WebParams.ERROR_MESSAGE)
                when (code) {
                    WebParams.SUCCESS_CODE -> {
                        showDialog()
                    }
                    WebParams.LOGOUT_CODE -> {
                        AlertDialogLogout.getInstance().showDialoginMain(activity, message)
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
    private fun showDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = resources.getString(R.string.shop_registration_success)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = getString(R.string.register_success_wait_for_verification)

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
                    change_location.visibility = View.VISIBLE
                    tv_address.visibility = View.VISIBLE
                    tv_address.text = address
                    ll_setLocation.visibility = View.GONE
                    longitude = data.getDoubleExtra("longitude", 0.0)
                    latitude = data.getDoubleExtra("latitude", 0.0)
                }
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