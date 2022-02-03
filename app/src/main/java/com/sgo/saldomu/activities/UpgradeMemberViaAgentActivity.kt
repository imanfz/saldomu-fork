package com.sgo.saldomu.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.sgo.saldomu.Beans.CustomAdapterModel
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.CustomAutoCompleteAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.RealmManager
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.retrofit.DistrictModel
import com.sgo.saldomu.models.retrofit.ProvinceModel
import com.sgo.saldomu.models.retrofit.SubDistrictModel
import com.sgo.saldomu.models.retrofit.UrbanVillageModel
import com.sgo.saldomu.widgets.BaseActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_upgrade_member_via_agent.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class UpgradeMemberViaAgentActivity : BaseActivity() {
    internal lateinit var locList: MutableList<CustomAdapterModel>
    internal lateinit var locLists: MutableList<String>
    internal lateinit var adapter: CustomAutoCompleteAdapter
    internal lateinit var adapters: ArrayAdapter<String>
    var memberDOB: String = ""
    private lateinit var fromFormat: DateFormat

    var provinceID = ""
    var districtID = ""
    var subDistrictID = ""
    val provinceList: ArrayList<ProvinceModel> = arrayListOf()
    val provincesNameList: ArrayList<String> = arrayListOf()
    val districtList: ArrayList<DistrictModel> = arrayListOf()
    val districtNameList: ArrayList<String> = arrayListOf()
    val subDistrictList: ArrayList<SubDistrictModel> = arrayListOf()
    val subDistrictNameList: ArrayList<String> = arrayListOf()
    val urbanVillageList: ArrayList<UrbanVillageModel> = arrayListOf()
    val urbanVillageNameList: ArrayList<String> = arrayListOf()

    override fun getLayoutResource(): Int {
        return R.layout.activity_upgrade_member_via_agent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.menu_item_title_upgrade_member)

        initPOBSpinner()

        initGenderSpinner()

        province_auto_text.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            for (i in 0 until provinceList.size) {
                if (provinceList[i].provinceName == province_auto_text.text.toString()) {
                    provinceID = provinceList[i].provinceCode
                    district_auto_text.setText("")
                    sub_district_auto_text.setText("")
                    urban_village_auto_text.setText("")
                }
            }
            clearListDistrict()
            clearListSubDistrict()
            clearListUrbanVillage()
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
            clearListSubDistrict()
            clearListUrbanVillage()
            getLocationData()
        }

        sub_district_auto_text.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            for (i in 0 until subDistrictList.size) {
                if (subDistrictList[i].subDistrictName == sub_district_auto_text.text.toString()) {
                    subDistrictID = subDistrictList[i].subDistrictCode
                    urban_village_auto_text.setText("")
                }
            }
            clearListUrbanVillage()
            getLocationData()
        }

        getLocationData()

        initReligionSpinner()

        initStatusSpinner()

        initNationalitySpinner()

        submit_button.setOnClickListener {
            if (inputValidation()) {

                val intent = Intent(this, DetailMemberToVerifyActivity::class.java)
                intent.putExtra(DefineValue.NIK, nik_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_CUST_NAME, fullname_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_POB, birth_place_list.text.toString())
                intent.putExtra(DefineValue.MEMBER_DOB, memberDOB)
                intent.putExtra(DefineValue.MEMBER_GENDER, gender_spinner.selectedItem.toString())
                intent.putExtra(DefineValue.MEMBER_ADDRESS, address_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_RT, rt_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_RW, rw_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_KELURAHAN, urban_village_auto_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_KECAMATAN, sub_district_auto_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_KABUPATEN, district_auto_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_PROVINSI, province_auto_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_RELIGION, religion_spinner.selectedItem.toString())
                intent.putExtra(DefineValue.MEMBER_STATUS, status_spinner.selectedItem.toString())
                intent.putExtra(DefineValue.MEMBER_OCUPATION, job_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_NATIONALITY, nationality_spinner.selectedItem.toString())
                intent.putExtra(DefineValue.MEMBER_MOTHERS_NAME, mothersname_edit_text.text.toString())
                startActivity(intent)
                finish()
            }
        }
        birthday_text_view.setOnClickListener {
            birthdayOnClick()
        }
    }

    private fun clearListDistrict(){
        districtID = ""
        districtList.clear()
        districtNameList.clear()
    }

    private fun clearListSubDistrict(){
        subDistrictID = ""
        subDistrictList.clear()
        subDistrictNameList.clear()
    }

    private fun clearListUrbanVillage(){
        urbanVillageList.clear()
        urbanVillageNameList.clear()
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
                                provinceList.add(
                                    ProvinceModel(
                                        jsonObject.optString(WebParams.KODE_PROVINSI),
                                        jsonObject.optString(WebParams.NAMA_PROVINSI)
                                    )
                                )
                                provincesNameList.add(provinceList[i].provinceName)
                            }
                            districtID == "" -> {
                                districtList.add(
                                    DistrictModel(
                                        jsonObject.optString(WebParams.KODE_KOT_KAB),
                                        jsonObject.optString(WebParams.NAMA_KOT_KAB)
                                    )
                                )
                                districtNameList.add(districtList[i].districtName)
                            }
                            subDistrictID == "" -> {
                                subDistrictList.add(
                                    SubDistrictModel(
                                        jsonObject.optString(WebParams.KODE_KECAMATAN),
                                        jsonObject.optString(WebParams.NAMA_KECAMATAN)
                                    )
                                )
                                subDistrictNameList.add(subDistrictList[i].subDistrictName)
                            }
                            else -> {
                                urbanVillageList.add(
                                    UrbanVillageModel(
                                        jsonObject.optString(
                                            WebParams.KODE_LUR_DES
                                        ), jsonObject.optString(WebParams.NAMA_LUR_DES)
                                    )
                                )
                                urbanVillageNameList.add(urbanVillageList[i].urbanVillageName)
                            }
                        }

                    }

                    val provincesAdapter = ArrayAdapter(
                        applicationContext,
                        android.R.layout.simple_spinner_dropdown_item,
                        provincesNameList
                    )
                    province_auto_text.setAdapter(provincesAdapter)
                    val districtAdapter = ArrayAdapter(
                        applicationContext,
                        android.R.layout.simple_spinner_dropdown_item,
                        districtNameList
                    )
                    district_auto_text.setAdapter(districtAdapter)
                    val subDistrictAdapter = ArrayAdapter(
                        applicationContext,
                        android.R.layout.simple_spinner_dropdown_item,
                        subDistrictNameList
                    )
                    sub_district_auto_text.setAdapter(subDistrictAdapter)
                    val urbanVillageAdapter = ArrayAdapter(
                        applicationContext,
                        android.R.layout.simple_spinner_dropdown_item,
                        urbanVillageNameList
                    )
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

    private fun initGenderSpinner() {
        val genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_type, android.R.layout.simple_spinner_item)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gender_spinner.adapter = genderAdapter
    }

    private fun initNationalitySpinner() {
        val nationalityAdapter = ArrayAdapter.createFromResource(this,
                R.array.list_nationality, android.R.layout.simple_spinner_item)
        nationalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        nationality_spinner.adapter = nationalityAdapter
    }

    private fun initStatusSpinner() {
        val statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.list_marital_status, android.R.layout.simple_spinner_item)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        status_spinner.adapter = statusAdapter
    }

    private fun initReligionSpinner() {
        val religionAdapter = ArrayAdapter.createFromResource(this,
                R.array.list_religion, android.R.layout.simple_spinner_item)
        religionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        religion_spinner.adapter = religionAdapter
    }

    private fun initPOBSpinner() {
        locList = ArrayList()
        locLists = ArrayList()
        adapter = CustomAutoCompleteAdapter(this, locList, android.R.layout.simple_spinner_dropdown_item)
        adapters = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locLists)
        birth_place_list.setAdapter(adapters)
        birth_place_list.threshold = 2

        val realm = Realm.getInstance(RealmManager.BBSConfiguration)
        val results = realm.where(List_BBS_Birth_Place::class.java).findAll()
        val list_bbs_birth_place = ArrayList(realm.copyFromRealm(results))

        for (model in list_bbs_birth_place) {
            locList.add(CustomAdapterModel(model))
            locLists.add(model.birthPlace_city)
        }

        adapters.notifyDataSetChanged()
    }

    private fun inputValidation(): Boolean {
        if (nik_edit_text.text.toString().isEmpty()) {
            nik_edit_text.requestFocus()
            nik_edit_text.error = resources.getString(R.string.ktp_warn)
            return false
        } else if (nik_edit_text.text.toString().length != 16) {
            nik_edit_text.requestFocus()
            nik_edit_text.error = resources.getString(R.string.ktp_warn1)
            return false
        } else if (fullname_edit_text.text.toString().isEmpty()) {
            fullname_edit_text.requestFocus()
            fullname_edit_text.error = resources.getString(R.string.myprofile_validation_name)
            return false
        } else if (mothersname_edit_text.text.toString().isEmpty()) {
            mothersname_edit_text.requestFocus()
            mothersname_edit_text.error = resources.getString(R.string.myprofile_validation_mothers_name)
            return false
        } else if (!locLists.contains(birth_place_list.text.toString())) run {
            birth_place_list.requestFocus()
            birth_place_list.error = resources.getString(R.string.city_not_found_message)
            return false
        } else if (birth_place_list.text.toString().trim({ it <= ' ' }).isEmpty()) run {
            birth_place_list.requestFocus()
            birth_place_list.error = resources.getString(R.string.city_empty_message)
            return false
        } else if (province_auto_text.text.isEmpty()) {
            province_auto_text.requestFocus()
            province_auto_text.error = resources.getString(R.string.province_validation)
            return false
        } else if (district_auto_text.text.isEmpty()) {
            district_auto_text.requestFocus()
            district_auto_text.error = resources.getString(R.string.district_validation)
            return false
        } else if (sub_district_auto_text.text.isEmpty()) {
            sub_district_auto_text.requestFocus()
            sub_district_auto_text.error = resources.getString(R.string.sub_district_validation)
            return false
        } else if (urban_village_auto_text.text.isEmpty()) {
            urban_village_auto_text.requestFocus()
            urban_village_auto_text.error = resources.getString(R.string.urban_village_validation)
            return false
        } else if (birthday_text_view.text.toString().isEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date_empty))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (gender_spinner.selectedItem.equals(getString(R.string.select_gender))) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.gender_validation))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (address_edit_text.text.toString().isEmpty()) {
            address_edit_text.requestFocus()
            address_edit_text.error = resources.getString(R.string.myprofile_validation_address)
            return false
        } else if (rt_edit_text.text.toString().isEmpty()) {
            rt_edit_text.requestFocus()
            rt_edit_text.error = resources.getString(R.string.rt_validation)
            return false
        } else if (rw_edit_text.text.toString().isEmpty()) {
            rw_edit_text.requestFocus()
            rw_edit_text.error = resources.getString(R.string.rw_validation)
            return false
        } else if (religion_spinner.selectedItem == getString(R.string.select_religion)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.religion_validation))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (job_edit_text.text.toString().isEmpty()) {
            job_edit_text.requestFocus()
            job_edit_text.error = resources.getString(R.string.job_validation)
            return false
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun birthdayOnClick() {
        val c = Calendar.getInstance()
        val yearNow = c.get(Calendar.YEAR)
        val monthNow = c.get(Calendar.MONTH)
        val dayNow = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
                DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                    val calendar: Calendar = Calendar.getInstance()
                    calendar.set(year, monthOfYear, dayOfMonth)

                    val monthDisplay = monthOfYear + 1

                    birthday_text_view.text = "$dayOfMonth - $monthDisplay - $year"
                    fromFormat = SimpleDateFormat("yyyy-MM-dd", Locale("ID", "INDONESIA"))
                    memberDOB = fromFormat.format(calendar.time)
                }, yearNow, monthNow, dayNow)
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}