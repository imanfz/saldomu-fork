package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserPOActivity
import com.sgo.saldomu.adapter.AdapterListItemCreatePOCanvasser
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.FormatQty
import com.sgo.saldomu.models.retrofit.ItemModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_order_confirm_toko.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

class FragCreatePOCanvasser : BaseFragment() {
    var memberCodeEspay = ""
    var commCodeEspay = ""
    var custIdEspay = ""
    var docDetail = ""
    var amount = ""
    var totalDiscount = ""
    var totalAmount = ""

    private val itemArrayList = ArrayList<ItemModel>()

    private var adapterListItemCreatePOCanvasser: AdapterListItemCreatePOCanvasser? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_confirm_po_canvasser, container, false)
        return v
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val canvasserPOActivity = activity as CanvasserPOActivity
        canvasserPOActivity.initializeToolbar(getString(R.string.purchase_order_confirmation))

        if (arguments != null) {
            memberCodeEspay = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCodeEspay = arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            docDetail = arguments!!.getString(DefineValue.DOC_DETAILS, "")
            custIdEspay = arguments!!.getString(DefineValue.CUST_ID_ESPAY, "")
            amount = arguments!!.getString(DefineValue.AMOUNT, "")
            totalDiscount = arguments!!.getString(DefineValue.TOTAL_DISC, "")
            totalAmount = arguments!!.getString(DefineValue.TOTAL_AMOUNT, "")
        }


        initalizeListItem()

        member_code_field.text = memberCodeEspay
        comm_code_field.text = commCodeEspay
        amount_field.text = MyApiClient.CCY_VALUE + " " + CurrencyFormat.format(amount)
        discount_field.text = MyApiClient.CCY_VALUE + " " + CurrencyFormat.format(totalDiscount)
        total_field.text = MyApiClient.CCY_VALUE + " " + CurrencyFormat.format(totalAmount)

        submit_btn.setOnClickListener {
            createPO()
        }

    }

    private fun initalizeListItem() {
        itemArrayList.clear()
        adapterListItemCreatePOCanvasser = AdapterListItemCreatePOCanvasser(context!!, itemArrayList, this)
        item_list_field.adapter = adapterListItemCreatePOCanvasser
        item_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val docDetailsJsonArray = JSONArray(docDetail)

        for (i in 0 until docDetailsJsonArray.length()) {
            val mappingItemJsonArray = docDetailsJsonArray.getJSONObject(i).getJSONArray(WebParams.MAPPING_ITEMS)
            for (j in 0 until mappingItemJsonArray.length()) {
                val itemName = mappingItemJsonArray.getJSONObject(j).getString(WebParams.ITEM_NAME)
                val itemCode = mappingItemJsonArray.getJSONObject(j).getString(WebParams.ITEM_CODE)
                val price = mappingItemJsonArray.getJSONObject(j).getString(WebParams.PRICE)
                val unit = mappingItemJsonArray.getJSONObject(j).getString(WebParams.UNIT)
                val subtotal = mappingItemJsonArray.getJSONObject(j).optString(WebParams.SUBTOTAL_AMOUNT)
                val formatQtyJsonArray = mappingItemJsonArray.getJSONObject(j).getJSONArray(WebParams.FORMAT_QTY)
                val formatQtys = ArrayList<FormatQty>()
                for (k in 0 until formatQtyJsonArray.length()) {
                    val mappingUnit = formatQtyJsonArray.getJSONObject(k).getString(WebParams.MAPPING_UNIT)
                    val mappingQty = formatQtyJsonArray.getJSONObject(k).getInt(WebParams.MAPPING_QTY)
                    val formatQty = FormatQty(mappingUnit, mappingQty)
                    formatQtys.add(formatQty)
                }
                itemArrayList.add(ItemModel(itemName, itemCode, price, unit, subtotal, formatQtys))
            }
        }

        adapterListItemCreatePOCanvasser!!.updateData(itemArrayList)
    }


    private fun createPO() {
        showProgressDialog()

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CREATE_PO, memberCodeEspay + commCodeEspay + amount)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
        params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
        params[WebParams.CUST_ID_ESPAY] = custIdEspay
        params[WebParams.CUST_ID] = userPhoneID
        params[WebParams.AMOUNT] = amount
        params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
        params[WebParams.DOC_DETAIL] = docDetail
        params[WebParams.CUST_TYPE] = DefineValue.CANVASSER
        params[WebParams.ACTION_CODE] = DefineValue.STRING_NO
        params[WebParams.TOTAL_AMOUNT] = totalAmount
        params[WebParams.DISCOUNT_AMOUNT] = totalDiscount

        Timber.d("isi params create PO:$params")

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CREATE_PO, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                val bundle = arguments!!
                                bundle.putString(DefineValue.TX_ID, response.getString(WebParams.TX_ID))
                                val frag: Fragment = FragConfirmCreatePOCanvasser()
                                frag.arguments = bundle
                                switchFragment(frag, "", "", true, "")
                            }
                            WebParams.LOGOUT_CODE -> {
                                AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                            }
                            DefineValue.ERROR_9333 -> {
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val appModel = model.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            }
                            DefineValue.ERROR_0066 -> {
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
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


    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserPOActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }

}