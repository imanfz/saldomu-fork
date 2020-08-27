package com.sgo.saldomu.fragments

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.FavoriteActivity
import com.sgo.saldomu.adapter.FavoriteAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogFrag
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.FavoriteModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_favorite.*
import timber.log.Timber


class FavoriteFragment : BaseFragment(), FavoriteAdapter.FavoriteListener, SwipeRefreshLayout.OnRefreshListener {
    internal lateinit var adapter: FavoriteAdapter
    lateinit var key: String
    internal lateinit var dialog: AlertDialog

    fun newInstance(key: String): FavoriteFragment {
        val args = Bundle()
        args.putString("key", key)
        val fragment = FavoriteFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onRefresh() {
        adapter.clearAdapter()
        getListFavorite()
        swipeRefresh.isRefreshing = false
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_favorite, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setupRecycler()
        getListFavorite()
    }


    private fun initialize() {
        val builder = AlertDialog.Builder(activity!!)
        builder.setView(R.layout.progress)
        dialog = builder.create()

        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setColorSchemeResources(R.color.colorPrimaryDark)
        key = this.arguments!!.getString("key").toString()
        adapter = FavoriteAdapter(this)
    }


    private fun setupRecycler() {
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.setHasFixedSize(true)
    }

    private fun getListFavorite() {
        showProgressDialog()
        extraSignature = key
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TRX_FAVORITE_LIST, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_ID] = memberIDLogin
        params[WebParams.TX_FAVORITE_TYPE] = key
        Timber.e("params: $params")

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_TRX_FAVORITE_LIST, params,
                object : ResponseListener {
                    override fun onResponses(jsonObject: JsonObject) {
                        try {
                            Timber.e("onResponses: $jsonObject")
                            val model = getGson().fromJson(jsonObject, jsonModel::class.java)

                            val code = model.error_code
                            val message = model.error_message

                            when (code) {
                                WebParams.SUCCESS_CODE -> {
                                    val type = object : TypeToken<List<FavoriteModel>>() {}.type
                                    val list = gson.fromJson<List<FavoriteModel>>(jsonObject.get("favorite"), type)
                                    Timber.e("onResponses: $list")
                                    adapter.updateAdapter(list)
                                }
                                WebParams.LOGOUT_CODE -> {
                                    val test = AlertDialogLogout.getInstance()
                                    test.showDialoginActivity(activity, message)
                                }
                                DefineValue.ERROR_9333 -> run {
                                    Timber.d("isi response app data:%s", model.app_data)
                                    val appModel = model.app_data
                                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                }
                                DefineValue.ERROR_0066 -> run {
                                    Timber.d("isi response maintenance:$jsonObject")
                                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                    alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e)
                        }

                    }

                    override fun onError(throwable: Throwable) {
                        Timber.e("onError: ${throwable.localizedMessage}")
                    }

                    override fun onComplete() {
                        Timber.e("onComplete")
                        dismissProgressDialog()
                    }
                })
    }

    private fun deleteFavoriteItem(model: FavoriteModel) {
        showProgressDialog()

        extraSignature = model.customer_id + model.product_type + model.tx_favorite_type
        val url = MyApiClient.LINK_TRX_FAVORITE_DELETE

        params = RetrofitService.getInstance().getSignature(url, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.CUSTOMER_ID] = model.customer_id
        params[WebParams.PRODUCT_TYPE] = model.product_type
        params[WebParams.TX_FAVORITE_TYPE] = model.tx_favorite_type
        params[WebParams.COMM_ID] = model.comm_id

        if (model.product_type.isBlank()) {
            params[WebParams.DENOM_ITEM_ID] = model.item_id
        }
        Timber.e("params: $params")

        RetrofitService.getInstance().PostObjectRequest(url, params,
                object : ResponseListener {
                    override fun onResponses(`object`: JsonObject) {
                        try {
                            Timber.e("onResponses: $`object`")
                            val model2 = getGson().fromJson(`object`, jsonModel::class.java)

                            val code = model2.error_code
                            val message = model2.error_message

                            when (code) {
                                WebParams.SUCCESS_CODE -> {
                                    adapter.removeItem(model)
                                }
                                WebParams.LOGOUT_CODE -> {
                                    val test = AlertDialogLogout.getInstance()
                                    test.showDialoginActivity(activity, message)
                                }
                                DefineValue.ERROR_9333 -> run {
                                    Timber.d("isi response app data:%s", model.app_data)
                                    val appModel = model.app_data
                                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                }
                                DefineValue.ERROR_0066 -> run {
                                    Timber.d("isi response maintenance:$`object`")
                                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                    alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                                }
                                else -> {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(throwable: Throwable) {
                        Timber.e("onError: ${throwable.localizedMessage}")
                    }

                    override fun onComplete() {
                        Timber.e("onComplete")
                        dismissProgressDialog()
                    }
                })
    }

    override fun onShowBillerActivity(model: FavoriteModel) {
        (activity as FavoriteActivity).startBillerActivity(model)
    }

    override fun onShowTransferActivity(model: FavoriteModel) {
        (activity as FavoriteActivity).startTransferActivity(model)
    }

    override fun onStartBBSActivity(model: FavoriteModel) {
        (activity as FavoriteActivity).startBBSActivity(model)
    }

    override fun onDeleteFavorite(model: FavoriteModel) {
        val dialogFrag: AlertDialogFrag = AlertDialogFrag.newInstance(activity!!.getString(R.string.menu_item_favorite),
                activity!!.getString(R.string.delete_item_favorite_dialog), activity!!.getString(R.string.yes),
                activity!!.getString(R.string.no), false)

        dialogFrag.setOkListener { _, _ ->
            deleteFavoriteItem(model)
        }
        dialogFrag.setCancelListener { dialog, _ ->
            dialog.dismiss()
        }
        dialogFrag.setTargetFragment(this@FavoriteFragment, 0)
        dialogFrag.show(activity!!.supportFragmentManager, AlertDialogFrag.TAG)
    }

}