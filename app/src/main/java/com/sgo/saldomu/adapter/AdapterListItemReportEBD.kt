package com.sgo.saldomu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.models.retrofit.ItemsItem

class AdapterListItemReportEBD(var context: Context, var itemList: List<ItemsItem>) : BaseAdapter() {

    class Holder{
        var itemName: TextView? = null
        var price: TextView? = null
        var itemQty: TextView? = null
        var subTotal: TextView? = null
        var border: View? = null
    }

    override fun getCount(): Int {
        return itemList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val holder: Holder
        if (convertView == null) {
            holder = Holder()
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.adapter_list_item_confirm, parent, false)
            holder.border = convertView.findViewById(R.id.border)
            holder.itemName = convertView.findViewById(R.id.tv_item_name)
            holder.itemQty = convertView.findViewById(R.id.tv_qty)
            holder.price = convertView.findViewById(R.id.tv_price)
            holder.subTotal = convertView.findViewById(R.id.tv_subtotal)

            convertView.tag = holder
        } else {
            holder = convertView.tag as Holder
        }

        holder.itemName!!.text = itemList[position].itemName
        holder.itemQty!!.text = itemList[position].formattedQty
        holder.price!!.text = context.getString(R.string.currency) + " " + CurrencyFormat.format(itemList[position].price)
        holder.subTotal!!.text = context.getString(R.string.currency) + " " + CurrencyFormat.format(itemList[position].subtotal!!)
        if (position != itemList.size)
            holder.border!!.visibility = View.VISIBLE
        return convertView!!
    }
}