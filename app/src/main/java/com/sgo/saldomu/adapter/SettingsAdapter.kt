package com.sgo.saldomu.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CoreApp
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.utils.LocaleManager
import timber.log.Timber

class SettingsAdapter(internal var listener: SettingsListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal lateinit var context: Context
    internal var itemList: MutableList<String> = arrayListOf()
    internal var language = ""

    interface SettingsListener {
        fun onClicked(model: String)
        fun onChangeLanguage(isBahasa: Boolean)
    }

    fun updateAdapter(itemList: List<String>) {
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return if (viewType == 0) {
            ParentHolder(LayoutInflater.from(context).inflate(R.layout.item_settings_header, parent, false))
        } else {
            ChildHolder(LayoutInflater.from(context).inflate(R.layout.item_child_settings, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemList[position] == "Bahasa" || itemList[position] == "Language") {
            1
        } else {
            0
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val model: String = itemList.elementAt(position)

        if (model == "Bahasa" || model == "Language") {

            val holder: ChildHolder = viewHolder as ChildHolder
            holder.contentTextView.text = model

            language = LocaleManager.getLanguagePref()

            Timber.tag("wenly adapter : ").e(language)

            if (language == DefineValue.LANGUAGE_CODE_IND) {
                viewHolder.rb_lang_ind.isChecked = true
                viewHolder.rb_lang_end.isChecked = false
            } else {
                viewHolder.rb_lang_ind.isChecked = false
                viewHolder.rb_lang_end.isChecked = true
            }

            viewHolder.groupLanguage.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_lang_end ->
                        listener.onChangeLanguage(false)
                    R.id.rb_lang_ind ->
                        listener.onChangeLanguage(true)
                }
            }
        } else {
            val holder: ParentHolder = viewHolder as ParentHolder

            holder.contentTextView.text = model
            holder.contentRelativeLayout.setOnClickListener {
                listener.onClicked(model)
            }

        }
    }

    internal inner class ParentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        var contentRelativeLayout: RelativeLayout = itemView.findViewById(R.id.contentRelativeLayout)
    }

    internal inner class ChildHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var groupLanguage: RadioGroup = itemView.findViewById(R.id.groupLanguage)
        var rb_lang_ind: RadioButton = itemView.findViewById(R.id.rb_lang_ind)
        var rb_lang_end: RadioButton = itemView.findViewById(R.id.rb_lang_end)
        var contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
    }

}