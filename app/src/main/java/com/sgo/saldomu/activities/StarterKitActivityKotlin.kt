package com.sgo.saldomu.activities

import android.app.PendingIntent.getActivity
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.widget.Toast
import com.sgo.saldomu.Beans.SCADMCommunityModel
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.ListJoinSCADMAdapter
import com.sgo.saldomu.adapter.StarterKitListFileAdapter
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.LevelClass
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.StarterKitFileModel
import com.sgo.saldomu.widgets.BaseActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.ArrayList

class StarterKitActivityKotlin : BaseActivity() {
    private lateinit var levelClass: LevelClass
    private var isLevel1: Boolean? = false
    private var isAgent: Boolean? = false
    internal lateinit var progdialog: ProgressDialog
    private lateinit var levelMember: String
    private lateinit var levelAgent: String
    private val starterKitFileArrayList = ArrayList<StarterKitFileModel>()
    private var recyclerView: RecyclerView? = null
    private var starterKitListFileAdapter: StarterKitListFileAdapter? = null


    override fun getLayoutResource(): Int {
        return R.layout.starterkit_activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InitializeToolbar()
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        levelClass = LevelClass(this, sp)

        recyclerView = findViewById(R.id.listFile)

        initData()

        initializeAdapter()

        getListFile()

    }

    private fun initializeAdapter() {
        starterKitListFileAdapter = StarterKitListFileAdapter()
        recyclerView!!.adapter = starterKitListFileAdapter

        recyclerView!!.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
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

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_LIST_FILE, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        try {

                            Timber.d("response list file : $response")
                            val code = response.getString(WebParams.ERROR_CODE)
                            val error_message = response.getString(WebParams.ERROR_MESSAGE)
                            if (code == WebParams.SUCCESS_CODE) {
                                dismissProgressDialog()

                                val mArrayStarterKits = JSONArray(response.getString(WebParams.STARTER_KITS))

                                for (i in 0 until mArrayStarterKits.length()) {
                                    val starterId = mArrayStarterKits.getJSONObject(i).getString(WebParams.STARTER_ID)
                                    val fileTitle = mArrayStarterKits.getJSONObject(i).getString(WebParams.FILE_TITLE)

                                    val starterKitFileModel = StarterKitFileModel()
                                    starterKitFileModel.STARTER_ID = starterId
                                    starterKitFileModel.FILE_TITLE = fileTitle

                                    starterKitFileArrayList.add(starterKitFileModel)
                                }
//
//                                listJoinSCADMAdapter.updateData(scadmCommunityModelArrayList)

                            } else {
                                Toast.makeText(this@StarterKitActivityKotlin, error_message, Toast.LENGTH_LONG).show()
                            }


                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(throwable: Throwable) {

                    }

                    override fun onComplete() {

                    }
                })
    }

    fun InitializeToolbar() {
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
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}