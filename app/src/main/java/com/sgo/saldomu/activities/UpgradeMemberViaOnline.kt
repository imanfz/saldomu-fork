package com.sgo.saldomu.activities

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.Beans.CustomAdapterModel
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.BankCashoutAdapter
import com.sgo.saldomu.adapter.CustomAutoCompleteAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.RealmManager
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.DefinedDialog
import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.BankCashoutModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.utils.PickAndCameraUtil
import com.sgo.saldomu.utils.camera.CameraActivity
import com.sgo.saldomu.widgets.BaseActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_upgrade_member_via_agent.*
import org.json.JSONArray
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class UpgradeMemberViaOnline : BaseActivity() {

    internal lateinit var locList: MutableList<CustomAdapterModel>
    internal lateinit var locLists: MutableList<String>
    internal lateinit var adapter: CustomAutoCompleteAdapter
    internal lateinit var adapters: ArrayAdapter<String>
    private var dedate: String? = null
    private var date_dob: String? = null
    private lateinit var dobFormat: DateFormat
    private lateinit var fromFormat: DateFormat
    internal lateinit var cb_termsncond: CheckBox
    internal lateinit var bankAdapter: BankCashoutAdapter
    internal var listBankCashOut: List<BankCashoutModel> = ArrayList()
    private var bankCode = ""
    private var set_result_photo: Int? = null
    private val RESULT_CAMERA_KTP = 201
    private val RESULT_SELFIE = 202
    private val RESULT_CAMERA_TTD = 203
    private val RC_CAMERA_STORAGE = 14
    private var pickAndCameraUtil: PickAndCameraUtil? = null

    var provincesName: String = String()
    var kabupatenName: String = String()
    var kecamatanName: String = String()
    var kelurahanName: String = String()
    val provincesList: ArrayList<String> = arrayListOf()
    var provincesObject = JSONObject()
    var provincesArray = JSONArray()
    val kabupatenList: ArrayList<String> = arrayListOf()
    var kabupatenObject = JSONObject()
    var kabupatenArray = JSONArray()
    val kecamatanList: ArrayList<String> = arrayListOf()
    var kecamatanObject = JSONObject()
    var kecamatanArray = JSONArray()
    val kelurahanList: ArrayList<String> = arrayListOf()
    var kelurahanObject = JSONObject()
    var kelurahanArray = JSONArray()

    var ktp: File? = null
    var selfie: File? = null
    var ttd: File? = null

    override fun getLayoutResource(): Int {
        return R.layout.activity_upgrade_member_via_agent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.menu_item_title_upgrade_online)

        layout_upgrade_online.visibility = View.VISIBLE

        pickAndCameraUtil = PickAndCameraUtil(this)

        initData()

        initBankSpinner()

        initPOBSpinner()

        initGenderSpinner()

        val handler = Handler()
        val runnable = Runnable {
            initProvinceSpinner()
        }
        showProgressDialog()
        val swipeTimer = Timer()
        swipeTimer.schedule(object : TimerTask() {
            override fun run() {
                handler.post(runnable)
                swipeTimer.cancel()
            }
        }, 1000)

        initReligionSpinner()

        initStatusSpinner()

        initNationalitySpinner()

        camera_ktp.setOnClickListener { cameraKTPListener() }

        camera_selfie_ktp.setOnClickListener { cameraSelfieListener() }

        camera_ttd.setOnClickListener { cameraTTDListener() }

        submit_button.setOnClickListener {
            if (inputValidation()) {

            }
        }
    }

    private fun inputValidation(): Boolean {
        if (nik_edit_text.text.toString().length == 0) {
            nik_edit_text.requestFocus()
            nik_edit_text.error = resources.getString(R.string.ktp_warn)
            return false
        } else if (nik_edit_text.text.toString().length != 16) {
            nik_edit_text.requestFocus()
            nik_edit_text.error = resources.getString(R.string.ktp_warn1)
            return false
        } else if (fullname_edit_text.text.toString().length == 0) {
            fullname_edit_text.requestFocus()
            fullname_edit_text.error = resources.getString(R.string.myprofile_validation_name)
            return false
        } else if (mothersname_edit_text.text.toString().length == 0) {
            mothersname_edit_text.requestFocus()
            mothersname_edit_text.error = resources.getString(R.string.myprofile_validation_mothers_name)
            return false
        } else if (!locLists.contains(birth_place_list.text.toString())) run {
            birth_place_list.requestFocus()
            birth_place_list.error = resources.getString(R.string.city_not_found_message)
            return false
        } else if (birth_place_list.text.toString().trim({ it <= ' ' }).length == 0) run {
            birth_place_list.requestFocus()
            birth_place_list.error = resources.getString(R.string.city_empty_message)
            return false
        } else if (!provincesList.contains(province_auto_text.text.toString())) run {
            province_auto_text.requestFocus()
            province_auto_text.error = resources.getString(R.string.province_not_found_message)
            return false
        } else if (province_auto_text.text.toString().trim({ it <= ' ' }).length == 0) run {
            province_auto_text.requestFocus()
            province_auto_text.error = resources.getString(R.string.province_validation)
            return false
        } else if (!kabupatenList.contains(district_auto_text.text.toString())) run {
            district_auto_text.requestFocus()
            district_auto_text.error = resources.getString(R.string.district_not_found_message)
            return false
        } else if (district_auto_text.text.toString().trim({ it <= ' ' }).length == 0) run {
            district_auto_text.requestFocus()
            district_auto_text.error = resources.getString(R.string.district_validation)
            return false
        } else if (!kecamatanList.contains(sub_district_auto_text.text.toString())) run {
            sub_district_auto_text.requestFocus()
            sub_district_auto_text.error = resources.getString(R.string.sub_district_not_found_message)
            return false
        } else if (sub_district_auto_text.text.toString().trim({ it <= ' ' }).length == 0) run {
            sub_district_auto_text.requestFocus()
            sub_district_auto_text.error = resources.getString(R.string.sub_district_validation)
            return false
        } else if (!kelurahanList.contains(urban_village_auto_text.text.toString())) run {
            urban_village_auto_text.requestFocus()
            urban_village_auto_text.error = resources.getString(R.string.urban_village_not_found_message)
            return false
        } else if (urban_village_auto_text.text.toString().trim({ it <= ' ' }).length == 0) run {
            urban_village_auto_text.requestFocus()
            urban_village_auto_text.error = resources.getString(R.string.urban_village_validation)
            return false
        } else if (gender_spinner.selectedItem.equals(getString(R.string.select_gender))) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.gender_validation))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (address_edit_text.text.toString().length == 0) {
            address_edit_text.requestFocus()
            address_edit_text.error = resources.getString(R.string.myprofile_validation_address)
            return false
        } else if (rt_edit_text.text.toString().length == 0) {
            rt_edit_text.requestFocus()
            rt_edit_text.error = resources.getString(R.string.rt_validation)
            return false
        } else if (rw_edit_text.text.toString().length == 0) {
            rw_edit_text.requestFocus()
            rw_edit_text.error = resources.getString(R.string.rw_validation)
            return false
        } else if (religion_spinner.selectedItem.equals(getString(R.string.select_religion))) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.religion_validation))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (job_edit_text.text.toString().length == 0) {
            job_edit_text.requestFocus()
            job_edit_text.error = resources.getString(R.string.job_validation)
            return false
        }
        return true
    }

    private fun initGenderSpinner() {
        val genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_type, android.R.layout.simple_spinner_item)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gender_spinner.adapter = genderAdapter
    }

    private fun initProvinceSpinner() {
        val jsonString =
                applicationContext.assets.open("province.txt").bufferedReader()
                        .use { it.readText() }
        val jsonObject = JSONObject(jsonString)

        provincesArray = jsonObject.getJSONArray("provinces")
        for (i in 0 until provincesArray.length()) {
            provincesObject = JSONObject(provincesArray[i].toString())
            provincesList.add(provincesObject.optString("nama_provinsi"))
        }

        val provincesAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, provincesList)

        province_auto_text.setAdapter(provincesAdapter)
        province_auto_text.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            for (i in 0 until provincesList.size) {
                provincesObject = JSONObject(provincesArray[i].toString())
                provincesName = provincesObject.optString("nama_provinsi")
                if (provincesName == province_auto_text.text.toString()) {
                    kabupatenList.clear()
                    kabupatenArray = provincesObject.getJSONArray("kabs")
                    for (j in 0 until kabupatenArray.length()) {
                        kabupatenObject = JSONObject(kabupatenArray[j].toString())
                        kabupatenList.add(kabupatenObject.optString("nama_kot_kab"))
                    }

                    val kabupatenAdapter =
                            ArrayAdapter(
                                    this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    kabupatenList
                            )
                    district_auto_text.setAdapter(kabupatenAdapter)
                    break
                }
            }
        }

        district_auto_text.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            for (i in 0 until kabupatenList.size) {
                kabupatenObject = JSONObject(kabupatenArray[i].toString())
                kabupatenName = kabupatenObject.optString("nama_kot_kab")
                if (kabupatenName == district_auto_text.text.toString()) {
                    kecamatanList.clear()
                    kecamatanArray = kabupatenObject.getJSONArray("kecamatans")
                    for (j in 0 until kecamatanArray.length()) {
                        kecamatanObject = JSONObject(kecamatanArray[j].toString())
                        kecamatanList.add(kecamatanObject.optString("nama_kecamatan"))
                    }

                    val kecamatanAdapter =
                            ArrayAdapter(
                                    this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    kecamatanList
                            )
                    sub_district_auto_text.setAdapter(kecamatanAdapter)
                    break
                }
            }
        }

        sub_district_auto_text.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            for (i in 0 until kecamatanList.size) {
                kecamatanObject = JSONObject(kecamatanArray[i].toString())
                kecamatanName = kecamatanObject.optString("nama_kecamatan")
                if (kecamatanName == sub_district_auto_text.text.toString()) {
                    kelurahanList.clear()
                    kelurahanArray = kecamatanObject.getJSONArray("kelurahan")
                    for (j in 0 until kelurahanArray.length()) {
                        kelurahanObject = JSONObject(kelurahanArray[j].toString())
                        kelurahanList.add(kelurahanObject.optString("nama_lur_des"))
                    }

                    val kelurahanAdapter =
                            ArrayAdapter(
                                    this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    kelurahanList
                            )
                    urban_village_auto_text.setAdapter(kelurahanAdapter)
                    break
                }
            }
        }

        urban_village_auto_text.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            for (i in 0 until kelurahanList.size) {
                kelurahanObject = JSONObject(kelurahanArray[i].toString())
                kelurahanName = kelurahanObject.optString("nama_lur_des")
                if (kelurahanName == urban_village_auto_text.text.toString())
                    break
            }
            Timber.e("prov : $provincesName")
            Timber.e("kab : $kabupatenName")
            Timber.e("kec : $kecamatanName")
            Timber.e("kel : $kelurahanName")
        }
        dismissProgressDialog()
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

    private fun initData() {
        dedate = sp.getString(DefineValue.PROFILE_DOB, "")


        fromFormat = SimpleDateFormat("yyyy-MM-dd", Locale("ID", "INDONESIA"))
        dobFormat = SimpleDateFormat("dd-MM-yyyy", Locale("ID", "INDONESIA"))

        try {
            birthday_text_view.setText(dobFormat.format(fromFormat.parse(sp.getString(DefineValue.PROFILE_DOB, ""))))
            birthday_text_view.isEnabled = false
        } catch (e: ParseException) {
            e.printStackTrace()
        }


        getBankCashout()
    }

    private fun initBankSpinner() {
        bankAdapter = BankCashoutAdapter(this, android.R.layout.simple_spinner_item)
        spinner_nameBank.setAdapter(bankAdapter)
        spinner_nameBank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val model = listBankCashOut[position]
                bankCode = model.bank_code
            }

        }
    }

    private fun getBankCashout() {
        try {
            val prodDialog = DefinedDialog.CreateProgressDialog(this, "")

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BANKCASHOUT, memberIDLogin)
            params[WebParams.COMM_ID] = MyApiClient.COMM_ID
            params[WebParams.MEMBER_ID] = memberIDLogin
            params[WebParams.USER_ID] = userPhoneID

            Timber.d("isi params get Bank cashout:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BANKCASHOUT, params,
                    object : ResponseListener {
                        override fun onResponses(`object`: JsonObject) {

                            val gson = Gson()
                            val model = gson.fromJson(`object`.toString(), jsonModel::class.java)

                            Log.e("getBankCashout", `object`.get("bank_cashout").toString())

                            val type = object : TypeToken<List<BankCashoutModel>>() {

                            }.type
                            val gson2 = Gson()
                            listBankCashOut = gson2.fromJson(`object`.get("bank_cashout"), type)

                            Log.e("getBankCashout", listBankCashOut.toString())

                            bankAdapter.updateAdapter(listBankCashOut)
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            if (prodDialog.isShowing)
                                prodDialog.dismiss()
                        }
                    })

        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun cameraKTPListener() {
        set_result_photo = RESULT_CAMERA_KTP
        cameraDialog()
    }

    private fun cameraSelfieListener() {
        set_result_photo = RESULT_SELFIE
        cameraDialog()
    }

    private fun cameraTTDListener() {
        set_result_photo = RESULT_CAMERA_TTD
        cameraDialog()
    }

    fun cameraDialog() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            set_result_photo?.let {
                CameraActivity.openCertificateCamera(this, CameraActivity.TYPE_COMPANY_PORTRAIT)
//                pickAndCameraUtil.runCamera(it)
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage),
                    RC_CAMERA_STORAGE, *perms)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}