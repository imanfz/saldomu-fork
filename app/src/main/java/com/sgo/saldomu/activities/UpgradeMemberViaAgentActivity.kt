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
import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place
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
                intent.putExtra(DefineValue.MEMBER_KELURAHAN, kelurahanName)
                intent.putExtra(DefineValue.MEMBER_KECAMATAN, kecamatanName)
                intent.putExtra(DefineValue.MEMBER_KABUPATEN, kabupatenName)
                intent.putExtra(DefineValue.MEMBER_PROVINSI, provincesName)
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
        province_auto_text.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
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

        district_auto_text.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
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

        sub_district_auto_text.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
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

        urban_village_auto_text.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
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
        } else if (!provincesList.contains(province_auto_text.text.toString())) run {
            province_auto_text.requestFocus()
            province_auto_text.error = resources.getString(R.string.province_not_found_message)
            return false
        } else if (province_auto_text.text.toString().trim({ it <= ' ' }).isEmpty()) run {
            province_auto_text.requestFocus()
            province_auto_text.error = resources.getString(R.string.province_validation)
            return false
        } else if (!kabupatenList.contains(district_auto_text.text.toString())) run {
            district_auto_text.requestFocus()
            district_auto_text.error = resources.getString(R.string.district_not_found_message)
            return false
        } else if (district_auto_text.text.toString().trim({ it <= ' ' }).isEmpty()) run {
            district_auto_text.requestFocus()
            district_auto_text.error = resources.getString(R.string.district_validation)
            return false
        } else if (!kecamatanList.contains(sub_district_auto_text.text.toString())) run {
            sub_district_auto_text.requestFocus()
            sub_district_auto_text.error = resources.getString(R.string.sub_district_not_found_message)
            return false
        } else if (sub_district_auto_text.text.toString().trim({ it <= ' ' }).isEmpty()) run {
            sub_district_auto_text.requestFocus()
            sub_district_auto_text.error = resources.getString(R.string.sub_district_validation)
            return false
        } else if (!kelurahanList.contains(urban_village_auto_text.text.toString())) run {
            urban_village_auto_text.requestFocus()
            urban_village_auto_text.error = resources.getString(R.string.urban_village_not_found_message)
            return false
        } else if (urban_village_auto_text.text.toString().trim({ it <= ' ' }).isEmpty()) run {
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