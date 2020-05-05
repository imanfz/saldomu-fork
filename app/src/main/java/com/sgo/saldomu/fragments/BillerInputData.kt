package com.sgo.saldomu.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sgo.saldomu.Beans.Biller_Data_Model
import com.sgo.saldomu.Beans.Biller_Type_Data_Model
import com.sgo.saldomu.Beans.listBankModel
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.*
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.dialogs.*
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.interfaces.OnLoadDataListener
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.loader.UtilsLoader
import com.sgo.saldomu.models.*
import com.sgo.saldomu.models.retrofit.GetTrxStatusModel
import com.sgo.saldomu.models.retrofit.InqBillerModel
import com.sgo.saldomu.models.retrofit.SentPaymentBillerModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.utils.CustomStringUtil
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_biller_input_new.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class BillerInputData : BaseFragment(), ReportBillerDialog.OnDialogOkCallback {

    private lateinit var viewLayout: View
    private val RC_READ_CONTACTS = 14

    val TAG = "BillerInputData"
    val REQUEST_BillerInqReq = 22
    private var billerTypeCode: String? = null
    private var tx_id: String? = null
    private var item_id: String? = null
    private var item_name: String? = null
    private var biller_comm_id: String? = null
    private var biller_comm_name: String? = null
    private var biller_item_id: String? = null
    private var cust_id: String? = "0"
    private var payment_name: String? = null
    private var ccy_id: String? = null
    private var description: String? = null
    private var enabledAdditionalFee: String? = null
    private var biller_comm_code: String? = null
    private var biller_api_key: String? = null
    //    private var callback_url: String? = null
    private var value_pin: String? = null
    private var _amount: String? = null
    private val amount_desire: String? = null
    private var attempt = 0
    private var failed = 0
    private var is_input_amount: Boolean? = null
    private var is_display_amount: Boolean = false
    private var isAgent: Boolean? = null
    private var isShowDescription: Boolean? = false
    private var is_sgo_plus: Boolean? = false
    private var isPIN: Boolean? = false
    private var fee = 0.0
    private var additional_fee = 0.0
    private var buy_type = 0
    private var amount = 0.0
    private var totalAmount = 0.0

    //    private var realm: Realm? = null
    private var realm2: Realm? = null
    private var mBillerData: BillerItem? = null

//    private var mDenomData: Biller_Data_Model? = null

    // new
    private var mDenomData: BillerItem? = null
    private var mListDenomData: List<DenomDataItem>? = null


    private var mListBillerData: List<Biller_Data_Model>? = null
    //    private var mListDenomData: List<Denom_Data_Model>? = null
    private val _data = ArrayList<String>()
    private var denomData: ArrayList<String>? = null
    private var adapterDenom: ArrayAdapter<String>? = null
    private var mBillerType: Biller_Type_Data_Model? = null
    private var paymentData: MutableList<String>? = null
    private var adapterPaymentOptions: ArrayAdapter<String>? = null
    private var mListBankBiller: List<BankBillerItem>? = null
    private var mTempBank: listBankModel? = null
    private lateinit var levelClass: LevelClass
    private lateinit var sentPaymentBillerModel: SentPaymentBillerModel

    private var billerItemList = ArrayList<BillerItem>()
    private val contactList = java.util.ArrayList<ContactList>()
    private var selectedContact: ContactList? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewLayout = inflater.inflate(R.layout.frag_biller_input_new, container, false)
        return viewLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        var args:Bundle? = getArguments()
        billerTypeCode = arguments!!.getString(DefineValue.BILLER_TYPE, "")
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        levelClass = LevelClass(activity, sp)
        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false)

        realm2 = Realm.getInstance(RealmManager.realmConfiguration)

        btn_submit_billerinput.setOnClickListener { submitInputListener() }

        initLayout()
        initEditTextListener()
//        initRealm()
//        if (_data.isEmpty()) {
        getBillerDenom()
//        } else {
        if (arguments!!.getString(DefineValue.CUST_ID, "") !== "") {
            billerinput_et_id_remark.setText(NoHPFormat.formatTo08(arguments?.getString(DefineValue.CUST_ID, "")))
//                checkOperator()
//                showChoosePayment()
        }
