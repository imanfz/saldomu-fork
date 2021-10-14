package com.sgo.saldomu.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.ConfirmationQrisActivity
import com.sgo.saldomu.activities.PayFriendsActivity
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.ScanQRUtils
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.dialogs.DefinedDialog
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.QrModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_scan.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class FragmentScanQris : BaseFragment(), ZXingScannerView.ResultHandler {

    private var mScannerView: ZXingScannerView? = null

    private var qrType: String = ""
    private var benef: String = ""
    private var benefName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.frag_scan, container, false)
        return v
    }

    override fun onStart() {
        doRequestPermission()
        super.onStart()
    }

    override fun onResume() {
        resumeScanner()
        super.onResume()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initScannerView()
    }

    private fun resumeScanner() {
        startScanner()
        mScannerView?.resumeCameraPreview(this)
    }

    private fun startScanner() {
        mScannerView?.startCamera()
    }

    private fun stopScanner() {
        mScannerView?.stopCamera()
    }

    private fun initScannerView() {
        mScannerView = ZXingScannerView(activity)
        mScannerView?.setAutoFocus(true)
        mScannerView?.setResultHandler(this)
        frame_layout_camera.addView(mScannerView)
        startScanner()
    }

    private fun doRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!EasyPermissions.hasPermissions(context!!, Manifest.permission.CAMERA))
                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_camera_and_storage),
                    100,
                    Manifest.permission.CAMERA
                )
        } else {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.CAMERA),
                    100
                )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            100 -> {
                initScannerView()
            }
            else -> {
                /* nothing to do in here */
            }
        }
    }

    override fun onPause() {
        stopScanner()
        super.onPause()
    }

    override fun handleResult(rawResult: Result?) {
        if (rawResult!!.text.contains("qr_type=QR_TYPE_FROM_DEFAULT_ACCOUNT")) {
            if (sp.getBoolean(DefineValue.ALLOW_TRANSFER, false)) {
                divideResult(rawResult.text.toString())
                val qrModel = QrModel(benef, benefName, qrType)
                requireActivity().finish()
                val i = Intent(activity, PayFriendsActivity::class.java)
                i.putExtra(DefineValue.QR_OBJ, qrModel)
                startActivity(i)
            } else
                dialogUnavailable()
        } else
            dialogUnavailable()
//            parsingQR(rawResult.text.toString())

    }

    private fun dialogUnavailable() {
        DefinedDialog.MessageDialog(
            activity, getString(R.string.alertbox_title_information),
            getString(R.string.level_dialog_title)
        ) {}.show()
    }

    private fun divideResult(rawResult: String) {
        val array = arrayOfNulls<String>(10)
        for ((i, value) in rawResult.split(ScanQRUtils.SCAN_QR_SEPARATOR).toTypedArray()
            .withIndex()) {
            array[i] = value
            when {
                array[i]!!.contains(DefineValue.QR_TYPE) -> {
                    qrType =
                        array[i]!!.substring(array[i]!!.indexOf(ScanQRUtils.EQUALS_SEPARATOR) + 1)
                }
                array[i]!!.contains(DefineValue.NO_HP_BENEF) -> {
                    benef =
                        array[i]!!.substring(array[i]!!.indexOf(ScanQRUtils.EQUALS_SEPARATOR) + 1)
                    Timber.d("benef:$benef")
                }
                array[i]!!.contains(DefineValue.SOURCE_ACCT_NAME) -> {
                    benefName =
                        array[i]!!.substring(array[i]!!.indexOf(ScanQRUtils.EQUALS_SEPARATOR) + 1)
                    Timber.d("benefName:$benefName")
                }
            }
        }
    }

    private fun parsingQR(qrisString: String?) {
        showProgressDialog()
        val params =
            RetrofitService.getInstance().getSignature(MyApiClient.LINK_QRIS_PARSING, qrisString)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.QRIS_STRING] = qrisString

        Timber.d("isi params qris parsing:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_QRIS_PARSING, params,
            object : ObjListeners {
                override fun onResponses(response: JSONObject) {
                    val code = response.getString(WebParams.ERROR_CODE)
                    val message = response.getString(WebParams.ERROR_MESSAGE)
                    when (code) {
                        WebParams.SUCCESS_CODE -> {
                            val intent = Intent(activity, ConfirmationQrisActivity::class.java)
                            intent.putExtra(DefineValue.RESPONSE, response.toString())
                            startActivity(intent)
                        }
                        WebParams.LOGOUT_CODE -> {
                            AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                        }
                        DefineValue.ERROR_9333 -> {
                            val model = gson.fromJson(response.toString(), jsonModel::class.java)
                            val appModel = model.app_data
                            AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                activity,
                                appModel.type,
                                appModel.packageName,
                                appModel.downloadUrl
                            )
                        }
                        DefineValue.ERROR_0066 -> {
                            AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
                        }
                        else -> {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            resumeScanner()
                        }
                    }
                }

                override fun onError(throwable: Throwable) {
                    dismissProgressDialog()
                }

                override fun onComplete() {
                    dismissProgressDialog()
                }
            })
    }
}