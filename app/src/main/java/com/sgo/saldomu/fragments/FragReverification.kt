package com.sgo.saldomu.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.LoginActivity
import com.sgo.saldomu.activities.Perkenalan
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.layout_reverification_fragment.*


class FragReverification : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.layout_reverification_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        tv_version.text = getString(R.string.appname) + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"
        btn_reverification.setOnClickListener { clearAppData() }
        btn_help.setOnClickListener { openHelp() }
    }

    private fun clearAppData() {
//        try {
//            // clearing app data
//            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
//                (getSystemService(ACTIVITY_SERVICE) as ActivityManager?)!!.clearApplicationUserData() // note: it has a return value!
//            } else {
//                val packageName: String = activity!!.getPackageName()
//                val runtime = Runtime.getRuntime()
//                runtime.exec("pm clear $packageName")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        context!!.cacheDir.deleteRecursively()
        sp.edit().clear().commit()
        startActivity(Intent(activity, Perkenalan::class.java))
        activity!!.finish()
    }

    private fun openHelp()
    {
        val bundle = Bundle()
        bundle.putBoolean(DefineValue.NOT_YET_LOGIN,true)
        val mFrag: Fragment = FragHelp()
        mFrag.arguments = bundle
        switchFragment(mFrag, "Contact", true)
    }

    private fun switchFragment(i: Fragment, name: String, isBackstack: Boolean) {
        if (activity == null) return
        val fca = activity as LoginActivity?
        fca!!.switchContent(i, name, isBackstack)
    }
}