package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import com.google.gson.JsonObject
import com.sgo.saldomu.Beans.Biller_Data_Model
import com.sgo.saldomu.Beans.Denom_Data_Model
import com.sgo.saldomu.Beans.bank_biller_model
import com.sgo.saldomu.Beans.listBankModel
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.*
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.dialogs.*
import com.sgo.saldomu.fragments.BillerDesciption2.REQUEST_BillerInqReq
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.interfaces.OnLoadDataListener
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.loader.UtilsLoader
import com.sgo.saldomu.models.retrofit.*
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.utils.Converter.Companion.hexStringToByteArray
import com.sgo.saldomu.utils.Converter.Companion.toHex
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_biller_input_new.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.*


@RequiresApi(Build.VERSION_CODES.KITKAT)
class BillerInputEmoney : BaseFragment(), ReportBillerDialog.OnDialogOkCallback, NfcAdapter.ReaderCallback {
    private val EMONEYSALDOMU: String = "EMONEYSALDOMU"

    private var nfcAdapter: NfcAdapter? = null
    private var realm: Realm? = null

    private lateinit var billerData: Biller_Data_Model
    private var paymentMethod: listBankModel? = null

    private var denomAdapter: ArrayAdapter<String>? = null
    private var paymentAdapter: ArrayAdapter<String>? = null

    private var denomData: ArrayList<String>? = null
    private var paymentData: ArrayList<String>? = null

    private var listDenomData: List<Denom_Data_Model>? = null
    private var listBankBiller: List<bank_biller_model>? = null

    private var enabledAdditionalFee: Boolean? = false
    private var isAgent: Boolean? = false

    private var additionalFee: Double = 0.0
    private var amount: Double = 0.0
    private var fee: Double = 0.0
    private var totalAmount: Double = 0.0

    private var buyType: Int? = null

