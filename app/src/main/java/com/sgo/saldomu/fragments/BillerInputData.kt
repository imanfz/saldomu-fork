package com.sgo.saldomu.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.gson.JsonObject
import com.sgo.saldomu.Beans.*
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.BillerActivity
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.activities.RegisterSMSBankingActivity
import com.sgo.saldomu.activities.TopUpActivity
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.dialogs.AlertDialogFrag
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.DefinedDialog
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.InqBillerModel
import com.sgo.saldomu.models.retrofit.SentPaymentBillerModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_biller_input_new.*
import timber.log.Timber
import java.util.*

class BillerInputData : BaseFragment() {

    private lateinit var viewLayout: View

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
    private var callback_url: String? = null
    private var is_input_amount: Boolean? = null
    private var is_display_amount: Boolean = false
    private var isAgent: Boolean? = null
    private var isShowDescription: Boolean? = false
    private var fee = 0.0
    private var item_price = 0.0
    private var additional_fee = 0.0
    private var buy_type = 0
    private var total = 0.0

    private var realm: Realm? = null
    private var mBillerData: Biller_Data_Model? = null
    private var mDenomData: Biller_Data_Model? = null
    private var mListBillerData: List<Biller_Data_Model>? = null
    private var mListDenomData: List<Denom_Data_Model>? = null
    private val _data = ArrayList<String>()
    private var denomData: ArrayList<String>? = null
    private var adapterDenom: ArrayAdapter<String>? = null
    private var mBillerType: Biller_Type_Data_Model? = null
    private var paymentData: MutableList<String>? = null
    private var adapterPaymentOptions: ArrayAdapter<String>? = null
    private var mListBankBiller: List<bank_biller_model>? = null
    private var mTempBank: listBankModel? = null
    private lateinit var levelClass: LevelClass
    private lateinit var sentPaymentBillerModel: SentPaymentBillerModel


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

        btn_submit_billerinput.setOnClickListener { submitInputListener() }

