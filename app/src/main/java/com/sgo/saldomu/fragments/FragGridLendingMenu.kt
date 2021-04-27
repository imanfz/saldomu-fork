package com.sgo.saldomu.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
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
import kotlinx.android.synthetic.main.frag_grid.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragGridLendingMenu : BaseFragment() {

    private val TAG: String? = "FragGridLendingMenu"

    private val menuStrings = ArrayList<String>()
    private val menuDrawables = ArrayList<Drawable>()
    private var adapter: GridMenu? = null
    private val providerModelArraylist = ArrayList<ProviderModel>()
    private var gridLendingActivity: GridLendingActivity? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_grid, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridLendingActivity = activity as GridLendingActivity
        inquiryLendingData()
        adapter = GridMenu(context!!, menuStrings, menuDrawables)
        grid.adapter = adapter
        grid.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            gridLendingActivity!!.showWebView(providerModelArraylist[position].review_url!!)
        }
    }

    private fun inquiryLendingData() {
        try {
            showProgressDialog()
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_LENDING_DATA)
            params[USER_ID] = userPhoneID
            Timber.d("params inquiry lending data:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_INQUIRY_LENDING_DATA, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject?) {
                            try {
                                val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                                val code = model.error_code
                                val message = model.error_message
                                Timber.d("isi response inquiry lending data:$response")
                                when (code) {
                                    SUCCESS_CODE -> {
                                        val mArrayProvider = response!!.getJSONArray(PROVIDER)
                                        for (i in 0 until mArrayProvider.length()) {
                                            val lendingName = mArrayProvider.getJSONObject(i).getString(LENDING_NAME)
                                            val lendingCode = mArrayProvider.getJSONObject(i).getString(LENDING_CODE)
                                            val reviewUrl = mArrayProvider.getJSONObject(i).getString(REVIEW_URL)+
                                                    "?mobilephone="+userPhoneID+"&email="+sp.getString(DefineValue.PROFILE_EMAIL, "")
                                            val providerModel = ProviderModel()
                                            providerModel.lending_code = lendingCode
                                            providerModel.lending_name = lendingName
                                            providerModel.review_url = reviewUrl
                                            providerModelArraylist.add(providerModel)
                                        }
                                        setTitleandIcon()
                                    }
                                    LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
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
                                        alertDialogMaintenance.showDialogMaintenance(activity, message)
                                    }
                                    else -> {
                                        Timber.d("isi error inquiry lending data:$response")
                                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace();
                            }
                        }

                        override fun onError(throwable: Throwable?) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun setTitleandIcon() {
        menuStrings.clear()
        menuDrawables.clear()
        for (i in providerModelArraylist.indices) {
            menuStrings.add(providerModelArraylist[i].lending_code!!)
            if (providerModelArraylist[i].lending_code!!.contains("INVESTREE"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.menu_lending_investree, null)!!)

            if (providerModelArraylist[i].lending_code!!.contains("AMARTHA"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.menu_lending_amartha, null)!!)

            if (providerModelArraylist[i].lending_code!!.contains("DANAMAS"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.menu_lending_danamas, null)!!)

            if (providerModelArraylist[i].lending_code!!.contains("BATUMBU"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.menu_lending_batumbu, null)!!)
        }
        adapter!!.notifyDataSetChanged()
    }

}