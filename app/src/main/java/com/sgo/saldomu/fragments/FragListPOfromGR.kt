package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.Beans.SCADMCommunityModel
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.ListPOAdapter
import com.sgo.saldomu.adapter.ListTopUpSCADMAdapter
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.models.ListPOModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list.*
import org.json.JSONArray
import java.util.*

class FragListPOfromGR : BaseFragment(), ListPOAdapter.listener {

    private val docListArrayList = ArrayList<ListPOModel>()

    private var listPOAdapter: ListPOAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list, container, false)
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
        recyclerViewList.setAdapter(listPOAdapter)
        recyclerViewList.setLayoutManager(LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false))

        val bundle = arguments

        val mArrayDoc = JSONArray(bundle!!.getString(WebParams.DOC_LIST))

        for (i in 0 until mArrayDoc.length()) {
            val doc_no = mArrayDoc.getJSONObject(i).getString(WebParams.DOC_NO)
            val doc_status = mArrayDoc.getJSONObject(i).getString(WebParams.DOC_STATUS)
            val total_amount = mArrayDoc.getJSONObject(i).getString(WebParams.TOTAL_AMOUNT)
            val due_date = mArrayDoc.getJSONObject(i).getString(WebParams.DUE_DATE)
            val listPOModel = ListPOModel()
            listPOModel.doc_no = doc_no
            listPOModel.doc_status = doc_status
            listPOModel.total_amount = total_amount
            listPOModel.due_date = due_date
            docListArrayList.add(listPOModel)
        }

        listPOAdapter!!.updateData(docListArrayList)
    }

    override fun onClick(item: ListPOModel?) {
        TODO("Not yet implemented")

    }


}