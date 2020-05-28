package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.gson.JsonObject
import com.sgo.saldomu.Beans.Biller_Data_Model
import com.sgo.saldomu.Beans.Denom_Data_Model
import com.sgo.saldomu.Beans.bank_biller_model
import com.sgo.saldomu.Beans.listBankModel
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.BillerActivity
import com.sgo.saldomu.activities.NFCActivity
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.InqBillerModel
import com.sgo.saldomu.utils.Converter.Companion.hexStringToByteArray
import com.sgo.saldomu.utils.Converter.Companion.toHex
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.frag_biller_input_new.*
import timber.log.Timber
import java.io.IOException
import java.util.*

class BillerInputEmoney : BaseFragment(), NfcAdapter.ReaderCallback {
    private val LINKAJASALDOMU: String = "LINKAJASALDOMU"
    private val EMONEYSALDOMU: String = "EMONEYSALDOMU"
    private val OVOSALDOMU: String = "OVOSALDOMU"

    private var nfcAdapter: NfcAdapter? = null
    private var realm: Realm? = null

    private var billerData: Biller_Data_Model? = null
    private var paymentMethod: listBankModel? = null

    private var denomAdapter: ArrayAdapter<String>? = null
    private var paymentAdapter: ArrayAdapter<String>? = null

    private var denomData: ArrayList<String>? = null
    private var paymentData: ArrayList<String>? = null

    private var listDenomData: List<Denom_Data_Model>? = null
    private var listBankBiller: List<bank_biller_model>? = null

    private var enabledAdditionalFee: Boolean? = false
    private var isAgent: Boolean? = false

    private var additionalFee = 0.0
    private var amount: Double = 0.0
    private var fee: Double = 0.0
    private var itemPrice: Double = 0.0
    private var total: Double = 0.0

    private var buyType: Int? = null

    private var billerCommId: String? = null
    private var billerCommCode: String? = null
    private var billerCommName: String? = null
    private var billerItemId: String? = null
    private var billerIdNumber: String? = null
    private var billerTypeCode: String? = null
    private var ccyId: String? = null
    private var description: String? = null
    private var itemId: String? = null
    private var itemName: String? = null
    private var txId: String? = null

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

        initLayout()
        initRealm()
    }

    private fun initLayout() {
        buyType = BillerActivity.PURCHASE_TYPE
        billerinput_et_id_remark.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(13))
        if (billerCommCode.equals(EMONEYSALDOMU)) {
            billerinput_text_id_remark.text = getString(R.string.billerinput_text_payment_remark_Emoney)
            billerinput_et_id_remark.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(16))
            if (BuildConfig.FLAVOR == "development")
