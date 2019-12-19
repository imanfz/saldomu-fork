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
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
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
        actionBarTitle = getString(R.string.menu_item_title_upgrade_member)

        setActionBarIcon(R.drawable.ic_arrow_left)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        upgrade_online_button.setOnClickListener {
            startActivity(Intent(this, MyProfileNewActivity::class.java))
        }

        upgrade_via_agent_button.setOnClickListener {
            if (sp.getString(DefineValue.COMM_UPGRADE_MEMBER, "").equals("O")) {
                DialogCantUpgradeviaAgent()
            } else
                reqUpgradeViaAgent()
        }
    }

    private fun DialogCantUpgradeviaAgent() {
        val dialognya = DefinedDialog.MessageDialog(this, this.getString(R.string.alertbox_title_information),
                this.getString(R.string.cashout_dialog_message)
        ) { v, isLongClick -> }

        dialognya.setCanceledOnTouchOutside(false)
        dialognya.setCancelable(false)
        dialognya.show()

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
                            }  else if (code == DefineValue.ERROR_9333) run {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(this@UpgradeMemberActivity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) run {
                                Timber.d("isi response maintenance:$`object`")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(this@UpgradeMemberActivity, model.error_message)
                            }else {
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
        val dialognya = DefinedDialog.MessageSearchAgent(this, this.getString(R.string.menu_item_title_upgrade_via_agent),
                getString(R.string.come_to_nearest_agent))

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