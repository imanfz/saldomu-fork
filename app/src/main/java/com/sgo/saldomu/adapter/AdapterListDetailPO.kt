package com.sgo.saldomu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.models.MappingItemsItem

class AdapterListDetailPO(var context: Context, var itemList: List<MappingItemsItem>) : RecyclerView.Adapter<AdapterListDetailPO.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.item_detail_po, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val qtyBAL = itemList[position].format_qty[0].mapping_qty.toString()
        val qtySLOP = itemList[position].format_qty[1].mapping_qty.toString()
        val qtyPACK = itemList[position].format_qty[2].mapping_qty.toString()
        holder.itemName.text = itemList[position].item_name
        holder.itemQty.text = "$qtyBAL / $qtySLOP / $qtyPACK"
        holder.price.text = context.getString(R.string.currency) + " " + CurrencyFormat.format(itemList[position].price) + " / " + itemList[position].unit
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemName: TextView = itemView.findViewById(R.id.tv_item_name)
        var price: TextView = itemView.findViewById(R.id.tv_price)
        var itemQty: TextView = itemView.findViewById(R.id.tv_qty)
    }
}