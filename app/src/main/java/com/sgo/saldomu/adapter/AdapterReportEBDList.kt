package com.sgo.saldomu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.models.retrofit.DocListItem

class AdapterReportEBDList(var context: Context, var itemList: List<DocListItem>, var listener: OnClick) : RecyclerView.Adapter<AdapterReportEBDList.Holder>() {

    interface OnClick {
        fun onClick(pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.item_report_ebd, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.layout.setOnClickListener { listener.onClick(position) }
        holder.date.text = itemList[position].createdAt
        holder.docNo.text = itemList[position].docNo
        holder.name.text = itemList[position].commCode
        holder.desc.text = itemList[position].memberCode
        holder.amount.text = CurrencyFormat.format(itemList[position].totalAmount)
        holder.status.text = itemList[position].paidStatusRemark
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layout: LinearLayout = itemView.findViewById(R.id.layout)
        var date: TextView = itemView.findViewById(R.id.text_tgl_trans)
        var docNo: TextView = itemView.findViewById(R.id.text_buss_scheme_name)
        var amount: TextView = itemView.findViewById(R.id.text_amount)
        var status: TextView = itemView.findViewById(R.id.text_tx_status)
        var name: TextView = itemView.findViewById(R.id.text_comm_name)
        var desc: TextView = itemView.findViewById(R.id.text_description)
    }
}