    private var bankCode: String? = null
    private var bankName: String? = null
    private var billerApiKey: String? = null
    private var billerCommId: String? = null
    private var billerCommCode: String? = null
    private var billerCommName: String? = null
    private var billerItemId: String? = null
    private var billerIdNumber: String? = null
    private var billerTypeCode: String? = null
    private var cardInfo: String? = null
    private var cardSelect: String? = null
    private var cardNumber: String? = null
    private var ccyId: String? = null
    private var custId: String? = null
    private var description: String? = null
    private var itemId: String? = null
    private var itemName: String? = null
    private var merchantType: String? = null
    private var productCode: String? = null
    private var productH2h: String? = null
    private var productName: String? = null
    private var productType: String? = null
    private var txId: String? = null
    private var valuePin: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_biller_input_new, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        realm = Realm.getInstance(RealmManager.BillerConfiguration)

        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false)

        billerTypeCode = arguments!!.getString(DefineValue.BILLER_TYPE, "")
        billerCommId = arguments!!.getString(DefineValue.COMMUNITY_ID, "")
        billerCommCode = arguments!!.getString(DefineValue.BILLER_COMM_CODE, "")
        billerCommName = arguments!!.getString(DefineValue.COMMUNITY_NAME, "")
        billerItemId = arguments!!.getString(DefineValue.BILLER_ITEM_ID, "")
        billerIdNumber = arguments!!.getString(DefineValue.BILLER_ID_NUMBER, "")
        billerApiKey = arguments!!.getString(DefineValue.BILLER_API_KEY, "")

        initLayout()
        initRealm()

        if (arguments!!.containsKey(DefineValue.CUST_ID)) {
            billerinput_et_id_remark.setText(arguments!!.getString(DefineValue.CUST_ID, ""))
        }

        if (billerItemId != null) {
            for (i in listDenomData!!.indices) {
                if (listDenomData!![i].item_id == billerItemId) {
                    billerinput_spinner_denom.setSelection(i + 1)
                }
            }
        }
    }

    private fun initLayout() {
        buyType = BillerActivity.PURCHASE_TYPE
        billerinput_et_id_remark.text.clear()
        billerinput_et_id_remark.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(13))
        if (billerCommCode.equals(EMONEYSALDOMU)) {
            billerinput_text_id_remark.text = getString(R.string.billerinput_text_payment_remark_Emoney)
            billerinput_et_id_remark.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(16))
            if (BuildConfig.FLAVOR == "development")
                billerinput_et_id_remark.setText("6032984008386579")
            if (nfcAdapter != null) {
                layout_cek_balance.visibility = View.VISIBLE
                btn_cek_balance.setOnClickListener { checkBalance() }
            }
        }
        billerinput_et_id_remark.inputType = InputType.TYPE_CLASS_NUMBER
        if (billerIdNumber != null)
            billerinput_et_id_remark.setText(billerIdNumber)

        billerinput_detail_layout_add_fee.visibility = View.GONE
        billerinput_layout_denom.visibility = View.VISIBLE
        ib_contact_list.visibility = View.GONE
        billerinput_layout_add_fee.visibility = View.GONE
        billerinput_et_add_fee.text.clear()
        billerinput_et_add_fee.inputType = InputType.TYPE_CLASS_NUMBER
        billerinput_et_add_fee.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                val string = s.toString()
                additionalFee = if (string == "0" || string == "") {
                    billerinput_et_add_fee.text.clear()
                    0.0
                } else {
                    string.toDouble()
                }
                billerinput_detail_admin_add_fee.text = getString(R.string.rp_) + " " + CurrencyFormat.format(additionalFee)
                countTotal()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        billerinput_layout_payment_method.visibility = View.GONE
        billerinput_layout_detail.visibility = View.GONE

        favorite_switch.setOnCheckedChangeListener { _, isChecked ->
            notes_edit_text.visibility = if (isChecked) View.VISIBLE else View.GONE
            notes_edit_text.isEnabled = isChecked
        }
        btn_submit_billerinput.setOnClickListener {
            if (inputValidation())
                sentPaymentBiller()
        }
        buttonSubmit(false)

        btn_cek_balance.setOnClickListener {
            val intent = Intent(activity, NFCActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkBalance() {
        val intent = Intent(activity, NFCActivity::class.java)
        startActivity(intent)
    }

    private fun buttonSubmit(enable: Boolean) {
        if (enable) {
            btn_submit_billerinput.isEnabled = true
            btn_submit_billerinput.setBackgroundResource(R.drawable.rounded_background_blue)
        } else {
            btn_submit_billerinput.isEnabled = false
            btn_submit_billerinput.setBackgroundResource(R.drawable.rounded_background_button_disabled)
        }
    }

    private fun initRealm() {
        billerData = Biller_Data_Model()
        billerData = realm!!.where(Biller_Data_Model::class.java).equalTo(WebParams.COMM_ID, billerCommId).findFirst()!!
        listDenomData = realm!!.copyFromRealm(billerData.denom_data_models)
        listBankBiller = realm!!.copyFromRealm(billerData.bank_biller_models)
        initSpinner()
    }

    private fun initSpinner() {
        if (listDenomData!!.isNotEmpty()) {
            denomData = ArrayList<String>()
            denomData?.clear()
            denomData?.add(getString(R.string.billerinput_text_spinner_default_emoney))
            for (i in listDenomData!!.indices) {
                denomData?.add(listDenomData!![i].item_name)
            }
            denomAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, denomData!!)
            denomAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            billerinput_spinner_denom.adapter = denomAdapter
            billerinput_spinner_denom.onItemSelectedListener = spinnerDenomListener
        }

        if (listBankBiller!!.isNotEmpty()) {
            paymentData = ArrayList<String>()
            paymentData?.clear()
            paymentData?.add(getString(R.string.billerinput_text_spinner_default_payment))
            for (i in listBankBiller!!.indices) {
                if (listBankBiller!![i].product_code == DefineValue.SCASH) {
                    paymentData?.add(getString(R.string.appname))
                    listBankBiller!![i].product_name = getString(R.string.appname)
                } else
                    paymentData?.add(listBankBiller!![i].product_name)
            }
            paymentAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, paymentData!!)
            paymentAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            billerinput_spinner_payment_options.adapter = paymentAdapter
            billerinput_spinner_payment_options.onItemSelectedListener = spinnerPaymentListener
            billerinput_spinner_payment_options.setSelection(1)
        }
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

    private val spinnerDenomListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position != 0) {
                itemId = listDenomData!![position - 1].item_id
                itemName = listDenomData!![position - 1].item_name
                if (inputValidation()) {
                    sentInquiryBiller()
                }
            } else {
                itemId = null
                itemName = null
                initLayout()
            }

        }

    }

    private val spinnerPaymentListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
            paymentMethod =
                    if (position != 0) {
                        listBankModel(listBankBiller!![position - 1].bank_code,
                                listBankBiller!![position - 1].bank_name,
                                listBankBiller!![position - 1].product_code,
                                listBankBiller!![position - 1].product_name,
                                listBankBiller!![position - 1].product_type,
                                listBankBiller!![position - 1].product_h2h)
                    } else {
                        null
                    }
        }

    }

    private fun sentInquiryBiller() {
        showProgressDialog()
        ToggleKeyboard.hide_keyboard(activity!!)

        custId = NoHPFormat.formatTo62(billerinput_et_id_remark.text.toString())
        extraSignature = billerCommId + itemId + custId

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_BILLER, extraSignature)
        params[WebParams.DENOM_ITEM_ID] = itemId
        params[WebParams.DENOM_ITEM_REMARK] = custId
        params[WebParams.COMM_ID] = billerCommId
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.COMM_ID_REMARK] = MyApiClient.COMM_ID

        Timber.d("isi params sent inquiry biller:$params")

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INQUIRY_BILLER, params,
                object : ResponseListener {
                    @SuppressLint("SetTextI18n")
                    override fun onResponses(response: JsonObject?) {
                        val model = getGson().fromJson(response, InqBillerModel::class.java)
                        val code = model.error_code
                        val message = model.error_message
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                txId = model.tx_id
                                ccyId = model.ccy_id
                                fee = model.admin_fee.toDouble()
                                amount = model.amount.toDouble() - fee
                                description = getGson().toJson(model.description)
                                enabledAdditionalFee = model.enabled_additional_fee == DefineValue.STRING_YES

                                if (isAgent!! && enabledAdditionalFee!!) {
                                    billerinput_layout_add_fee.visibility = View.VISIBLE
                                    billerinput_detail_layout_add_fee.visibility = View.VISIBLE
                                }

                                billerinput_layout_payment_method.visibility = View.VISIBLE
                                billerinput_layout_detail.visibility = View.VISIBLE
                                billerinput_detail_text_name.text = itemName
                                billerinput_detail_price.text = getString(R.string.rp_) + " " + CurrencyFormat.format(amount)
                                billerinput_detail_admin_fee.text = getString(R.string.rp_) + " " + CurrencyFormat.format(fee)
                                buttonSubmit(true)
                            }
                            WebParams.LOGOUT_CODE -> {
                                AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                            }
                            DefineValue.ERROR_9333 -> {
                                val appModel = model.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            }
                            DefineValue.ERROR_0066 -> {
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
                            }
                            else -> {
                                if (isVisible) {
                                    Toast.makeText(activity, "$code : $message", Toast.LENGTH_LONG).show()
                                    fragManager.popBackStack()
                                }
                                billerinput_layout_detail.visibility = View.GONE
                            }
                        }
                    }

                    override fun onError(throwable: Throwable?) {

                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                        countTotal()
                    }
                })
    }

    private fun sentPaymentBiller() {
        showProgressDialog()
        bankCode = paymentMethod?.bank_code
        bankName = paymentMethod?.bank_name
        productCode = paymentMethod?.product_code
        productName = paymentMethod?.product_name
        productH2h = paymentMethod?.product_h2h
        productType = paymentMethod?.product_type

        extraSignature = txId + itemId + billerCommId + productCode

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_PAYMENT_BILLER, extraSignature)
        params[WebParams.DENOM_ITEM_ID] = itemId
        params[WebParams.DENOM_ITEM_REMARK] = custId
        params[WebParams.TX_ID] = txId
        params[WebParams.AMOUNT] = amount + fee
        params[WebParams.BANK_CODE] = bankCode
        params[WebParams.PRODUCT_CODE] = productCode
        params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
        params[WebParams.COMM_ID] = billerCommId
        params[WebParams.MEMBER_CUST] = sp.getString(DefineValue.CUST_ID, "")
        params[WebParams.DATETIME] = DateTimeFormat.getCurrentDateTime()
        params[WebParams.COMM_CODE] = billerCommCode
        params[WebParams.USER_COMM_CODE] = sp.getString(DefineValue.COMMUNITY_CODE, "")
        params[WebParams.PRODUCT_H2H] = productH2h
        params[WebParams.PRODUCT_TYPE] = productType
        params[WebParams.USER_ID] = userPhoneID
        if (isAgent!!)
            if (additionalFee != 0.0)
                params[WebParams.ADDITIONAL_FEE] = additionalFee.toString()
            else
                params[WebParams.ADDITIONAL_FEE] = "0"

        Timber.d("isi params sent payment biller:$params")

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_PAYMENT_BILLER, params,
                object : ResponseListener {
                    override fun onResponses(response: JsonObject?) {
                        val sentPaymentBillerModel = getGson().fromJson(response, SentPaymentBillerModel::class.java)
                        val code = sentPaymentBillerModel.error_code
                        val message = sentPaymentBillerModel.error_message
                        merchantType = sentPaymentBillerModel.merchant_type
                        when (code) {
                            WebParams.SUCCESS_CODE -> {

                                if (productType == DefineValue.BANKLIST_TYPE_IB)
                                    inputPIN(-1)
                                else {
                                    var attempt = sentPaymentBillerModel.failed_attempt
                                    if (attempt != -1)
                                        attempt = sentPaymentBillerModel.max_failed - attempt
                                    sentDataReqToken(attempt)
                                }
                            }
                            WebParams.LOGOUT_CODE -> {
                                AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                            }
                            DefineValue.ERROR_9333 -> {
                                val appModel = sentPaymentBillerModel.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            }
                            DefineValue.ERROR_0066 -> {
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
                            }
                            else -> {
                                Toast.makeText(activity, "$code : $message", Toast.LENGTH_LONG).show()
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
    }

    private fun inputPIN(attempt: Int) {
        UtilsLoader(activity, sp).getFailedPIN(userPhoneID, object : OnLoadDataListener {
            override fun onSuccess(deData: Any) {

            }

            override fun onFail(message: Bundle) {

            }

            override fun onFailure(message: String) {

            }
        })
        callPINinput(attempt)
        btn_submit_billerinput.isEnabled = true
    }

    private fun callPINinput(attempt: Int) {
        val i = Intent(activity, InsertPIN::class.java)
        if (attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, attempt)
        startActivityForResult(i, MainPage.REQUEST_FINISH)
    }

    private fun sentDataReqToken(attempt: Int) {

        showProgressDialog()
        extraSignature = txId + billerCommCode + productCode

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature)
        params[WebParams.COMM_CODE] = billerCommCode
        params[WebParams.TX_ID] = txId
        params[WebParams.PRODUCT_CODE] = productCode
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.COMM_ID] = MyApiClient.COMM_ID

        Timber.d("isi params regtoken Sgol:$params")

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                object : ResponseListener {
                    override fun onResponses(response: JsonObject?) {
                        val model = getGson().fromJson(response, jsonModel::class.java)
                        val code = model.error_code
                        val message = model.error_message
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                when {
                                    productType == DefineValue.BANKLIST_TYPE_SMS -> showDialog()
                                    merchantType == DefineValue.AUTH_TYPE_OTP -> showDialog()
                                    else -> inputPIN(attempt)
                                }
                            }
                            WebParams.LOGOUT_CODE -> {
                                AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                            }
                            DefineValue.ERROR_9333 -> {
                                val appModel = model.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            }
                            DefineValue.ERROR_0066 -> {
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
                            }
                            ErrorDefinition.WRONG_PIN_BILLER -> {
                                showDialogError(message)
                            }
                            ErrorDefinition.ERROR_CODE_LESS_BALANCE -> {
                                val messageDialog = "\"" + message + "\"\n" + getString(R.string.dialog_message_less_balance, getString(R.string.appname))
                                val dialogFrag = AlertDialogFrag.newInstance(getString(R.string.dialog_title_less_balance), messageDialog, getString(R.string.ok), getString(R.string.cancel), false)
                                dialogFrag.okListener = DialogInterface.OnClickListener { dialog, which ->
                                    val mI = Intent(activity, TopUpActivity::class.java)
                                    mI.putExtra(DefineValue.IS_ACTIVITY_FULL, true)
                                    startActivityForResult(mI, REQUEST_BillerInqReq)
                                }
                                dialogFrag.cancelListener = DialogInterface.OnClickListener { dialog, which ->
                                    sentInquiryBiller()
                                }
                                dialogFrag.setTargetFragment(this@BillerInputEmoney, 0)
                                dialogFrag.show(activity!!.supportFragmentManager, AlertDialogFrag.TAG)
                            }
                            "0059" -> showDialogSMS(bankName)
                            else -> {
                                Toast.makeText(activity, "$code : $message", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onError(throwable: Throwable?) {

                    }

                    override fun onComplete() {
                        btn_submit_billerinput.isEnabled = true
                        dismissProgressDialog()
                    }

                })
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
        val dialog = DefinedDialog.MessageDialog(activity, getString(R.string.error), code) { fragmentManager?.popBackStack() }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showDialog() {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = getString(R.string.smsBanking_dialog_validation_title)
        dialog.title_dialog.text = resources.getString(R.string.regist1_notif_title_verification)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = getString(R.string.appname) + " " + getString(R.string.dialog_token_message_sms)

        dialog.btn_dialog_notification_ok.setOnClickListener {
            inputPIN(-1)
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BillerInqReq)
            resetDenom()
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                valuePin = data?.getStringExtra(DefineValue.PIN_VALUE)

                if (favorite_switch.isChecked) {
                    onSaveToFavorite()
                } else {
                    sentInsertTransTopup()
                }
            }
        }
    }

    private fun resetDenom() {
        billerinput_spinner_denom.setSelection(0)
    }

    @SuppressLint("SetTextI18n")
    private fun countTotal() {
        totalAmount = amount + additionalFee + fee
        billerinput_detail_total.text = getString(R.string.rp_) + " " + CurrencyFormat.format(totalAmount)
    }

    private fun inputValidation(): Boolean {
        if (billerinput_et_id_remark.text.isEmpty()) {
            if (billerCommCode.equals(EMONEYSALDOMU) && billerinput_et_id_remark.text.length != 16)
                billerinput_et_id_remark.error = getString(R.string.insert_16_card_number)
            else
                billerinput_et_id_remark.error = getString(R.string.regist1_validation_nohp)
            billerinput_et_id_remark.requestFocus()
            resetDenom()
            return false
        }
        if (itemName == null) {
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

    private fun onSaveToFavorite() {
        showProgressDialog()
        extraSignature = custId + billerTypeCode + "BIL"
        Timber.tag("extraSignature params ").e(extraSignature)
        val url = MyApiClient.LINK_TRX_FAVORITE_SAVE
        val params = RetrofitService.getInstance().getSignature(url, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.PRODUCT_TYPE] = billerTypeCode
        params[WebParams.CUSTOMER_ID] = custId
        params[WebParams.TX_FAVORITE_TYPE] = "BIL"
        params[WebParams.COMM_ID] = billerCommId
        params[WebParams.NOTES] = notes_edit_text.text.toString()
        params[WebParams.DENOM_ITEM_ID] = itemId

        Timber.tag("params ").e(params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(url, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        try {
                            val model = RetrofitService.getInstance().gson.fromJson(response.toString(), jsonModel::class.java)
                            Timber.tag("onResponses ").e(response.toString())
                            val code = response.getString(WebParams.ERROR_CODE)
                            val message = response.getString(WebParams.ERROR_MESSAGE)
                            when (code) {
                                WebParams.SUCCESS_CODE -> {
                                    sentInsertTransTopup()
                                }
                                DefineValue.ERROR_9333 -> {
                                    val appModel = model.app_data
                                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                }
                                DefineValue.ERROR_0066 -> {
                                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                    alertDialogMaintenance.showDialogMaintenance(activity)
                                }
                                else -> {
                                    Toast.makeText(activity, "$code : $message", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(throwable: Throwable) {
                        Timber.tag("onResponses ").e(throwable.localizedMessage!!)
                        throwable.printStackTrace()
                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                    }
                })
    }

    private fun sentInsertTransTopup() {
        try {
            showProgressDialog()

            val link = MyApiClient.LINK_INSERT_TRANS_TOPUP
            val subStringLink = link.substring(link.indexOf("saldomu/"))
            extraSignature = txId + billerCommCode + productCode + valuePin
            val params = RetrofitService.getInstance().getSignature(link, extraSignature)
            val uuid: String = params[WebParams.RC_UUID].toString()
            val dateTime: String = params[WebParams.RC_DTIME].toString()
            params[WebParams.TX_ID] = txId
            params[WebParams.PRODUCT_CODE] = productCode
            params[WebParams.COMM_CODE] = billerCommCode
            params[WebParams.COMM_ID] = billerCommId
            params[WebParams.MEMBER_ID] = sp.getString(DefineValue.MEMBER_ID, "")
            params[WebParams.PRODUCT_VALUE] = RSA.opensslEncryptCommID(billerCommId, uuid, dateTime, userPhoneID, valuePin, subStringLink)
            params[WebParams.USER_ID] = userPhoneID

            Timber.d("isi params insertTrxTOpupSGOL:$params")

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    object : ResponseListener {
                        override fun onResponses(response: JsonObject) {
                            val model = getGson().fromJson(response, SentPaymentBillerModel::class.java)
                            val code = model.error_code
                            val message = model.error_message
                            when (code) {
                                WebParams.SUCCESS_CODE -> {
                                    getTrxStatus()
                                    setResultActivity()
                                }
                                WebParams.LOGOUT_CODE -> {
                                    AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                                }
                                else -> {
                                    Toast.makeText(activity, "$code : $message", Toast.LENGTH_LONG).show()
                                    if (message == "PIN tidak sesuai") {
                                        val i = Intent(activity, InsertPIN::class.java)
                                        val attempt = model.failed_attempt
                                        val failed = model.max_failed
                                        if (attempt != -1)
                                            i.putExtra(DefineValue.ATTEMPT, failed - attempt)
                                        startActivityForResult(i, MainPage.REQUEST_FINISH)
                                    } else {
                                        onOkButton()
                                    }
                                }
                            }
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
//                            dismissProgressDialog()
                            btn_submit_billerinput.isEnabled = true
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

    private fun getTrxStatus() {
        showProgressDialog()
        extraSignature = txId + billerCommId
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature)

        params[WebParams.TX_ID] = txId
        params[WebParams.COMM_ID] = billerCommId
        if (buyType == BillerActivity.PURCHASE_TYPE)
            params[WebParams.TYPE] = DefineValue.BIL_PURCHASE_TYPE
        else
            params[WebParams.TYPE] = DefineValue.BIL_PAYMENT_TYPE
        params[WebParams.PRIVACY] = ""
        params[WebParams.TX_TYPE] = DefineValue.ESPAY
        params[WebParams.USER_ID] = userPhoneID

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                object : ResponseListener {
                    override fun onResponses(response: JsonObject) {
                        val model = getGson().fromJson(response, GetTrxStatusReportModel::class.java)

                        val code = model.error_code
                        val message = model.error_message

                        if (!model.on_error) {
                            if (code == WebParams.SUCCESS_CODE || code == "0003") {
                                showReportBillerDialog(model)
                            } else if (code == WebParams.LOGOUT_CODE) {
                                AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                            } else if (code == DefineValue.ERROR_9333) {
                                val appModel = model.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) {
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
                            } else {
                                showDialog(message)
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
    }

    private fun showReportBillerDialog(model: GetTrxStatusReportModel) {
        val args = Bundle()
        val txStatus = model.tx_status
        val dialog = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.USER_NAME, sp.getString(DefineValue.USER_NAME, ""))
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(model.created!!))
        args.putString(DefineValue.TX_ID, txId)
        args.putString(DefineValue.USERID_PHONE, sp.getString(DefineValue.USERID_PHONE, ""))
        args.putString(DefineValue.DENOM_DATA, itemName)
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount))
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER)
        args.putInt(DefineValue.BUY_TYPE, buyType!!)
        args.putString(DefineValue.PAYMENT_NAME, productName)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee))
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(totalAmount))
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(additionalFee))
        args.putString(DefineValue.DESTINATION_REMARK, NoHPFormat.formatTo62(custId))
        args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, true)

        var txStat: Boolean? = false
        when {
            txStatus == DefineValue.SUCCESS -> {
                txStat = true
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
            }
            txStatus == DefineValue.ONRECONCILED -> {
                txStat = true
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
            }
//            txStatus == DefineValue.SUSPECT -> {
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//            }
//            txStatus != DefineValue.FAILED -> {
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//            }
//            else -> {
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
//            }
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        args.putString(DefineValue.TRX_STATUS_REMARK, model.tx_status_remark)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, model.tx_remark)

        args.putString(DefineValue.DETAILS_BILLER, model.product_name)

//        if (model.product_name == null) {
//            args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_PLN)
//            if (billerTypeCode.equals(DefineValue.BILLER_TYPE_BPJS, ignoreCase = true))
//                args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_BPJS)
            args.putString(DefineValue.BILLER_TYPE, model.biller_type)
            args.putString(DefineValue.PAYMENT_REMARK, model.payment_remark)
//        }

        args.putString(DefineValue.BILLER_DETAIL, toJson(model.biller_detail).toString())
        args.putString(DefineValue.BUSS_SCHEME_CODE, model.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, model.buss_scheme_name)
        args.putString(DefineValue.PRODUCT_NAME, model.product_name)

        dialog.arguments = args
        val ft = activity!!.supportFragmentManager.beginTransaction()
        ft.add(dialog, ReportBillerDialog.TAG)
        ft.commitAllowingStateLoss()
    }

    private fun showDialog(msg: String) {
        // Create custom dialog object
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification)

        // set values for custom dialog components - text, image and button
        val btnDialog = dialog.findViewById<Button>(R.id.btn_dialog_notification_ok)
        val Title = dialog.findViewById<TextView>(R.id.title_dialog)
        val Message = dialog.findViewById<TextView>(R.id.message_dialog)

        Message.visibility = View.VISIBLE
        Title.text = getString(R.string.error)
        Message.text = msg

        btnDialog.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onOkButton() {
        initLayout()
        fragmentManager != null
        fragmentManager!!.popBackStackImmediate(BillerActivity.FRAG_BIL_INPUT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onResume() {
        super.onResume()
        if (nfcAdapter != null) {
            nfcAdapter!!.enableReaderMode(activity, this, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onPause() {
        super.onPause()
        if (nfcAdapter != null) {
            nfcAdapter!!.disableReaderMode(activity)
        }
    }

    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        try {
            isoDep.connect()

            val cardSelectResponse = isoDep.transceive(hexStringToByteArray("00A40400080000000000000001"))
            val cardInfoResponse = isoDep.transceive(hexStringToByteArray("00B300003F"))

            activity!!.runOnUiThread {

                Timber.tag("SELECT_RESPONSE : ").d(toHex(cardSelectResponse))
                cardSelect = toHex(cardSelectResponse)

                if(cardSelect.equals("9000")) {
                    Timber.tag("CARD_INFO : ").d(toHex(cardInfoResponse))
                    cardInfo = toHex(cardInfoResponse)
                    cardNumber = cardInfo!!.substring(0, 16)
                    billerinput_et_id_remark.setText(cardNumber)
                }else{
                    Toast.makeText(activity!!, "Kartu anda tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
