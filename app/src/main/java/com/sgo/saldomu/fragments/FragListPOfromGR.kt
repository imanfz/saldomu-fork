package com.sgo.saldomu.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserGoodReceiptActivity
import com.sgo.saldomu.activities.CanvasserPOActivity
import com.sgo.saldomu.adapter.ListPOAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.models.ListPOModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list_po.*
import org.json.JSONArray
import java.util.*

class FragListPOfromGR : BaseFragment(), ListPOAdapter.listener {

    private val docListArrayList = ArrayList<ListPOModel>()
    private var listPOAdapter: ListPOAdapter? = null
    var bundle = Bundle()
    private var partner =""
    private var partnerCodeEspay =""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list_po, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bundle = arguments!!
        if (bundle != null) {
            partner = bundle.getString(DefineValue.PARTNER, "")
            partnerCodeEspay = bundle.getString(DefineValue.PARTNER_CODE_ESPAY, "")
        }
        docListArrayList.clear()

        val canvasserGoodReceiptActivity = activity as CanvasserGoodReceiptActivity
        canvasserGoodReceiptActivity.initializeToolbar(getString(R.string.list_po))

        initializeListPO()

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                listPOAdapter!!.filter.filter(editable.toString())
            }
        })
    }

    private fun initializeListPO()
    {

        listPOAdapter = ListPOAdapter(docListArrayList, activity, this)
        recyclerViewList.adapter = listPOAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val bundle = arguments

        val mArrayDoc = JSONArray(bundle!!.getString(WebParams.DOC_LIST))

        for (i in 0 until mArrayDoc.length()) {
            val docNo = mArrayDoc.getJSONObject(i).getString(WebParams.DOC_NO)
            val docStatus = mArrayDoc.getJSONObject(i).getString(WebParams.DOC_STATUS)
            val totalAmount = mArrayDoc.getJSONObject(i).getString(WebParams.TOTAL_AMOUNT)
            val dueDate = mArrayDoc.getJSONObject(i).getString(WebParams.DUE_DATE)
            val custID = mArrayDoc.getJSONObject(i).getString(WebParams.CUST_ID)
            val memberCode = mArrayDoc.getJSONObject(i).getString(WebParams.MEMBER_CODE)
            val commCode = mArrayDoc.getJSONObject(i).getString(WebParams.COMM_CODE)
            val paidStatus = mArrayDoc.getJSONObject(i).getString(WebParams.PAID_STATUS_REMARK)
            val listPOModel = ListPOModel()
            listPOModel.doc_no = docNo
            listPOModel.doc_status = docStatus
            listPOModel.total_amount = totalAmount
            listPOModel.due_date = dueDate
            listPOModel.cust_id = custID
            listPOModel.comm_code = commCode
            listPOModel.member_code = memberCode
            listPOModel.paid_status = paidStatus
            listPOModel.partner = partner
            docListArrayList.add(listPOModel)
        }

        listPOAdapter!!.updateData(docListArrayList)
    }

    override fun onClick(item: ListPOModel?) {
        if (!item!!.doc_status.equals("Close"))
        {
            val bundle = Bundle()
            bundle.putString(DefineValue.DOC_NO, item.doc_no)
            bundle.putString(DefineValue.MEMBER_CODE_ESPAY, item.member_code)
            bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, item.comm_code)
            bundle.putString(DefineValue.CUST_ID_ESPAY, item.cust_id)
            bundle.putString(DefineValue.PARTNER, item.partner)
            bundle.putString(DefineValue.PARTNER_CODE_ESPAY, partnerCodeEspay)
            val frag: Fragment = FragInputQtyGoodReceipt()
            frag.arguments = bundle
            switchFragment(frag,"","",true, "")
        }else{
            Toast.makeText(activity, getString(R.string.gr_closed), Toast.LENGTH_LONG).show()
        }
    }

    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserGoodReceiptActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }


}