        initLayout()
        initEditTextListener()
        initRealm()
    }

    private val spinnerDenomListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {
            if (position != 0) {
                item_id = mListDenomData?.get(position - 1)?.item_id
                item_name = mListDenomData?.get(position - 1)?.item_name
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
                if (payment_name == mListBankBiller?.get(i)?.product_name) {
                    mTempBank = listBankModel(mListBankBiller?.get(i)?.bank_code,
                            mListBankBiller?.get(i)?.bank_name,
                            mListBankBiller?.get(i)?.product_code,
                            mListBankBiller?.get(i)?.product_name,
                            mListBankBiller?.get(i)?.product_type,
                            mListBankBiller?.get(i)?.product_h2h)
                }
                i++
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }

    private fun checkOperator(string: String?) {
        val billerIdNumber = PrefixOperatorValidator.validation(activity, string)

        if (billerIdNumber != null) {

            if (billerIdNumber.prefix_name.toLowerCase().equals("telkomsel", ignoreCase = true)) {
                img_operator.background = resources.getDrawable(R.drawable.telkomsel)
            } else if (billerIdNumber.prefix_name.toLowerCase().equals("xl", ignoreCase = true)) {
                img_operator.background = resources.getDrawable(R.drawable.xl)
            } else if (billerIdNumber.prefix_name.toLowerCase().equals("indosat", ignoreCase = true)) {
                img_operator.background = resources.getDrawable(R.drawable.indosat)
            } else if (billerIdNumber.prefix_name.toLowerCase().equals("three", ignoreCase = true)) {
                img_operator.background = resources.getDrawable(R.drawable.three)
            } else if (billerIdNumber.prefix_name.toLowerCase().equals("smart", ignoreCase = true)) {
                img_operator.background = resources.getDrawable(R.drawable.smartfren)
            } else
                img_operator.visibility = View.GONE

            for (i in _data.indices) {
                Timber.d("_data" + _data[i])
                if (_data != null) {
                    if (_data.get(i).toLowerCase().contains(billerIdNumber.prefix_name.toLowerCase())) {
                        biller_comm_id = mListBillerData?.get(i)?.comm_id
                        biller_comm_name = mListBillerData?.get(i)?.comm_name
                        biller_item_id = mListBillerData?.get(i)?.item_id

                        initializeSpinnerDenom()
                    }
                }

            }
        }
    }

    private fun initLayout() {
        buy_type = BillerActivity.PURCHASE_TYPE
        billerinput_text_denom.text = "Jenis Paket Data"
        billerinput_et_nomor_hp.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(13))
        billerinput_et_nomor_hp.inputType = InputType.TYPE_CLASS_NUMBER
        billerinput_et_add_fee.inputType = InputType.TYPE_CLASS_NUMBER
        billerinput_detail_layout_add_fee.visibility = View.GONE
        billerinput_layout_denom.visibility = View.VISIBLE
        billerinput_layout_add_fee.visibility = View.GONE
        billerinput_layout_detail.visibility = View.GONE
    }

    private fun initEditTextListener() {
        billerinput_et_nomor_hp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val string = editable.toString()
                if (string.length > 3) {
                    cust_id = string
                    checkOperator(string)
                } else
                    cust_id = NoHPFormat.formatTo62(billerinput_et_nomor_hp.text.toString())
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
                } else {
                    billerinput_detail_admin_add_fee.text = getString(R.string.rp_) + " " + CurrencyFormat.format(string)
                    additional_fee = string.toDouble()
                    countTotal()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    private fun initializeSpinnerDenom() {
        mDenomData = Biller_Data_Model()
        mDenomData = realm?.where(Biller_Data_Model::class.java)?.equalTo(WebParams.COMM_ID, biller_comm_id)?.equalTo(WebParams.COMM_NAME, biller_comm_name)?.equalTo(WebParams.DENOM_ITEM_ID, biller_item_id)?.findFirst()
        mListDenomData = realm?.copyFromRealm(mDenomData?.denom_data_models)
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
                        denomData?.add(mListDenomData?.get(i)?.item_name!!)
                    }

                    activity!!.runOnUiThread {
                        billerinput_spinner_denom.visibility = View.VISIBLE
                        adapterDenom!!.notifyDataSetChanged()
                    }
                }
            }
            deproses.run()
        } else {
            item_id = mDenomData?.item_id
        }
        val spinAdapter = ArrayAdapter.createFromResource(activity!!, R.array.privacy_list, android.R.layout.simple_spinner_item)
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        mBillerData = Biller_Data_Model()
        mBillerData = realm?.where(Biller_Data_Model::class.java)?.equalTo(WebParams.COMM_ID, biller_comm_id)?.equalTo(WebParams.COMM_NAME, biller_comm_name)?.findFirst()
        mListBankBiller = realm?.copyFromRealm(mBillerData?.bank_biller_models)
        biller_comm_code = mBillerData?.comm_code
        biller_api_key = mBillerData?.api_key
        callback_url = mBillerData?.callback_url
        if (mListBillerData!!.isNotEmpty()) {
            paymentData = ArrayList()
            adapterPaymentOptions = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, paymentData)
            adapterPaymentOptions?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            billerinput_spinner_payment_options.adapter = adapterPaymentOptions
            billerinput_spinner_payment_options.onItemSelectedListener = spinnerPaymentListener
            if (isVisible) {
                val tempDataPaymentName = ArrayList<String>()
                paymentData?.add(getString(R.string.billerinput_text_spinner_default_payment))

                for (i in mListBankBiller!!.indices) {
                    if (mListBankBiller?.get(i)?.product_code == DefineValue.SCASH) {
                        paymentData?.add(getString(R.string.appname))
                        mListBankBiller?.get(i)?.product_name = getString(R.string.appname)
                    } else {
                        tempDataPaymentName.add(mListBankBiller?.get(i)?.product_name!!)
                    }
                }
                if (tempDataPaymentName.isNotEmpty())
                    tempDataPaymentName.sort()

                paymentData?.addAll(tempDataPaymentName)
                adapterPaymentOptions?.notifyDataSetChanged()

                billerinput_spinner_payment_options.setSelection(1) //set metode pembayaran jadi saldomu
            }
        } else {
            biller_item_id = mBillerData?.item_id
        }
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
        if (billerinput_et_nomor_hp.text.length < 10 ||
                billerinput_et_nomor_hp.text.length > 15) {
            billerinput_et_nomor_hp.requestFocus()
            billerinput_et_nomor_hp.error = getString(R.string.regist1_validation_nohp)
            return false
        }
        if (item_name == null) {
            billerinput_spinner_denom.requestFocus()
            Toast.makeText(activity, getString(R.string.billerinput_validation_spinner_default_data), Toast.LENGTH_LONG).show()
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

    private fun initRealm() {
        realm = Realm.getInstance(RealmManager.BillerConfiguration)
        mBillerType = realm?.where(Biller_Type_Data_Model::class.java)?.equalTo(WebParams.BILLER_TYPE_CODE, "DATA")?.findFirst()
        if (mBillerType != null) {
            mListBillerData = mBillerType!!.biller_data_models
            _data.clear()
            for (i in mListBillerData!!.indices) {
                _data.add(mListBillerData?.get(i)?.comm_name!!)
            }
        } else {
            mListBillerData = ArrayList()
        }
    }

    private fun switchFragment(mFrag: BillerConfirm, name: String, next_name: String?, isBackstack: Boolean, tag: String) {
        if (activity == null)
            return

        val fca = activity as BillerActivity?
        fca?.switchContent(mFrag, name, next_name, isBackstack, tag)
        billerinput_et_nomor_hp.text.clear()
        billerinput_spinner_denom.setSelection(0)
    }

    private fun countTotal() {
        total = item_price + additional_fee + fee
        billerinput_detail_total.text = getString(R.string.rp_) + " " + CurrencyFormat.format(total)
    }

    private fun sentInquryBiller() {
        try {
            showProgressDialog()
            ToggleKeyboard.hide_keyboard(activity!!)

            cust_id = NoHPFormat.formatTo62(billerinput_et_nomor_hp.text.toString())

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
                                item_price = model.amount.toDouble()
                                item_name = model.item_name
                                description = getGson().toJson(model.description)
                                fee = model.admin_fee.toDouble()
                                enabledAdditionalFee = model.enabled_additional_fee

                                if (isAgent!! && enabledAdditionalFee.equals(DefineValue.Y)) {
                                    billerinput_layout_add_fee.visibility = View.VISIBLE
                                    billerinput_detail_layout_add_fee.visibility = View.VISIBLE
                                }

                                billerinput_layout_detail.visibility = View.VISIBLE
                                isShowDescription = false
                                billerinput_detail_text_name.text = item_name
                                billerinput_detail_price.text = getString(R.string.rp_) + " " + CurrencyFormat.format(item_price)
                                billerinput_detail_admin_fee.text = getString(R.string.rp_) + " " + CurrencyFormat.format(fee)
                            } else if (code == WebParams.LOGOUT_CODE) {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(activity, message)
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

            val bank_code = mTempBank?.bank_code
            val product_code = mTempBank?.product_code

            extraSignature = tx_id + item_id + biller_comm_id + product_code

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_PAYMENT_BILLER, extraSignature)
            params[WebParams.DENOM_ITEM_ID] = item_id
            params[WebParams.DENOM_ITEM_REMARK] = cust_id
            params[WebParams.TX_ID] = tx_id
            params[WebParams.AMOUNT] = item_price
            params[WebParams.BANK_CODE] = bank_code
            params[WebParams.PRODUCT_CODE] = product_code
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
                                    changeToConfirmBiller(sentPaymentBillerModel.fee, sentPaymentBillerModel.merchant_type, bank_code, product_code, -1)
                                } else {
                                    var attempt = sentPaymentBillerModel.failed_attempt
                                    if (attempt != -1)
                                        attempt = sentPaymentBillerModel.max_failed - attempt
                                    sentDataReqToken(tx_id, product_code, biller_comm_code, sentPaymentBillerModel.fee, sentPaymentBillerModel.merchant_type, bank_code, attempt)
                                }
                            } else if (code == WebParams.LOGOUT_CODE) {
                                var message = sentPaymentBillerModel.error_message
                                var alertDialog = AlertDialogLogout.getInstance()
                                alertDialog.showDialoginActivity(activity, message)
                            } else {
                                code = sentPaymentBillerModel.error_code + " : " + sentPaymentBillerModel.fee
                                Toast.makeText(activity, code, Toast.LENGTH_LONG).show()
                                fragmentManager?.popBackStack()
                                dismissProgressDialog()
                            }
                        }

                        override fun onError(throwable: Throwable?) {

                        }

                        override fun onComplete() {
                            btn_submit_billerinput.isEnabled = true
                        }

                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun changeToConfirmBiller(fee: String, merchant_type: String, bank_code: String?, product_code: String?, attempt: Int) {
        val mArgs = Bundle()
        mArgs.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription!!)
        mArgs.putString(DefineValue.TX_ID, tx_id)
        mArgs.putString(DefineValue.CCY_ID, ccy_id)
        mArgs.putString(DefineValue.AMOUNT, sentPaymentBillerModel.amount)
        mArgs.putString(DefineValue.ITEM_ID, item_id)
        mArgs.putString(DefineValue.ITEM_NAME, item_name)
        mArgs.putString(DefineValue.BILLER_COMM_ID, biller_comm_id)
        mArgs.putString(DefineValue.BILLER_NAME, biller_comm_name)
        mArgs.putString(DefineValue.BILLER_ITEM_ID, item_id)
        mArgs.putString(DefineValue.BILLER_COMM_CODE, biller_comm_code)
        mArgs.putString(DefineValue.BILLER_API_KEY, biller_api_key)
        mArgs.putString(DefineValue.PAYMENT_NAME, payment_name)
        mArgs.putString(DefineValue.CUST_ID, cust_id)
        mArgs.putInt(DefineValue.BUY_TYPE, buy_type)
        mArgs.putString(DefineValue.CALLBACK_URL, callback_url)
        mArgs.putString(DefineValue.FEE, sentPaymentBillerModel.fee)
        mArgs.putString(DefineValue.TOTAL_AMOUNT, sentPaymentBillerModel.total_amount)
        mArgs.putString(DefineValue.PRODUCT_PAYMENT_TYPE, mTempBank?.product_type)
        mArgs.putString(DefineValue.BILLER_TYPE, billerTypeCode)
        mArgs.putString(DefineValue.BANK_CODE, bank_code)
        mArgs.putString(DefineValue.PRODUCT_CODE, product_code)
        mArgs.putBoolean(DefineValue.IS_DISPLAY, is_display_amount!!)
        mArgs.putBoolean(DefineValue.IS_INPUT, getIs_input_amount()!!)
        mArgs.putString(DefineValue.SHARE_TYPE, "")
        mArgs.putBoolean(DefineValue.IS_SGO_PLUS, mTempBank?.product_type == DefineValue.BANKLIST_TYPE_IB)
        mArgs.putString(DefineValue.AUTHENTICATION_TYPE, merchant_type)
        mArgs.putInt(DefineValue.ATTEMPT, attempt)
        mArgs.putString(DefineValue.ADDITIONAL_FEE, sentPaymentBillerModel.additional_fee)

        if (is_display_amount)
            mArgs.putString(DefineValue.DESCRIPTION, description)

        if (getIs_input_amount()!!)
            mArgs.putString(DefineValue.TOTAL_AMOUNT, total.toString())


        val newFrag = BillerConfirm()
        newFrag.arguments = mArgs
        switchFragment(newFrag, BillerActivity.FRAG_BIL_INPUT, null, true, BillerConfirm.TAG)

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
                                    changeToConfirmBiller(fee, merchant_type, bank_code, product_code, attempt)
                            } else if (code == WebParams.LOGOUT_CODE) {
                                var message = model.error_message
                                var alertDialog = AlertDialogLogout.getInstance()
                                alertDialog.showDialoginActivity(activity, message)
                            } else if (code == ErrorDefinition.WRONG_PIN_BILLER) {
                                code = model.error_message
                                showDialogError(code)
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
                        }

                        override fun onError(throwable: Throwable?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onComplete() {
                            btn_submit_billerinput.isEnabled = true
                            dismissProgressDialog()
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

        title_dialog.text = getString(R.string.smsBanking_dialog_validation_title)
        title_dialog.text = resources.getString(R.string.regist1_notif_title_verification)
        message_dialog.visibility = View.VISIBLE
        message_dialog.text = getString(R.string.appname) + " " + getString(R.string.dialog_token_message_sms)

        btn_dialog_notification_ok.setOnClickListener(View.OnClickListener {
            changeToConfirmBiller(fee, merchantType, bankCode, productCode, -1)
            dialog.dismiss()
        })
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BillerInqReq)
            sentInquryBiller()
    }

}

