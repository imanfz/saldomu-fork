package com.sgo.saldomu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.models.EBDCommunityModel
import com.sgo.saldomu.models.MemberListItem

class AdapterListToko(var context: Context, var itemList: List<MemberListItem>, var listener: OnClick) : RecyclerView.Adapter<AdapterListToko.Holder>() {

    interface OnClick {
        fun onClick(pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.item_list_toko, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.btnDetail.setOnClickListener { listener.onClick(position) }
        holder.storeName.text = itemList[position].shopName
        holder.status.text = itemList[position].status
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var storeName: TextView = itemView.findViewById(R.id.store_name)
        var status: TextView = itemView.findViewById(R.id.status)
        var btnDetail: Button = itemView.findViewById(R.id.btn_detail)
    }
}