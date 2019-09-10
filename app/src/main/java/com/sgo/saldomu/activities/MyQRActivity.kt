package com.sgo.saldomu.activities

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.ScanQRUtils
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_my_qr.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MyQRActivity : BaseActivity() {
    private var sourceAcct: String = ""
    private var sourceAcctName: String = ""
    private lateinit var imageBitmap: Bitmap

    override fun getLayoutResource(): Int {
        return R.layout.activity_my_qr
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.lbl_qr_saya)

        val layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resources.displayMetrics.widthPixels)
        iv_qr.layoutParams = layoutParams


        sourceAcct = intent.getStringExtra("sourceAcct")
        sourceAcctName = intent.getStringExtra("sourceAcctName")
        imageBitmap = ScanQRUtils.getInstance(this).generateQRCode(DefineValue.QR_TYPE_FROM_DEFAULT_ACCOUNT, sourceAcct, sourceAcctName)
        name_text_view.text = sourceAcctName
        iv_qr.setImageBitmap(imageBitmap)

        save_button.setOnClickListener {
            storeImage()
        }
    }

    private fun storeImage() {
        val pictureFile: File? = getOutputMediaFile()
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions")
            return
        }
        try {
            val fos = FileOutputStream(pictureFile)
//            imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
            TakeScreenShot(main_linear_layout).compress(Bitmap.CompressFormat.PNG, 90, fos)
            galleryAddPicture(pictureFile.path)
            fos.close()
            Toast.makeText(this, "Berhasil menyimpan gambar", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: " + e.message)
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: " + e.message)
        }
    }

    private fun galleryAddPicture(mCurrentPhotoPath: String) {
        val values = ContentValues()

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.MediaColumns.DATA, mCurrentPhotoPath)

        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    }

    private fun getOutputMediaFile(): File? {
        var mediaStorageDir = File(Environment.getExternalStorageDirectory().path, this.getString(R.string.appname) + "Image")

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        var mediaFile: File
        val mImageName: String = "SALDOMU_" + System.currentTimeMillis() + ".jpg"
        mediaFile = File(mediaStorageDir.path + File.separator + mImageName)
        return mediaFile
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

    fun TakeScreenShot(rootView: View): Bitmap {

        val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        rootView.draw(canvas)
        return bitmap
    }

    companion object {
        internal const val TAG = "MyQRActivity"
    }

}