//        }

        favorite_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            notes_edit_text.visibility = if (isChecked) View.VISIBLE else View.GONE
            notes_edit_text.isEnabled = isChecked
        }

        ib_contact_list.setOnClickListener {
            if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                showListContact()
            } else {
                ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS),
                        RC_READ_CONTACTS)
            }
        }
    }

    private fun showListContact() {
        getContactList()
        Collections.sort(contactList) { contactList: ContactList, t1: ContactList -> contactList.name.compareTo(t1.name, ignoreCase = true) }
        val bundle = Bundle()
        bundle.putInt(DefineValue.SEARCH_TYPE, ActivitySearch.TYPE_SEARCH_CONTACT)
        //bundle.putString(DefineValue.TYPE, type);
        bundle.putParcelableArrayList(DefineValue.BUNDLE_LIST, contactList)
        val intent = Intent(activity, ActivitySearch::class.java)
        intent.putExtra(DefineValue.BUNDLE_FRAG, bundle)
        startActivityForResult(intent, RC_READ_CONTACTS)
    }

    private fun getContactList() {
        if (contactList.size > 0) {
            contactList.clear()
        }
        val cr = activity!!.contentResolver
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null)
        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME))
                if (cur.getInt(cur.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null)
                    while (pCur.moveToNext()) {
                        val phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER))
                        Timber.i("Name: $name")
                        Timber.i("Phone Number: $phoneNo")
                        contactList.add(ContactList(name, phoneNo))
                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
    }

    private val spinnerDenomListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {
            if (position != 0) {
                item_id = mListDenomData?.get(position - 1)?.itemId
                item_name = mListDenomData?.get(position - 1)?.itemName
                if (cust_id!!.length >= 10) {
                    sentInquryBiller()
                }
            } else {
                item_id = null
                item_name = null
            }
        }

        override fun onNothingSelected(adapterView: AdapterView<*>) {

        }
    }

    private val spinnerPaymentListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val item = adapterView?.getItemAtPosition(position)
            payment_name = item.toString()
            var i = 0
            while (i < mListBankBiller!!.size) {
                if (payment_name == mListBankBiller?.get(i)?.productName) {
                    mTempBank = listBankModel(mListBankBiller?.get(i)?.bankCode,
                            mListBankBiller?.get(i)?.bankName,
                            mListBankBiller?.get(i)?.productCode,
                            mListBankBiller?.get(i)?.productName,
                            mListBankBiller?.get(i)?.productType,
                            mListBankBiller?.get(i)?.productH2h)
                }
                i++
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }

    private fun checkOperator() {
        val billerIdNumber = PrefixOperatorValidator.validation(activity, cust_id)

        if (billerIdNumber != null) {

            when {
                billerIdNumber.prefix_name.toLowerCase().equals("telkomsel", ignoreCase = true) -> {
                    img_operator.background = resources.getDrawable(R.drawable.telkomsel)
                }
                billerIdNumber.prefix_name.toLowerCase().equals("xl", ignoreCase = true) -> {
                    img_operator.background = resources.getDrawable(R.drawable.xl)
                }
                billerIdNumber.prefix_name.toLowerCase().equals("indosat", ignoreCase = true) -> {
                    img_operator.background = resources.getDrawable(R.drawable.indosat)
                }
                billerIdNumber.prefix_name.toLowerCase().equals("three", ignoreCase = true) -> {
                    img_operator.background = resources.getDrawable(R.drawable.three)
                }
                billerIdNumber.prefix_name.toLowerCase().equals("smart", ignoreCase = true) -> {
                    img_operator.background = resources.getDrawable(R.drawable.smartfren)
                }
                else -> img_operator.visibility = View.GONE
            }

            for (i in _data.indices) {
                Timber.d("_data" + _data[i])

                if (_data != null) {
                    if (_data.get(i).toLowerCase().contains(billerIdNumber.prefix_name.toLowerCase())) {
                        Timber.d("_data " + billerItemList[i].commName)
                        biller_comm_id = billerItemList[i].commId
                        biller_comm_name = billerItemList[i].commName
                        biller_item_id = billerItemList[i].itemId

                        initializeSpinnerDenom(i)
                    }
                }

            }
        }
    }

    private fun initLayout() {
        buy_type = BillerActivity.PURCHASE_TYPE
        billerinput_text_denom.text = getString(R.string.billerinput_text_spinner_data)
        billerinput_et_id_remark.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(13))
        billerinput_et_id_remark.inputType = InputType.TYPE_CLASS_NUMBER
        billerinput_et_add_fee.inputType = InputType.TYPE_CLASS_NUMBER
        billerinput_detail_layout_add_fee.visibility = View.GONE
        billerinput_layout_denom.visibility = View.VISIBLE
        billerinput_layout_add_fee.visibility = View.GONE
        billerinput_layout_detail.visibility = View.GONE
    }

    private fun initEditTextListener() {
        billerinput_et_id_remark.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val string = editable.toString()
                if (string.length > 3) {
                    cust_id = string
                    checkOperator()
                } else
                    cust_id = NoHPFormat.formatTo62(billerinput_et_id_remark.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        billerinput_et_add_fee.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val string = s.toString()
                if (string == "0" || string == "") {
                    billerinput_et_add_fee.text.clear()
                    additional_fee = 0.0
                } else {
                    additional_fee = string.toDouble()
                }
                billerinput_detail_admin_add_fee.text = getString(R.string.rp_) + " " + CurrencyFormat.format(additional_fee)
                countTotal()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    private fun initializeSpinnerDenom(indexBiller: Int) {
        mDenomData = BillerItem()
        mDenomData = billerItemList[indexBiller]
        mListDenomData = billerItemList[indexBiller].denomData

        if (mListDenomData!!.isNotEmpty()) {
            denomData = ArrayList()
            adapterDenom = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, denomData)
            adapterDenom?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            billerinput_spinner_denom.adapter = adapterDenom
            billerinput_spinner_denom.onItemSelectedListener = spinnerDenomListener
            billerinput_spinner_denom.visibility = View.GONE

            val deproses = object : Thread() {
                override fun run() {
                    denomData?.clear()
                    denomData?.add(getString(R.string.billerinput_text_spinner_default_data))
                    for (i in mListDenomData!!.indices) {
                        denomData?.add(mListDenomData?.get(i)?.itemName!!)
                    }

                    activity!!.runOnUiThread {
                        billerinput_spinner_denom.visibility = View.VISIBLE
                        adapterDenom!!.notifyDataSetChanged()
                    }
                }
            }
            deproses.run()
        } else {
            item_id = mDenomData?.itemId
        }
        val spinAdapter = ArrayAdapter.createFromResource(activity!!, R.array.privacy_list, android.R.layout.simple_spinner_item)
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        mBillerData = BillerItem()
        mBillerData = billerItemList[indexBiller]
        mListBankBiller = billerItemList[indexBiller].bankBiller

        biller_comm_code = mBillerData?.commCode
        biller_api_key = mBillerData?.apiKey
//        callback_url = mBillerData?.callback_url
        if (billerItemList.isNotEmpty()) {
            paymentData = ArrayList()
            adapterPaymentOptions = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, paymentData)
            adapterPaymentOptions?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            billerinput_spinner_payment_options.adapter = adapterPaymentOptions
            billerinput_spinner_payment_options.onItemSelectedListener = spinnerPaymentListener
            if (isVisible) {
                val tempDataPaymentName = ArrayList<String>()
                paymentData?.add(getString(R.string.billerinput_text_spinner_default_payment))

                for (i in mListBankBiller!!.indices) {
                    if (mListBankBiller?.get(i)?.productCode == DefineValue.SCASH) {
                        paymentData?.add(getString(R.string.appname))
                        mListBankBiller?.get(i)?.productName = getString(R.string.appname)
                    } else {
                        tempDataPaymentName.add(mListBankBiller?.get(i)?.productName!!)
                    }
                }
                if (tempDataPaymentName.isNotEmpty())
                    tempDataPaymentName.sort()

                paymentData?.addAll(tempDataPaymentName)
                adapterPaymentOptions?.notifyDataSetChanged()

                billerinput_spinner_payment_options.setSelection(1) //set metode pembayaran jadi saldomu
            }
        } else {
            biller_item_id = mBillerData?.itemId
        }
    }

    private fun showChoosePayment() {
        val tempDataPaymentName = ArrayList<String>()
        paymentData?.add(getString(R.string.billerinput_text_spinner_default_payment))

        for (i in mListBankBiller!!.indices) {
            if (mListBankBiller?.get(i)?.productCode == DefineValue.SCASH) {
                paymentData?.add(getString(R.string.appname))
                mListBankBiller?.get(i)?.productName = getString(R.string.appname)
            } else {
                tempDataPaymentName.add(mListBankBiller?.get(i)?.productName!!)
            }
        }
        if (tempDataPaymentName.isNotEmpty())
            tempDataPaymentName.sort()

        paymentData?.addAll(tempDataPaymentName)
        adapterPaymentOptions?.notifyDataSetChanged()

        billerinput_spinner_payment_options.setSelection(1) //set metode pembayaran jadi saldomu

    }

    private fun submitInputListener() {
        if (InetHandler.isNetworkAvailable(activity)) {
            if (inputValidation()) {
                btn_submit_billerinput.isEnabled = false
                sentPaymentBiller()
            }
        } else {
            DefinedDialog.showErrorDialog(activity, getString(R.string.inethandler_dialog_message))
        }
    }

    private fun inputValidation(): Boolean {
        if (billerinput_et_id_remark.text.length < 10 ||
                billerinput_et_id_remark.text.length > 15) {
            billerinput_et_id_remark.requestFocus()
            billerinput_et_id_remark.error = getString(R.string.regist1_validation_nohp)
            //leo
//            initializeSpinnerDenom()
            return false
        }
        if (item_name == null) {
            billerinput_spinner_denom.requestFocus()
            Toast.makeText(activity, getString(R.string.billerinput_validation_spinner_default_data), Toast.LENGTH_LONG).show()
            return false
        }
        if (favorite_switch.isChecked && notes_edit_text.text.toString().isEmpty()) {
            notes_edit_text.requestFocus()
            notes_edit_text.error = getString(R.string.payfriends_notes_zero)
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                if (fragmentManager!!.backStackEntryCount > 0)
                    fragmentManager!!.popBackStack()
                else
                    activity!!.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    private fun initRealm() {
//        realm = Realm.getInstance(RealmManager.BillerConfiguration)
//
//        mBillerType = realm?.where(Biller_Type_Data_Model::class.java)?.equalTo(WebParams.BILLER_TYPE_CODE, "DATA")?.findFirst()
//        if (mBillerType != null) {
//            mListBillerData = mBillerType!!.biller_data_models
//            _data.clear()
//            for (i in mListBillerData!!.indices) {
//                Log.e(TAG, "initRealm : " + mListBillerData?.get(i)?.comm_name!!)
//                _data.add(mListBillerData?.get(i)?.comm_name!!)
//            }
//        } else {
//            mListBillerData = ArrayList()
//        }
//    }

    private fun initRealm() {
        Log.v(TAG, "initRealm()")
        var realmResults: RealmResults<BillerItem>? = realm2?.where(BillerItem::class.java)?.equalTo("billerType", "DATA")?.findAll()

        billerItemList.clear()
        _data.clear()
        billerItemList.addAll(realmResults!!)
        realmResults.forEach { result ->
            biller_comm_id = result.commId
            biller_comm_name = result.commName
            biller_item_id = result.itemId

            _data.add(result.commName)
        }
    }

    private fun switchFragment(mFrag: BillerConfirm, name: String, next_name: String?, isBackstack: Boolean, tag: String) {
        if (activity == null)
            return

        val fca = activity as BillerActivity?
        fca?.switchContent(mFrag, name, next_name, isBackstack, tag)
        billerinput_et_id_remark.text.clear()
        billerinput_spinner_denom.setSelection(0)
    }

    private fun countTotal() {
        totalAmount = amount + additional_fee + fee
        billerinput_detail_total.text = getString(R.string.rp_) + " " + CurrencyFormat.format(totalAmount)
    }

    private fun sentInquryBiller() {
        try {
            showProgressDialog()
            ToggleKeyboard.hide_keyboard(activity!!)

            cust_id = NoHPFormat.formatTo62(billerinput_et_id_remark.text.toString())

            extraSignature = biller_comm_id + item_id + cust_id

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_BILLER, extraSignature)
            params[WebParams.DENOM_ITEM_ID] = item_id
            params[WebParams.DENOM_ITEM_REMARK] = cust_id
            params[WebParams.COMM_ID] = biller_comm_id
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID_REMARK] = MyApiClient.COMM_ID

            Timber.d("isi params sent inquiry biller:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INQUIRY_BILLER, params,
                    object : ResponseListener {
                        override fun onResponses(response: JsonObject?) {
                            val model = getGson().fromJson(response, InqBillerModel::class.java)
                            var code = model.error_code
                            if (code == WebParams.SUCCESS_CODE) {
                                setIs_input_amount(model.biller_input_amount == DefineValue.STRING_YES)
                                is_display_amount = model.biller_display_amount == DefineValue.STRING_YES

                                tx_id = model.tx_id
                                item_id = model.item_id
                                ccy_id = model.ccy_id
                                amount = model.amount.toDouble() - model.admin_fee.toDouble()
                                item_name = model.item_name
                                description = getGson().toJson(model.description)
                                fee = model.admin_fee.toDouble()
                                enabledAdditionalFee = model.enabled_additional_fee

                                if (isAgent!! && enabledAdditionalFee.equals(DefineValue.Y)) {
                                    billerinput_layout_add_fee.visibility = View.VISIBLE
                                    billerinput_detail_layout_add_fee.visibility = View.VISIBLE
                                }

                                billerinput_layout_detail.visibility = View.VISIBLE

                                if (is_display_amount)
                                    isShowDescription = true
                                billerinput_detail_text_name.text = item_name
                                billerinput_detail_price.text = getString(R.string.rp_) + " " + CurrencyFormat.format(amount)
                                billerinput_detail_admin_fee.text = getString(R.string.rp_) + " " + CurrencyFormat.format(fee)
                            } else if (code == WebParams.LOGOUT_CODE) {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(activity, message)
                            } else if (code == DefineValue.ERROR_9333) run {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) run {
                                Timber.d("isi response maintenance:" + response.toString())
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                            } else {
                                code = model.error_code + " : " + model.error_message
                                if (isVisible) {
                                    Toast.makeText(activity, code, Toast.LENGTH_LONG).show()
                                    fragManager.popBackStack()
                                }
                                billerinput_layout_detail.visibility = View.GONE
                            }
                        }

                        override fun onError(throwable: Throwable?) {
                            fragManager.popBackStack()
                        }

                        override fun onComplete() {
                            dismissProgressDialog()

                            countTotal()
                        }

                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    fun getIs_input_amount(): Boolean? {
        return is_input_amount
    }

    fun setIs_input_amount(is_input_amount: Boolean?) {
        this.is_input_amount = is_input_amount
    }

    private fun sentPaymentBiller() {
        try {
            showProgressDialog()

            val bankCode = mTempBank?.bank_code
            val productCode = mTempBank?.product_code

            extraSignature = tx_id + item_id + biller_comm_id + productCode

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_PAYMENT_BILLER, extraSignature)
            params[WebParams.DENOM_ITEM_ID] = item_id
            params[WebParams.DENOM_ITEM_REMARK] = cust_id
            params[WebParams.TX_ID] = tx_id
            params[WebParams.AMOUNT] = amount + fee
            params[WebParams.BANK_CODE] = bankCode
            params[WebParams.PRODUCT_CODE] = productCode
            params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
            params[WebParams.COMM_ID] = biller_comm_id
            params[WebParams.MEMBER_CUST] = sp.getString(DefineValue.CUST_ID, "")
            params[WebParams.DATETIME] = DateTimeFormat.getCurrentDateTime()
            params[WebParams.COMM_CODE] = biller_comm_code
            params[WebParams.USER_COMM_CODE] = sp.getString(DefineValue.COMMUNITY_CODE, "")
            params[WebParams.PRODUCT_H2H] = mTempBank?.product_h2h
            params[WebParams.PRODUCT_TYPE] = mTempBank?.product_type
            params[WebParams.USER_ID] = userPhoneID

            if (isAgent!!)
                if (additional_fee != 0.0)
                    params[WebParams.ADDITIONAL_FEE] = additional_fee.toString()
                else
                    params[WebParams.ADDITIONAL_FEE] = "0"

            Timber.d("isi params sent payment biller:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_PAYMENT_BILLER, params,
                    object : ResponseListener {
                        override fun onResponses(response: JsonObject?) {
                            sentPaymentBillerModel = getGson().fromJson(response, SentPaymentBillerModel::class.java)
                            var code = sentPaymentBillerModel.error_code
                            if (code == WebParams.SUCCESS_CODE) {
                                if (mTempBank!!.product_type == DefineValue.BANKLIST_TYPE_IB) {
                                    submitBiller(bankCode, productCode, -1)
                                } else {
                                    var attempt = sentPaymentBillerModel.failed_attempt
                                    if (attempt != -1)
                                        attempt = sentPaymentBillerModel.max_failed - attempt
                                    sentDataReqToken(tx_id, productCode, biller_comm_code, sentPaymentBillerModel.fee, sentPaymentBillerModel.merchant_type, bankCode, attempt)
                                }
                            } else if (code == WebParams.LOGOUT_CODE) {
                                var message = sentPaymentBillerModel.error_message
                                var alertDialog = AlertDialogLogout.getInstance()
                                alertDialog.showDialoginActivity(activity, message)
                            } else if (code == DefineValue.ERROR_9333) run {
                                Timber.d("isi response app data:" + sentPaymentBillerModel.app_data)
                                val appModel = sentPaymentBillerModel.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) run {
                                Timber.d("isi response maintenance:" + response.toString())
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, sentPaymentBillerModel.error_message)
                            } else {
                                code = sentPaymentBillerModel.error_code + " : " + sentPaymentBillerModel.error_message
                                Toast.makeText(activity, code, Toast.LENGTH_LONG).show()
                                fragmentManager?.popBackStack()
                                dismissProgressDialog()
                            }
                        }

                        override fun onError(throwable: Throwable?) {
                            dismissProgressDialog()
                        }

                        override fun onComplete() {

                        }

                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun submitBiller(bank_code: String?, product_code: String?, attempt: Int) {
        isPIN = true
        UtilsLoader(activity, sp).getFailedPIN(userPhoneID, object : OnLoadDataListener {
            override fun onSuccess(deData: Any) {

            }

            override fun onFail(message: Bundle) {

            }

            override fun onFailure(message: String) {

            }
        })

        if (isPIN!!) {
            callPINinput(attempt)
            btn_submit_billerinput.isEnabled = true
        }

    }

    private fun callPINinput(_attempt: Int) {
        val i = Intent(activity, InsertPIN::class.java)
        if (_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, _attempt)
        startActivityForResult(i, MainPage.REQUEST_FINISH)
    }

    private fun sentDataReqToken(tx_id: String?, product_code: String?, biller_comm_code: String?, fee: String, merchant_type: String, bank_code: String?, attempt: Int) {
        try {
            extraSignature = tx_id + biller_comm_code + product_code

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature)
            params[WebParams.COMM_CODE] = biller_comm_code
            params[WebParams.TX_ID] = tx_id
            params[WebParams.PRODUCT_CODE] = product_code
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID] = MyApiClient.COMM_ID

            Timber.d("isi params regtoken Sgo+:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    object : ResponseListener {
                        override fun onResponses(response: JsonObject?) {
                            var model = getGson().fromJson(response, jsonModel::class.java)
                            var code = model.error_code
                            if (code == WebParams.SUCCESS_CODE) {
                                if (mTempBank!!.product_type == DefineValue.BANKLIST_TYPE_SMS)
                                    showDialog(fee, merchant_type, product_code, bank_code)
                                else if (merchant_type == DefineValue.AUTH_TYPE_OTP)
                                    showDialog(fee, merchant_type, product_code, bank_code)
                                else
                                    submitBiller(bank_code, product_code, attempt)
                            } else if (code == WebParams.LOGOUT_CODE) {
                                var message = model.error_message
                                var alertDialog = AlertDialogLogout.getInstance()
                                alertDialog.showDialoginActivity(activity, message)
                            } else if (code == ErrorDefinition.WRONG_PIN_BILLER) {
                                code = model.error_message
                                showDialogError(code)
                            } else if (code == DefineValue.ERROR_9333) run {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) run {
                                Timber.d("isi response maintenance:" + response.toString())
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                            } else {
                                var code_msg = model.error_message
                                when (code) {
                                    "0059" -> showDialogSMS(mTempBank?.bank_name)
                                    ErrorDefinition.ERROR_CODE_LESS_BALANCE -> {
                                        var message_dialog = "\"" + code_msg + "\"\n" + getString(R.string.dialog_message_less_balance, getString(R.string.appname))

                                        var dialogFrag = AlertDialogFrag.newInstance(getString(R.string.dialog_title_less_balance), message_dialog, getString(R.string.ok), getString(R.string.cancel), false)
                                        dialogFrag.okListener = DialogInterface.OnClickListener { dialog, which ->
                                            val mI = Intent(activity, TopUpActivity::class.java)
                                            mI.putExtra(DefineValue.IS_ACTIVITY_FULL, true)
                                            startActivityForResult(mI, REQUEST_BillerInqReq)
                                        }
                                        dialogFrag.cancelListener = DialogInterface.OnClickListener { dialog, which ->
                                            sentInquryBiller()
                                        }
                                        dialogFrag.setTargetFragment(this@BillerInputData, 0)
                                        dialogFrag.show(activity?.supportFragmentManager, AlertDialogFrag.TAG)
                                    }
                                    else -> {
                                        code = model.error_code + " : " + model.error_message
                                        Toast.makeText(activity, code, Toast.LENGTH_LONG).show()
                                        fragManager.popBackStack()
                                    }
                                }
                            }
                            dismissProgressDialog()
                        }

                        override fun onError(throwable: Throwable?) {
                            dismissProgressDialog()
                        }

                        override fun onComplete() {
                            btn_submit_billerinput.isEnabled = true
                        }

                    })

        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun showDialogSMS(bankName: String?) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        val levelClass = LevelClass(activity)
        message_dialog.visibility = View.VISIBLE
        message_dialog.text = getString(R.string.topup_not_registered, bankName)
        title_dialog.text = getString(R.string.topup_dialog_not_registered)
        btn_dialog_notification_ok.text = getString(R.string.firstscreen_button_daftar)
        if (levelClass.isLevel1QAC)
            btn_dialog_notification_ok.text = getString(R.string.ok)

        btn_dialog_notification_ok.setOnClickListener(View.OnClickListener {
            if (!levelClass.isLevel1QAC) {
                var intent = Intent(activity, RegisterSMSBankingActivity::class.java)
                intent.putExtra(DefineValue.BANK_NAME, bankName)
                switchActivity(intent)
            }
            dialog.dismiss()
        })
    }

    private fun switchActivity(intent: Intent) {
        if (activity == null)
            return
        val fca = activity as BillerActivity?
        fca?.switchActivity(intent, MainPage.ACTIVITY_RESULT)
    }

    private fun showDialogError(code: String?) {
        var dialog = DefinedDialog.MessageDialog(activity, getString(R.string.error), code) { v, isLongClick -> fragmentManager?.popBackStack() }
        dialog.show()
    }

    private fun showDialog(fee: String, merchantType: String, productCode: String?, bankCode: String?) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = getString(R.string.smsBanking_dialog_validation_title)
        dialog.title_dialog.text = resources.getString(R.string.regist1_notif_title_verification)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = getString(R.string.appname) + " " + getString(R.string.dialog_token_message_sms)

        dialog.btn_dialog_notification_ok.setOnClickListener {
            submitBiller(bankCode, productCode, -1)
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BillerInqReq)
            sentInquryBiller()
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                value_pin = data?.getStringExtra(DefineValue.PIN_VALUE)
                if (is_input_amount!!)
                    _amount = amount_desire
                else
                    _amount = amount.toString()

                if (favorite_switch.isChecked) {
                    onSaveToFavorite()
                } else {
                    sentInsertTransTopup(value_pin)
                }
            }
        }

        if (requestCode == RC_READ_CONTACTS) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                selectedContact = data.getParcelableExtra(DefineValue.ITEM_SELECTED)
                //trim 0878 - 0872 - 0888 -> ommit "-"
                val finalPhoneNo = CustomStringUtil.filterPhoneNo(selectedContact!!.getPhoneNo())
                billerinput_et_id_remark.setText(finalPhoneNo)
            }
        }

    }

    private fun getBillerDenom() {
        Log.v(TAG, "getBillerDenom()")

        extraSignature = "DATA"
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_BILLER_DENOM, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.COMM_ID] = MyApiClient.COMM_ID
        params[WebParams.BILLER_TYPE] = "DATA"

        Log.v(TAG, "getBillerDenom : " + "params")
        Log.v(TAG, "getBillerDenom : $params")

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_BILLER_DENOM, params, object : ResponseListener {
            override fun onResponses(`object`: JsonObject) {
                Log.v(TAG, "getBillerDenom : " + "onResponses")
                Log.v(TAG, "getBillerDenom : $`object`")

                val gson = Gson()
                val response = gson.fromJson(`object`, BillerDenomResponse::class.java)

                if (response.errorCode == WebParams.SUCCESS_CODE) {

                    response.biller?.forEach { result ->
                        _data.add(result.commName)
                    }

                    billerItemList.addAll(response.biller)

                    realm2?.beginTransaction()
                    realm2?.copyToRealmOrUpdate(response.biller)
                    realm2?.commitTransaction()

                    if (arguments!!.getString(DefineValue.CUST_ID, "") !== "") {
                        billerinput_et_id_remark.setText(NoHPFormat.formatTo08(arguments?.getString(DefineValue.CUST_ID, "")))
                        checkOperator()
                        showChoosePayment()
                    }
                } else {
                    Toast.makeText(context, response.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(throwable: Throwable) {
                Log.e(TAG, "getBillerDenom : " + "onError")
                Log.e(TAG, "getBillerDenom : " + throwable.message)
                Toast.makeText(context, throwable.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onComplete() {
                if (_data.isEmpty()) {
                    initRealm()
                }
            }
        })
    }

    private fun onSaveToFavorite() {
        showProgressDialog()
        extraSignature = cust_id + billerTypeCode + "BIL"
        Log.e("extraSignature params ", extraSignature)
        val url = MyApiClient.LINK_TRX_FAVORITE_SAVE
        val params = RetrofitService.getInstance().getSignature(url, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.PRODUCT_TYPE] = billerTypeCode
        params[WebParams.CUSTOMER_ID] = cust_id
        params[WebParams.TX_FAVORITE_TYPE] = "BIL"
        params[WebParams.COMM_ID] = biller_comm_id
        params[WebParams.NOTES] = notes_edit_text.text.toString()
        params[WebParams.DENOM_ITEM_ID] = item_id

        Log.e("params ", params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(url, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        try {
                            val model = RetrofitService.getInstance().gson.fromJson(response.toString(), jsonModel::class.java)
                            Log.e("onResponses ", response.toString())
                            val code = response.getString(WebParams.ERROR_CODE)
                            val message = response.getString(WebParams.ERROR_MESSAGE)
                            if (code == WebParams.SUCCESS_CODE) {
                                sentInsertTransTopup(value_pin)
                            } else if (code == DefineValue.ERROR_9333) {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) {
                                Timber.d("isi response maintenance:$response")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                            } else {
                                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(throwable: Throwable) {
                        Log.e("onResponses ", throwable.localizedMessage)
                        throwable.printStackTrace()
                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                    }
                })
    }

    private fun sentInsertTransTopup(tokenValue: String?) {
        try {
            showProgressDialog()

            val link = MyApiClient.LINK_INSERT_TRANS_TOPUP
            val subStringLink = link.substring(link.indexOf("saldomu/"))
            extraSignature = tx_id + biller_comm_code + mTempBank?.product_code + tokenValue
            val params = RetrofitService.getInstance().getSignature(link, extraSignature)
            val uuid: String = params[WebParams.RC_UUID].toString()
            val dateTime: String = params[WebParams.RC_DTIME].toString()
            params[WebParams.TX_ID] = tx_id
            params[WebParams.PRODUCT_CODE] = mTempBank?.product_code
            params[WebParams.COMM_CODE] = biller_comm_code
            params[WebParams.COMM_ID] = biller_comm_id
            params[WebParams.MEMBER_ID] = sp.getString(DefineValue.MEMBER_ID, "")
            params[WebParams.PRODUCT_VALUE] = RSA.opensslEncryptCommID(biller_comm_id, uuid, dateTime, userPhoneID, tokenValue, subStringLink)
            params[WebParams.USER_ID] = userPhoneID

            Timber.d("isi params insertTrxTOpupSGOL:$params")

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    object : ResponseListener {
                        override fun onResponses(response: JsonObject) {
                            val model = getGson().fromJson(response, SentPaymentBillerModel::class.java)

                            var code = model.error_code
                            if (code == WebParams.SUCCESS_CODE) {

                                getTrxStatus(tx_id, biller_comm_id)
                                setResultActivity(MainPage.RESULT_BALANCE)

                            } else if (code == WebParams.LOGOUT_CODE) {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(activity, message)
                            } else {

                                code = model.error_code + " : " + model.error_message
                                Toast.makeText(activity, code, Toast.LENGTH_LONG).show()
                                val message = model.error_message

                                if (isPIN!! && message == "PIN tidak sesuai") {
                                    val i = Intent(activity, InsertPIN::class.java)

                                    attempt = model.failed_attempt
                                    failed = model.max_failed

                                    if (attempt != -1)
                                        i.putExtra(DefineValue.ATTEMPT, failed - attempt)

                                    startActivityForResult(i, MainPage.REQUEST_FINISH)
                                } else {
                                    onOkButton()
                                }

                            }


                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                            btn_submit_billerinput.isEnabled = true
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }

    }

    private fun setResultActivity(result: Int) {
        if (activity == null)
            return

        val fca = activity as BillerActivity?
        fca!!.setResultActivity(result)
    }

    private fun getTrxStatus(txId: String?, comm_id: String?) {
        try {
            showProgressDialog()
            extraSignature = txId + comm_id
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature)

            params[WebParams.TX_ID] = txId
            params[WebParams.COMM_ID] = comm_id
            if (buy_type == BillerActivity.PURCHASE_TYPE)
                params[WebParams.TYPE] = DefineValue.BIL_PURCHASE_TYPE
            else
                params[WebParams.TYPE] = DefineValue.BIL_PAYMENT_TYPE
            params[WebParams.PRIVACY] = ""
            params[WebParams.TX_TYPE] = DefineValue.ESPAY
            params[WebParams.USER_ID] = userPhoneID

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    object : ResponseListener {
                        override fun onResponses(response: JsonObject) {
                            val model = getGson().fromJson(response, GetTrxStatusModel::class.java)

                            val code = model.error_code

                            if (!model.on_error) {
                                if (code == WebParams.SUCCESS_CODE || code == "0003") {

                                    val txstatus = model.tx_status
                                    showReportBillerDialog(sp.getString(DefineValue.USER_NAME, ""),
                                            sp.getString(DefineValue.USERID_PHONE, ""), txId, item_name,
                                            txstatus!!, model)
                                } else if (code == WebParams.LOGOUT_CODE) {
                                    val message = model.error_message
                                    val test = AlertDialogLogout.getInstance()
                                    test.showDialoginActivity(activity, message)
                                } else if (code == DefineValue.ERROR_9333) {
                                    Timber.d("isi response app data:" + model.app_data)
                                    val appModel = model.app_data
                                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                } else if (code == DefineValue.ERROR_0066) {
                                    Timber.d("isi response maintenance:$response")
                                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                    alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                                } else {
                                    val msg = model.error_message
                                    showDialog(msg)
                                }
                            } else {
                                Toast.makeText(activity, model.error_message, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                            btn_submit_billerinput.isEnabled = true
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }

    }

    private fun showDialog(msg: String) {
        // Create custom dialog object
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification)

        // set values for custom dialog components - text, image and button
        val btnDialogOTP = dialog.findViewById<Button>(R.id.btn_dialog_notification_ok)
        val Title = dialog.findViewById<TextView>(R.id.title_dialog)
        val Message = dialog.findViewById<TextView>(R.id.message_dialog)

        Message.visibility = View.VISIBLE
        Title.text = getString(R.string.error)
        Message.text = msg

        btnDialogOTP.setOnClickListener { view -> dialog.dismiss() }

        dialog.show()
    }

    private fun showReportBillerDialog(name: String?, userId: String?, txId: String?, itemName: String?, txStatus: String,
                                       model: GetTrxStatusModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.USER_NAME, name)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(model.created!!))
        args.putString(DefineValue.TX_ID, txId)
        args.putString(DefineValue.USERID_PHONE, userId)
        args.putString(DefineValue.DENOM_DATA, itemName)
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount))
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER)
        args.putInt(DefineValue.BUY_TYPE, buy_type)
        args.putString(DefineValue.PAYMENT_NAME, payment_name)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee))
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(totalAmount))
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(additional_fee))
        args.putString(DefineValue.DESTINATION_REMARK, NoHPFormat.formatTo62(cust_id))
        args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription!!)

        var txStat: Boolean? = false
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        } else if (txStatus == DefineValue.SUSPECT) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
        } else if (txStatus != DefineValue.FAILED) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, model.tx_remark)

        var _isi_amount_desired = ""
        if (is_input_amount!!)
            _isi_amount_desired = amount_desire!!

        args.putString(DefineValue.DETAILS_BILLER, model.product_name)

        if (_isi_amount_desired.isEmpty())
            args.putString(DefineValue.AMOUNT_DESIRED, _isi_amount_desired)
        else
            args.putString(DefineValue.AMOUNT_DESIRED, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_isi_amount_desired))

        if (model.product_name == null) {
            args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_PLN)
            if (billerTypeCode.equals(DefineValue.BILLER_TYPE_BPJS, ignoreCase = true))
                args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_BPJS)
            args.putString(DefineValue.BILLER_TYPE, billerTypeCode)
        }

        args.putString(DefineValue.BILLER_DETAIL, toJson(model.biller_detail).toString())
        args.putString(DefineValue.BUSS_SCHEME_CODE, model.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, model.buss_scheme_name)
        args.putString(DefineValue.PRODUCT_NAME, model.product_name)

        dialog.arguments = args
        val ft = activity!!.supportFragmentManager.beginTransaction()
        ft.add(dialog, ReportBillerDialog.TAG)
        ft.commitAllowingStateLoss()
    }

    override fun onOkButton() {
        fragmentManager != null
        fragmentManager!!.popBackStackImmediate(BillerActivity.FRAG_BIL_INPUT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}

