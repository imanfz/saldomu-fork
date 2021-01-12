package com.sgo.saldomu.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.models.retrofit.ItemModel
import com.sgo.saldomu.adapter.BonusItemGoodReceiptAdapter.Holder
import com.sgo.saldomu.fragments.FragInputQtyGoodReceipt
import java.util.ArrayList

open class BonusItemGoodReceiptAdapter(var context: FragmentActivity?, var itemList: List<ItemModel>, var listener: FragInputQtyGoodReceipt) : RecyclerView.Adapter<Holder>()  {
    private var mContext: Activity? = null
    private var bonusItemArrayList: ArrayList<ItemModel>? = null

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)  {
        var border: View = itemView.findViewById(R.id.border)
        var itemName: TextView = itemView.findViewById(R.id.adapter_item_name_field)

        //BAL
        var itemQty1: TextView = itemView.findViewById(R.id.adapter_item_et_qty_1)

        //SLOP
        var itemQty2: TextView = itemView.findViewById(R.id.adapter_item_et_qty_2)

        //PACK
        var itemQty3: TextView = itemView.findViewById(R.id.adapter_item_et_qty_3)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        return Holder(LayoutInflater.from(context).inflate(R.layout.item_bonus, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemName.text = itemList[position].itemName
        holder.itemQty1.setText(itemList[position].formatQty.get(0).mapping_qty.toString())
        holder.itemQty2.setText(itemList[position].formatQty.get(1).mapping_qty.toString())
        holder.itemQty3.setText(itemList[position].formatQty.get(2).mapping_qty.toString())
    }
    interface bonusItemGoodReceiptListener {
        fun onClick(item: ItemModel?)
    }
    fun updateData(bonusItemArrayList: ArrayList<ItemModel>) {
        this.bonusItemArrayList = bonusItemArrayList
        notifyDataSetChanged()
    }
}