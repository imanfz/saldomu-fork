package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserPOActivity
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.AdapterListDetailPO
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.DocDetailsItem
import com.sgo.saldomu.models.FormatQtyItem
import com.sgo.saldomu.models.MappingItemsItem
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_detail_po.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

class FragPurchaseOrderDetail : BaseFragment() {

    private val docTypeID = "PO"
    var memberCode: String = ""
    var commCode: String = ""
    var docNo: String = ""
    var type: String = ""

    private var adapterDetailPOItem: AdapterListDetailPO? = null
    private var adapterDetailPOBonusItem: AdapterListDetailPO? = null
    private val itemList = ArrayList<MappingItemsItem>()
    private val bonusItemList = ArrayList<MappingItemsItem>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_detail_po, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()



        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCode = arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            docNo = arguments!!.getString(DefineValue.DOC_NO, "")
            type = arguments!!.getString(DefineValue.TYPE, "")
        }

        if (type == DefineValue.CANVASSER) {
            val canvasserPOActivity = activity as CanvasserPOActivity
            canvasserPOActivity.initializeToolbar(getString(R.string.detail_document))
        } else {
            val tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
            tokoPurchaseOrderActivity.initializeToolbar(getString(R.string.detail_document))
        }

        adapterDetailPOItem = AdapterListDetailPO(context!!, itemList)
        adapterDetailPOBonusItem = AdapterListDetailPO(context!!, bonusItemList)
        recyclerViewList.adapter = adapterDetailPOItem
        recyclerViewListBonus.adapter = adapterDetailPOBonusItem
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerViewListBonus.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        getPODetail()

        btn_payment.setOnClickListener { payInvoice() }
    }

    private fun getPODetail() {
        showProgressDialog()

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_DETAIL, memberCode + commCode + docNo)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCode
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.DOC_NO] = docNo

        Timber.d("isi params get $docTypeID detail:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_DETAIL, params,
                object : ObjListeners {
                    @SuppressLint("SetTextI18n")
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                tv_docNo.text = response.getString(WebParams.DOC_NO)
                                tv_totalAmount.text = getString(R.string.currency) + " " + CurrencyFormat.format(response.getString(WebParams.TOTAL_AMOUNT))
                                val mArrayItem = JSONArray(response.getString(WebParams.ITEMS))
                                val bonusItem = response.getString(WebParams.BONUS_ITEMS)
                                for (i in 0 until mArrayItem.length()) {
                                    val itemName = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_NAME)
                                    val itemCode = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_CODE)
                                    val price = mArrayItem.getJSONObject(i).getString(WebParams.PRICE)
                                    val unit = mArrayItem.getJSONObject(i).getString(WebParams.UNIT)
                                    val formatQtyJsonArray = mArrayItem.getJSONObject(i).getJSONArray(WebParams.FORMAT_QTY)
                                    val formatQtyItemList = ArrayList<FormatQtyItem>()
                                    for (j in 0 until formatQtyJsonArray.length()) {
                                        val mappingUnit = formatQtyJsonArray.getJSONObject(j).getString(WebParams.MAPPING_UNIT)
                                        val mappingQty = formatQtyJsonArray.getJSONObject(j).getInt(WebParams.MAPPING_QTY)
                                        val formatQtyItem = FormatQtyItem()
                                        formatQtyItem.mapping_unit = mappingUnit
                                        formatQtyItem.mapping_qty = mappingQty
                                        formatQtyItemList.add(formatQtyItem)
                                    }
                                    val mappingItemsItem = MappingItemsItem()
                                    mappingItemsItem.item_name = itemName
                                    mappingItemsItem.item_code = itemCode
                                    mappingItemsItem.price = price.toInt()
                                    mappingItemsItem.unit = unit
                                    mappingItemsItem.format_qty = formatQtyItemList
                                    itemList.add(mappingItemsItem)
                                }
                                if (bonusItem != "") {
                                    val model = getGson().fromJson(response.toString(), DocDetailsItem::class.java)
                                    bonusItemList.addAll(model.bonus_items)
                                } else
                                    layout_bonus_item.visibility = View.GONE

                                adapterDetailPOItem!!.notifyDataSetChanged()
                                adapterDetailPOBonusItem!!.notifyDataSetChanged()
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

    fun payInvoice() {

    }


}