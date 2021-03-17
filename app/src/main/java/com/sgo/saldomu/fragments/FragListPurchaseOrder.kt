package com.sgo.saldomu.fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.ListPOTokoAdapter
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.ListPOModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list_po.*
import kotlinx.android.synthetic.main.frag_list_po.spinner_doc_status
import kotlinx.android.synthetic.main.frag_report_ebd_list.*
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragListPurchaseOrder : BaseFragment() {

    private val docTypeID = "PO"
    var memberCode: String? = null
    var commCode: String? = null
    var shopName: String? = null
    var partner: String? = ""
    var anchorCompany: String? = ""

    var itemList = ArrayList<ListPOModel>()
    var itemListAdapter: ListPOTokoAdapter? = null

    var tokoPurchaseOrderActivity: TokoPurchaseOrderActivity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        v = inflater.inflate(R.layout.frag_list_po, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity!!.initializeToolbar(getString(R.string.list_po))
        btn_create_po.visibility = View.VISIBLE
        layout_spinner.visibility = View.VISIBLE
        btn_create_po.setOnClickListener {
            val fragment = FragListItemToko()
            val bundle = Bundle()
            bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCode)
            bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCode)
            bundle.putString(DefineValue.MEMBER_SHOP_NAME, shopName)
            bundle.putString(DefineValue.PARTNER, partner)
            bundle.putString(DefineValue.ANCHOR_COMPANY, anchorCompany)
            fragment.arguments = bundle
            tokoPurchaseOrderActivity!!.switchContent(fragment, getString(R.string.choose_catalog), true, (activity as TokoPurchaseOrderActivity).FRAG_INPUT_ITEM_TAG)
        }

        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCode = arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            shopName = arguments!!.getString(DefineValue.MEMBER_SHOP_NAME, "")
            anchorCompany = arguments!!.getString(DefineValue.ANCHOR_COMPANY, "")
        }

        itemListAdapter = ListPOTokoAdapter(itemList, activity, object : ListPOTokoAdapter.listener {
            override fun onClick(docNo: String) {
                val fragment = FragPurchaseOrderDetail()
                val bundle = Bundle()
                bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCode)
                bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCode)
                bundle.putString(DefineValue.DOC_NO, docNo)
                bundle.putString(DefineValue.PARTNER, partner)
                fragment.arguments = bundle
                tokoPurchaseOrderActivity!!.switchContent(fragment, getString(R.string.detail_document), true, "FragPurchaseOrderDetail")
            }

            override fun onCancel(docNo: String) {
                cancelPO(docNo)
            }

        })
        recyclerViewList.adapter = itemListAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val docStatusAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.list_doc_status_po_bat))
        docStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_doc_status.adapter = docStatusAdapter
        spinner_doc_status.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                getPOList()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        getPOList()

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                itemListAdapter!!.filter.filter(editable.toString())
            }
        })
    }

    private fun getPOList() {
        showProgressDialog()
        val docStatus = spinner_doc_status.selectedItem.toString()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LIST_PO, memberCode + commCode)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCode
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        when (docStatus) {
            getString(R.string.open) -> params[WebParams.DOC_STATUS] = DefineValue.OPEN
            getString(R.string.proses) -> params[WebParams.DOC_STATUS] = DefineValue.PROCESS
        }
        Timber.d("isi params get $docTypeID list:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_LIST_PO, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                partner = response.getString(WebParams.PARTNER)
                                val jsonArray = response.getJSONArray(WebParams.DOC_LIST)
                                itemList.clear()
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    val docNo = jsonObject.getString(WebParams.DOC_NO)
                                    val docStatus = jsonObject.getString(WebParams.DOC_STATUS)
                                    val nettAmount = jsonObject.getString(WebParams.NETT_AMOUNT)
                                    val dueDate = jsonObject.getString(WebParams.DUE_DATE)
                                    val custID = jsonObject.getString(WebParams.CUST_ID)
                                    val memberCode = jsonObject.getString(WebParams.MEMBER_CODE)
                                    val commCode = jsonObject.getString(WebParams.COMM_CODE)
                                    val paidStatus = jsonObject.getString(WebParams.PAID_STATUS)
                                    val paidStatusRemark = jsonObject.getString(WebParams.PAID_STATUS_REMARK)
                                    val listPOModel = ListPOModel()
                                    listPOModel.doc_no = docNo
                                    listPOModel.doc_status = docStatus
                                    listPOModel.nett_amount = nettAmount
                                    listPOModel.due_date = dueDate
                                    listPOModel.cust_id = custID
                                    listPOModel.member_code = memberCode
                                    listPOModel.comm_code = commCode
                                    listPOModel.paid_status = paidStatus
                                    listPOModel.paid_status_remark = paidStatusRemark
                                    listPOModel.partner = partner
                                    itemList.add(listPOModel)
                                }
                                itemListAdapter!!.notifyDataSetChanged()
                            }
                            WebParams.LOGOUT_CODE -> {
                                AlertDialogLogout.getInstance().showDialoginActivity2(tokoPurchaseOrderActivity, message)
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


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.favorite).isVisible = false
        menu.findItem(R.id.notifications).isVisible = false
        menu.findItem(R.id.settings).isVisible = false
        menu.findItem(R.id.search).isVisible = false
        menu.findItem(R.id.cancel).isVisible = false
        menu.findItem(R.id.promo).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.promo) {
            val fragment = FragListPromo()
            val bundle = Bundle()
            bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCode)
            bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCode)
            fragment.arguments = bundle
            tokoPurchaseOrderActivity!!.switchContent(fragment, getString(R.string.list_promo), true, "FragListPromo")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity!!.menuInflater.inflate(R.menu.ab_notification, menu)
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val spanString = SpannableString(menu.getItem(i).title.toString())
            spanString.setSpan(ForegroundColorSpan(Color.WHITE), 0, spanString.length, 0) //fix the color to white
            item.title = spanString
        }
    }

    private fun cancelPO(docNo: String) {
        showProgressDialog()

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CANCEL_DOC, memberCode + commCode + docNo)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCode
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.CUST_ID] = userPhoneID
        params[WebParams.CUST_TYPE] = DefineValue.TOKO
        params[WebParams.TYPE_ID] = docTypeID
        params[WebParams.DOC_NO] = docNo
        params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE

        Timber.d("isi params cancel $docTypeID :$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CANCEL_DOC, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                showDialog(getString(R.string.order_canceled))
                            }
                            WebParams.LOGOUT_CODE -> {
                                AlertDialogLogout.getInstance().showDialoginActivity2(tokoPurchaseOrderActivity, message)
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

    private fun showDialog(msg: String) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        val btnDialog: Button = dialog.findViewById(R.id.btn_dialog_notification_ok)
        val title: TextView = dialog.findViewById(R.id.title_dialog)
        val message: TextView = dialog.findViewById(R.id.message_dialog)
        message.visibility = View.VISIBLE
        title.text = getString(R.string.remark)
        message.text = msg
        btnDialog.setOnClickListener {
            dialog.dismiss()
            getPOList()
        }
        dialog.show()
    }
}