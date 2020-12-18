package com.sgo.saldomu.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.DenomSCADMActivity
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.AdapterEBDCatalogList
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.EBDCatalogModel
import com.sgo.saldomu.models.EBDOrderModel
import com.sgo.saldomu.models.FormatQtyItem
import com.sgo.saldomu.models.MappingItemsItem
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_input_item_list.*
import kotlinx.android.synthetic.main.item_search_contact.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragListItemToko : BaseFragment() {

    var memberCode = ""
    var commCode = ""
    var paymentOption = ""

    val itemList = ArrayList<EBDCatalogModel>()
    private val order = EBDOrderModel()
    private val mappingItemList = ArrayList<MappingItemsItem>()
    private val paymentListOption = ArrayList<String>()
    var itemListAdapter: AdapterEBDCatalogList? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_input_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity.initializeToolbar(getString(R.string.choose_catalog))
        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE, "")
            commCode = arguments!!.getString(DefineValue.COMMUNITY_CODE, "")
        }

        layout_payment_method.visibility = View.VISIBLE
        itemListAdapter = AdapterEBDCatalogList(context!!, itemList, object : AdapterEBDCatalogList.Listener {
            override fun onChangeQty(itemCode: String, itemName: String, qty: Int, price: Int, unit: String, qtyType: String) {
                if (mappingItemList.size == 0)
                    addOrder(itemCode, itemName, price, qty, unit, qtyType)
                else {
                    for (i in mappingItemList.indices) {
                        if (mappingItemList[i].item_code == itemCode) {
                            val mappingItemFormatQty = mappingItemList[i].format_qty
                            if (qty != 0) {
                                when (qtyType) {
                                    DefineValue.BAL -> mappingItemFormatQty[0].mapping_qty = qty
                                    DefineValue.SLOP -> mappingItemFormatQty[1].mapping_qty = qty
                                    DefineValue.PACK -> mappingItemFormatQty[2].mapping_qty = qty
                                }
                            } else
                                mappingItemList.removeAt(i)
                            break
                        }
                        if (i == mappingItemList.size - 1)
                            addOrder(itemCode, itemName, price, qty, unit, qtyType)
                    }
                }
                Timber.e(order.toString())
            }
        })

        paymentListOption.add(getString(R.string.pay_now))
        paymentListOption.add(getString(R.string.pay_later))
        val paymentOptionsAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, paymentListOption)
        paymentOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_payment_options.adapter = paymentOptionsAdapter
        spinner_payment_options.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                paymentOption = paymentListOption[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        frag_input_item_list_field.adapter = itemListAdapter
        frag_input_item_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(frag_input_item_list_field)

        getCatalogList()

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                itemListAdapter!!.filter.filter(editable.toString())
            }
        })
        frag_input_item_submit_btn.setOnClickListener {
            if (checkInput()) {
                frag_input_item_list_field.scrollTo(0, 0)
                val frag = FragOrderConfirmToko()

                val bundle = Bundle()
                bundle.putString(DefineValue.MEMBER_CODE, memberCode)
                bundle.putString(DefineValue.COMMUNITY_CODE, commCode)

                frag.arguments = bundle
                tokoPurchaseOrderActivity.switchContent(frag, getString(R.string.purchase_order), true, "FragOrderConfirmToko")
                addFragment(frag, DenomSCADMActivity.DENOM_PAYMENT)
            }
        }
    }

    private fun addOrder(itemCode: String, itemName: String, price: Int, qty: Int, unit: String, qtyType: String) {
        if (qty != 0) {
            val mappingItem = MappingItemsItem()
            mappingItem.item_code = itemCode
            mappingItem.item_name = itemName
            mappingItem.price = price
            mappingItem.unit = unit
            val formatQtyItemList = ArrayList<FormatQtyItem>()
            formatQtyItemList.add(0, FormatQtyItem(DefineValue.BAL, 0))
            formatQtyItemList.add(1, FormatQtyItem(DefineValue.SLOP, 0))
            formatQtyItemList.add(2, FormatQtyItem(DefineValue.PACK, 0))
            when (qtyType) {
                DefineValue.BAL -> formatQtyItemList[0].mapping_qty = qty
                DefineValue.SLOP -> formatQtyItemList[1].mapping_qty = qty
                DefineValue.PACK -> formatQtyItemList[2].mapping_qty = qty
            }
            mappingItem.format_qty = formatQtyItemList
            mappingItemList.add(mappingItem)
            order.reff_no = ""
            order.mapping_items = mappingItemList
        }
    }

    private fun getCatalogList() {
        try {
            showProgressDialog()

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_CATALOG_EBD, memberCode + commCode)
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.MEMBER_CODE_ESPAY] = memberCode
            params[WebParams.COMM_CODE_ESPAY] = commCode
            params[WebParams.CUST_ID_ESPAY] = userPhoneID

            Timber.d("isi params get catalog list:$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_CATALOG_EBD, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val code = response.getString(WebParams.ERROR_CODE)
                                val message = response.getString(WebParams.ERROR_MESSAGE)
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        val jsonArray = response.getJSONArray(WebParams.ITEMS)
                                        for (i in 0 until jsonArray.length()) {
                                            val jsonObject = jsonArray.getJSONObject(i)
                                            val itemCode = jsonObject.getString(WebParams.ITEM_CODE)
                                            val itemName = jsonObject.getString(WebParams.ITEM_NAME)
                                            val price = jsonObject.getInt(WebParams.PRICE)
                                            val unit = jsonObject.getString(WebParams.UNIT)
                                            val minQty = jsonObject.getInt(WebParams.MIN_QTY)
                                            val maxQty = jsonObject.getInt(WebParams.MAX_QTY)
                                            itemList.add(EBDCatalogModel(itemCode, itemName, price, unit, minQty, maxQty))
                                        }
                                        itemListAdapter!!.notifyDataSetChanged()
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
                                        showDialog(message)
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {
                            dismissProgressDialog()
                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: Exception) {
            e.printStackTrace();
            Timber.d("httpclient:%s", e.message)
        }

    }

    private fun showDialog(msg: String) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        val btnDialogOTP: Button = dialog.findViewById(R.id.btn_dialog_notification_ok)
        val title: TextView = dialog.findViewById(R.id.title_dialog)
        val message: TextView = dialog.findViewById(R.id.message_dialog)
        message.visibility = View.VISIBLE
        title.text = getString(R.string.error)
        message.text = msg
        btnDialogOTP.setOnClickListener {
            dialog.dismiss()
            fragmentManager!!.popBackStack()
        }
        dialog.show()
    }

    private fun checkInput(): Boolean {
        val parentArr = JSONArray()
        val mappingItemArray = JSONArray()
        val parentObj = JSONObject()

        parentObj.put(WebParams.REFF_NO, order.reff_no)
        for (i in mappingItemList.indices) {
            val mappingItemObj = JSONObject()
            val formatQtyArray = JSONArray()

            val orderMappingItemsFormatQty = mappingItemList[i].format_qty
            for (j in orderMappingItemsFormatQty.indices) {
                val formatQtyObj = JSONObject()
                formatQtyObj.put(WebParams.MAPPING_UNIT, orderMappingItemsFormatQty[j].mapping_unit)
                formatQtyObj.put(WebParams.MAPPING_QTY, orderMappingItemsFormatQty[j].mapping_qty)
                formatQtyArray.put(formatQtyObj)
            }

            mappingItemObj.put(WebParams.ITEM_NAME, mappingItemList[i].item_name)
            mappingItemObj.put(WebParams.ITEM_CODE, mappingItemList[i].item_code)
            mappingItemObj.put(WebParams.PRICE, mappingItemList[i].price)
            mappingItemObj.put(WebParams.UNIT, mappingItemList[i].unit)
            mappingItemObj.put(WebParams.FORMAT_QTY, formatQtyArray)
            mappingItemArray.put(mappingItemObj)
        }

        parentObj.put(WebParams.MAPPING_ITEMS, mappingItemArray)
        parentArr.put(parentObj)
        Timber.e(parentArr.toString())
        return false
    }
}