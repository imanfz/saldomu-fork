package com.sgo.saldomu.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.StarterKitListFileAdapter
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.dialogs.DefinedDialog
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.StarterKitFileModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseActivity
import timber.log.Timber
import java.net.URLDecoder

class StarterKitActivityKotlin : BaseActivity(), StarterKitListFileAdapter.StarterKitListener {
    private lateinit var levelClass: LevelClass
    private var isLevel1: Boolean? = false
    private var isAgent: Boolean? = false
    internal lateinit var progdialog: ProgressDialog
    private lateinit var levelMember: String
    private lateinit var levelAgent: String
    private var recyclerView: RecyclerView? = null
    private var starterKitListFileAdapter: StarterKitListFileAdapter? = null
    private lateinit var message: String

    override fun getLayoutResource(): Int {
        return R.layout.starterkit_activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeToolbar()
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        levelClass = LevelClass(this, sp)

        recyclerView = findViewById(R.id.listFile)

        starterKitListFileAdapter?.clearData()

        initData()

        initializeAdapter()

        getListFile()

    }

    private fun initializeAdapter() {
        starterKitListFileAdapter = StarterKitListFileAdapter(this)
        recyclerView!!.adapter = starterKitListFileAdapter

        recyclerView!!.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))

        val itemDecoration = DividerItemDecoration(this, null)
        recyclerView!!.addItemDecoration(itemDecoration)

    }

    fun initData() {
        levelClass.refreshData()
        isLevel1 = levelClass.isLevel1QAC()
        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false)
        if (isLevel1 == true && isAgent == false) {
            levelMember = "1"
            levelAgent = "0"
        } else if (isLevel1 == false && isAgent == false) {
            levelMember = "2"
            levelAgent = "0"
        } else {
            levelMember = "2"
            levelAgent = "1"
        }
    }

    fun getListFile() {
        showProgressDialog()

        val extraSignature = levelMember + levelAgent
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LIST_FILE, extraSignature)

        params.put(WebParams.MEMBER_LEVEL, levelMember)
        params.put(WebParams.MEMBER_ID, memberIDLogin)
        params.put(WebParams.IS_AGENT, levelAgent)
        params.put(WebParams.USER_ID, userPhoneID)
        Timber.d("params list file : " + params.toString())


        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_LIST_FILE, params, object : ResponseListener {
            override fun onResponses(response: JsonObject) {
                val model = getGson().fromJson(response, jsonModel::class.java)
                val code = model.error_code
                val message = model.error_message

                if (code == WebParams.SUCCESS_CODE) {

                    val type = object : TypeToken<List<StarterKitFileModel>>() {
                    }.type
                    val list = gson.fromJson<List<StarterKitFileModel>>(response.get(WebParams.STARTER_KITS), type)

                    starterKitListFileAdapter?.updateData(list)
                    dismissProgressDialog()
                } else if (code == WebParams.LOGOUT_CODE) {
                    switchLogout()
                } else if (code == DefineValue.ERROR_9333) run {
                    Timber.d("isi response app data:" + model.app_data)
                    val appModel = model.app_data
                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                    alertDialogUpdateApp.showDialogUpdate(this@StarterKitActivityKotlin, appModel.type, appModel.packageName, appModel.downloadUrl)
                } else if (code == DefineValue.ERROR_0066) run {
                    Timber.d("isi response maintenance:" + response.toString())
                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                    alertDialogMaintenance.showDialogMaintenance(this@StarterKitActivityKotlin)
                } else {
                    Toast.makeText(this@StarterKitActivityKotlin, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(throwable: Throwable) {
            }

            override fun onComplete() {
            }
        })
    }

    override fun onClick(model: StarterKitFileModel) {
        Log.e(StarterKitActivityKotlin.TAG, "onClick: ")
        getDownloadFile(model)
    }

    fun getDownloadFile(starterKitFileModel: StarterKitFileModel) {
        showProgressDialog()

        extraSignature = memberIDLogin + levelMember + levelAgent
        val params = RetrofitService.getInstance()
                .getSignature(MyApiClient.LINK_DOWNLOAD_FILE, extraSignature)

        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_ID] = memberIDLogin
        params[WebParams.MEMBER_NAME] = sp.getString(DefineValue.CUST_NAME, "")
        params[WebParams.MEMBER_EMAIL] = URLDecoder.decode(sp.getString(DefineValue.PROFILE_EMAIL, ""))
        params[WebParams.MEMBER_LEVEL] = levelMember
        params[WebParams.IS_AGENT] = levelAgent
        params[WebParams.STARTER_ID] = starterKitFileModel.starter_id

        Timber.d("isi params download file:$params")

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_DOWNLOAD_FILE, params,
                object : ResponseListener {
                    override fun onResponses(response: JsonObject) {
                        dismissProgressDialog()
                        Log.e(StarterKitActivityKotlin.TAG, "getDownloadFile : $response")

                        val model = getGson().fromJson(response, jsonModel::class.java)

                        when (model.error_code) {
                            WebParams.SUCCESS_CODE -> {
                                message = response.get(WebParams.MESSAGE).toString()
                                dialogSuccess(message)
                            }
                            WebParams.LOGOUT_CODE -> {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(this@StarterKitActivityKotlin, message)
                            }
                            WebParams.ERROR_9333 -> {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(this@StarterKitActivityKotlin, appModel.type, appModel.packageName, appModel.downloadUrl)
                            }
                            WebParams.ERROR_0066 -> {
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(this@StarterKitActivityKotlin)
                            }
                            else -> {
                                val msg = model.error_message
                                Toast.makeText(this@StarterKitActivityKotlin, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        dismissProgressDialog()
                        Log.d("onErrorDownloadFile", "onErrorDownloadFile")
                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                    }
                })
    }

    private fun dialogSuccess(msg: String) {
        var dialognya = DefinedDialog.MessageDialog(this, this!!.getString(R.string.dialog_download_title),
                msg
        ) {}

        dialognya.show()
    }

    companion object {
        private const val TAG = "StarterKitActivity"
    }

    fun initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.menu_item_title_starterkit)
    }

    private fun switchLogout() {
        setResult(MainPage.RESULT_LOGOUT)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
