package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.RealmManager
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.databinding.FragmentBBSTransferFundFormBinding
import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place
import com.sgo.saldomu.models.retrofit.BBSTransModel
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.bbs_cash_in_cash_out.*
import timber.log.Timber
import java.util.*


class BBSTransferFundFormFragment : BaseFragment() {
    val TAG = "com.sgo.saldomu.fragments.BBSTransferFundForm"
    private var _binding: FragmentBBSTransferFundFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var model: BBSTransModel
    private var custIDTypes = ""
    private var sumberdana = ""
    private var purposeOfTrx = ""
    private var gender = ""
    private lateinit var realm: Realm

    // from insert tfd
    private var transaksi = ""
    private var source_product_h2h = ""
    private var source_product_type = ""
    private var benef_product_name = ""
    private var name_benef = ""
    private var no_benef = ""
    private var benef_product_type = ""
    private var comm_code = ""
    private var callback_url = ""
    private var api_key = ""
    private var comm_id = ""
    private var remark = ""
    private var source_product_name = ""
    private var TCASH_hp_validation = false
    private var mandiriLKDValidation = false
    private var max_token_resend = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            model = getSerializable("data") as BBSTransModel
            transaksi = getString(DefineValue.TRANSACTION, "")
            source_product_h2h = getString(DefineValue.PRODUCT_H2H, "")
            source_product_type = getString(DefineValue.PRODUCT_TYPE, "")
            benef_product_name = getString(DefineValue.BANK_BENEF, "")
            name_benef = getString(DefineValue.NAME_BENEF, "")
            no_benef = getString(DefineValue.NO_BENEF, "")
            benef_product_type = getString(DefineValue.TYPE_BENEF, "")
            comm_code = getString(DefineValue.COMMUNITY_CODE, "")
            callback_url = getString(DefineValue.CALLBACK_URL, "")
            api_key = getString(DefineValue.API_KEY, "")
            comm_id = getString(DefineValue.COMMUNITY_ID, "")
            remark = getString(DefineValue.REMARK, "")
            source_product_name = getString(DefineValue.SOURCE_ACCT, "")
            TCASH_hp_validation = getBoolean(DefineValue.TCASH_HP_VALIDATION, false)
            mandiriLKDValidation = getBoolean(DefineValue.MANDIRI_LKD_VALIDATION, false)
            if (!containsKey(DefineValue.MAX_RESEND)) max_token_resend =
                getString(DefineValue.MAX_RESEND, "3").toInt()
        }

        realm = Realm.getInstance(RealmManager.BBSConfiguration)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBBSTransferFundFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun init() {
        binding.apply {
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, model.cust_id_types)
                .also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spIdentityType.adapter = adapter

                    spIdentityType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            custIDTypes = parent?.getItemAtPosition(position).toString()
                        }

                    }
                }

            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, model.source_of_fund)
                .also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spSource.adapter = adapter

                    spSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            sumberdana = parent?.getItemAtPosition(position).toString()
                            if (sumberdana.equals("LAINNYA", ignoreCase = true)) {
                                etSource.visibility = View.VISIBLE
                            } else etSource.visibility = View.GONE
                        }

                    }
                }

            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, model.purpose_of_trx)
                .also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spPurposeOfTrx.adapter = adapter

                    spPurposeOfTrx.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            purposeOfTrx = parent?.getItemAtPosition(position).toString()
                        }

                    }
                }

            val results: RealmResults<List_BBS_Birth_Place> = realm.where(List_BBS_Birth_Place::class.java).findAll()
            Timber.d("REALM RESULTS:%s", results.toString())
            val list = arrayListOf<String>()

            for (i in results.indices) {
                if (results[i]!!.birthPlace_city != null || !results[i]!!
                        .birthPlace_city.equals("", ignoreCase = true)
                ) {
                    list.add(results[i]!!.birthPlace_city)
                }
            }

            Timber.d("Size of List name Birth place:%s", list.size)
            val cityAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_selectable_list_item,
                list
            )
            etBirthPlace.threshold = 1
            etBirthPlace.setAdapter(cityAdapter)

            etDistrict.threshold = 1
            etDistrict.setAdapter(cityAdapter)

            etDate.setOnClickListener {
                selectDate(etDate)
            }
            etBirthdate.setOnClickListener {
                selectDate(etBirthdate)
            }
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                gender = when(checkedId) {
                    rbMale.id -> "L"
                    rbFemale.id -> "P"
                    else -> ""
                }
            }
            backBtn.setOnClickListener { fragManager.popBackStack() }
            prosesBtn.setOnClickListener {
                if (!validation()) return@setOnClickListener
                confirm()
            }
        }
    }

    fun confirm() {

        extraSignature = model.tx_id + sp.getString(DefineValue.MEMBER_ID, "") + custIDTypes
        val params = RetrofitService.getInstance()
            .getSignature(MyApiClient.LINK_BBS_CUSTOMER_TFD, extraSignature)

        if (sumberdana.equals("LAINNYA", ignoreCase = true))
            sumberdana = binding.etSource.text.toString()

        params[WebParams.CUST_BIRTH_PLACE] = binding.etBirthPlace.text.toString()
        params[WebParams.CUST_BIRTH_DATE] = binding.etBirthdate.text.toString()
        params[WebParams.TRANSFER_TO] = "O"
        params[WebParams.CUST_NAME] = binding.etName.text.toString()
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.TX_ID] = model.tx_id
        params[WebParams.MEMBER_ID] = memberIDLogin
        params[WebParams.CUST_PHONE] = binding.etPhone.text.toString()
        params[WebParams.CUST_ADDRESS] = binding.etAddress.text.toString()
        params[WebParams.CUST_ID_TYPE] = custIDTypes
        params[WebParams.CUST_ID_NUMBER] = binding.etNoIdentity.text.toString()
        params[WebParams.SOURCE_OF_FUND] = sumberdana
        params["nationality"] = binding.etCitizenship.text.toString()
        params["purpose_of_trx"] = purposeOfTrx
        params["cust_id_type_expiry"] = binding.etDate.text.toString()
        params["gender"] = gender
        params["cust_pekerjaan"] = binding.etProfession.text.toString()
        params["cust_rw"] = binding.etRw.text
        params["cust_propinsi"] = binding.etProvince.text.toString()
        params["cust_kabupaten"] = binding.etDistrict.text.toString()
        params["cust_kecamatan"] = binding.etSubDistrict.text.toString()
        params["receiver_phone"] = binding.etReceiverPhone.text.toString()
        params["receiver_name"] = binding.etReceiverFullname.text.toString()
        params["receiver_id_number"] = binding.etReceiverIdentity.text.toString()

        val mArgs = Bundle()
        mArgs.putSerializable("params", params)

        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h)
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type)
        mArgs.putString(DefineValue.SHARE_TYPE, "1")
        mArgs.putString(DefineValue.CALLBACK_URL, callback_url)
        mArgs.putString(DefineValue.API_KEY, api_key)
        mArgs.putString(DefineValue.COMMUNITY_ID, comm_id)
        mArgs.putString(DefineValue.BANK_BENEF, benef_product_name)
        mArgs.putString(DefineValue.NAME_BENEF, model.benef_product_value_name)
        mArgs.putString(DefineValue.NO_BENEF, model.benef_product_value_code)
        mArgs.putString(DefineValue.TYPE_BENEF, benef_product_type)
        mArgs.putString(DefineValue.REMARK, remark)
        mArgs.putString(DefineValue.SOURCE_ACCT, source_product_name)
        mArgs.putString(DefineValue.MAX_RESEND, max_token_resend.toString())
        mArgs.putString(DefineValue.TRANSACTION, transaksi)
        mArgs.putBoolean(DefineValue.TCASH_HP_VALIDATION, TCASH_hp_validation)
        mArgs.putBoolean(DefineValue.MANDIRI_LKD_VALIDATION, mandiriLKDValidation)

        val mFrag: Fragment = BBSTransferFundFormFragment()
        mFrag.arguments = mArgs
        fragmentManager!!.beginTransaction().addToBackStack("")
            .replace(R.id.bbs_content, mFrag, BBSTransferFundFormFragment().TAG).commit()
    }

    @SuppressLint("SetTextI18n")
    private fun selectDate(et: EditText) {
        val calendar: Calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)

        //show dialog
        val datePickerDialog = DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                val day = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val months = if (month+1 < 10) "0${month+1}" else "${month+1}"
                et.setText("$day/$months/$year")
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    private fun validation(): Boolean {
        with(binding) {
            return etNoIdentity.valid() && etDate.valid() && etName.valid()
                    && custIDTypes.isNotBlank() && sumberdana.isNotBlank()
                    && etPhone.valid() && etBirthdate.valid() && etBirthPlace.valid()
                    && gender.isNotBlank() && etAddress.valid() && etRt.valid()
                    && etRw.valid() && etDistrict.valid() && etSubDistrict.valid() && purposeOfTrx.isNotBlank()
                    && etCitizenship.valid() && etProvince.valid() && etProfession.valid() && etCitizenship.valid()
                    && etReceiverIdentity.valid() && etReceiverFullname.valid() && etReceiverPhone.valid()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun EditText.valid(): Boolean {
        if (text.isNotBlank()) {
            return true
        } else {
            this.error = "Tidak boleh kosong"
            this.requestFocus()
            return false
        }
    }

//    private fun sendData() {
//        showProgressDialog()
////        try {
//            extraSignature = model.tx_id + sp.getString(DefineValue.MEMBER_ID, "") + custIDTypes
//            val params = RetrofitService.getInstance()
//                .getSignature(MyApiClient.LINK_BBS_CUSTOMER_TFD, extraSignature)
//
//            if (sumberdana.equals("LAINNYA", ignoreCase = true))
//                sumberdana = binding.etSource.text.toString()
//
//            params[WebParams.CUST_BIRTH_PLACE] = binding.etBirthPlace.text.toString()
//            params[WebParams.CUST_BIRTH_DATE] = binding.etBirthdate.text.toString()
//            params[WebParams.TRANSFER_TO] = "O"
//            params[WebParams.CUST_NAME] = binding.etName.text.toString()
//            params[WebParams.USER_ID] = userPhoneID
//            params[WebParams.TX_ID] = model.tx_id
//            params[WebParams.MEMBER_ID] = memberIDLogin
//            params[WebParams.CUST_PHONE] = binding.etPhone.text.toString()
//            params[WebParams.CUST_ADDRESS] = binding.etAddress.text.toString()
//            params[WebParams.CUST_ID_TYPE] = custIDTypes
//            params[WebParams.CUST_ID_NUMBER] = binding.etNoIdentity.text.toString()
//            params[WebParams.SOURCE_OF_FUND] = sumberdana
//            params["nationality"] = binding.etCitizenship.text.toString()
//            params["purpose_of_trx"] = purposeOfTrx
//            params["cust_id_type_expiry"] = binding.etDate.text.toString()
//            params["gender"] = gender
//            params["cust_pekerjaan"] = binding.etProfession.text.toString()
//            params["cust_rw"] = binding.etRw.text
//            params["cust_propinsi"] = binding.etProvince.text.toString()
//            params["cust_kabupaten"] = binding.etDistrict.text.toString()
//            params["cust_kecamatan"] = binding.etSubDistrict.text.toString()
//            params["receiver_phone"] = binding.etReceiverPhone.text.toString()
//            params["receiver_name"] = binding.etReceiverFullname.text.toString()
//            params["receiver_id_number"] = binding.etReceiverIdentity.text.toString()
//
//            Timber.d("params bbs send data : %s", params.toString())
//            RetrofitService.getInstance().PostObjectRequestDebounce(
//                MyApiClient.LINK_BBS_CUSTOMER_TFD, params, object : ResponseListener {
//                    override fun onResponses(response: JsonObject?) {
//                        Log.e("TAG", "onResponses: $response")
//                    }
//
//                    override fun onError(throwable: Throwable?) {
//                        TODO("Not yet implemented")
//                    }
//
//                    override fun onComplete() {
//                        TODO("Not yet implemented")
//                    }
//
//                }
//            )
////            RetrofitService.getInstance().PostObjectRequest(
////                MyApiClient.LINK_BBS_CUSTOMER_TFD, params
////            ) { response ->
////                Log.e("TAG", "onResponses: $response")
////                /*   when (code) {
////                                WebParams.SUCCESS_CODE -> {
////                                    Log.e("TAG", "sendData: $model" )
////                        //                        changeToBBSCashInConfirm(
////                        //                            jsonObject.getString(WebParams.ADMIN_FEE),
////                        //                            jsonObject.getString(WebParams.AMOUNT),
////                        //                            jsonObject.getString(WebParams.TOTAL_AMOUNT)
////                        //                        )
////                                }
////                                WebParams.LOGOUT_CODE -> {
////                                    Timber.d("isi response autologout:%s", response.toString())
////                                    AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
////                                }
////                                DefineValue.ERROR_9333 -> {
////                                    Timber.d("isi response app data:%s", model.app_data)
////                                    val appModel = model.app_data
////                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(
////                                        activity,
////                                        appModel.type,
////                                        appModel.packageName,
////                                        appModel.downloadUrl
////                                    )
////                                }
////                                DefineValue.ERROR_0066 -> {
////                                    Timber.d("isi response maintenance:%s", response.toString())
////                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
////                                }
////                                else -> {
////                                    Timber.d("isi error bbs send data:%s", response.toString())
////                                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
////                                }
////                            }
////                            dismissProgressDialog()*/
////            }
////            { response: JsonObject ->
////                Log.e("TAG", "sendData: $response" )
////                try {
////                    val model = getGson().fromJson(response.toString(), jsonModel::class.java)
////                    val jsonObject = JSONObject(response.toString())
////                    val code = jsonObject.getString(WebParams.ERROR_CODE)
////                    val message = jsonObject.getString(WebParams.ERROR_MESSAGE)
////                    Timber.d("response bbs send data : %s", jsonObject.toString())
////                    when (code) {
////                        WebParams.SUCCESS_CODE -> {
////                            Log.e("TAG", "sendData: $model" )
////                //                        changeToBBSCashInConfirm(
////                //                            jsonObject.getString(WebParams.ADMIN_FEE),
////                //                            jsonObject.getString(WebParams.AMOUNT),
////                //                            jsonObject.getString(WebParams.TOTAL_AMOUNT)
////                //                        )
////                        }
////                        WebParams.LOGOUT_CODE -> {
////                            Timber.d("isi response autologout:%s", response.toString())
////                            AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
////                        }
////                        DefineValue.ERROR_9333 -> {
////                            Timber.d("isi response app data:%s", model.app_data)
////                            val appModel = model.app_data
////                            AlertDialogUpdateApp.getInstance().showDialogUpdate(
////                                activity,
////                                appModel.type,
////                                appModel.packageName,
////                                appModel.downloadUrl
////                            )
////                        }
////                        DefineValue.ERROR_0066 -> {
////                            Timber.d("isi response maintenance:%s", response.toString())
////                            AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
////                        }
////                        else -> {
////                            Timber.d("isi error bbs send data:%s", response.toString())
////                            Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
////                        }
////                    }
////                    dismissProgressDialog()
////                } catch (e: JSONException) {
////                    e.printStackTrace()
////                }
////            }
////        } catch (e: Exception) {
////            Timber.d("httpclient:%s", e.message)
////        }
//    }
}