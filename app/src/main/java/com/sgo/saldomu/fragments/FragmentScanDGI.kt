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

class FragmentScanDGI : BaseFragment(), ZXingScannerView.ResultHandler {

    private var mScannerView: ZXingScannerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!EasyPermissions.hasPermissions(context!!, Manifest.permission.CAMERA))
                EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage), 100, Manifest.permission.CAMERA);
        } else {
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
        parsingQR(rawResult!!.text.toString())
    }

    private fun parsingQR(qrString: String?) {
//        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_QRIS_PARSING, qrString)
        params[WebParams.USER_ID] = userPhoneID

        Timber.d("qrString:$qrString")
        fragManager.popBackStack()
//        Timber.d("isi params qr parsing:$params")
//        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_QRIS_PARSING, params,
//                object : ObjListeners {
//                    override fun onResponses(response: JSONObject) {
//                        val code = response.getString(WebParams.ERROR_CODE)
//                        val message = response.getString(WebParams.ERROR_MESSAGE)
//                        when (code) {
//                            WebParams.SUCCESS_CODE -> {
//
//                            }
//                            WebParams.LOGOUT_CODE -> {
//                                AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
//                            }
//                            DefineValue.ERROR_9333 -> {
//                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
//                                val appModel = model.app_data
//                                AlertDialogUpdateApp.getInstance().showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
//                            }
//                            DefineValue.ERROR_0066 -> {
//                                AlertDialogMaintenance.getInstance().showDialogMaintenance(activity, message)
//                            }
//                            else -> {
//                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                                resumeScanner()
//                            }
//                        }
//                    }
//
//                    override fun onError(throwable: Throwable) {
//                        dismissProgressDialog()
//                    }
//
//                    override fun onComplete() {
//                        dismissProgressDialog()
//                    }
//                })
    }
}