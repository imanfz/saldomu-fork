package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.sgo.saldomu.utils.NumberTextWatcherForThousand
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_biller_confirm.*
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_biller_input_game.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


class BillerInputGame : BaseFragment(), ReportBillerDialog.OnDialogOkCallback {

    private lateinit var viewLayout: View

    val TAG = "BillerInputGame"
    val REQUEST_BillerInqReq = 22
    private var billerTypeCode: String = ""
    private var txId: String = ""
    private var itemId: String = ""
    private var itemName: String = ""
    private var billerCommId: String = ""
    private var custId: String = ""
    private var paymentName: String = ""
    private var ccyId: String = ""
    private var description: String = ""
    private var enabledAdditionalFee: String = ""
    private var billerCommCode: String = ""
    private var billerCommName: String = ""

    private var valuePin: String = ""
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
    private var additionalFee = 0.0
    private var buyType = 0
    private var amount = 0.0
    private var totalAmount = 0.0

    private var mBillerData: BillerItem? = null
    private var mListDenomData: List<DenomDataItem> = ArrayList()
    private var billerIdPatterns: List<PatternBillerItem> = ArrayList()

    private var paymentData: MutableList<String>? = null
    private var adapterPaymentOptions: ArrayAdapter<String>? = null
    private var mListBankBiller: List<BankBillerItem>? = null
    private var mTempBank: listBankModel? = null
    private lateinit var sentPaymentBillerModel: SentPaymentBillerModel

