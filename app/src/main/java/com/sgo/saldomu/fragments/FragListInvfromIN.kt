package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserInvoiceActivity
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.ListInvoiceAdapter
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.dialogs.PopUpDialog
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.*
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list.*
import kotlinx.android.synthetic.main.frag_list.btn_proses_gr
import kotlinx.android.synthetic.main.frag_list_invoice.*
import kotlinx.android.synthetic.main.frag_list_po.recyclerViewList
import kotlinx.android.synthetic.main.list_recycle_history_item.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragListInvfromIN : BaseFragment(), ListInvoiceAdapter.listener {

    private val docListArrayList = ArrayList<ListPOModel>()
    private val payMethodArrayList = ArrayList<ListPayMethodModel>()

    private var listInvoiceAdapter: ListInvoiceAdapter? = null


    var memberCode: String? = null
    var commCode: String? = null

    var memberCodeEspay: String = ""
    var commCodeEspay: String = ""
    var custIdEspay: String = ""
    var docNo: String = ""
    var doc_detail: String = ""
    var type_id: String = ""
    var partner_code_espay: String = ""

    var cust_id: String = ""
    var reff_id: String = ""
    var ccy_id: String = ""
    var cust_type: String = ""
    var total_disc: String = ""

    var obj: ListPOModel? = null;

    var paymentOption = ""

    private val paymentListOption = ArrayList<String>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list_invoice, container, false)
        memberCode = memberIDLogin
        commCode = commIDLogin
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val canvasserInvoiceActivity = activity as CanvasserInvoiceActivity
        canvasserInvoiceActivity.initializeToolbar(getString(R.string.invoice_title))

        docListArrayList.clear()

        initializeListInv()

        btn_proses_gr.setOnClickListener {
            if (inputValidation()) {
                if (obj!!.paid_status.equals("Y")) {
                    Toast.makeText(activity, getString(R.string.invoice_already_paid), Toast.LENGTH_LONG).show()
                } else {
                    requestPayment(obj!!)
                }
            }
        }


        var paymentOptionsAdapter = ArrayAdapter(activity!!, R.layout.layout_spinner_list_cust, paymentListOption)
        paymentOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_bank_produk.adapter = paymentOptionsAdapter
        spinner_bank_produk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                paymentOption = paymentListOption[p2]

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }


    }

    private fun inputValidation(): Boolean {
        if (obj == null) {
            Toast.makeText(activity, getString(R.string.choose_invoice_validation), Toast.LENGTH_LONG).show()
        } else if (paymentOption!!.equals(getString(R.string.lbl_choose))) {
            Toast.makeText(activity, getString(R.string.billerinput_validation_spinner_default_payment), Toast.LENGTH_LONG).show()
        }

        return true
    }

    private fun initializeListInv() {

        listInvoiceAdapter = ListInvoiceAdapter(docListArrayList, activity, this)
        recyclerViewList.adapter = listInvoiceAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val bundle = arguments

        partner_code_espay = bundle!!.getString(DefineValue.PARTNER_CODE_ESPAY, "")

        val mArrayDoc = JSONArray(bundle!!.getString(WebParams.DOC_LIST))

        for (i in 0 until mArrayDoc.length()) {
            val docNo = mArrayDoc.getJSONObject(i).getString(WebParams.DOC_NO)
            val docStatus = mArrayDoc.getJSONObject(i).getString(WebParams.DOC_STATUS)
            val totalAmount = mArrayDoc.getJSONObject(i).getString(WebParams.TOTAL_AMOUNT)
            val dueDate = mArrayDoc.getJSONObject(i).getString(WebParams.DUE_DATE)
            val custID = mArrayDoc.getJSONObject(i).getString(WebParams.CUST_ID)
            val memberCode = mArrayDoc.getJSONObject(i).getString(WebParams.MEMBER_CODE)
            val commCode = mArrayDoc.getJSONObject(i).getString(WebParams.COMM_CODE)
            val reffId = mArrayDoc.getJSONObject(i).getString(WebParams.REFF_ID)
            val reffNo = mArrayDoc.getJSONObject(i).getString(WebParams.REFF_NO)
            val createAt = mArrayDoc.getJSONObject(i).getString(WebParams.CREATE_AT)
            val issueDate = mArrayDoc.getJSONObject(i).getString(WebParams.ISSUE_DATE)
            val paidstats = mArrayDoc.getJSONObject(i).getString(WebParams.PAID_STATUS)
            val promoListJsonArray = mArrayDoc.getJSONObject(i).getJSONArray(WebParams.PROMO)
            var promoArrayList = ArrayList<PromoCanvasserModel>()
            for (i in 0 until promoListJsonArray.length()) {
                total_disc = promoListJsonArray.getJSONObject(i).getString(WebParams.TOTAL_DISC)
                var promo = PromoCanvasserModel()
                promo.total_disc = total_disc
                promoArrayList.add(promo)
            }


            val listPOModel = ListPOModel()
            listPOModel.doc_no = docNo
            listPOModel.doc_status = docStatus
            listPOModel.total_amount = totalAmount
            listPOModel.due_date = dueDate
            listPOModel.cust_id = custID
            listPOModel.comm_code = commCode
            listPOModel.member_code = memberCode
            listPOModel.type_id = memberCode
            listPOModel.reff_id = reffId
            listPOModel.reff_no = reffNo
            listPOModel.created_at = createAt
            listPOModel.issue_date = issueDate
            listPOModel.paid_status = paidstats
            listPOModel.promo = promoArrayList

            docListArrayList.add(listPOModel)
        }

        listInvoiceAdapter!!.updateData(docListArrayList)

        val mArrayPayMethod = JSONArray(bundle!!.getString(WebParams.PAYMENT_TYPE))

        paymentListOption.add(getString(R.string.lbl_choose))

        for (i in 0 until mArrayPayMethod.length()) {

            paymentListOption.add(mArrayPayMethod.getJSONObject(i).getString(WebParams.PAYMENT_NAME))

            val payCode = mArrayPayMethod.getJSONObject(i).getString(WebParams.PAYMENT_CODE)
            val payName = mArrayPayMethod.getJSONObject(i).getString(WebParams.PAYMENT_NAME)


            val obj = ListPayMethodModel()
            obj.payment_code = payCode
            obj.payment_name = payName
            payMethodArrayList.add(obj)
        }


    }


    fun requestPayment(obj: ListPOModel?) {

        try {

            var amount = (obj!!.total_amount)!!.toDouble() - (obj.promo[0].total_disc).toDouble()

            showProgressDialog()
            extraSignature = obj!!.member_code + obj!!.comm_code + obj!!.doc_no
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_PAYMENT, extraSignature)
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_CODE_ESPAY] = obj!!.comm_code
            params[WebParams.MEMBER_CODE_ESPAY] = obj!!.member_code
            params[WebParams.COMM_CODE] = commCode
            params[WebParams.MEMBER_CODE] = memberCode


            params[WebParams.PAYMENT_TYPE] = paymentOption
            params[WebParams.AMOUNT] = amount
            params[WebParams.SHOP_PHONE] = obj!!.cust_id
            params[WebParams.LATITUDE] = sp.getDouble(DefineValue.LATITUDE_UPDATED, 0.0)
            params[WebParams.LONGITUDE] = sp.getDouble(DefineValue.LONGITUDE_UPDATED, 0.0)

            params[WebParams.CUST_ID_ESPAY] = obj!!.cust_id
            params[WebParams.CUST_ID] = userPhoneID
            params[WebParams.REFF_ID] = obj!!.reff_id
            params[WebParams.REFF_NO] = obj!!.reff_no

            params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE;
            params[WebParams.TYPE_ID] = DefineValue.IN
            params[WebParams.CUST_TYPE] = DefineValue.CANVASSER //
            params[WebParams.DOC_NO] = obj!!.doc_no
            params[WebParams.PARTNER_CODE_ESPAY] = partner_code_espay


            Timber.d("params request payment canvasser:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_PAYMENT, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response request payment canvasser:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        showPopUp()
                                    }
                                    WebParams.LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
                                        val message = response.getString(WebParams.ERROR_MESSAGE)
                                        val test = AlertDialogLogout.getInstance()
                                        test.showDialoginActivity(activity, message)
                                    }
                                    DefineValue.ERROR_9333 -> {
                                        Timber.d("isi response app data:%s", model.app_data)
                                        val appModel = model.app_data
                                        val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                        alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                    }
                                    DefineValue.ERROR_0066 -> {
                                        Timber.d("isi response maintenance:$response")
                                        val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                        alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                                    }
                                    else -> {
                                        Timber.d("isi error request payment canvasser:$response")
                                        Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()

                            }
                        }

                        override fun onError(throwable: Throwable) {
                            showPopUp()

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: java.lang.Exception) {
            Timber.d("httpclient:%s", e.message)
        }


    }

    fun showPopUp() {
        val bundle = Bundle();
        val dialogFragment: DialogFragment = PopUpDialog.newDialog(bundle, object : PopUpDialog.PopUpListener {
            override fun onClick(dialog: DialogFragment?) {
                dialog!!.dismiss();
                activity!!.finish();
            }
        })
        dialogFragment.show(activity!!.supportFragmentManager, "Dialog Pop Up")
    }

    override fun onClick(item: ListPOModel?) {
        obj = item
        tv_phone_no.setText(obj!!.cust_id)
        tv_total.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(obj!!.total_amount))

    }
}