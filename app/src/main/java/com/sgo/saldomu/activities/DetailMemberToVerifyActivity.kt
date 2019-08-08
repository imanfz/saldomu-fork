package com.sgo.saldomu.activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.view.MenuItem
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_detail_member_to_verify.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class DetailMemberToVerifyActivity : BaseActivity() {
    private val RESULT_CAMERA = 99
    private val RC_CAMERA_STORAGE = 14
    private var fileUri: Uri? = null//Uri to capture image
    private val perms2 = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)

    override fun getLayoutResource(): Int {
        return R.layout.activity_detail_member_to_verify
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        actionBarTitle = getString(R.string.menu_item_title_upgrade_member)

        upload_ktp_image_button.setOnClickListener {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
                openCamera(RESULT_CAMERA)
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage),
                        RC_CAMERA_STORAGE, *perms2)
            }
        }

        upload_customer_image_button.setOnClickListener {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
                openCamera(RESULT_CAMERA)
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage),
                        RC_CAMERA_STORAGE, *perms2)
            }
        }

        upload_ktp_and_customer_image_button.setOnClickListener {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
                openCamera(RESULT_CAMERA)
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage),
                        RC_CAMERA_STORAGE, *perms2)
            }
        }

        submit_button.setOnClickListener {
            finish()
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

    private fun openCamera(reqCode: Int) {
        var file = File(
                externalCacheDir,
                System.currentTimeMillis().toString() + ".jpg"
        )
        fileUri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                file
        );

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        }
        startActivityForResult(intent, reqCode)
    }

}