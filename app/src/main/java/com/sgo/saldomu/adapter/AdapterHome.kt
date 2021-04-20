package com.sgo.saldomu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import java.util.*

class AdapterHome(var context: Context, text: ArrayList<String>, drawable: ArrayList<Drawable>, var listener: AdapterHome.OnClick) : RecyclerView.Adapter<AdapterHome.Holder>() {

    private var text = ArrayList<String>()
    private var drawable = ArrayList<Drawable>()

    init {
        this.text = text
        this.drawable = drawable
        notifyDataSetChanged()
    }

    interface OnClick {
        fun onClick(itemName: String)
    }

    override fun getItemCount(): Int {
        return text.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.grid_home, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.layout.setOnClickListener { listener.onClick(text[position]) }
        holder.textView.text = text[position]
        holder.imageView.setImageDrawable(drawable[position])
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layout: LinearLayout = itemView.findViewById(R.id.grid_layout)
        var textView: TextView = itemView.findViewById(R.id.grid_text)
        var imageView: ImageView = itemView.findViewById(R.id.grid_image)
    }
}