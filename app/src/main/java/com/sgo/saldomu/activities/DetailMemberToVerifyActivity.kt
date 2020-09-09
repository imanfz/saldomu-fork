package com.sgo.saldomu.activities

import android.Manifest
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.google.gson.JsonObject
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.GlideManager
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.dialogs.DefinedDialog
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.UploadFotoModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.utils.PickAndCameraUtil
import com.sgo.saldomu.utils.camera.CameraActivity
import com.sgo.saldomu.widgets.BaseActivity
import com.sgo.saldomu.widgets.ProgressRequestBody
import kotlinx.android.synthetic.main.activity_detail_member_to_verify.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File

class DetailMemberToVerifyActivity : BaseActivity() {
    private val RESULT_CAMERA_KTP = 201
    private val RC_CAMERA_STORAGE = 14
    private lateinit var pickAndCameraUtil: PickAndCameraUtil
    private val KTP_TYPE = 1
    internal var ktp: File? = null
    private var set_result_photo: Int? = null

    override fun getLayoutResource(): Int {
        return R.layout.activity_detail_member_to_verify
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        initialize()
    }

    private fun initialize() {

        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.menu_item_title_upgrade_member)

        pickAndCameraUtil = PickAndCameraUtil(this)

        camera_ktp_paspor_via_agent.setOnClickListener {
            set_result_photo = RESULT_CAMERA_KTP
            camera_dialog()
        }

//        camera_selfie_ktp_paspor.setOnClickListener {
//            set_result_photo = RESULT_CAMERA_CUST_KTP
//            camera_dialog()
//        }
//
//        camera_ttd.setOnClickListener {
//            set_result_photo = RESULT_CAMERA_TTD
//            camera_dialog()
//        }

