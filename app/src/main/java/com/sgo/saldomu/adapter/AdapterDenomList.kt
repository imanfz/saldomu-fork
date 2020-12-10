package com.sgo.saldomu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.models.DenomDataItem

class AdapterDenomList(var context: Context, var itemList: List<DenomDataItem>, var listener: OnClick) : RecyclerView.Adapter<AdapterDenomList.Holder>() {
    interface OnClick {
        fun onClick(pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.item_denom_description, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.layout.setOnClickListener {
            listener.onClick(position)
        }
        holder.title.text = itemList[position].itemName
        holder.description.text = itemList[position].itemDescription
        holder.price.text = context.getString(R.string.rp_) + " " + CurrencyFormat.format(itemList[position].itemPrice)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layout: LinearLayout = itemView.findViewById(R.id.layout)
        var title: TextView = itemView.findViewById(R.id.denom_title)
        var description: TextView = itemView.findViewById(R.id.denom_description)
        var price: TextView = itemView.findViewById(R.id.denom_price)
    }
}
