package com.sgo.saldomu.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
import com.sgo.saldomu.models.retrofit.SentExecCustModel
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_upgrade_member.*
import timber.log.Timber

class UpgradeMemberActivity : BaseActivity() {


    override fun getLayoutResource(): Int {
        return R.layout.activity_upgrade_member
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        actionBarTitle = getString(R.string.menu_item_title_upgrade_via_agent)

        setActionBarIcon(R.drawable.ic_arrow_left)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()


        if (sp.getString(DefineValue.COMM_UPGRADE_MEMBER, "") != null)
            if (sp.getString(DefineValue.COMM_UPGRADE_MEMBER, "") == "O") {
                upgrade_via_agent_button.isEnabled == false
            } else if (sp.getString(DefineValue.COMM_UPGRADE_MEMBER, "") == "A") {
                upgrade_online_button.isEnabled == false
            }

        upgrade_online_button.setOnClickListener {
            startActivity(Intent(this, MyProfileNewActivity::class.java))
        }

        upgrade_via_agent_button.setOnClickListener {
            reqUpgradeViaAgent()
        }
    }

    private fun reqUpgradeViaAgent() {
        try {

            showProgressDialog()

            val params = RetrofitService.getInstance()
                    .getSignature(MyApiClient.LINK_REQ_UPGRADE_MEMBER, memberIDLogin)
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.MEMBER_ID] = memberIDLogin
            params[WebParams.COMM_ID] = MyApiClient.COMM_ID

            Timber.d("isi params request upgrade member via agent:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REQ_UPGRADE_MEMBER, params,
                    object : ResponseListener {
                        override fun onResponses(`object`: JsonObject) {
                            val model = gson.fromJson(`object`, SentExecCustModel::class.java)

                            var code = model.error_code
                            if (code == WebParams.SUCCESS_CODE) {
                                dialogSuccess()
                            } else if (code == WebParams.LOGOUT_CODE) {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(this@UpgradeMemberActivity, message)
                            } else {
                                code = model.error_message

                                Toast.makeText(this@UpgradeMemberActivity, code, Toast.LENGTH_LONG).show()
                                fragmentManager.popBackStack()
                            }
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }

    }

    private fun dialogSuccess() {
        var dialognya = DefinedDialog.MessageDialog(this, this!!.getString(R.string.menu_item_title_upgrade_via_agent),
                "Silahkan datang ke Agent terdekat."
        ) { v, isLongClick ->

        }

        dialognya.show()
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