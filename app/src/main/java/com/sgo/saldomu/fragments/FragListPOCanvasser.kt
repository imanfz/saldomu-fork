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
import com.sgo.saldomu.activities.CanvasserPOActivity
import com.sgo.saldomu.adapter.ListPOAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.models.ListPOModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list_po.*
import org.json.JSONArray
import java.util.ArrayList

class FragListPOCanvasser : BaseFragment(), ListPOAdapter.listener {
    var memberCodeEspay = ""
    var commCodeEspay = ""
    var custIdEspay = ""
    var partner = ""
    var bundle = Bundle()

    private val docListArrayList = ArrayList<ListPOModel>()

    private var listPOAdapter: ListPOAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list_po, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bundle = arguments!!

        val canvasserPOActivity = activity as CanvasserPOActivity
        canvasserPOActivity.initializeToolbar(getString(R.string.list_po))

        btn_create_po.visibility = View.VISIBLE

        if (bundle != null) {
            memberCodeEspay = bundle.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCodeEspay = bundle.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            custIdEspay = bundle.getString(DefineValue.CUST_ID_ESPAY, "")
            partner = bundle.getString(DefineValue.PARTNER, "")
        }

        initializeListPO()

//        recyclerViewList.addOnScrollListener(object : RecyclerView.OnScrollListener(){
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//
//                // 3 lines below are not needed.
//                Log.d("TAG","Last visible item is: ${gridLayoutManager.findLastVisibleItemPosition()}")
//                Log.d("TAG","Item count is: ${gridLayoutManager.itemCount}")
//                Log.d("TAG","end? : ${gridLayoutManager.findLastVisibleItemPosition() == gridLayoutManager.itemCount-1}")
//
//                if(gridLayoutManager.findLastVisibleItemPosition() == gridLayoutManager.itemCount-1){
//                    // We have reached the end of the recycler view.
//                }
//
//                super.onScrolled(recyclerView, dx, dy)
//            }
//        })

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                listPOAdapter!!.filter.filter(editable.toString())
            }
        })

        btn_create_po.setOnClickListener {
            val fragment = FragListItemPOCanvasser()
            bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCodeEspay)
            bundle.putString(DefineValue.CUST_ID_ESPAY, custIdEspay)
            bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCodeEspay)
            bundle.putString(DefineValue.PARTNER, partner)
            fragment.arguments = bundle
            switchFragment(fragment, "", "", true, "")
        }

    }

    private fun initializeListPO() {

        listPOAdapter = ListPOAdapter(docListArrayList, activity, this)
        recyclerViewList.adapter = listPOAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val bundle = arguments

        docListArrayList.clear()

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
        val bundle = Bundle()
        bundle.putString(DefineValue.DOC_NO, item!!.doc_no)
        bundle.putString(DefineValue.MEMBER_CODE_ESPAY, item!!.member_code)
        bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, item!!.comm_code)
        bundle.putString(DefineValue.TYPE, DefineValue.CANVASSER)

        val fragment = FragPurchaseOrderDetail()
        fragment.arguments = bundle
        switchFragment(fragment, "", "", true, "")
    }

    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserPOActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }
}