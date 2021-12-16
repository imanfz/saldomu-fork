package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserGoodReceiptActivity
import com.sgo.saldomu.adapter.BonusItemGoodReceiptAdapter
import com.sgo.saldomu.adapter.UpdateProductGoodReceiptAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.EBDCatalogModel
import com.sgo.saldomu.models.FormatQtyItem
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list.*
import kotlinx.android.synthetic.main.frag_list_po.recyclerViewList
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragInputQtyGoodReceipt() : BaseFragment(), UpdateProductGoodReceiptAdapter.UpdateProductGoodReceiptListener, BonusItemGoodReceiptAdapter.bonusItemGoodReceiptListener {

    var memberCodeEspay: String = ""
    var commCodeEspay: String = ""
    var custIdEspay: String = ""
    var docNo: String = ""
    var tempGson = ""
    var gson = ""
    var bonusItems = ""
    var partner = ""
    var partnerCodeEspay = ""

    private val itemArrayList = ArrayList<EBDCatalogModel>()
    private val bonusItemArrayList = ArrayList<EBDCatalogModel>()

    private var updateProductGoodReceiptAdapter: UpdateProductGoodReceiptAdapter? = null
    private var bonusItemGoodReceiptAdapter: BonusItemGoodReceiptAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val bundle = arguments

        val canvasserGoodReceiptActivity = activity as CanvasserGoodReceiptActivity
        canvasserGoodReceiptActivity.initializeToolbar(getString(R.string.detail_document))

        memberCodeEspay = bundle!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
        commCodeEspay = bundle!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
        custIdEspay = bundle!!.getString(DefineValue.CUST_ID_ESPAY, "")
        docNo = bundle!!.getString(DefineValue.DOC_NO, "")
        partner = bundle!!.getString(DefineValue.PARTNER, "")
        partnerCodeEspay = bundle!!.getString(DefineValue.PARTNER_CODE_ESPAY, "")


        getDetail()

        btn_add_item.setOnClickListener {
            val temp = ArrayList<HashMap<String, Any>>()

            temp.add(setMappingItemsHashMap())

            val gson = Gson()
            tempGson = gson.toJson(temp)

            bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCodeEspay)
            bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCodeEspay)
            bundle.putString(DefineValue.CUST_ID_ESPAY, custIdEspay)
            bundle.putString(DefineValue.DOC_DETAILS, tempGson)
            bundle.putString(DefineValue.DOC_NO, docNo)
            bundle.putString(DefineValue.BONUS_ITEMS, bonusItems)
            bundle.putString(DefineValue.PARTNER, partner)
            bundle.putString(DefineValue.PARTNER_CODE_ESPAY, partnerCodeEspay)
            val frag: Fragment = FragListAddItemGRCanvasser()
            frag.arguments = bundle
            switchFragment(frag, "", "", true, "")
        }


        btn_proses_gr.setOnClickListener {
//            confirmDocument()
            val bundle = Bundle()
            val temp = ArrayList<HashMap<String, Any>>()

            temp.add(setMappingItemsHashMap())

            val gson = Gson()
            tempGson = gson.toJson(temp)

            bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCodeEspay)
            bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCodeEspay)
            bundle.putString(DefineValue.CUST_ID_ESPAY, custIdEspay)
            bundle.putString(DefineValue.DOC_DETAILS, tempGson)
            bundle.putString(DefineValue.BONUS_ITEMS, bonusItems)
            bundle.putString(DefineValue.DOC_NO, docNo)
            bundle.putString(DefineValue.PARTNER, partner)
            bundle.putString(DefineValue.PARTNER_CODE_ESPAY, partnerCodeEspay)

            //selain ERATEL skip promo (kebutuhan demo)
            if (partnerCodeEspay.contains("ERATEL")) {
                val frag: Fragment = FragInputPromoCodeGRCanvasser()
                frag.arguments = bundle
                switchFragment(frag, "", "", true, "")
            } else {
                confirmDocument()
            }
        }
    }

    private fun getDetail() {
        try {
            showProgressDialog()
            extraSignature = memberCodeEspay + commCodeEspay + docNo
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_DETAIL, extraSignature)
            params[WebParams.DOC_NO] = docNo
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            params[WebParams.CUST_ID_ESPAY] = custIdEspay
            params[WebParams.USER_ID] = userPhoneID
            Timber.d("params inquiry doc detail:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_DETAIL, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response inquiry doc detail:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        tv_item_list.visibility = View.VISIBLE
                                        btn_proses_gr.visibility = View.VISIBLE
//                                        btn_add_item.visibility = View.VISIBLE
                                        val items = response.optString(WebParams.ITEMS)
                                        bonusItems = response.getString(WebParams.BONUS_ITEMS)
                                        itemArrayList.clear()
                                        initializeListProduct(items)
                                        if (bonusItems != "") {
                                            bonusItemArrayList.clear()
                                            layout_bonus_item.visibility = View.VISIBLE
                                            initializeListProductBonus(bonusItems)
                                        }
                                    }
                                    WebParams.LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
                                        val message = response.getString(WebParams.ERROR_MESSAGE)
                                        val test = AlertDialogLogout.getInstance()
                                        test.showDialoginActivity(activity, message)
                                    }
                                    DefineValue.ERROR_9333 -> {
                                        Timber.d("isi response app data:%s", model.app_data)
                                        val appModel = model.app_data
                                        val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                        alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                    }
                                    DefineValue.ERROR_0066 -> {
                                        Timber.d("isi response maintenance:$response")
                                        val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                        alertDialogMaintenance.showDialogMaintenance(activity)
                                    }
                                    else -> {
                                        Timber.d("isi error inquiry doc detail:$response")
                                        Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {}
                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: java.lang.Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    fun initializeListProduct(items: String) {
        updateProductGoodReceiptAdapter = UpdateProductGoodReceiptAdapter(activity, itemArrayList, this)
        recyclerViewList.adapter = updateProductGoodReceiptAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val mArrayItem = JSONArray(items)

        for (i in 0 until mArrayItem.length()) {
            val itemImage =mArrayItem.getJSONObject(i).getString(WebParams.IMAGE_URL)
            val itemName = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_NAME)
            val itemCode = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_CODE)
            val price = mArrayItem.getJSONObject(i).getInt(WebParams.PRICE)
            val discAmount = mArrayItem.getJSONObject(i).getInt(WebParams.DISC_AMOUNT)
            val nettPrice = mArrayItem.getJSONObject(i).getInt(WebParams.NETT_PRICE)
            val unit = mArrayItem.getJSONObject(i).getString(WebParams.UNIT)
            val description = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_DESCRIPTION)
            val minQty = 0
            val maxQty = 0
            val remarkMappingUnit = mArrayItem.getJSONObject(i).getJSONArray(WebParams.REMARK_MAPPING_UNITS)
            val listRemarkMappingUnit = ArrayList<String>()
            for (j in 0 until remarkMappingUnit.length()) {
                listRemarkMappingUnit.add(remarkMappingUnit[j].toString())
            }
            val formatQtyJsonArray = mArrayItem.getJSONObject(i).getJSONArray(WebParams.FORMAT_QTY)
            var formatQtys = ArrayList<FormatQtyItem>()
            for (i in 0 until formatQtyJsonArray.length()) {
                var mappingUnit = formatQtyJsonArray.getJSONObject(i).getString(WebParams.MAPPING_UNIT)
                var mappingQty = formatQtyJsonArray.getJSONObject(i).getInt(WebParams.MAPPING_QTY)
                var formatQty = FormatQtyItem(mappingUnit, mappingQty)
                formatQtys.add(formatQty)
            }
            var isFavorite = false

            itemArrayList.add(EBDCatalogModel(itemImage, itemCode, itemName, description, price, discAmount, nettPrice, unit, minQty, maxQty, listRemarkMappingUnit, isFavorite, formatQtys))
        }

        updateProductGoodReceiptAdapter!!.updateData(itemArrayList)
    }

    fun initializeListProductBonus (bonusItem: String) {
        bonusItemGoodReceiptAdapter = BonusItemGoodReceiptAdapter(activity, bonusItemArrayList, this)
        recyclerViewListBonusItems.adapter = bonusItemGoodReceiptAdapter
        recyclerViewListBonusItems.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val mArrayItem = JSONArray(bonusItem)

        for (i in 0 until mArrayItem.length()) {
            val itemImage = ""
            val itemName = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_NAME)
            val itemCode = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_CODE)
            val price = mArrayItem.getJSONObject(i).getInt(WebParams.PRICE)
            val discAmount = mArrayItem.getJSONObject(i).getInt(WebParams.DISC_AMOUNT)
            val nettPrice = mArrayItem.getJSONObject(i).getInt(WebParams.NETT_PRICE)
            val unit = mArrayItem.getJSONObject(i).getString(WebParams.UNIT)
            val description = ""
            val minQty = 0
            val maxQty = 0
            val remarkMappingUnit = mArrayItem.getJSONObject(i).getJSONArray(WebParams.REMARK_MAPPING_UNITS)
            val listRemarkMappingUnit = ArrayList<String>()
            for (j in 0 until remarkMappingUnit.length()) {
                listRemarkMappingUnit.add(remarkMappingUnit[j].toString())
            }

            val formatQtyJsonArray = mArrayItem.getJSONObject(i).getJSONArray(WebParams.FORMAT_QTY)
            var formatQtys = ArrayList<FormatQtyItem>()
            for (i in 0 until formatQtyJsonArray.length()) {
                var mappingUnit = formatQtyJsonArray.getJSONObject(i).getString(WebParams.MAPPING_UNIT)
                var mappingQty = formatQtyJsonArray.getJSONObject(i).getInt(WebParams.MAPPING_QTY)
                var formatQty = FormatQtyItem(mappingUnit, mappingQty)
                formatQtys.add(formatQty)
            }
            var isFavorite = false
            bonusItemArrayList.add(EBDCatalogModel(itemImage, itemCode, itemName, description, price, discAmount, nettPrice, unit, minQty, maxQty, listRemarkMappingUnit, isFavorite, formatQtys))
        }

        bonusItemGoodReceiptAdapter!!.updateData(bonusItemArrayList)
    }

