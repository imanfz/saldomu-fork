package com.sgo.saldomu.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserGoodReceiptActivity
import com.sgo.saldomu.activities.CanvasserInvoiceActivity
import com.sgo.saldomu.adapter.ListInvAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.models.ListPOModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list_po.*
import org.json.JSONArray
import java.util.*

class FragListInvfromIN : BaseFragment(), ListInvAdapter.listener {

    private val docListArrayList = ArrayList<ListPOModel>()

    private var listInvAdapter: ListInvAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        docListArrayList.clear()

        initializeListPO()

    }


    private fun initializeListInv()
    {

        listInvAdapter = ListInvAdapter(docListArrayList, activity, this)
        recyclerViewList.adapter = listInvAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

//        search.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//
//            override fun afterTextChanged(editable: Editable) {
//                listInvAdapter!!.filter.filter(editable.toString())
//            }
//        })
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
            val listPOModel = ListPOModel()
            listPOModel.doc_no = docNo
            listPOModel.doc_status = docStatus
            listPOModel.total_amount = totalAmount
            listPOModel.due_date = dueDate
            listPOModel.cust_id = custID
            listPOModel.comm_code = commCode
            listPOModel.member_code = memberCode
            docListArrayList.add(listPOModel)
        }

        listInvAdapter!!.updateData(docListArrayList)
    }

    private fun initializeListPO()
    {

        listInvAdapter = ListInvAdapter(docListArrayList, activity, this)
        recyclerViewList.adapter = listInvAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

//        search.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//
//            override fun afterTextChanged(editable: Editable) {
//                listInvAdapter!!.filter.filter(editable.toString())
//            }
//        })
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
            val reffId = mArrayDoc.getJSONObject(i).getString(WebParams.REFF_ID)
            val reffNo = mArrayDoc.getJSONObject(i).getString(WebParams.REFF_NO)

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



            docListArrayList.add(listPOModel)
        }

        listInvAdapter!!.updateData(docListArrayList)
    }

    override fun onClick(item: ListPOModel?) {
        val bundle = Bundle()
        bundle.putString(DefineValue.DOC_NO, item!!.doc_no)
        bundle.putString(DefineValue.MEMBER_CODE_ESPAY, item.member_code)
        bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, item.comm_code)
        bundle.putString(DefineValue.CUST_ID_ESPAY, item.cust_id)
        bundle.putParcelable(DefineValue.OBJ, item)
        val frag: Fragment =  FragConInvoice()
        frag.arguments = bundle
        switchFragment(frag,"","",true, "")
    }

    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserInvoiceActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }


}