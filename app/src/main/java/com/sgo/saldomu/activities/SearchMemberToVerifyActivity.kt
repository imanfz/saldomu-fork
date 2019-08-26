package com.sgo.saldomu.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.gson.JsonObject
import com.securepreferences.SecurePreferences
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.DefinedDialog
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_search_member_to_verifiy.*
import timber.log.Timber

class SearchMemberToVerifyActivity : BaseActivity() {
    private var progdialog: ProgressDialog? = null
    private lateinit var memberIdCust: String
    private lateinit var custId: String

    override fun getLayoutResource(): Int {
        return R.layout.activity_search_member_to_verifiy
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        initialize()
    }

    private fun initialize() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.menu_item_title_upgrade_member)

        submit_button.setOnClickListener {
            searchMember()
        }

        next_button.setOnClickListener {
            val intent = Intent(this, UpgradeMemberViaAgentActivity::class.java)
//            intent.putExtra(DefineValue.MEMBER_ID_CUST, memberIdCust)
            startActivity(intent)
        }
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

    private fun searchMember() {
        try {

            if (progdialog == null)
                progdialog = DefinedDialog.CreateProgressDialog(this@SearchMemberToVerifyActivity, "")
            else
                progdialog!!.show()

            val params = RetrofitService.getInstance()
                    .getSignature(MyApiClient.LINK_SEARCH_MEMBER)
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID] = MyApiClient.COMM_ID
            params[WebParams.CUST_ID] = etNote.text.toString()

            Timber.d("isi params check member:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_SEARCH_MEMBER, params,
                    object : ResponseListener {
                        override fun onResponses(response: JsonObject) {
                            val model = gson.fromJson(response, jsonModel::class.java)

                            var code = model.error_code
                            if (code == WebParams.SUCCESS_CODE) {
                                val custName: String
                                custName = response.get(WebParams.CUST_NAME).asString
                                val mEditor = sp.edit()
                                mEditor.putString(DefineValue.MEMBER_ID_CUST, response.get(WebParams.MEMBER_ID_CUST).asString)
                                mEditor.putString(DefineValue.CUST_ID_MEMBER, response.get(WebParams.CUST_ID).asString)
                                mEditor.apply()
                                mEditor.commit()

                                layout_memberid.visibility = View.VISIBLE
                                etNote.isEnabled = false
                                submit_button.visibility = View.GONE

                                val maskedName = StringBuilder()
                                val nameArray = custName.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                for (i in nameArray.indices) {
                                    val originName = nameArray[i]
                                    var tempName = ""

                                    val maskingName = StringBuilder()
                                    if (originName.length > 2) {
                                        for (j in 0 until originName.length - 2) {
                                            maskingName.append("*")
                                        }
                                        tempName = originName.replace(originName.substring(2, originName.length), maskingName.toString())

                                    } else {
                                        maskedName.append(originName)
                                    }
                                    maskedName.append("$tempName")

                                }

                                tv_membername.setText(maskedName)

                            } else if (code == WebParams.LOGOUT_CODE) {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(this@SearchMemberToVerifyActivity, message)
                            } else {
                                code = model.error_message
                                Toast.makeText(this@SearchMemberToVerifyActivity, code, Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            if (progdialog!!.isShowing())
                                progdialog!!.dismiss()
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }

    }
}