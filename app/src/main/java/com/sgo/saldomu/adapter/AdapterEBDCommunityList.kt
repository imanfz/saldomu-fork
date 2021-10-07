package com.sgo.saldomu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.models.EBDCommunityModel

class AdapterEBDCommunityList(var context: Context, var itemList: List<EBDCommunityModel>, var listener: OnClick) : RecyclerView.Adapter<AdapterEBDCommunityList.Holder>() {

    interface OnClick {
        fun onClick(pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.item_ebd, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.layout.setOnClickListener { listener.onClick(position) }
        holder.memberCode.text = itemList[position].member_code
        holder.communityCode.text = itemList[position].comm_code
        holder.shopName.text = itemList[position].shop_name
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layout: LinearLayout = itemView.findViewById(R.id.layout1)
        var communityCode: TextView = itemView.findViewById(R.id.community_code)
        var memberCode: TextView = itemView.findViewById(R.id.member_code)
        var shopName: TextView = itemView.findViewById(R.id.shop_name)
    }
}