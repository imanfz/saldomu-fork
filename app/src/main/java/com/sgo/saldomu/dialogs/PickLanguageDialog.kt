package com.sgo.saldomu.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.PickLangAdapter
import com.sgo.saldomu.coreclass.DividerItemDecoration
import java.util.*

class PickLanguageDialog : DialogFragment() {

    lateinit var onTap : onClick
    var list = ArrayList<String>()
    lateinit var recyclerview : RecyclerView

    interface onClick{
        fun onTap()
    }

    companion object {
        fun initDialog(listener : onClick): PickLanguageDialog {
            val dialog = PickLanguageDialog()
            dialog.onTap = listener

            return dialog
        }
    }

    override fun onStart() {
        super.onStart()

        if (dialog.window != null){
//            dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pick_language_dialog_layout, container, false)
        recyclerview = view.findViewById(R.id.pick_lang_dialog_list)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        _adapter = PickLangAdapter(activity, list)

        recyclerview.adapter = PickLangAdapter(activity, list)
        recyclerview.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val drawable : Drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity!!.resources.getDrawable(R.drawable.divider, null)
        }else{
            activity!!.resources.getDrawable(R.drawable.divider)
        }
        recyclerview.addItemDecoration(DividerItemDecoration(drawable))

        list.add("Indonesia")
        list.add("Inggris")

        recyclerview.adapter.notifyDataSetChanged()

    }

}