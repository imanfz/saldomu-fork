package com.sgo.saldomu.adapter

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.UpdateProductGoodReceiptAdapter.Holder
import com.sgo.saldomu.fragments.FragInputQtyGoodReceipt
import com.sgo.saldomu.models.ListPOModel
import com.sgo.saldomu.models.retrofit.ItemModel
import java.util.*

class UpdateProductGoodReceiptAdapter(var context: FragmentActivity?, var itemList: List<ItemModel>, var listener: UpdateProductGoodReceiptAdapter.UpdateProductGoodReceiptListener) : RecyclerView.Adapter<Holder>() {
    private var mContext: Activity? = null
    private var itemArrayList: ArrayList<ItemModel>? = null


    fun UpdateProductGoodReceiptAdapter(itemArrayList: ArrayList<ItemModel>, mContext: Activity?,) {
        this.itemArrayList = itemArrayList
        this.mContext = mContext
        this.listener = listener
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var border: View = itemView.findViewById(R.id.border)
        var itemName: TextView = itemView.findViewById(R.id.adapter_item_name_field)
        //BAL
        var itemQty1: EditText = itemView.findViewById(R.id.adapter_item_et_qty_1)
        //SLOP
        var itemQty2: EditText = itemView.findViewById(R.id.adapter_item_et_qty_2)
        //PACK
        var itemQty3: EditText = itemView.findViewById(R.id.adapter_item_et_qty_3)
    }

    interface UpdateProductGoodReceiptListener {
        fun onClick(item: ItemModel?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.item_list_gr_qty, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemName.text = itemList[position].item_name
        holder.itemQty1.setText(itemList[position].format_qty?.get(0)!!.mapping_qty.toString())
        holder.itemQty2.setText(itemList[position].format_qty?.get(1)!!.mapping_qty.toString())
        holder.itemQty3.setText(itemList[position].format_qty?.get(2)!!.mapping_qty.toString())
    }

    override fun getItemCount(): Int {
        return itemList.size

    }

    fun updateData(itemArrayList: ArrayList<ItemModel>) {
        this.itemArrayList = itemArrayList
        notifyDataSetChanged()
    }
}