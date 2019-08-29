package com.sgo.saldomu.fragments

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.FavoriteActivity
import com.sgo.saldomu.activities.HistoryActivity
import com.sgo.saldomu.adapter.FavoriteAdapter
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogFrag
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.FavoriteModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_favorite.*

private const val TAG = "FavoriteFragment"

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
        swipeRefresh.setColorSchemeResources(R.color.orange_600)
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
        Log.e(TAG, "params: $params")

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_TRX_FAVORITE_LIST, params,
                object : ResponseListener {
                    override fun onResponses(`object`: JsonObject) {
                        try {
                            Log.e(TAG, "onResponses: ${`object`.toString()}")
                            val model = getGson().fromJson(`object`, jsonModel::class.java)

                            val code = model.error_code
                            val message = model.error_message

                            if (code == WebParams.SUCCESS_CODE) {
                                val type = object : TypeToken<List<FavoriteModel>>() {}.type
                                val list = gson.fromJson<List<FavoriteModel>>(`object`.get("favorite"), type)
                                Log.e(TAG, "onResponses: $list")
                                adapter.updateAdapter(list)
                            } else {

                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(throwable: Throwable) {
                        Log.e(HistoryActivity.TAG, "onError: ${throwable.localizedMessage}")
                    }

                    override fun onComplete() {
                        Log.e(HistoryActivity.TAG, "onComplete")
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
        Log.e(TAG, "params: $params")

        RetrofitService.getInstance().PostObjectRequest(url, params,
                object : ResponseListener {
                    override fun onResponses(`object`: JsonObject) {
                        try {
                            Log.e(TAG, "onResponses: ${`object`.toString()}")
                            val model2 = getGson().fromJson(`object`, jsonModel::class.java)

                            val code = model2.error_code
                            val message = model2.error_message

                            if (code == WebParams.SUCCESS_CODE) {
                                adapter.removeItem(model)
                            }else
                            {
                                Toast.makeText(context,message, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(throwable: Throwable) {
                        Log.e(TAG, "onError: ${throwable.localizedMessage}")
                    }

                    override fun onComplete() {
                        Log.e(TAG, "onComplete")
                        dismissProgressDialog()
                    }
                })
    }

    private fun setDialog(show: Boolean) {
        if (show)
            dialog.show()
        else
            dialog.dismiss()
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
        var dialogFrag : AlertDialogFrag = AlertDialogFrag.newInstance(activity!!.getString(R.string.menu_item_favorite),
                activity!!.getString(R.string.delete_item_favorite_dialog), activity!!.getString(R.string.yes),
                activity!!.getString(R.string.no), false)

        dialogFrag.setOkListener { dialog, _ ->
            deleteFavoriteItem(model)
        }
        dialogFrag.setCancelListener { dialog, _ ->
            dialog.dismiss()
        }
        dialogFrag.setTargetFragment(this@FavoriteFragment, 0)
        dialogFrag.show(activity!!.supportFragmentManager, AlertDialogFrag.TAG)
    }

}