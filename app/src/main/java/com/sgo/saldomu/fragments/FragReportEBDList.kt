package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.AdapterReportEBDList
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DateTimeFormat
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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.frag_report_ebd_list.*
import org.json.JSONObject
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragReportEBDList(private val reportType: String, private var memberCodeEspay: String, private var commCodeEspay: String) : BaseFragment(), ReportBillerDialog.OnDialogOkCallback {

    private var adapterReportEBDList: AdapterReportEBDList? = null
    private val docListItemArray = ArrayList<DocListItem>()

    var memberCode = ""
    var oriFromDate = ""
    var oriToDate = ""

    private var dateFrom: Calendar? = null
    private var bakDateFrom: Calendar? = null
    private var dateTo: Calendar? = null
    private var bakDateTo: Calendar? = null

    private val DATEFROM = "tagFrom"
    private val DATETO = "tagTo"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_report_ebd_list, container, false)
        return v
    }

    override fun onResume() {
        super.onResume()
        filter_toggle_btn.isChecked = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        oriFromDate = DateTimeFormat.getCurrentDateMinus(365)
        oriToDate = DateTimeFormat.getCurrentDate()

        dateFrom = stringToCal(oriFromDate)
        dateTo = stringToCal(oriToDate)

        var date = """${getString(R.string.from)} :
${dateFrom?.get(Calendar.DAY_OF_MONTH)}-${dateFrom?.get(Calendar.MONTH)?.plus(1)}-${dateFrom?.get(Calendar.YEAR)}"""
        filter_date_from.text = date
        date = """${getString(R.string.to)} :
${dateTo?.get(Calendar.DAY_OF_MONTH)}-${dateTo?.get(Calendar.MONTH)?.plus(1)}-${dateTo?.get(Calendar.YEAR)}"""
        filter_date_to.text = date

        filter_date_from.setOnClickListener {
            filter_toggle_btn.isChecked = false
            val datePickerDialog = DatePickerDialog.newInstance(
                    dobPickerSetListener,
                    dateFrom!!.get(Calendar.YEAR),
                    dateFrom!!.get(Calendar.MONTH),
                    dateFrom!!.get(Calendar.DAY_OF_MONTH)
            )

            if (fragmentManager != null) {
                datePickerDialog.show(fragmentManager!!, DATEFROM)
            }
        }
        filter_date_to.setOnClickListener {
            filter_toggle_btn.isChecked = false
            val datePickerDialog = DatePickerDialog.newInstance(
                    dobPickerSetListener,
                    dateTo!!.get(Calendar.YEAR),
                    dateTo!!.get(Calendar.MONTH),
                    dateTo!!.get(Calendar.DAY_OF_MONTH)
            )

            if (fragmentManager != null) {
                datePickerDialog.show(fragmentManager!!, DATETO)
            }
        }

        filter_toggle_btn.setOnCheckedChangeListener { compoundButton, isChecked -> getDataReportEBD() }

        adapterReportEBDList = AdapterReportEBDList(context!!, docListItemArray, object : AdapterReportEBDList.OnClick {
            override fun onClick(pos: Int) {
                commCodeEspay = docListItemArray[pos].commCode
                memberCode = docListItemArray[pos].memberCode
                reportDetail(docListItemArray[pos].docNo, memberCode)
            }
        })
        recyclerView.adapter = adapterReportEBDList
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val docStatusAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.list_doc_status_bat))
        docStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_doc_status.adapter = docStatusAdapter
        spinner_doc_status.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                getDataReportEBD()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
        getDataReportEBD()
    }

    private fun getDataReportEBD() {
        showProgressDialog()
        val docStatus = spinner_doc_status.selectedItem.toString()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_LIST, memberCodeEspay)
        params[WebParams.COMM_ID] = sp.getString(DefineValue.COMMUNITY_ID, "")
        params[WebParams.USER_ID] = sp.getString(DefineValue.USERID_PHONE, "")
        params[WebParams.CUST_ID_ESPAY] = sp.getString(DefineValue.USERID_PHONE, "")
        if (sp.getBoolean(DefineValue.IS_AGENT, false))
            params[WebParams.CANVASSER_CUSTID] = sp.getString(DefineValue.USERID_PHONE, "")
        params[WebParams.MEMBER_ID] = sp.getString(DefineValue.MEMBER_ID, "")
        params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
        params[WebParams.TYPE_ID] = reportType
        when (docStatus) {
            getString(R.string.open) -> params[WebParams.DOC_STATUS] = DefineValue.OPEN
            getString(R.string.close) -> params[WebParams.DOC_STATUS] = DefineValue.CLOSE
            getString(R.string.proses) -> params[WebParams.DOC_STATUS] = DefineValue.PROCESS
            getString(R.string.rejected) -> params[WebParams.DOC_STATUS] = DefineValue.REJECTED
        }
        params[WebParams.ISSUE_DATE_FROM] = calToString(dateFrom!!)
        params[WebParams.ISSUE_DATE_TO] = calToString(dateTo!!)

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

    private fun reportDetail(docNo: String, memberCode: String) {
        showProgressDialog()
        if (sp.getBoolean(DefineValue.IS_AGENT, false) == true)
            memberCodeEspay = memberCode
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

    private fun stringToCal(src: String): Calendar? {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale("id", "INDONESIA"))
        val tempCalendar = Calendar.getInstance()
        try {
            tempCalendar.time = Objects.requireNonNull(format.parse(src))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return tempCalendar
    }

    private fun calToString(src: Calendar): String? {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale("id", "INDONESIA"))
        return format.format(src.time)
    }

    private val dobPickerSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        val date: String
        if (view.tag != null) {
            if (view.tag == DATEFROM) {
                date = """${getString(R.string.from)} :
$dayOfMonth-${monthOfYear + 1}-$year"""
                bakDateFrom = dateFrom!!.clone() as Calendar
                dateFrom!!.set(year, monthOfYear, dayOfMonth)
                filter_date_from.text = date
            } else {
                date = """${getString(R.string.to)} :
$dayOfMonth-${monthOfYear + 1}-$year"""
                bakDateTo = dateTo!!.clone() as Calendar
                dateTo!!.set(year, monthOfYear, dayOfMonth)
                filter_date_to.text = date
            }
        }
    }
}