package com.sgo.saldomu.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sgo.saldomu.Beans.listBankModel
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.*
import com.sgo.saldomu.adapter.AdapterDenomList
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
import com.sgo.saldomu.utils.NumberTextWatcherForThousand
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.dialog_biller_confirm.*
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_biller_input_with_description.*
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
    private var notes: String = ""
    private var attempt = 0
    private var failed = 0
    private var is_input_amount: Boolean? = null
    private var is_display_amount: Boolean = false
    private var isAgent: Boolean? = null
    private var isShowDescription: Boolean? = false
    private var isPIN: Boolean? = false
    private var saveToFavorite: Boolean = false
    private var fee = 0.0
    private var additional_fee = 0.0
    private var buy_type = 0
    private var amount = 0.0
    private var totalAmount = 0.0

    private var realm: Realm? = null
    private var mBillerData: BillerItem? = null

    // new
    private var mDenomData: BillerItem? = null
    private var mListDenomData: List<DenomDataItem> = ArrayList()

    private val _data = ArrayList<String>()
    private var denomData: ArrayList<String>? = null
    private var adapterDenom: ArrayAdapter<String>? = null
    private var paymentData: MutableList<String>? = null
    private var adapterPaymentOptions: ArrayAdapter<String>? = null
    private var mListBankBiller: List<BankBillerItem>? = null
    private var mTempBank: listBankModel? = null
    private lateinit var levelClass: LevelClass
    private lateinit var sentPaymentBillerModel: SentPaymentBillerModel

    private var billerItemList = ArrayList<BillerItem>()
    private val contactList = java.util.ArrayList<ContactList>()
    private var selectedContact: ContactList? = null
    private var adapterDenomList: AdapterDenomList? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLayout = inflater.inflate(R.layout.frag_biller_input_with_description, container, false)
        return viewLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        var args:Bundle? = getArguments()
        billerTypeCode = arguments!!.getString(DefineValue.BILLER_TYPE, "")
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        levelClass = LevelClass(activity, sp)
        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false)

        realm = Realm.getInstance(RealmManager.realmConfiguration)

        initLayout()
        initEditTextListener()
        getBillerDenom()
        if (arguments!!.getString(DefineValue.CUST_ID, "") !== "") {
            billerinput_et_id.setText(
                NoHPFormat.formatTo08(
                    arguments?.getString(
                        DefineValue.CUST_ID,
                        ""
                    )
                )
            )
        }

        ib_contact_list.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showListContact()
            } else {
                ActivityCompat.requestPermissions(
                    activity!!, arrayOf(Manifest.permission.READ_CONTACTS),
                    RC_READ_CONTACTS
                )
            }
        }
    }

    private fun showListContact() {
//        getContactList()
        //Collections.sort(contactList) { contactList: ContactList, t1: ContactList -> contactList.name.compareTo(t1.name, ignoreCase = true) }
        val bundle = Bundle()
        bundle.putInt(DefineValue.SEARCH_TYPE, ActivitySearch.TYPE_SEARCH_CONTACT)
        //bundle.putString(DefineValue.TYPE, type);
        bundle.putParcelableArrayList(DefineValue.BUNDLE_LIST, contactList)
        val intent = Intent(activity, ActivitySearch::class.java)
        intent.putExtra(DefineValue.BUNDLE_FRAG, bundle)
        startActivityForResult(intent, RC_READ_CONTACTS)
    }

    private val spinnerPaymentListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            adapterView: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            val item = adapterView?.getItemAtPosition(position)
            payment_name = item.toString()
            var i = 0
            while (i < mListBankBiller!!.size) {
                if (payment_name == mListBankBiller?.get(i)?.productName) {
                    mTempBank = listBankModel(
                        mListBankBiller?.get(i)?.bankCode,
                        mListBankBiller?.get(i)?.bankName,
                        mListBankBiller?.get(i)?.productCode,
                        mListBankBiller?.get(i)?.productName,
                        mListBankBiller?.get(i)?.productType,
                        mListBankBiller?.get(i)?.productH2h
                    )
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
                billerIdNumber.prefix_name.toLowerCase(Locale.ROOT)
                    .equals("telkomsel", ignoreCase = true) -> {
                    img_operator.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.telkomsel, null)
                }
                billerIdNumber.prefix_name.toLowerCase(Locale.ROOT)
                    .equals("xl", ignoreCase = true) -> {
                    img_operator.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.xl, null)
                }
                billerIdNumber.prefix_name.toLowerCase(Locale.ROOT)
                    .equals("indosat", ignoreCase = true) -> {
                    img_operator.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.indosat, null)
                }
                billerIdNumber.prefix_name.toLowerCase(Locale.ROOT)
                    .equals("three", ignoreCase = true) -> {
                    img_operator.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.three, null)
                }
                billerIdNumber.prefix_name.toLowerCase(Locale.ROOT)
                    .equals("smart", ignoreCase = true) -> {
                    img_operator.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.smartfren, null)
                }
                else -> img_operator.visibility = View.GONE
            }

            for (i in _data.indices) {
                Timber.d("_data%s", _data[i])

                if (_data[i].toLowerCase(Locale.ROOT)
                        .contains(billerIdNumber.prefix_name.toLowerCase(Locale.ROOT))
                ) {
                    Timber.d("_data %s", billerItemList[i].commName)
                    biller_comm_id = billerItemList[i].commId
                    biller_comm_name = billerItemList[i].commName
                    biller_item_id = billerItemList[i].itemId

                    initializeSpinnerDenom(i)
                }

            }
        }
    }

    private fun initLayout() {
        buy_type = BillerActivity.PURCHASE_TYPE
//        billerinput_et_id.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(13))
        billerinput_et_id.inputType = InputType.TYPE_CLASS_NUMBER
    }

    private fun initEditTextListener() {
        billerinput_et_id.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val string = editable.toString()
                if (string.length > 3) {
                    cust_id = string
                    checkOperator()
                } else
                    cust_id = NoHPFormat.formatTo62(billerinput_et_id.text.toString())
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
        adapterDenomList =
            AdapterDenomList(context!!, mListDenomData, object : AdapterDenomList.OnClick {
                override fun onClick(pos: Int) {
                    if (inputValidation()) {
                        item_id = mListDenomData[pos].itemId
                        sentInquryBiller()
                    }
                }
            })
        billerinput_list_denom.adapter = adapterDenomList
        billerinput_list_denom.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        adapterDenomList!!.notifyDataSetChanged()

        mBillerData = BillerItem()
        mBillerData = billerItemList[indexBiller]
        mListBankBiller = billerItemList[indexBiller].bankBiller

        biller_comm_code = mBillerData?.commCode
        biller_api_key = mBillerData?.apiKey
//        callback_url = mBillerData?.callback_url
        if (billerItemList.isNotEmpty()) {
            paymentData = ArrayList()
            adapterPaymentOptions =
                ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, paymentData!!)
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
                sentPaymentBiller()
            }
        } else {
            DefinedDialog.showErrorDialog(activity, getString(R.string.inethandler_dialog_message))
        }
    }

    private fun inputValidation(): Boolean {
        if (billerinput_et_id.text!!.length < 10 ||
            billerinput_et_id.text!!.length > 15
        ) {
            billerinput_et_id.requestFocus()
            if (billerinput_et_id.text!!.length < 10)
                billerinput_et_id.error = getString(R.string.regist1_validation_length_nohp)
            else if (billerinput_et_id.text!!.length > 15)
                billerinput_et_id.error = getString(R.string.validation_length_min_15)
            return false
        } else if (payment_name == getString(R.string.billerinput_text_spinner_default_payment)) {
            Toast.makeText(
                activity,
                getString(R.string.billerinput_validation_spinner_default_payment),
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
        Timber.tag(TAG).v("initRealm()")
        val realmResults: RealmResults<BillerItem>? =
            realm?.where(BillerItem::class.java)?.equalTo(DefineValue.BILLER_TYPE, billerTypeCode)
                ?.findAll()

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

    private fun countTotal() {
        totalAmount = amount + additional_fee + fee
    }

    private fun sentInquryBiller() {
        try {
            showProgressDialog()
            ToggleKeyboard.hide_keyboard(activity!!)

            cust_id = NoHPFormat.formatTo62(billerinput_et_id.text.toString())

            extraSignature = biller_comm_id + item_id + cust_id

            val params = RetrofitService.getInstance()
                .getSignature(MyApiClient.LINK_INQUIRY_BILLER, extraSignature)
            params[WebParams.DENOM_ITEM_ID] = item_id
            params[WebParams.DENOM_ITEM_REMARK] = cust_id
            params[WebParams.COMM_ID] = biller_comm_id
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID_REMARK] = MyApiClient.COMM_ID

            Timber.d("isi params sent inquiry biller:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INQUIRY_BILLER, params,
                object : ResponseListener {
                    @SuppressLint("SetTextI18n")
                    override fun onResponses(response: JsonObject?) {
                        val model = getGson().fromJson(response, InqBillerModel::class.java)
                        var code = model.error_code
                        var message = model.error_message
                        if (code == WebParams.SUCCESS_CODE) {
                            setIsInputAmount(model.biller_input_amount == DefineValue.STRING_YES)
                            is_display_amount =
                                model.biller_display_amount == DefineValue.STRING_YES

                            tx_id = model.tx_id
                            item_id = model.item_id
                            ccy_id = model.ccy_id
                            amount = model.amount.toDouble() - model.admin_fee.toDouble()
                            item_name = model.item_name
                            description = getGson().toJson(model.description)
                            fee = model.admin_fee.toDouble()
                            enabledAdditionalFee = model.enabled_additional_fee
                            countTotal()
                            if (is_display_amount)
                                isShowDescription = true

                            val dialog = Dialog(activity!!)
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            dialog.setCanceledOnTouchOutside(false)
                            dialog.setContentView(R.layout.dialog_biller_confirm)

                            if (isAgent!! && enabledAdditionalFee.equals(DefineValue.STRING_YES))
                                dialog.billerinput_layout_add_fee.visibility = View.VISIBLE
                            else
                                dialog.billerinput_layout_add_fee.visibility = View.GONE

                            if (additional_fee > 0)
                                dialog.billerinput_et_add_fee.setText(
                                    NumberTextWatcherForThousand.getDecimalFormattedString(
                                        additional_fee.toInt().toString()
                                    )
                                )

                            dialog.billerinput_et_add_fee.addTextChangedListener(object :
                                TextWatcher {
                                @SuppressLint("SetTextI18n")
                                override fun afterTextChanged(s: Editable?) {
                                    dialog.billerinput_et_add_fee.removeTextChangedListener(this)
                                    val value = s.toString()
                                    if (value != "") {
                                        if (value.startsWith("0") && !value.startsWith("0.")) dialog.billerinput_et_add_fee.setText(
                                            ""
                                        ) else {
                                            val str =
                                                NumberTextWatcherForThousand.trimCommaOfString(value)
                                            dialog.billerinput_et_add_fee.setText(
                                                NumberTextWatcherForThousand.getDecimalFormattedString(
                                                    str
                                                )
                                            )
                                        }
                                        additional_fee =
                                            NumberTextWatcherForThousand.trimCommaOfString(dialog.billerinput_et_add_fee.text.toString())
                                                .toDouble()
                                        dialog.billerinput_et_add_fee.setSelection(dialog.billerinput_et_add_fee.text.toString().length)
                                    } else
                                        additional_fee = 0.0
                                    countTotal()
                                    dialog.total.text =
                                        getString(R.string.rp_) + " " + CurrencyFormat.format(
                                            totalAmount
                                        )
                                    dialog.billerinput_et_add_fee.addTextChangedListener(this)
                                }

                                override fun beforeTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    count: Int,
                                    after: Int
                                ) {

                                }

                                override fun onTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    before: Int,
                                    count: Int
                                ) {

                                }

                            })

                            dialog.denom.text = item_name
                            dialog.price.text =
                                getString(R.string.rp_) + " " + CurrencyFormat.format(amount)
                            dialog.fee.text =
                                getString(R.string.rp_) + " " + CurrencyFormat.format(fee)

                            dialog.total.text =
                                getString(R.string.rp_) + " " + CurrencyFormat.format(totalAmount)

                            dialog.layout_notes.visibility = View.GONE
                            dialog.checkBox.setOnCheckedChangeListener { _, isChecked ->
                                run {
                                    saveToFavorite = isChecked
                                    if (isChecked)
                                        dialog.layout_notes.visibility = View.VISIBLE
                                    else
                                        dialog.layout_notes.visibility = View.GONE
                                }
                            }

                            dialog.notes.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(
                                    p0: CharSequence?,
                                    p1: Int,
                                    p2: Int,
                                    p3: Int
                                ) {

                                }

                                override fun onTextChanged(
                                    p0: CharSequence?,
                                    p1: Int,
                                    p2: Int,
                                    p3: Int
                                ) {

                                }

                                override fun afterTextChanged(p0: Editable?) {
                                    notes = p0.toString()
                                }

                            })
                            dialog.btnCancel.setOnClickListener {
                                dialog.dismiss()
                            }

                            dialog.btnSubmit.setOnClickListener {
                                if (saveToFavorite && notes == "") {
                                    dialog.notes.requestFocus()
                                    dialog.notes.error = getString(R.string.payfriends_notes_zero)
                                } else {
                                    dialog.dismiss()
                                    submitInputListener()
                                }
                            }
                            dialog.show()
                            countTotal()
                        } else if (code == WebParams.LOGOUT_CODE) {
                            AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                        } else if (code == DefineValue.ERROR_9333) run {
                            Timber.d("isi response app data:%s", model.app_data)
                            val appModel = model.app_data
                            AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                activity,
                                appModel.type,
                                appModel.packageName,
                                appModel.downloadUrl
                            )
                        } else if (code == DefineValue.ERROR_0066) run {
                            Timber.d("isi response maintenance:%s", response.toString())
                            val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                            alertDialogMaintenance.showDialogMaintenance(activity)
                        } else {
                            if (isVisible) {
                                Toast.makeText(activity, "$code : $message", Toast.LENGTH_LONG)
                                    .show()
                                fragManager.popBackStack()
                            }
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
            Timber.d("httpclient:%s", e.message)
        }
    }

    fun setIsInputAmount(is_input_amount: Boolean?) {
        this.is_input_amount = is_input_amount
    }

    private fun sentPaymentBiller() {
        try {
            showProgressDialog()

            val bankCode = mTempBank?.bank_code
            val productCode = mTempBank?.product_code

            extraSignature = tx_id + item_id + biller_comm_id + productCode

            val params = RetrofitService.getInstance()
                .getSignature(MyApiClient.LINK_PAYMENT_BILLER, extraSignature)
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
                        sentPaymentBillerModel =
                            getGson().fromJson(response, SentPaymentBillerModel::class.java)
                        val code = sentPaymentBillerModel.error_code
                        val message = sentPaymentBillerModel.error_message
                        if (code == WebParams.SUCCESS_CODE) {
                            if (mTempBank!!.product_type == DefineValue.BANKLIST_TYPE_IB) {
                                submitBiller(-1)
                            } else {
                                var attempt = sentPaymentBillerModel.failed_attempt
                                if (attempt != -1)
                                    attempt = sentPaymentBillerModel.max_failed - attempt
                                sentDataReqToken(
                                    tx_id,
                                    productCode,
                                    biller_comm_code,
                                    sentPaymentBillerModel.merchant_type,
                                    attempt
                                )
                            }
                        } else if (code == WebParams.LOGOUT_CODE) {
                            AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                        } else if (code == DefineValue.ERROR_9333) run {
                            Timber.d("isi response app data:%s", sentPaymentBillerModel.app_data)
                            val appModel = sentPaymentBillerModel.app_data
                            val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                            alertDialogUpdateApp.showDialogUpdate(
                                activity,
                                appModel.type,
                                appModel.packageName,
                                appModel.downloadUrl
                            )
                        } else if (code == DefineValue.ERROR_0066) run {
                            Timber.d("isi response maintenance:%s", response.toString())
                            AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
                        } else {
                            Toast.makeText(activity, "$code : $message", Toast.LENGTH_LONG).show()
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
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun submitBiller(attempt: Int) {
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
        }

    }

    private fun callPINinput(_attempt: Int) {
        val i = Intent(activity, InsertPIN::class.java)
        if (_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, _attempt)
        startActivityForResult(i, MainPage.REQUEST_FINISH)
    }

    private fun sentDataReqToken(
        tx_id: String?,
        product_code: String?,
        biller_comm_code: String?,
        merchant_type: String,
        attempt: Int
    ) {
        try {
            extraSignature = tx_id + biller_comm_code + product_code

            val params = RetrofitService.getInstance()
                .getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature)
            params[WebParams.COMM_CODE] = biller_comm_code
            params[WebParams.TX_ID] = tx_id
            params[WebParams.PRODUCT_CODE] = product_code
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID] = MyApiClient.COMM_ID

            Timber.d("isi params regtoken Sgo+:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                object : ResponseListener {
                    override fun onResponses(response: JsonObject?) {
                        val model = getGson().fromJson(response, jsonModel::class.java)
                        val code = model.error_code
                        val message = model.error_message
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                when {
                                    mTempBank!!.product_type == DefineValue.BANKLIST_TYPE_SMS -> showDialog()
                                    merchant_type == DefineValue.AUTH_TYPE_OTP -> showDialog()
                                    else -> submitBiller(attempt)
                                }
                            }
                            WebParams.LOGOUT_CODE -> {
                                val alertDialog = AlertDialogLogout.getInstance()
                                alertDialog.showDialoginActivity(activity, message)
                            }
                            ErrorDefinition.WRONG_PIN_BILLER -> {
                                showDialogError(message)
                            }
                            DefineValue.ERROR_9333 -> run {
                                Timber.d("isi response app data:%s", model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(
                                    activity,
                                    appModel.type,
                                    appModel.packageName,
                                    appModel.downloadUrl
                                )
                            }
                            DefineValue.ERROR_0066 -> run {
                                Timber.d("isi response maintenance:%s", response.toString())
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity)
                            }
                            else -> {
                                when (code) {
                                    "0059" -> showDialogSMS(mTempBank?.bank_name)
                                    ErrorDefinition.ERROR_CODE_LESS_BALANCE -> {
                                        val messageDialog = "\"" + message + "\"\n" + getString(
                                            R.string.dialog_message_less_balance,
                                            getString(R.string.appname)
                                        )

                                        val dialogFrag = AlertDialogFrag.newInstance(
                                            getString(R.string.dialog_title_less_balance),
                                            messageDialog,
                                            getString(R.string.ok),
                                            getString(R.string.cancel),
                                            false
                                        )
                                        dialogFrag.okListener =
                                            DialogInterface.OnClickListener { dialog, which ->
                                                val mI = Intent(activity, TopUpActivity::class.java)
                                                mI.putExtra(DefineValue.IS_ACTIVITY_FULL, true)
                                                startActivityForResult(mI, REQUEST_BillerInqReq)
                                            }
                                        dialogFrag.cancelListener =
                                            DialogInterface.OnClickListener { dialog, which ->
                                                sentInquryBiller()
                                            }
                                        dialogFrag.setTargetFragment(this@BillerInputData, 0)
                                        dialogFrag.show(
                                            activity!!.supportFragmentManager,
                                            AlertDialogFrag.TAG
                                        )
                                    }
                                    else -> {
                                        Toast.makeText(
                                            activity,
                                            "$code : $message",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        fragManager.popBackStack()
                                    }
                                }
                            }
                        }
                        dismissProgressDialog()
                    }

                    override fun onError(throwable: Throwable?) {
                        dismissProgressDialog()
                    }

                    override fun onComplete() {
                    }

                })

        } catch (e: Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun showDialogSMS(bankName: String?) {
        val dialog = Dialog(activity!!)
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

        btn_dialog_notification_ok.setOnClickListener {
            if (!levelClass.isLevel1QAC) {
                val intent = Intent(activity, RegisterSMSBankingActivity::class.java)
                intent.putExtra(DefineValue.BANK_NAME, bankName)
                switchActivity(intent)
            }
            dialog.dismiss()
        }
    }

    private fun switchActivity(intent: Intent) {
        if (activity == null)
            return
        val fca = activity as BillerActivity?
        fca?.switchActivity(intent, MainPage.ACTIVITY_RESULT)
    }

    private fun showDialogError(code: String?) {
        val dialog = DefinedDialog.MessageDialog(
            activity,
            getString(R.string.error),
            code
        ) { fragmentManager?.popBackStack() }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showDialog() {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = resources.getString(R.string.regist1_notif_title_verification)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text =
            getString(R.string.appname) + " " + getString(R.string.dialog_token_message_sms)

        dialog.btn_dialog_notification_ok.setOnClickListener {
            submitBiller(-1)
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
                _amount = if (is_input_amount!!)
                    amount_desire
                else
                    amount.toString()

                if (saveToFavorite) {
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
                val finalPhoneNo = CustomStringUtil.filterPhoneNo(selectedContact!!.phoneNo)
                billerinput_et_id.setText(finalPhoneNo)
            }
        }

    }

    private fun getBillerDenom() {
        extraSignature = billerTypeCode
        val params = RetrofitService.getInstance()
            .getSignature(MyApiClient.LINK_GET_BILLER_DENOM, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.COMM_ID] = MyApiClient.COMM_ID
        params[WebParams.BILLER_TYPE] = billerTypeCode

        Timber.tag(TAG).v("getBillerDenom : $params")

        RetrofitService.getInstance().PostObjectRequest(
            MyApiClient.LINK_GET_BILLER_DENOM,
            params,
            object : ResponseListener {
                override fun onResponses(jsonObject: JsonObject) {
                    Timber.tag(TAG).v("getBillerDenom : $jsonObject")

                    val gson = Gson()
                    val response = gson.fromJson(jsonObject, BillerDenomResponse::class.java)

                    if (response.errorCode == WebParams.SUCCESS_CODE) {

                        response.biller?.forEach { result ->
                            _data.add(result.commName)
                        }

                        billerItemList.addAll(response.biller)

                        realm?.beginTransaction()
                        realm?.copyToRealmOrUpdate(response.biller)
                        realm?.commitTransaction()

                        if (arguments!!.getString(DefineValue.CUST_ID, "") !== "") {
                            billerinput_et_id.setText(
                                NoHPFormat.formatTo08(
                                    arguments?.getString(
                                        DefineValue.CUST_ID,
                                        ""
                                    )
                                )
                            )
                            checkOperator()
                            showChoosePayment()
                        }
                    } else {
                        Toast.makeText(context, response.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(throwable: Throwable) {
                    Timber.e("getBillerDenom : %s", throwable.message)
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
        val url = MyApiClient.LINK_TRX_FAVORITE_SAVE
        val params = RetrofitService.getInstance().getSignature(url, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.PRODUCT_TYPE] = billerTypeCode
        params[WebParams.CUSTOMER_ID] = cust_id
        params[WebParams.TX_FAVORITE_TYPE] = "BIL"
        params[WebParams.COMM_ID] = biller_comm_id
        params[WebParams.NOTES] = notes
        params[WebParams.DENOM_ITEM_ID] = item_id

        Timber.tag("params ").e(params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(url, params,
            object : ObjListeners {
                override fun onResponses(response: JSONObject) {
                    try {
                        val model = RetrofitService.getInstance().gson.fromJson(
                            response.toString(),
                            jsonModel::class.java
                        )
                        Timber.tag("onResponses ").e(response.toString())
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                sentInsertTransTopup(value_pin)
                            }
                            DefineValue.ERROR_9333 -> {
                                Timber.d("isi response app data:%s", model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(
                                    activity,
                                    appModel.type,
                                    appModel.packageName,
                                    appModel.downloadUrl
                                )
                            }
                            DefineValue.ERROR_0066 -> {
                                Timber.d("isi response maintenance:$response")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity)
                            }
                            else -> {
                                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }

                override fun onError(throwable: Throwable) {
                    Timber.tag("onResponses ").e(throwable.localizedMessage)
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
            params[WebParams.PRODUCT_VALUE] = RSA.opensslEncryptCommID(
                biller_comm_id,
                uuid,
                dateTime,
                userPhoneID,
                tokenValue,
                subStringLink
            )
            params[WebParams.USER_ID] = userPhoneID

            Timber.d("isi params insertTrxTOpupSGOL:$params")

            RetrofitService.getInstance().PostObjectRequest(link, params,
                object : ResponseListener {
                    override fun onResponses(response: JsonObject) {
                        val model = getGson().fromJson(response, SentPaymentBillerModel::class.java)
                        val code = model.error_code
                        val message = model.error_message
                        if (code == WebParams.SUCCESS_CODE) {
                            getTrxStatus(tx_id, biller_comm_id)
                            setResultActivity()
                        } else if (code == WebParams.LOGOUT_CODE) {
                            val test = AlertDialogLogout.getInstance()
                            test.showDialoginActivity(activity, message)
                        } else {
                            Toast.makeText(activity, "$code : $message", Toast.LENGTH_LONG).show()
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
                    }
                })
        } catch (e: Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun setResultActivity() {
        if (activity == null)
            return

        val fca = activity as BillerActivity?
        fca!!.setResultActivity(MainPage.RESULT_BALANCE)
    }

    private fun getTrxStatus(txId: String?, comm_id: String?) {
        try {
            showProgressDialog()
            extraSignature = txId + comm_id
            val params = RetrofitService.getInstance()
                .getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature)

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
                        val message = model.error_message
                        if (!model.on_error) {
                            if (code == WebParams.SUCCESS_CODE || code == "0003") {

                                val txstatus = model.tx_status
                                showReportBillerDialog(
                                    sp.getString(DefineValue.USER_NAME, ""),
                                    sp.getString(DefineValue.USERID_PHONE, ""), txId, item_name,
                                    txstatus!!, model
                                )
                            } else if (code == WebParams.LOGOUT_CODE) {
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(activity, message)
                            } else if (code == DefineValue.ERROR_9333) {
                                Timber.d("isi response app data:%s", model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(
                                    activity,
                                    appModel.type,
                                    appModel.packageName,
                                    appModel.downloadUrl
                                )
                            } else if (code == DefineValue.ERROR_0066) {
                                Timber.d("isi response maintenance:$response")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity)
                            } else {
                                showDialog(message)
                            }
                        } else {
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(throwable: Throwable) {

                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                    }
                })
        } catch (e: Exception) {
            Timber.d("httpclient:%s", e.message)
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

        btnDialogOTP.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showReportBillerDialog(
        name: String?, userId: String?, txId: String?, itemName: String?, txStatus: String,
        model: GetTrxStatusModel
    ) {
        showProgressDialog()
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.USER_NAME, name)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(model.created!!))
        args.putString(DefineValue.TX_ID, txId)
        args.putString(DefineValue.USERID_PHONE, userId)
        args.putString(DefineValue.DENOM_DATA, itemName)
        args.putString(
            DefineValue.AMOUNT,
            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount)
        )
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER)
        args.putInt(DefineValue.BUY_TYPE, buy_type)
        args.putString(DefineValue.PAYMENT_NAME, payment_name)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee))
        args.putString(
            DefineValue.TOTAL_AMOUNT,
            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(totalAmount)
        )
        args.putString(
            DefineValue.ADDITIONAL_FEE,
            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(additional_fee)
        )
        args.putString(DefineValue.DESTINATION_REMARK, NoHPFormat.formatTo62(cust_id))
        args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription!!)

        var txStat: Boolean? = false
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        }
//        else if (txStatus == DefineValue.SUSPECT) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//        } else if (txStatus != DefineValue.FAILED) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, model.tx_remark)
        args.putString(DefineValue.TRX_STATUS_REMARK, model.tx_status_remark)

        args.putString(DefineValue.DETAILS_BILLER, model.product_name)

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
        dismissProgressDialog()
        fragmentManager != null
        fragmentManager!!.popBackStackImmediate(
            BillerActivity.FRAG_BIL_INPUT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }
}

