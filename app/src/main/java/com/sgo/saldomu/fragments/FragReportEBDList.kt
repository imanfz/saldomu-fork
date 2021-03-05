package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.AdapterReportEBDList
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.dialogs.ReportBillerDialog
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.retrofit.DocListItem
import com.sgo.saldomu.models.retrofit.EBDDocStrukReportModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_report_ebd_list.*
import org.json.JSONObject
import timber.log.Timber

class FragReportEBDList(private val reportType: String, private val memberCodeEspay: String, private var commCodeEspay: String) : BaseFragment(), ReportBillerDialog.OnDialogOkCallback {

    private var adapterReportEBDList: AdapterReportEBDList? = null
    private val docListItemArray = ArrayList<DocListItem>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_report_ebd_list, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapterReportEBDList = AdapterReportEBDList(context!!, docListItemArray, object : AdapterReportEBDList.OnClick {
            override fun onClick(pos: Int) {
                commCodeEspay = docListItemArray[pos].commCode
                reportDetail(docListItemArray[pos].docNo)
            }
        })
        recyclerView.adapter = adapterReportEBDList
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        switch_status.setOnCheckedChangeListener { compoundButton, isChecked ->
            getDataReportEBD()
        }
        getDataReportEBD()
    }

    private fun getDataReportEBD() {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_LIST, memberCodeEspay)
        params[WebParams.COMM_ID] = sp.getString(DefineValue.COMMUNITY_ID, "")
        params[WebParams.USER_ID] = sp.getString(DefineValue.USERID_PHONE, "")
        params[WebParams.CUST_ID_ESPAY] = sp.getString(DefineValue.USERID_PHONE, "")
        params[WebParams.CANVASSER_CUSTID] = sp.getString(DefineValue.USERID_PHONE, "")
        params[WebParams.MEMBER_ID] = sp.getString(DefineValue.MEMBER_ID, "")
        params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
        params[WebParams.TYPE_ID] = reportType
        if (switch_status.isChecked)
            params[WebParams.DOC_STATUS] = DefineValue.OPEN
        else
            params[WebParams.DOC_STATUS] = DefineValue.CLOSE
        Timber.d("params inquiry doc list : %s", params)
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_LIST, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                Timber.d("response inquiry doc list %s: %s", reportType, response)
                val code = response.getString(WebParams.ERROR_CODE)
                val message = response.getString(WebParams.ERROR_MESSAGE)
                when (code) {
                    WebParams.SUCCESS_CODE -> {
                        recyclerView.visibility = View.VISIBLE
                        layout_no_data.visibility = View.GONE
                        val docList = response.getJSONArray(WebParams.DOC_LIST)
                        docListItemArray.clear()
                        for (i in 0 until docList.length()) {
                            val docListItem = getGson().fromJson(docList.getJSONObject(i).toString(), DocListItem::class.java)
                            docListItemArray.add(docListItem)
                        }
                        adapterReportEBDList!!.notifyDataSetChanged()
                    }
                    "31" -> {
                        recyclerView.visibility = View.GONE
                        layout_no_data.visibility = View.VISIBLE
                    }
                    WebParams.LOGOUT_CODE -> {
                        val alertDialogLogout = AlertDialogLogout.getInstance()
                        alertDialogLogout.showDialoginActivity2(activity, message)
                    }
                    DefineValue.ERROR_9333 -> {
                        val model = gson.fromJson(response.toString(), jsonModel::class.java)
                        val appModel = model.app_data
                        val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                        alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                    }
                    DefineValue.ERROR_0066 -> {
                        val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                        alertDialogMaintenance.showDialogMaintenance(activity, message)
                    }
                    else -> {
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onError(throwable: Throwable) {
                dismissProgressDialog()
            }

            override fun onComplete() {
                dismissProgressDialog()
            }
        })
    }

    private fun reportDetail(docNo: String) {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_STRUK, memberCodeEspay + commCodeEspay + docNo)
        params[WebParams.USER_ID] = sp.getString(DefineValue.USERID_PHONE, "")
        params[WebParams.CUST_ID_ESPAY] = sp.getString(DefineValue.USERID_PHONE, "")
        params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
        params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
        params[WebParams.DOC_NO] = docNo
        Timber.d("params inquiry doc struk : %s", params)
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_STRUK, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val code = response.getString(WebParams.ERROR_CODE)
                val message = response.getString(WebParams.ERROR_MESSAGE)
                when (code) {
                    WebParams.SUCCESS_CODE -> {
                        try {
                            showReport(response)
                        } catch (e: Exception) {
                            Timber.d(e)
                        }

                    }
                    WebParams.LOGOUT_CODE -> {
                        val alertDialogLogout = AlertDialogLogout.getInstance()
                        alertDialogLogout.showDialoginActivity2(activity, message)
                    }
                    DefineValue.ERROR_9333 -> {
                        val model = gson.fromJson(response.toString(), jsonModel::class.java)
                        val appModel = model.app_data
                        val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                        alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                    }
                    DefineValue.ERROR_0066 -> {
                        val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                        alertDialogMaintenance.showDialogMaintenance(activity, message)
                    }
                    else -> {
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onError(throwable: Throwable) {
                dismissProgressDialog()
            }

            override fun onComplete() {
                dismissProgressDialog()
            }
        })
    }

    private fun showReport(response: JSONObject) {
        val ebdDocStrukReportModel = getGson().fromJson(response.toString(), EBDDocStrukReportModel::class.java)
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)

        args.putBoolean(DefineValue.IS_REPORT, true)
        args.putBoolean(DefineValue.TRX_STATUS, false)
        args.putString(DefineValue.TRX_STATUS_REMARK, getString(R.string.transaction_success))
        args.putString(DefineValue.TRX_REMARK, ebdDocStrukReportModel.paidStatusRemark)
        args.putString(DefineValue.DATE_TIME, ebdDocStrukReportModel.dueDate)
        args.putString(DefineValue.TX_ID, ebdDocStrukReportModel.docNo)
        args.putString(DefineValue.BUSS_SCHEME_CODE, DefineValue.EBD)
        args.putString(DefineValue.COMMUNITY_CODE, commCodeEspay)
        args.putString(DefineValue.STORE_CODE, memberCodeEspay)
        args.putString(DefineValue.PARTNER_CODE_ESPAY, ebdDocStrukReportModel.partnerCodeEspay)
        args.putString(DefineValue.ITEMS, response.getString(WebParams.ITEMS))
        args.putString(DefineValue.BONUS_ITEMS, response.getString(WebParams.BONUS_ITEMS))
        args.putString(DefineValue.PARTNER, response.getString(WebParams.PARTNER))
        args.putString(DefineValue.RESPONSE, response.toString())
        args.putString(DefineValue.PAID_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(ebdDocStrukReportModel.paidAmount))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(ebdDocStrukReportModel.amount))
        args.putString(DefineValue.TOTAL_DISC, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(ebdDocStrukReportModel.discountAmount))
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(ebdDocStrukReportModel.totalAmount))

        dialog.arguments = args
        dialog.show(activity!!.supportFragmentManager, ReportBillerDialog.TAG)
    }

    override fun onOkButton() {

    }
}