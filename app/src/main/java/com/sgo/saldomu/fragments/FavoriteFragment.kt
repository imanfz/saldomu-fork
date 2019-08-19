package com.sgo.saldomu.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.HistoryActivity
import com.sgo.saldomu.adapter.FavoriteAdapter
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.FavoriteModel
import com.sgo.saldomu.models.retrofit.HistoryModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_favorite.*

private const val TAG = "FavoriteFragment"

class FavoriteFragment : BaseFragment(), FavoriteAdapter.FavoriteListener {
    override fun onClick(model: HistoryModel) {

    }

    override fun onShowTransferActivity() {

    }

    internal lateinit var adapter: FavoriteAdapter
    lateinit var key: String

    fun newInstance(key: String): FavoriteFragment {
        val args = Bundle()
        args.putString("key", key)
        val fragment = FavoriteFragment()
        fragment.arguments = args
        return fragment
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
        key = this.arguments!!.getString("key").toString()
        adapter = FavoriteAdapter(this)
    }


    private fun setupRecycler() {
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.setHasFixedSize(true)
    }

    private fun getListFavorite() {
//        setDialog(true)

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
                    }
                })
    }
}