    private var adapterDenomList: AdapterDenomList? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLayout = inflater.inflate(R.layout.frag_biller_input_game, container, false)
        return viewLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        billerTypeCode = arguments!!.getString(DefineValue.BILLER_TYPE, "")
        mBillerData = arguments!!.getParcelable(DefineValue.BILLER_ITEM);
        billerCommId = mBillerData!!.commId
        billerCommCode = mBillerData!!.commCode
        billerCommName = mBillerData!!.commName
        mListBankBiller = mBillerData!!.bankBiller
        billerIdPatterns = mBillerData!!.billerIdPatterns
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false)

        initLayout()
    }

    private val spinnerPaymentListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            adapterView: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            paymentName = adapterView?.getItemAtPosition(position).toString()
            var i = 0
            while (i < mListBankBiller!!.size) {
                if (paymentName == mListBankBiller?.get(i)?.productName) {
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

    private fun initLayout() {
        buyType = BillerActivity.PURCHASE_TYPE
        when {
            billerIdPatterns.isEmpty() -> {
                billerinput_et_id.hint = resources.getText(R.string.user_id)
                billerinput_layout_et_id_extra_1.visibility = View.GONE
                billerinput_layout_et_id_extra_2.visibility = View.GONE
            }
            billerIdPatterns.size == 1 -> {
                billerinput_et_id.hint = billerIdPatterns[0].label
                billerinput_layout_et_id_extra_1.visibility = View.GONE
                billerinput_layout_et_id_extra_2.visibility = View.GONE
            }
            billerIdPatterns.size == 2 -> {
                billerinput_et_id.hint = billerIdPatterns[0].label
                if (billerIdPatterns[1].type.equals("keyin")) {
                    billerinput_et_id_extra_1.hint = billerIdPatterns[1].label
                } else if (billerIdPatterns[1].type.equals("hardcode"))
                    billerinput_layout_et_id_extra_1.visibility = View.GONE
                billerinput_layout_et_id_extra_2.visibility = View.GONE
            }
            billerIdPatterns.size == 3 -> {
                billerinput_et_id.hint = billerIdPatterns[0].label
                if (billerIdPatterns[1].type.equals("keyin")) {
                    billerinput_et_id_extra_1.hint = billerIdPatterns[1].label
                } else if (billerIdPatterns[1].type.equals("hardcode"))
                    billerinput_layout_et_id_extra_1.visibility = View.GONE
                if (billerIdPatterns[2].type.equals("keyin")) {
                    billerinput_et_id_extra_2.hint = billerIdPatterns[2].label
                } else if (billerIdPatterns[2].type.equals("hardcode"))
                    billerinput_layout_et_id_extra_2.visibility = View.GONE
            }
        }
        initializeSpinnerDenom()
    }

    private fun initializeSpinnerDenom() {
        mListDenomData = mBillerData!!.denomData
        adapterDenomList =
            AdapterDenomList(context!!, mListDenomData, object : AdapterDenomList.OnClick {
                override fun onClick(pos: Int) {
                    if (inputValidation()) {
                        itemId = mListDenomData[pos].itemId
                        sentInquryBiller()
                    }
                }
            })
        billerinput_list_denom.adapter = adapterDenomList
        billerinput_list_denom.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        adapterDenomList!!.notifyDataSetChanged()

        billerCommCode = mBillerData!!.commCode
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
        when {
            billerinput_et_id.visibility == View.VISIBLE -> {
                if (billerinput_et_id.text!!.isEmpty()) {
                    billerinput_et_id.requestFocus()
                    billerinput_et_id.error = "Input " + billerIdPatterns[0].label
                    return false
                }
            }
            billerinput_et_id_extra_1.visibility == View.VISIBLE -> {
                if (billerinput_et_id_extra_1.text!!.isEmpty()) {
                    billerinput_et_id_extra_1.requestFocus()
                    billerinput_et_id_extra_1.error = "Input " + billerIdPatterns[1].label
                    return false
                }
            }
            billerinput_et_id_extra_2.visibility == View.VISIBLE -> {
                if (billerinput_et_id_extra_2.text!!.isEmpty()) {
                    billerinput_et_id_extra_2.requestFocus()
                    billerinput_et_id_extra_2.error = "Input " + billerIdPatterns[2].label
                    return false
                }
            }
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

    private fun countTotal() {
        totalAmount = amount + additionalFee + fee
    }

    private fun sentInquryBiller() {
        try {
            showProgressDialog()
            ToggleKeyboard.hide_keyboard(activity!!)
            //Mole
            //[userId]|[zoneId]
            //Ragnarok M Eternal Love
            //[userId]|eternal
            //Ragnarok M Midnight Party
            //[userId]|midnight
            //AOV, FF, Speed Drifters
            //[userId]
            //Bleach Mobile 3D, Era of Celestials
            //[roleName]|[userId]|[serverId]
            //Dragon Nest
            //[roleName]|[serverId]
            val text1 = billerinput_et_id.text.toString()
            val text2 = billerinput_et_id_extra_1.text.toString()
            val text3 = billerinput_et_id_extra_2.text.toString()

            when {
                billerIdPatterns.isEmpty() || billerIdPatterns.size == 1 -> {
                    custId = text1
                }
                billerIdPatterns.size > 1 -> {
                    for (i in billerIdPatterns.indices) {
                        if (billerIdPatterns[i].type.equals("keyin")) {
                            when (i) {
                                0 -> {
                                    custId += text1
                                }
                                1 -> {
                                    custId += text2
                                }
                                2 -> {
                                    custId += text3
                                }
                            }
                        } else if (billerIdPatterns[i].type.equals("hardcode")) {
                            custId += billerIdPatterns[i].typeValue
                        }
                        if (i < billerIdPatterns.size - 1)
                            custId += "|"
                    }
                }
            }

            extraSignature = billerCommId + itemId + custId

            val params = RetrofitService.getInstance()
                .getSignature(MyApiClient.LINK_INQUIRY_BILLER, extraSignature)
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
                        var code = model.error_code
                        val message = model.error_message
                        if (code == WebParams.SUCCESS_CODE) {
                            setIsInputAmount(model.biller_input_amount == DefineValue.STRING_YES)
                            is_display_amount =
                                model.biller_display_amount == DefineValue.STRING_YES

                            txId = model.tx_id
                            itemId = model.item_id
                            ccyId = model.ccy_id
                            amount = model.amount.toDouble() - model.admin_fee.toDouble()
                            itemName = model.item_name
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

                            if (additionalFee > 0)
                                dialog.billerinput_et_add_fee.setText(
                                    NumberTextWatcherForThousand.getDecimalFormattedString(
                                        additionalFee.toInt().toString()
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
                                        additionalFee =
                                            NumberTextWatcherForThousand.trimCommaOfString(dialog.billerinput_et_add_fee.text.toString())
                                                .toDouble()
                                        dialog.billerinput_et_add_fee.setSelection(dialog.billerinput_et_add_fee.text.toString().length)
                                    } else
                                        additionalFee = 0.0
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

                            dialog.denom.text = itemName
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
                            AlertDialogMaintenance.getInstance().showDialogMaintenance(
                                activity
                            )
                        } else {
                            if (isVisible) {
                                Toast.makeText(activity, "$code : $message", Toast.LENGTH_LONG).show()
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

            extraSignature = txId + itemId + billerCommId + productCode

            val params = RetrofitService.getInstance()
                .getSignature(MyApiClient.LINK_PAYMENT_BILLER, extraSignature)
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
            params[WebParams.PRODUCT_H2H] = mTempBank?.product_h2h
            params[WebParams.PRODUCT_TYPE] = mTempBank?.product_type
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
                                    txId,
                                    productCode,
                                    billerCommCode,
                                    sentPaymentBillerModel.merchant_type,
                                    attempt
                                )
                            }
                        } else if (code == WebParams.LOGOUT_CODE) {
                            AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                        } else if (code == DefineValue.ERROR_9333) run {
                            Timber.d("isi response app data:%s", sentPaymentBillerModel.app_data)
                            val appModel = sentPaymentBillerModel.app_data
                            AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                activity,
                                appModel.type,
                                appModel.packageName,
                                appModel.downloadUrl
                            )
                        } else if (code == DefineValue.ERROR_0066) run {
                            Timber.d("isi response maintenance:%s", response.toString())
                            val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                            alertDialogMaintenance.showDialogMaintenance(
                                activity
                            )
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
                                AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                            }
                            ErrorDefinition.WRONG_PIN_BILLER -> {
                                showDialogError(message)
                            }
                            DefineValue.ERROR_9333 -> run {
                                Timber.d("isi response app data:%s", model.app_data)
                                val appModel = model.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                    activity,
                                    appModel.type,
                                    appModel.packageName,
                                    appModel.downloadUrl
                                )
                            }
                            DefineValue.ERROR_0066 -> run {
                                Timber.d("isi response maintenance:%s", response.toString())
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(
                                    activity
                                )
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
                                        dialogFrag.setTargetFragment(this@BillerInputGame, 0)
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
                if (data != null) {
                    valuePin = data.getStringExtra(DefineValue.PIN_VALUE)!!
                    if (saveToFavorite) {
                        onSaveToFavorite()
                    } else {
                        sentInsertTransTopup(valuePin)
                    }
                }
            }
        }
    }

    private fun onSaveToFavorite() {
        showProgressDialog()
        extraSignature = custId + billerTypeCode + "BIL"
        val url = MyApiClient.LINK_TRX_FAVORITE_SAVE
        val params = RetrofitService.getInstance().getSignature(url, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.PRODUCT_TYPE] = billerTypeCode
        params[WebParams.CUSTOMER_ID] = custId
        params[WebParams.TX_FAVORITE_TYPE] = "BIL"
        params[WebParams.COMM_ID] = billerCommId
        params[WebParams.NOTES] = notes
        params[WebParams.DENOM_ITEM_ID] = itemId

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
                                sentInsertTransTopup(valuePin)
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
            extraSignature = txId + billerCommCode + mTempBank?.product_code + tokenValue
            val params = RetrofitService.getInstance().getSignature(link, extraSignature)
            val uuid: String = params[WebParams.RC_UUID].toString()
            val dateTime: String = params[WebParams.RC_DTIME].toString()
            params[WebParams.TX_ID] = txId
            params[WebParams.PRODUCT_CODE] = mTempBank?.product_code
            params[WebParams.COMM_CODE] = billerCommCode
            params[WebParams.COMM_ID] = billerCommId
            params[WebParams.MEMBER_ID] = sp.getString(DefineValue.MEMBER_ID, "")
            params[WebParams.PRODUCT_VALUE] = RSA.opensslEncryptCommID(
                billerCommId,
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
                            getTrxStatus(txId, billerCommId)
                            setResultActivity()
                        } else if (code == WebParams.LOGOUT_CODE) {
                            AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
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
                        val model = getGson().fromJson(response, GetTrxStatusModel::class.java)

                        val code = model.error_code
                        val message = model.error_message
                        if (!model.on_error) {
                            if (code == WebParams.SUCCESS_CODE || code == "0003") {

                                val txStatus = model.tx_status
                                showReportBillerDialog(
                                    sp.getString(DefineValue.USER_NAME, ""),
                                    sp.getString(DefineValue.USERID_PHONE, ""), txId, itemName,
                                    txStatus!!, model
                                )
                            } else if (code == WebParams.LOGOUT_CODE) {
                                AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                            } else if (code == DefineValue.ERROR_9333) {
                                Timber.d("isi response app data:%s", model.app_data)
                                val appModel = model.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                    activity,
                                    appModel.type,
                                    appModel.packageName,
                                    appModel.downloadUrl
                                )
                            } else if (code == DefineValue.ERROR_0066) {
                                Timber.d("isi response maintenance:$response")
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
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
        args.putInt(DefineValue.BUY_TYPE, buyType)
        args.putString(DefineValue.PAYMENT_NAME, paymentName)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee))
        args.putString(
            DefineValue.TOTAL_AMOUNT,
            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(totalAmount)
        )
        args.putString(
            DefineValue.ADDITIONAL_FEE,
            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(additionalFee)
        )
        args.putString(DefineValue.DESTINATION_REMARK, NoHPFormat.formatTo62(custId))
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