        submit_button.setOnClickListener {
            if (ktp != null) {
                sendUpgradeCustData()
            }
        }

    }

    private fun sendUpgradeCustData() {
        try {
            showProgressDialog()

            val params = RetrofitService.getInstance()
                    .getSignature(MyApiClient.LINK_EXEC_UPGRADE_MEMBER, sp.getString(DefineValue.MEMBER_ID_CUST, ""))
            params[WebParams.CUST_ID] = sp.getString(DefineValue.CUST_ID_MEMBER, "")
            params[WebParams.CUST_NAME] = intent.getStringExtra(DefineValue.MEMBER_CUST_NAME)
            params[WebParams.CUST_ID_TYPE] = "KTP"
            params[WebParams.CUST_ID_NUMBER] = intent.getStringExtra(DefineValue.NIK)
            params[WebParams.CUST_BIRTH_PLACE] = intent.getStringExtra(DefineValue.MEMBER_POB)
            params[WebParams.MEMBER_ID] = sp.getString(DefineValue.MEMBER_ID_CUST, "")
            params[WebParams.CUST_BIRTH_DATE] = intent.getStringExtra(DefineValue.MEMBER_DOB)
            params[WebParams.CUST_ADDRESS] = intent.getStringExtra(DefineValue.MEMBER_ADDRESS)
            params[WebParams.CUST_RT] = intent.getStringExtra(DefineValue.MEMBER_RT)
            params[WebParams.CUST_RW] = intent.getStringExtra(DefineValue.MEMBER_RW)
            params[WebParams.CUST_KELURAHAN] = intent.getStringExtra(DefineValue.MEMBER_KELURAHAN)
            params[WebParams.CUST_KECAMATAN] = intent.getStringExtra(DefineValue.MEMBER_KECAMATAN)
            params[WebParams.CUST_KABUPATEN] = intent.getStringExtra(DefineValue.MEMBER_KABUPATEN)
            params[WebParams.CUST_PROVINSI] = intent.getStringExtra(DefineValue.MEMBER_PROVINSI)
            params[WebParams.CUST_RELIGION] = intent.getStringExtra(DefineValue.MEMBER_RELIGION)
            params[WebParams.CUST_MARRIAGE_STATUS] = intent.getStringExtra(DefineValue.MEMBER_STATUS)
            params[WebParams.CUST_OCCUPATION] = intent.getStringExtra(DefineValue.MEMBER_OCUPATION)
            params[WebParams.CUST_NATIONALITY] = intent.getStringExtra(DefineValue.MEMBER_NATIONALITY)
            params[WebParams.CUST_GENDER] = intent.getStringExtra(DefineValue.MEMBER_GENDER)
            params[WebParams.MOTHER_NAME] = intent.getStringExtra(DefineValue.MEMBER_MOTHERS_NAME)
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID] = MyApiClient.COMM_ID
            params[WebParams.IS_REGISTER] = "Y"
            params[WebParams.FROM_AGENT] = "Y"

            Timber.d("isi params upgrade member:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_EXEC_UPGRADE_MEMBER, params,
                    object : ResponseListener {
                        override fun onResponses(response: JsonObject) {
                            val model = gson.fromJson(response, jsonModel::class.java)

                            var code = model.error_code
                            if (code == WebParams.SUCCESS_CODE) {

                                dialogSuccessUploadPhoto()
                            } else if (code == WebParams.LOGOUT_CODE) {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(this@DetailMemberToVerifyActivity, message)
                            } else if (code == DefineValue.ERROR_9333) run {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(this@DetailMemberToVerifyActivity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) run {
                                Timber.d("isi response maintenance:$response")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(this@DetailMemberToVerifyActivity, model.error_message)
                            } else {
                                var msg = model.error_message

                                Toast.makeText(this@DetailMemberToVerifyActivity, msg, Toast.LENGTH_LONG).show()
                                if (code == "0160")
                                    finish()
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

    private fun dialogSuccessUploadPhoto() {
        val dialognya = DefinedDialog.MessageDialog(this@DetailMemberToVerifyActivity, this.getString(R.string.upgrade_member),
                this.getString(R.string.success_upgrade_member_via_agent)
        ) { _, _ -> finish() }

        dialognya.setCanceledOnTouchOutside(false)
        dialognya.setCancelable(false)

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


    fun camera_dialog() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            set_result_photo?.let {
                CameraActivity.openCertificateCamera(this, CameraActivity.TYPE_COMPANY_PORTRAIT)
//                pickAndCameraUtil.runCamera(it)
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage),
                    RC_CAMERA_STORAGE, *perms)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CameraActivity.REQUEST_CODE ->
                if (data != null) {
                    if (CameraActivity.getResult(data) != null) {
                        val path = CameraActivity.getResult(data)
                        ImageCompressionAsyncTask(KTP_TYPE).execute(path)
                    } else {
                        camera_ktp_paspor_via_agent.setImageDrawable(getResources().getDrawable(R.drawable.camera_retry));
                        Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun uploadFileToServer(photoFile: File, flag: Int) {

        extraSignature = (flag).toString()

        val params = RetrofitService.getInstance()
                .getSignature2(MyApiClient.LINK_UPLOAD_KTP, extraSignature)

        val request1 = RequestBody.create(MediaType.parse("text/plain"),
                userPhoneID)
        val request2 = RequestBody.create(MediaType.parse("text/plain"),
                MyApiClient.COMM_ID)
        val request3 = RequestBody.create(MediaType.parse("text/plain"),
                (flag).toString())
        val request4 = RequestBody.create(MediaType.parse("text/plain"),
                sp.getString(DefineValue.CUST_ID_MEMBER, ""))

        params[WebParams.USER_ID] = request1
        params[WebParams.COMM_ID] = request2
        params[WebParams.TYPE] = request3
        params[WebParams.CUST_ID] = request4
        Timber.d("params upload foto ktp: $params")
        Timber.d("params upload foto type: $flag")

        val requestFile = ProgressRequestBody(photoFile,
                ProgressRequestBody.UploadCallbacks { percentage ->
                    when (flag) {
                    }
                })

        val filePart = MultipartBody.Part.createFormData(WebParams.USER_IMAGES, photoFile.name,
                requestFile)

        RetrofitService.getInstance().MultiPartRequest(MyApiClient.LINK_UPLOAD_KTP, params, filePart
        ) { `object` ->
            val model = gson.fromJson(`object`, UploadFotoModel::class.java!!)

            val error_code = model.error_code
            val error_message = model.error_message
            if (error_code.equals("0000", ignoreCase = true)) {
                Timber.d("onsuccess upload foto type: $flag")
            } else if (error_code == WebParams.LOGOUT_CODE) {
                val test = AlertDialogLogout.getInstance()
                test.showDialoginActivity(this@DetailMemberToVerifyActivity, error_message)
            } else if (error_code == DefineValue.ERROR_9333) run {
                Timber.d("isi response app data:" + model.app_data)
                val appModel = model.app_data
                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                alertDialogUpdateApp.showDialogUpdate(this@DetailMemberToVerifyActivity, appModel.type, appModel.packageName, appModel.downloadUrl)
            } else if (error_code == DefineValue.ERROR_0066) run {
                Timber.d("isi response maintenance:$`object`")
                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                alertDialogMaintenance.showDialogMaintenance(this@DetailMemberToVerifyActivity, model.error_message)
            } else {
                Toast.makeText(this@DetailMemberToVerifyActivity, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show()

                if (flag == KTP_TYPE) {
                    camera_ktp_paspor_via_agent.setImageDrawable(getResources().getDrawable(R.drawable.camera_retry));
                }
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    inner class ImageCompressionAsyncTask internal constructor(private val type: Int) : AsyncTask<String, Void, File>() {

        override fun doInBackground(vararg params: String): File {
            return pickAndCameraUtil!!.compressImage(params[0])
        }

        override fun onPostExecute(file: File) {
            when (type) {
                KTP_TYPE -> {
                    GlideManager.sharedInstance().initializeGlideProfile(this@DetailMemberToVerifyActivity, file, camera_ktp_paspor_via_agent)
                    ktp = file
                    uploadFileToServer(ktp!!, KTP_TYPE)
                }
            }
        }
    }

}