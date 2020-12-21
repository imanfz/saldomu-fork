package com.sgo.saldomu.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.fragments.FragCreateGR
import com.sgo.saldomu.models.retrofit.ItemModel
import java.util.ArrayList

class AdapterListBonusItem (var context: FragmentActivity?, var itemList: List<ItemModel>, var listener: FragCreateGR) : RecyclerView.Adapter<AdapterListBonusItem.Holder>()  {
    private var mContext: Activity? = null
    private var itemArrayList: ArrayList<ItemModel>? = null

    fun AdapterListItemConfirmGR(itemArrayList: ArrayList<ItemModel>, mContext: Activity?) {
        this.itemArrayList = itemArrayList
        this.mContext = mContext
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.adapter_list_item_confirm, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        var qtyBAL = itemList[position].format_qty?.get(0)!!.mapping_qty.toString()
        var qtySLOP = itemList[position].format_qty?.get(1)!!.mapping_qty.toString()
        var qtyPACK = itemList[position].format_qty?.get(2)!!.mapping_qty.toString()
        var unit = itemList[position].unit
        holder.itemName.text = itemList[position].item_name
        holder.itemQty.setText(qtyBAL + " / " + qtySLOP + " / " + qtyPACK)
        holder.price.setText((MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(itemList[position].price)) + " / " +unit)
        holder.subTotal.setText((MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(itemList[position].subtotal_amount)))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    interface ListItemConfirmGRListener {
        fun onClick(item: ItemModel?)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var border: View = itemView.findViewById(R.id.border)
        var itemName: TextView = itemView.findViewById(R.id.tv_item_name)
        var price: TextView = itemView.findViewById(R.id.tv_price)
        var itemQty: TextView = itemView.findViewById(R.id.tv_qty)
        var subTotal: TextView = itemView.findViewById(R.id.tv_subtotal)
    }

    fun updateData(itemArrayList: ArrayList<ItemModel>) {
        this.itemArrayList = itemArrayList
        notifyDataSetChanged()
    }
}