package com.sgo.saldomu.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.fragments.FragCreatePOCanvasser
import com.sgo.saldomu.models.retrofit.ItemModel
import java.util.ArrayList

class AdapterListItemCreatePOCanvasser(var context: Context, var itemList: List<ItemModel>, var listener: FragCreatePOCanvasser) : RecyclerView.Adapter<AdapterListItemCreatePOCanvasser.Holder>()  {
    private var mContext: Activity? = null
    private var itemArrayList: ArrayList<ItemModel>? = null

    fun AdapterListItemConfirmPOCanvasser(itemArrayList: ArrayList<ItemModel>, mContext: Activity?) {
        this.itemArrayList = itemArrayList
        this.mContext = mContext
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.adapter_list_item_confirm, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        val qtyBAL = itemList[position].formatQty.get(0).mapping_qty.toString()
        val qtySLOP = itemList[position].formatQty.get(1).mapping_qty.toString()
        val qtyPACK = itemList[position].formatQty.get(2).mapping_qty.toString()
        val unit = itemList[position].unit
        holder.itemName.text = itemList[position].itemName
        holder.itemQty.setText(qtyBAL + " / " + qtySLOP + " / " + qtyPACK)
        holder.price.setText((MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(itemList[position].price)) + " / " +unit)
        holder.subTotal.setText((MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(itemList[position].subTotalAmount)))
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

    fun updateData(itemArrayList: ArrayList<ItemModel>) {
        this.itemArrayList = itemArrayList
        notifyDataSetChanged()
    }
}