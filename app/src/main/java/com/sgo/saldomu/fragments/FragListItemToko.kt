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
import com.sgo.saldomu.R
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
import com.sgo.saldomu.models.DocDetailsItem
import com.sgo.saldomu.models.EBDCatalogModel
import com.sgo.saldomu.models.FormatQtyItem
import com.sgo.saldomu.models.MappingItemsItem
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_input_item_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragListItemToko : BaseFragment() {

    var memberCode = ""
    var commCode = ""
    var paymentOption = ""

    val itemList = ArrayList<EBDCatalogModel>()
    private val order = DocDetailsItem()
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
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCode = arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
        }

        itemListAdapter = AdapterEBDCatalogList(context!!, itemList, object : AdapterEBDCatalogList.Listener {
            override fun onChangeQty(itemCode: String, qty: Int, qtyType: String) {

                for (i in itemList.indices) {
                    if (itemCode == itemList[i].itemCode) {
                        val mappingItemsItem = MappingItemsItem()
                        mappingItemsItem.item_code = itemList[i].itemCode
                        mappingItemsItem.item_name = itemList[i].itemName
                        mappingItemsItem.price = itemList[i].price
                        mappingItemsItem.unit = itemList[i].unit
                        val formatQtyItemList = ArrayList<FormatQtyItem>()

                        when {
                            qtyType == DefineValue.BAL -> formatQtyItemList.add(0, FormatQtyItem(DefineValue.BAL, qty))
                            itemList[i].formatQtyItem.isNotEmpty() -> formatQtyItemList.add(0, FormatQtyItem(DefineValue.BAL, itemList[i].formatQtyItem[0].mapping_qty))
                            else -> formatQtyItemList.add(0, FormatQtyItem(DefineValue.BAL, 0))
                        }

                        when {
                            qtyType == DefineValue.SLOP -> formatQtyItemList.add(1, FormatQtyItem(DefineValue.SLOP, qty))
                            itemList[i].formatQtyItem.isNotEmpty() -> formatQtyItemList.add(1, FormatQtyItem(DefineValue.SLOP, itemList[i].formatQtyItem[1].mapping_qty))
                            else -> formatQtyItemList.add(1, FormatQtyItem(DefineValue.SLOP, 0))
                        }

                        when {
                            qtyType == DefineValue.PACK -> formatQtyItemList.add(2, FormatQtyItem(DefineValue.PACK, qty))
                            itemList[i].formatQtyItem.isNotEmpty() -> formatQtyItemList.add(2, FormatQtyItem(DefineValue.PACK, itemList[i].formatQtyItem[2].mapping_qty))
                            else -> formatQtyItemList.add(2, FormatQtyItem(DefineValue.PACK, 0))
                        }

                        val qtyBAL = formatQtyItemList[0].mapping_qty
                        val qtySLOP = formatQtyItemList[1].mapping_qty
                        val qtyPACK = formatQtyItemList[2].mapping_qty
                        mappingItemsItem.format_qty = formatQtyItemList


                        if (qtyBAL == 0 && qtySLOP == 0 && qtyPACK == 0) {
                            itemList[i].formatQtyItem.clear()
                            for (j in mappingItemList.indices) {
                                if (itemList[i].itemCode == mappingItemList[j].item_code)
                                    mappingItemList.removeAt(j)
                            }
                        } else {
                            if (mappingItemList.size == 0)
                                mappingItemList.add(mappingItemsItem)
                            else
                                for (j in mappingItemList.indices) {
                                    if (itemList[i].itemCode == mappingItemList[j].item_code)
                                        mappingItemList[j].format_qty = formatQtyItemList
                                    else if (j == mappingItemList.size - 1)
                                        mappingItemList.add(mappingItemsItem)
                                }
                            itemList[i].formatQtyItem = formatQtyItemList
                        }


//                        for (j in mappingItemList.indices) {
//                            if (mappingItemList[j].item_code == itemCode) {
//                                val mappingItemFormatQty = mappingItemList[j].format_qty
//                                val itemListFormatQty = itemList[i].formatQtyItem
//                                if (qty != 0) {
//                                    when (qtyType) {
//                                        DefineValue.BAL -> {
//                                            mappingItemFormatQty[0].mapping_qty = qty
//                                            itemListFormatQty[0].mapping_qty = qty
//                                        }
//                                        DefineValue.SLOP -> {
//                                            mappingItemFormatQty[1].mapping_qty = qty
//                                            itemListFormatQty[1].mapping_qty = qty
//                                        }
//                                        DefineValue.PACK -> {
//                                            mappingItemFormatQty[2].mapping_qty = qty
//                                            itemListFormatQty[2].mapping_qty = qty
//                                        }
//                                    }
//                                } else
//                                    mappingItemList.removeAt(j)
//                                break
//                            }
//
//                        }
                    }
                }
            }
        })

        frag_input_item_list_field.adapter = itemListAdapter
        frag_input_item_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        layout_payment_method.visibility = View.VISIBLE
        paymentListOption.clear()
        paymentListOption.add(getString(R.string.pay_now))
        paymentListOption.add(getString(R.string.pay_later))
        val paymentOptionsAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, paymentListOption)
        paymentOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_payment_options.adapter = paymentOptionsAdapter
        spinner_payment_options.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        paymentOption = paymentListOption[p2]
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }

        getCatalogList()

        search.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                    override fun afterTextChanged(editable: Editable) {
                        itemListAdapter!!.filter.filter(editable.toString())
                    }
                })
        frag_input_item_submit_btn.setOnClickListener {
            val docDetail = createJSONDocDetail()
            val frag = if (paymentOption == getString(R.string.pay_now)) FragInputPromoCodeToko() else FragOrderConfirmToko()
            val fragName = if (paymentOption == getString(R.string.pay_now)) getString(R.string.promo_code) else getString(R.string.purchase_order_confirmation)

            val bundle = Bundle()
            bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCode)
            bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCode)
            bundle.putString(DefineValue.PAYMENT_OPTION, paymentOption)
            bundle.putString(DefineValue.DOC_DETAILS, docDetail)

            frag.arguments = bundle
            tokoPurchaseOrderActivity.addFragment(frag, fragName, tokoPurchaseOrderActivity.FRAG_INPUT_ITEM_TAG)
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
                                        itemList.clear()
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

    private fun confirmationDoc() {
        showProgressDialog()
        val docDetail = createJSONDocDetail()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRMATION_DOC, memberCode + userPhoneID)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCode
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.CUST_ID] = userPhoneID
        params[WebParams.REFF_ID] = order.reff_no
        params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
        params[WebParams.DOC_DETAIL] = docDetail
        params[WebParams.TYPE_ID] = DefineValue.PO
        params[WebParams.CUST_TYPE] = DefineValue.TOKO

        Timber.d("isi params confirm doc :$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRMATION_DOC, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                frag_input_item_list_field.scrollTo(0, 0)
                                val frag = FragOrderConfirmToko()

                                val bundle = Bundle()
                                bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCode)
                                bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCode)
                                bundle.putString(DefineValue.PAYMENT_OPTION, paymentOption)
                                bundle.putString(DefineValue.DOC_DETAILS, docDetail)
                                bundle.putString(DefineValue.EBD_CONFIRM_DATA, response.toString())

                                frag.arguments = bundle
                                (activity as TokoPurchaseOrderActivity).addFragment(frag, getString(R.string.purchase_order_confirmation), (activity as TokoPurchaseOrderActivity).FRAG_INPUT_ITEM_TAG)
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
                    }

                    override fun onError(throwable: Throwable?) {
                        dismissProgressDialog()
                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                    }

                })
    }

    private fun createJSONDocDetail(): String {
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
        Timber.e("doc_detail : $parentArr")
        return parentArr.toString()
    }
}