//                billerinput_et_id_remark.setText("6032984008386579")
                layout_cek_balance.visibility = View.VISIBLE
        }
        billerinput_et_id_remark.inputType = InputType.TYPE_CLASS_NUMBER
        if (billerIdNumber != null)
            billerinput_et_id_remark.setText(billerIdNumber)

        billerinput_detail_layout_add_fee.visibility = View.GONE
        billerinput_layout_denom.visibility = View.VISIBLE
        ib_contact_list.visibility = View.GONE
        billerinput_layout_add_fee.visibility = View.GONE
        billerinput_et_add_fee.inputType = InputType.TYPE_CLASS_NUMBER
        billerinput_et_add_fee.addTextChangedListener(object : TextWatcher {
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
        btn_submit_billerinput.setOnClickListener { sentPaymentBiller() }
        buttonSubmit(false)

        btn_cek_balance.setOnClickListener {
            val intent = Intent(activity, NFCActivity::class.java)
            startActivity(intent)
        }
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
        billerData = realm!!.where(Biller_Data_Model::class.java).equalTo(WebParams.COMM_ID, billerCommId).findFirst()
        if (billerData != null) {
            listDenomData = realm!!.copyFromRealm(billerData!!.denom_data_models)
            listBankBiller = realm!!.copyFromRealm(billerData!!.bank_biller_models)
        }
        initiSpinner()
    }

    private fun initiSpinner() {
        if (listDenomData!!.isNotEmpty()) {
            denomData = ArrayList<String>()
            denomData?.clear()
            denomData?.add(getString(R.string.billerinput_text_spinner_default_emoney))
            for (i in listDenomData!!.indices) {
                denomData?.add(listDenomData!![i].item_name)
            }
            denomAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, denomData)
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
            paymentAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, paymentData)
            paymentAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            billerinput_spinner_payment_options.adapter = paymentAdapter
            billerinput_spinner_payment_options.onItemSelectedListener = spinnerPaymentListener
            billerinput_spinner_payment_options.setSelection(1)
        }
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
            }

        }

    }

    private val spinnerPaymentListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position != 0) {
                paymentMethod = listBankModel(listBankBiller!![position - 1].bank_code,
                        listBankBiller!![position - 1].bank_name,
                        listBankBiller!![position - 1].product_code,
                        listBankBiller!![position - 1].product_name,
                        listBankBiller!![position - 1].product_type,
                        listBankBiller!![position - 1].product_h2h)
            } else {
                paymentMethod = null
            }

        }

    }

    private fun sentInquiryBiller() {
        showProgressDialog()
        ToggleKeyboard.hide_keyboard(activity!!)

        val custId: String = NoHPFormat.formatTo62(billerinput_et_id_remark.text.toString())
        val extraSignature: String = billerCommId + itemId + custId

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
                                itemPrice = model.amount.toDouble() - model.admin_fee.toDouble()
                                description = getGson().toJson(model.description)
                                fee = model.admin_fee.toDouble()
                                enabledAdditionalFee = model.enabled_additional_fee == DefineValue.Y

                                if (isAgent!! && enabledAdditionalFee!!) {
                                    billerinput_layout_add_fee.visibility = View.VISIBLE
                                    billerinput_detail_layout_add_fee.visibility = View.VISIBLE
                                }

                                billerinput_layout_payment_method.visibility = View.VISIBLE
                                billerinput_layout_detail.visibility = View.VISIBLE
                                billerinput_layout_favorite.visibility = View.GONE
                                billerinput_detail_text_name.text = itemName
                                billerinput_detail_price.text = getString(R.string.rp_) + " " + CurrencyFormat.format(itemPrice)
                                billerinput_detail_admin_fee.text = getString(R.string.rp_) + " " + CurrencyFormat.format(fee)
                                buttonSubmit(true)
                            }
                            WebParams.LOGOUT_CODE -> {
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(activity, message)
                            }
                            DefineValue.ERROR_9333 -> {
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            }
                            DefineValue.ERROR_0066 -> {
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
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

    }

    @SuppressLint("SetTextI18n")
    private fun countTotal() {
        amount = itemPrice + fee
        total = itemPrice + additionalFee + fee
        billerinput_detail_total.text = getString(R.string.rp_) + " " + CurrencyFormat.format(total)
    }

    private fun inputValidation(): Boolean {
        if (billerinput_et_id_remark.text.isEmpty()) {
            if (billerCommCode.equals(EMONEYSALDOMU) && billerinput_et_id_remark.text.length != 16)
                billerinput_et_id_remark.error = getString(R.string.insert_16_card_number)
            else
                billerinput_et_id_remark.error = getString(R.string.regist1_validation_nohp)
            billerinput_et_id_remark.requestFocus()
            billerinput_spinner_denom.setSelection(0)
            return false
        }
        if (itemName == null) {
            billerinput_spinner_denom.requestFocus()
            Toast.makeText(activity, getString(R.string.billerinput_validation_spinner_default_data), Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter != null) {
            nfcAdapter!!.enableReaderMode(activity, this, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter!!.disableReaderMode(activity)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        try {
            isoDep.connect()
//            byte[] selectEmoneyResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
//                    "00A40400080000000000000001"));
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d("SELECT_RESPONSE : ", Converter.Companion.toHex(selectEmoneyResponse));
//                    cardSelect = Converter.Companion.toHex(selectEmoneyResponse);
//                }
//            });
//
//            byte[] cardAttirbuteResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
//                    "00F210000B"));
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d("CARD_ATTRIBUTE : ", Converter.Companion.toHex(cardAttirbuteResponse));
//                    cardAttribute = Converter.Companion.toHex(cardAttirbuteResponse);
//                }
//            });
//
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d("UUID : ", Converter.Companion.toHex(tag.getId()));
//                    cardUid = Converter.Companion.toHex(tag.getId());
//                }
//            });
            val cardInfoResponse = isoDep.transceive(hexStringToByteArray(
                    "00B300003F"))
            activity!!.runOnUiThread {
                Log.d("CARD_INFO : ", toHex(cardInfoResponse))
                val cardInfo = toHex(cardInfoResponse)
                val numberCard = cardInfo.substring(0, 16)
                billerinput_et_id_remark.setText(numberCard)
            }
//            byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
//                    "00B500000A"));
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                    Log.d("LAST_BALANCE : ", Converter.Companion.toHex(lastBalanceResponse));
//                    cardBalance = Converter.Companion.toHex(lastBalanceResponse);
////                    cardBalanceResult.setText("RP. " + Converter.Companion.toLittleEndian(cardBalance.substring(0, 8)));
//                    Log.d("SALDO : ", String.valueOf(Converter.Companion.toLittleEndian(cardBalance.substring(0, 8))));
//                    saldo = String.valueOf(Converter.Companion.toLittleEndian(cardBalance.substring(0, 8)));
//                }
//            });
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
