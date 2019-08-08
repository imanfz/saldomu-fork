package com.sgo.saldomu.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.FavoriteAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.HistoryModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_history.*
import java.util.HashMap

private const val TAG = "FavoriteActivity"
class FavoriteActivity : BaseActivity(), FavoriteAdapter.FavoriteListener {
    internal lateinit var params: HashMap<String, Any>
    internal lateinit var adapter: FavoriteAdapter
    internal lateinit var dialog: AlertDialog

    override fun getLayoutResource(): Int {
        return R.layout.activity_history
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setupRecycler()
        getListFavorite()
    }

    private fun initialize () {
        adapter = FavoriteAdapter(this)

        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.progress)
        dialog = builder.create()
    }

    private fun setupRecycler() {
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
    }

    private fun getListFavorite () {
        setDialog(true)

        extraSignature = sp.getString(DefineValue.MEMBER_ID, "")
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TRX_FAVORITE_LIST, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_ID] = memberIDLogin
        Log.e(TAG, "params: $params")

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_TRX_FAVORITE_LIST, params, object : ResponseListener {
            override fun onResponses(`object`: JsonObject) {
                Log.e(TAG, "onResponses: $`object`")
                val model = getGson().fromJson(`object`, jsonModel::class.java)

                val code = model.error_code
                val message = model.error_message

                if (code == WebParams.SUCCESS_CODE) {
                    val type = object : TypeToken<List<HistoryModel>>() {
                    }.type
                    val list = gson.fromJson<List<HistoryModel>>(`object`.get("report_data"), type)

                    adapter.updateAdapter(list)
                } else if (code == WebParams.LOGOUT_CODE) {
                    val test = AlertDialogLogout.getInstance()
                    test.showDialoginActivity(this@FavoriteActivity, message)
                }
            }

            override fun onError(throwable: Throwable) {
                setDialog(false)
                Toast.makeText(this@FavoriteActivity, throwable.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onComplete() {
                setDialog(false)
                recycler_view.visibility = View.VISIBLE
            }
        })
    }

    private fun setDialog(show: Boolean) {
        if (show)
            dialog.show()
        else
            dialog.dismiss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}