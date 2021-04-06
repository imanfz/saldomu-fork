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

class AdapterListItemConfirmPO(var context: Context, var itemList: List<MappingItemsItem>) : RecyclerView.Adapter<AdapterListItemConfirmPO.Holder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.adapter_list_item_confirm, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (!itemList[position].format_qty.isEmpty()) {
            val qtyBAL = itemList[position].format_qty[0].mapping_qty.toString()
            val qtySLOP = itemList[position].format_qty[1].mapping_qty.toString()
            val qtyPACK = itemList[position].format_qty[2].mapping_qty.toString()
            holder.itemQty.text = "$qtyBAL / $qtySLOP / $qtyPACK"
        }
        holder.itemName.text = itemList[position].item_name
        holder.price.text = context.getString(R.string.currency) + " " + CurrencyFormat.format(itemList[position].price) + " / " + itemList[position].unit
        holder.subTotal.text = context.getString(R.string.currency) + " " + CurrencyFormat.format(itemList[position].subtotal_amount)
        holder.border.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var border: View = itemView.findViewById(R.id.border)
        var itemName: TextView = itemView.findViewById(R.id.tv_item_name)
        var price: TextView = itemView.findViewById(R.id.tv_price)
        var itemQty: TextView = itemView.findViewById(R.id.tv_qty)
        var subTotal: TextView = itemView.findViewById(R.id.tv_subtotal)
    }
}