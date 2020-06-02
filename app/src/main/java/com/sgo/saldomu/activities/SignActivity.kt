package com.sgo.saldomu.activities

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.widget.Toast
import com.github.gcacace.signaturepad.views.SignaturePad
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_sign.*
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class SignActivity : BaseActivity(), PermissionCallbacks {

    private val RC_REQUEST_WRITE_EXTERNAL_STORAGE_AND_PRINT = 112
    val RESULT_SIGNATURE = 230
    var signed: Boolean = false
    var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeToolbar()
        signature_pad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                signed = true
            }

            override fun onClear() {
                signed = false
            }

            override fun onSigned() {

            }

        })
        ib_refresh.setOnClickListener { signature_pad.clear() }
        btn_submit.setOnClickListener { submitSignature() }
    }

    private fun submitSignature() {
        if (inputValidation()) {
            if (EasyPermissions.hasPermissions(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                getSignaturePad()
            else
                EasyPermissions.requestPermissions(this@SignActivity, getString(R.string.rationale_save_image_permission), RC_REQUEST_WRITE_EXTERNAL_STORAGE_AND_PRINT, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_sign
    }

    fun initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.signature)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(InsertPIN.RESULT_CANCEL_ORDER)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getSignaturePad() {
        val signatureBitmap: Bitmap = signature_pad.signatureBitmap
        if (addJpgSignatureToGallery(signatureBitmap)) {
            val i = Intent()
            i.putExtra(DefineValue.SIGNATURE_PHOTO, photoFile)
            setResult(RESULT_SIGNATURE, i)
            finish()
        } else
            Toast.makeText(this@SignActivity, "Unable to store the signature", Toast.LENGTH_SHORT).show()
    }

    private fun addJpgSignatureToGallery(signature: Bitmap): Boolean {
        var result = false
        try {
            val photo = File(
                    getAlbumStorageDir("SignaturePad"),
                    String.format("Signature_%d.jpg", System.currentTimeMillis())
            )
            saveBitmapToJPG(signature, photo)
            scanMediaFile(photo)
//            setParamPhoto(photo)
            photoFile = photo
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    private fun scanMediaFile(photo: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri: Uri = Uri.fromFile(photo)
        mediaScanIntent.data = contentUri
        sendBroadcast(mediaScanIntent)
    }

    @Throws(IOException::class)
    private fun saveBitmapToJPG(bitmap: Bitmap, photo: File) {
        val stream: OutputStream = FileOutputStream(photo)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        stream.close()
    }

    private fun getAlbumStorageDir(albumName: String): File? {
        val file = File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ), albumName
        )
        if (!file.mkdirs()) {
            Timber.e("Directory not created")
        }
        return file
    }

    private fun inputValidation(): Boolean {
        if (!signed) {
            Toast.makeText(this@SignActivity, getString(R.string.rationale_save_image_permission), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
