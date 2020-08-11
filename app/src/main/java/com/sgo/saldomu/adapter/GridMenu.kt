package com.sgo.saldomu.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.securepreferences.SecurePreferences
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CustomSecurePref
import java.util.ArrayList

class GridMenu(private val mContext: Context, text: ArrayList<String>, drawable: ArrayList<Drawable>) : BaseAdapter() {
    private var text = ArrayList<String>()
    private var drawable = ArrayList<Drawable>()

    init {
        this.text = text
        this.drawable = drawable
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return text.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            holder = ViewHolder()
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.grid_home, parent, false)
            holder.textView = convertView!!.findViewById(R.id.grid_text)
            holder.imageView = convertView.findViewById(R.id.grid_image)

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.textView!!.text = text[position]
        holder.imageView!!.setImageDrawable(drawable[position])

        return convertView
    }

    private inner class ViewHolder {
        internal var textView: TextView? = null
        internal var imageView: ImageView? = null
    }

}