package com.sgo.saldomu.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
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

        initProvinceSpinner()

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

    private fun initProvinceSpinner(){
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
                ArrayAdapter(applicationContext, android.R.layout.simple_spinner_dropdown_item, provincesList)

        province_spinner.adapter = provincesAdapter
        province_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                kabupatenList.clear()
                provincesObject = JSONObject(provincesArray[position].toString())
                provincesName = provincesObject.optString("nama_provinsi")
                kabupatenArray = provincesObject.getJSONArray("kabs")
                for (i in 0 until kabupatenArray.length()) {
                    kabupatenObject = JSONObject(kabupatenArray[i].toString())
                    kabupatenList.add(kabupatenObject.optString("nama_kot_kab"))
                }

                val kabupatenAdapter =
                        ArrayAdapter(
                                applicationContext,
                                android.R.layout.simple_spinner_dropdown_item,
                                kabupatenList
                        )

                city_spinner.adapter = kabupatenAdapter
            }

        }

        city_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                kecamatanList.clear()
                kabupatenObject = JSONObject(kabupatenArray[position].toString())
                kabupatenName = kabupatenObject.optString("nama_kot_kab")
                kecamatanArray = kabupatenObject.getJSONArray("kecamatans")
                for (i in 0 until kecamatanArray.length()) {
                    kecamatanObject = JSONObject(kecamatanArray[i].toString())
                    kecamatanList.add(kecamatanObject.optString("nama_kecamatan"))
                }

                val kecamatanAdapter =
                        ArrayAdapter(
                                applicationContext,
                                android.R.layout.simple_spinner_dropdown_item,
                                kecamatanList
                        )

                sub_district_spinner.adapter = kecamatanAdapter
            }

        }

        sub_district_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                kelurahanList.clear()
                kecamatanObject = JSONObject(kecamatanArray[position].toString())
                kecamatanName = kecamatanObject.optString("nama_kecamatan")
                kelurahanArray = kecamatanObject.getJSONArray("kelurahan")
                for (i in 0 until kelurahanArray.length()) {
                    kelurahanObject = JSONObject(kelurahanArray[i].toString())
                    kelurahanList.add(kelurahanObject.optString("nama_lur_des"))
                }

                val kelurahanAdapter =
                        ArrayAdapter(
                                applicationContext,
                                android.R.layout.simple_spinner_dropdown_item,
                                kelurahanList
                        )

                urban_village_spinner.adapter = kelurahanAdapter
            }

        }
        urban_village_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                kelurahanObject = JSONObject(kelurahanArray[position].toString())
                kelurahanName = kelurahanObject.optString("nama_lur_des")
            }

        }
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
        }else if (!locLists.contains(birth_place_list.text.toString())) run {
            birth_place_list.requestFocus()
            birth_place_list.error = "Nama kota tidak ditemukan!"
            return false
        } else if (birth_place_list.text.toString().trim({ it <= ' ' }).length == 0) run {
            birth_place_list.error = "Kota kosong"
            birth_place_list.requestFocus()
            return false
        }else if (birthday_text_view.text.toString().length==0) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date_empty))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        }else if (gender_spinner.selectedItem.equals(getString(R.string.select_gender))) {
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

    @SuppressLint("SetTextI18n")
    private fun birthdayOnClick() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
                DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val calendar : Calendar = Calendar.getInstance()
                    calendar.set(year, monthOfYear, dayOfMonth )

                    val monthdisplay = monthOfYear+1

                    birthday_text_view.text = "$dayOfMonth - $monthdisplay - $year"
                    fromFormat = SimpleDateFormat("yyyy-MM-dd", Locale("ID", "INDONESIA"))
                    memberDOB = fromFormat.format(calendar.time)
                }, year, month, day)
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