package com.sgo.saldomu.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.sgo.saldomu.Beans.SCADMCommunityModel
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.GridLendingActivity
import com.sgo.saldomu.adapter.GridMenu
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams.*
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.retrofit.ProviderModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.bbs_cash_in_cash_out.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragGridLendingMenu : BaseFragment() {

    private val TAG: String? = "FragGridLendingMenu"

    private val menuStrings = ArrayList<String>()
    private val menuDrawables = ArrayList<Drawable>()
    private var gridLendingActivity: GridLendingActivity? = null
    private var adapter: GridMenu? = null
    private val providerModelArraylist = java.util.ArrayList<ProviderModel>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag_grid, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridLendingActivity = activity as GridLendingActivity
        gridLendingActivity!!.setToolbarTitle(getString(R.string.menu_item_lending))
        inquiryLendingData()
    }

    fun inquiryLendingData()
    {
        try {
            showProgressDialog()
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_LENDING_DATA)
            params[USER_ID] = userPhoneID
            Timber.d("params inquiry lending data:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_INQUIRY_LENDING_DATA, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(ERROR_CODE)
                                Timber.d("isi response inquiry lending data:$response")
                                when (code) {
                                    SUCCESS_CODE -> {
                                        val mArrayProvider = JSONArray(response.getString(PROVIDER))

                                        for (i in 0 until mArrayProvider.length()) {
                                            val lending_name = mArrayProvider.getJSONObject(i).getString(LENDING_NAME)
                                            val lending_code = mArrayProvider.getJSONObject(i).getString(LENDING_CODE)
                                            val review_url = mArrayProvider.getJSONObject(i).getString(REVIEW_URL)
                                            val providerModel = ProviderModel()
                                            providerModel.lending_code = lending_code
                                            providerModel.lending_name = lending_name
                                            providerModel.review_url = review_url
                                            providerModelArraylist.add(providerModel)
                                        }

                                        setTitleandIcon()

//                                        listTopUpSCADMAdapter.updateData(scadmCommunityModelArrayList)
                                    }
                                    LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
                                        val message = response.getString(ERROR_MESSAGE)
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
                                        alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                                    }
                                    else -> {
                                        Timber.d("isi error inquiry lending data:$response")
                                        val code_msg = response.getString(ERROR_MESSAGE)
                                        Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {}
                        override fun onComplete() {
                            proses_btn.isEnabled = true
                            showProgressDialog()
                        }
                    })
        } catch (e: java.lang.Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun setTitleandIcon() {
        menuStrings.clear()
        menuDrawables.clear()
        for (i in providerModelArraylist!!.indices) {
            menuStrings.add(providerModelArraylist!![i].lending_code!!)
            if (providerModelArraylist!![i].lending_code!!.contains("LinkAja"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.icon_emoney_linkaja, null)!!)

            if (providerModelArraylist!![i].lending_code!!.contains("Emoney Mandiri") ||
                    providerModelArraylist!![i].lending_code!!.contains("Mandiri E-Money"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.icon_emoney_mandiri, null)!!)

            if (providerModelArraylist!![i].lending_code!!.contains("OVO"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.icon_emoney_ovo, null)!!)

        }
    }

}