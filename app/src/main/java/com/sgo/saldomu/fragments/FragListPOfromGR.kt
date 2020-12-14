package com.sgo.saldomu.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.ListPOAdapter
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.models.ListPOModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list_po.*
import org.json.JSONArray
import java.util.*

class FragListPOfromGR : BaseFragment(), ListPOAdapter.listener {

    private val docListArrayList = ArrayList<ListPOModel>()

    private var listPOAdapter: ListPOAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list_po, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        docListArrayList.clear()

        initializeListPO()

    }

    private fun initializeListPO()
    {

        listPOAdapter = ListPOAdapter(docListArrayList, activity, this)
        recyclerViewList.adapter = listPOAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                listPOAdapter!!.filter.filter(editable.toString())
            }
        })
        val bundle = arguments

        val mArrayDoc = JSONArray(bundle!!.getString(WebParams.DOC_LIST))

        for (i in 0 until mArrayDoc.length()) {
            val docNo = mArrayDoc.getJSONObject(i).getString(WebParams.DOC_NO)
            val docStatus = mArrayDoc.getJSONObject(i).getString(WebParams.DOC_STATUS)
            val totalAmount = mArrayDoc.getJSONObject(i).getString(WebParams.TOTAL_AMOUNT)
            val dueDate = mArrayDoc.getJSONObject(i).getString(WebParams.DUE_DATE)
            val listPOModel = ListPOModel()
            listPOModel.doc_no = docNo
            listPOModel.doc_status = docStatus
            listPOModel.total_amount = totalAmount
            listPOModel.due_date = dueDate
            docListArrayList.add(listPOModel)
        }

        listPOAdapter!!.updateData(docListArrayList)
    }

    override fun onClick(item: ListPOModel?) {
        TODO("Not yet implemented")

    }


}