//    fun getDocDetail(temp: ArrayList<ItemModel>): JSONArray? {
//        val jsonArray = JSONArray()
//        try {
//            for (obj in itemArrayList) {
//                val jsonObject = JSONObject()
//                jsonObject.put("item_name", obj.item_name)
//                jsonObject.put("item_code", obj.item_code)
//                jsonObject.put("price", obj.price!!.toInt())
//                jsonObject.put("unit", obj.unit)
//                val formatQtyArrayList = ArrayList<FormatQty>()
//                for (formatQty in obj.format_qty!!) {
//                    val formatQtyTemp = FormatQty()
//                    formatQtyTemp.mapping_qty = formatQty.mapping_qty
//                    formatQtyTemp.mapping_unit = formatQty.mapping_unit
//                    formatQtyArrayList.add(formatQtyTemp)
//                }
//                jsonObject.put("format_qty", formatQtyArrayList)
//
//                jsonArray.put(jsonObject)
//
//                temp.add(obj)
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//        return jsonArray
//    }

    fun setMappingItemsHashMap(): HashMap<String, Any> {
        val finalMappingItemsHashMap = HashMap<String, Any>()
        val mappingItemsArrayList = ArrayList<HashMap<String, Any>>()
        try {
            for (obj in updateProductGoodReceiptAdapter!!.itemList) {
                val mappingItemsHashMap = HashMap<String, Any>()
                mappingItemsHashMap["item_name"] = obj.itemName
                mappingItemsHashMap["item_code"] = obj.itemCode
                mappingItemsHashMap["price"] = obj.price
                mappingItemsHashMap["unit"] = obj.unit
                mappingItemsHashMap["disc_amount"] = obj.discAmount
                mappingItemsHashMap["nett_price"] = obj.nettPrice
                mappingItemsHashMap["image_url"] = obj.itemImage
                mappingItemsHashMap["item_description"] = obj.description
                mappingItemsHashMap["is_favorite"] = obj.isFavorite
                mappingItemsHashMap["remark_mapping_units"] = obj.remarkMappingUnit

                val formatQtyArrayList = ArrayList<HashMap<String, Any>>()
                for (formatQty in obj.formatQtyItem) {
                    val formatQtyHashMap = HashMap<String, Any>()
                    formatQtyHashMap["mapping_qty"] = formatQty.mapping_qty
                    formatQtyHashMap["mapping_unit"] = formatQty.mapping_unit
                    formatQtyArrayList.add(formatQtyHashMap)
                }
                mappingItemsHashMap["format_qty"] = formatQtyArrayList
                mappingItemsArrayList.add(mappingItemsHashMap)
                finalMappingItemsHashMap.put("mapping_items", mappingItemsArrayList)
                finalMappingItemsHashMap.put("reff_no", docNo)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return finalMappingItemsHashMap
    }

    private fun confirmDocument() {
        try {
            showProgressDialog()

//            val docArrayList: JSONArray? = getDocDetail(temp)


            extraSignature = memberCodeEspay + custIdEspay
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRMATION_DOC, extraSignature)
            params[WebParams.DOC_NO] = docNo
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            params[WebParams.CUST_ID_ESPAY] = custIdEspay
            params[WebParams.CUST_ID] = userPhoneID
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.TYPE_ID] = DefineValue.GR
//            params[WebParams.DOC_DETAIL] = docArrayList
            params[WebParams.DOC_DETAIL] = tempGson
            params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
            params[WebParams.CUST_TYPE] = DefineValue.CANVASSER
            params[WebParams.INVOICE_NOTE] = ""
            params[WebParams.PARTNER_CODE_ESPAY] = partnerCodeEspay
            Timber.d("params confirm doc:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRMATION_DOC, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response confirm doc:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        val bundle = Bundle()
                                        bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCodeEspay)
                                        bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCodeEspay)
                                        bundle.putString(DefineValue.CUST_ID_ESPAY, custIdEspay)
                                        bundle.putString(DefineValue.DOC_DETAILS, response.optString(WebParams.DOC_DETAILS))
                                        bundle.putString(DefineValue.AMOUNT, response.optString(WebParams.AMOUNT))
                                        bundle.putString(DefineValue.TOTAL_AMOUNT, response.optString(WebParams.TOTAL_AMOUNT))
                                        bundle.putString(DefineValue.TOTAL_DISC, response.optString(WebParams.DISCOUNT_AMOUNT))
                                        bundle.putString(DefineValue.DOC_NO, docNo)
                                        val frag: Fragment = FragCreateGR()
                                        frag.arguments = bundle
                                        switchFragment(frag, "", "", true, "")

                                    }
                                    WebParams.LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
                                        val message = response.getString(WebParams.ERROR_MESSAGE)
                                        val test = AlertDialogLogout.getInstance()
                                        test.showDialoginActivity(activity, message)
                                    }
                                    DefineValue.ERROR_9333 -> {
                                        Timber.d("isi response app data:%s", model.app_data)
                                        val appModel = model.app_data
                                        val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                        alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                    }
                                    DefineValue.ERROR_0066 -> {
                                        Timber.d("isi response maintenance:$response")
                                        val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                        alertDialogMaintenance.showDialogMaintenance(activity)
                                    }
                                    else -> {
                                        Timber.d("isi error confirm doc:$response")
                                        Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {}
                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: java.lang.Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    override fun onClick(item: EBDCatalogModel?) {
        TODO("Not yet implemented")
    }

    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserGoodReceiptActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }
}