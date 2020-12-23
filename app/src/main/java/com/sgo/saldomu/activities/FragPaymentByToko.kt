package com.sgo.saldomu.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.AdapterListDetailPO
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.FormatQtyItem
import com.sgo.saldomu.models.MappingItemsItem
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_detail_po.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragPaymentByToko : BaseFragment() {
    var memberCode: String = ""
    var commCodeEspay: String = ""
    var commCode: String = ""
    var docNo: String = ""
    var txId: String = ""
    private var adapterDetailPO: AdapterListDetailPO? = null
    private val itemList = ArrayList<MappingItemsItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_detail_po, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCodeEspay= arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            commCode= arguments!!.getString(DefineValue.COMMUNITY_CODE, "")
            docNo = arguments!!.getString(DefineValue.DOC_NO, "")
            txId = arguments!!.getString(DefineValue.TX_ID, "")
        }
        val paymentTokoActivity = activity as PaymentTokoActivity
        paymentTokoActivity.initializeToolbar(getString(R.string.detail_document))

        adapterDetailPO = AdapterListDetailPO(context!!, itemList)
        recyclerViewList.adapter = adapterDetailPO
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        getPODetail()

        btn_payment.setOnClickListener { payInvoice() }
    }

    private fun getPODetail() {
        showProgressDialog()

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_DETAIL, memberCode + commCode + docNo)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.DOC_NO] = docNo

        Timber.d("isi params get doc detail paymentByToko:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_DETAIL, params,
                object : ObjListeners {
                    @SuppressLint("SetTextI18n")
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                btn_payment.visibility = View.VISIBLE
                                tv_docNo.text = response.getString(WebParams.DOC_NO)
                                tv_totalAmount.text = getString(R.string.currency) + " " + CurrencyFormat.format(response.getString(WebParams.TOTAL_AMOUNT))
                                val mArrayItem = JSONArray(response.getString(WebParams.ITEMS))
                                for (i in 0 until mArrayItem.length()) {
                                    val itemName = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_NAME)
                                    val itemCode = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_CODE)
                                    val price = mArrayItem.getJSONObject(i).getString(WebParams.PRICE)
                                    val unit = mArrayItem.getJSONObject(i).getString(WebParams.UNIT)
                                    val formatQtyJsonArray = mArrayItem.getJSONObject(i).getJSONArray(WebParams.FORMAT_QTY)
                                    val formatQtyItemList = ArrayList<FormatQtyItem>()
                                    for (j in 0 until formatQtyJsonArray.length()) {
                                        val mappingUnit = formatQtyJsonArray.getJSONObject(j).getString(WebParams.MAPPING_UNIT)
                                        val mappingQty = formatQtyJsonArray.getJSONObject(j).getInt(WebParams.MAPPING_QTY)
                                        val formatQtyItem = FormatQtyItem()
                                        formatQtyItem.mapping_unit = mappingUnit
                                        formatQtyItem.mapping_qty = mappingQty
                                        formatQtyItemList.add(formatQtyItem)
                                    }
                                    val mappingItemsItem = MappingItemsItem()
                                    mappingItemsItem.item_name = itemName
                                    mappingItemsItem.item_code = itemCode
                                    mappingItemsItem.price = price.toInt()
                                    mappingItemsItem.unit = unit
                                    mappingItemsItem.format_qty = formatQtyItemList
                                    itemList.add(mappingItemsItem)
                                }
                                adapterDetailPO!!.notifyDataSetChanged()
                            }
                            WebParams.LOGOUT_CODE -> {
                                AlertDialogLogout.getInstance().showDialoginMain(activity, message)
                            }
                            DefineValue.ERROR_9333 -> {
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val appModel = model.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            }
                            DefineValue.ERROR_0066 -> {
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(activity, message)
                            }
                            else -> {
                                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onError(throwable: Throwable?) {
                        dismissProgressDialog()
                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                    }

                })
    }

    private fun payInvoice() {
        showProgressDialog()
        extraSignature = txId + commCode
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_PAYMENT_DGI, extraSignature)
        params[WebParams.APP_ID] = BuildConfig.APP_ID
        params[WebParams.TX_ID] = txId
        params[WebParams.COMM_CODE] = commCode
        params[WebParams.USER_COMM_CODE] = sp.getString(DefineValue.COMMUNITY_CODE, "")
        params[WebParams.USER_ID] = userPhoneID
        Timber.d("params confirm payment invoice : $params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_PAYMENT_DGI, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        try {
                            dismissProgressDialog()
                            val model = gson.fromJson(response.toString(), jsonModel::class.java)
                            val code = response.getString(WebParams.ERROR_CODE)
                            val error_message = response.getString(WebParams.ERROR_MESSAGE)
                            Timber.d("response confirm payment invoice : $response")
                            if (code == WebParams.SUCCESS_CODE) {
                                sentInquiry()
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
                                Toast.makeText(activity, error_message, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(throwable: Throwable) {}
                    override fun onComplete() {}
                })
    }

    fun sentInquiry() {
        try {
            showProgressDialog()
//            extraSignature = txId + sp.getString(DefineValue.COMM_CODE, "") + product_code
//            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature)
//            params[WebParams.TX_ID] = txId
//            params[WebParams.PRODUCT_CODE] = product_code
//            params[WebParams.COMM_CODE] = sp.getString(DefineValue.COMM_CODE_DGI, "")
//            params[WebParams.USER_ID] = userPhoneID
//            params[WebParams.COMM_ID] = MyApiClient.COMM_ID
//            Timber.d("isi params InquiryTrx DGI:$params")
//            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
//                    object : ObjListeners {
//                        override fun onResponses(response: JSONObject) {
//                            try {
//                                val model = getGson().fromJson(response.toString(), jsonModel::class.java)
//                                var code = response.getString(WebParams.ERROR_CODE)
//                                val error_message = response.getString(WebParams.ERROR_MESSAGE)
//                                Timber.d("isi response InquiryTrx DGI: $response")
//                                if (code == WebParams.SUCCESS_CODE) {
//                                    if (!paymentTypeCode.equals(DefineValue.CT_CODE, ignoreCase = true)) {
//                                        sentInsertTrxNew()
//                                    } else {
//                                        getFailedPin()
//                                        CallPINinput(attempt)
//                                    }
//                                } else if (code == WebParams.LOGOUT_CODE) {
//                                    Timber.d("isi response autologout:$response")
//                                    val message = response.getString(WebParams.ERROR_MESSAGE)
//                                    val test = AlertDialogLogout.getInstance()
//                                    test.showDialoginActivity(activity, message)
//                                } else if (code == DefineValue.ERROR_9333) {
//                                    Timber.d("isi response app data:" + model.app_data)
//                                    val appModel = model.app_data
//                                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
//                                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
//                                } else if (code == DefineValue.ERROR_0066) {
//                                    Timber.d("isi response maintenance:$response")
//                                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
//                                    alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
//                                } else {
//                                    Timber.d("Error resendTokenSGOL:$response")
//                                    code = response.getString(WebParams.ERROR_MESSAGE)
//                                    Toast.makeText(activity, code, Toast.LENGTH_SHORT).show()
//                                }
//                                btn_resend.setEnabled(true)
//                            } catch (e: JSONException) {
//                                e.printStackTrace()
//                            }
//                        }
//
//                        override fun onError(throwable: Throwable) {}
//                        override fun onComplete() {
//                            dismissProgressDialog()
//                            btn_confirm.setEnabled(true)
//                        }
//                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }
//
//    private fun CallPINinput(_attempt: Int) {
//        val i = Intent(activity, InsertPIN::class.java)
//        if (_attempt == 1) i.putExtra(DefineValue.ATTEMPT, _attempt)
//        startActivityForResult(i, MainPage.REQUEST_FINISH)
//    }
//
//    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == MainPage.REQUEST_FINISH) {
//            //  Log.d("onActivity result", "Biller Fragment masuk request exit");
//            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
//                val value_pin = data.getStringExtra(DefineValue.PIN_VALUE)
//
//                //call insert trx
//                sentInsertTrx(value_pin)
//            }
//        }
//    }
//
//    fun sentInsertTrx(tokenValue: String) {
//        try {
//            showProgressDialog()
//            val args = arguments
//            val kode_otp: String = et_otp.getText().toString()
//            val link = MyApiClient.LINK_INSERT_TRANS_TOPUP
//            val subStringLink = link.substring(link.indexOf("saldomu/"))
//            val uuid: String
//            val dateTime: String
//            extraSignature = tx_id + sp.getString(DefineValue.COMM_CODE_DGI, "") + product_code + tokenValue
//            params = RetrofitService.getInstance().getSignature(link, extraSignature)
//            uuid = params[WebParams.RC_UUID].toString()
//            dateTime = params[WebParams.RC_DTIME].toString()
//            attempt = args!!.getInt(DefineValue.ATTEMPT, -1)
//            params[WebParams.TX_ID] = tx_id
//            params[WebParams.PRODUCT_CODE] = product_code
//            params[WebParams.COMM_CODE] = sp.getString(DefineValue.COMM_CODE_DGI, "")
//            params[WebParams.COMM_ID] = MyApiClient.COMM_ID
//            params[WebParams.MEMBER_ID] = sp.getString(DefineValue.MEMBER_ID, "")
//            params[WebParams.PRODUCT_VALUE] = RSA.opensslEncrypt(uuid, dateTime, userPhoneID, tokenValue, subStringLink)
//            params[WebParams.USER_ID] = userPhoneID
//            params[WebParams.KODE_OTP] = kode_otp
//            Timber.d("isi params insertTrxTOpupSGOL:$params")
//            RetrofitService.getInstance().PostJsonObjRequest(link, params,
//                    object : ObjListeners {
//                        override fun onResponses(response: JSONObject) {
//                            try {
//                                dismissProgressDialog()
//                                val model = getGson().fromJson(response.toString(), jsonModel::class.java)
//                                var code = response.getString(WebParams.ERROR_CODE)
//                                Timber.d("isi response insertTrxTOpupSGOL:$response")
//                                if (code == WebParams.SUCCESS_CODE) {
//                                    getTrxStatus(tx_id, MyApiClient.COMM_ID)
//                                    setResultActivity(MainPage.RESULT_BALANCE)
//                                } else if (code == WebParams.LOGOUT_CODE) {
//                                    Timber.d("isi response autologout:$response")
//                                    val message = response.getString(WebParams.ERROR_MESSAGE)
//                                    val test = AlertDialogLogout.getInstance()
//                                    test.showDialoginActivity(activity, message)
//                                } else if (code == DefineValue.ERROR_9333) {
//                                    Timber.d("isi response app data:" + model.app_data)
//                                    val appModel = model.app_data
//                                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
//                                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
//                                } else if (code == DefineValue.ERROR_0066) {
//                                    Timber.d("isi response maintenance:$response")
//                                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
//                                    alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
//                                } else {
//                                    code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE)
//                                    Toast.makeText(activity, code, Toast.LENGTH_LONG).show()
//                                    val message = response.getString(WebParams.ERROR_MESSAGE)
//                                    if (message == "PIN tidak sesuai") {
//                                        val i = Intent(activity, InsertPIN::class.java)
//                                        attempt = response.optInt(WebParams.FAILED_ATTEMPT, -1)
//                                        val failed = response.optInt(WebParams.MAX_FAILED, 0)
//                                        if (attempt != -1) i.putExtra(DefineValue.ATTEMPT, failed - attempt)
//                                        startActivityForResult(i, MainPage.REQUEST_FINISH)
//                                    } else {
//                                        resendToken()
//                                        et_otp.setText("")
//                                    }
//                                    //                                getFragmentManager().popBackStack();
//                                }
//                            } catch (e: JSONException) {
//                                e.printStackTrace()
//                            }
//                        }
//
//                        override fun onError(throwable: Throwable) {}
//                        override fun onComplete() {
//                            dismissProgressDialog()
//                            btn_confirm.setEnabled(true)
//                        }
//                    })
//        } catch (e: Exception) {
//            Timber.d("httpclient:" + e.message)
//        }
//    }
}