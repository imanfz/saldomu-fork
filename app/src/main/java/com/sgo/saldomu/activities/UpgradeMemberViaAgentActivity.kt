package com.sgo.saldomu.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class UpgradeMemberViaAgentActivity : BaseActivity() {
    internal lateinit var locList: MutableList<CustomAdapterModel>
    internal lateinit var locLists: MutableList<String>
    internal lateinit var adapter: CustomAutoCompleteAdapter
    internal lateinit var adapters: ArrayAdapter<String>
    var memberDOB: String = ""
    private lateinit var fromFormat: DateFormat

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
                intent.putExtra(DefineValue.MEMBER_ADDRESS, address_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_RT, rt_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_RW, rw_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_KELURAHAN, urban_village_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_KECAMATAN, sub_district_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_RELIGION, religion_spinner.selectedItem.toString())
                intent.putExtra(DefineValue.MEMBER_STATUS, status_spinner.selectedItem.toString())
                intent.putExtra(DefineValue.MEMBER_OCUPATION, job_edit_text.text.toString())
                intent.putExtra(DefineValue.MEMBER_NATIONALITY, nationality_spinner.selectedItem.toString())
                startActivity(intent)
                finish()
            }
        }
        birthday_text_view.setOnClickListener {
            birthdayOnClick()
        }
    }

    private fun initNationalitySpinner() {
        val nationalityAdapter = ArrayAdapter.createFromResource(this,
                R.array.list_nationality, android.R.layout.simple_spinner_item)
        nationalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        nationality_spinner.setAdapter(nationalityAdapter)
    }

    private fun initStatusSpinner() {
        val statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.list_marital_status, android.R.layout.simple_spinner_item)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        status_spinner.setAdapter(statusAdapter)
    }

    private fun initReligionSpinner() {
        val religionAdapter = ArrayAdapter.createFromResource(this,
                R.array.list_religion, android.R.layout.simple_spinner_item)
        religionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        religion_spinner.setAdapter(religionAdapter)
    }

    private fun initPOBSpinner() {
        locList = ArrayList<CustomAdapterModel>()
        locLists = ArrayList<String>()
        adapter = CustomAutoCompleteAdapter(this, locList, android.R.layout.simple_spinner_dropdown_item)
        adapters = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, locLists)
        birth_place_list.setAdapter(adapters)
        birth_place_list.setThreshold(2)

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
        if (nik_edit_text.getText().toString().length == 0) {
            nik_edit_text.requestFocus()
            nik_edit_text.setError(resources.getString(R.string.ktp_warn))
            return false
        } else if (nik_edit_text.getText().toString().length != 16) {
            nik_edit_text.requestFocus()
            nik_edit_text.setError(resources.getString(R.string.ktp_warn1))
            return false
        } else if (fullname_edit_text.getText().toString().length == 0) {
            fullname_edit_text.requestFocus()
            fullname_edit_text.setError(resources.getString(R.string.myprofile_validation_name))
            return false
        } else if (!locLists.contains(birth_place_list.getText().toString())) run {
            birth_place_list.requestFocus()
            birth_place_list.setError("Nama kota tidak ditemukan!")
            return false
        } else if (birth_place_list.getText().toString().trim({ it <= ' ' }).length == 0) run {
            birth_place_list.setError("Kota kosong")
            birth_place_list.requestFocus()
            return false
        }else if (birthday_text_view.getText().toString().length==0) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date_empty))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (address_edit_text.getText().toString().length == 0) {
            address_edit_text.requestFocus()
            address_edit_text.setError(resources.getString(R.string.myprofile_validation_address))
            return false
        } else if (rt_edit_text.getText().toString().length == 0) {
            rt_edit_text.requestFocus()
            rt_edit_text.setError(resources.getString(R.string.rt_validation))
            return false
        } else if (rw_edit_text.getText().toString().length == 0) {
            rw_edit_text.requestFocus()
            rw_edit_text.setError(resources.getString(R.string.rw_validation))
            return false
        } else if (urban_village_edit_text.getText().toString().length == 0) {
            urban_village_edit_text.requestFocus()
            urban_village_edit_text.setError(resources.getString(R.string.urban_village_validation))
            return false
        } else if (sub_district_edit_text.getText().toString().length == 0) {
            sub_district_edit_text.requestFocus()
            sub_district_edit_text.setError(resources.getString(R.string.sub_district_validation))
            return false
        } else if (religion_spinner.selectedItem.equals("-Pilih Agama-")) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.religion_validation))
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            return false
        } else if (job_edit_text.getText().toString().length == 0) {
            job_edit_text.requestFocus()
            job_edit_text.setError(resources.getString(R.string.job_validation))
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
                    var calendar : Calendar = Calendar.getInstance()
                    calendar.set(year, monthOfYear, dayOfMonth )

                    var monthdisplay = monthOfYear+1

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