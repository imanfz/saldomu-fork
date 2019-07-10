package com.sgo.saldomu.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DateTimeFormat
import com.sgo.saldomu.models.retrofit.HistoryModel

class HistoryAdapter(internal var listener: HistoryListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal lateinit var context: Context
    internal var itemList: MutableList<HistoryModel> = arrayListOf()

    interface HistoryListener {
        fun onClick(model: HistoryModel)
    }

    fun updateAdapter(itemList: List<HistoryModel>) {
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    fun clearAdapter() {
        this.itemList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return Holder(LayoutInflater.from(context).inflate(R.layout.adapter_history, parent, false))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as Holder

        val model = itemList.elementAt(position)
        if (model.history_detail_type == "Top Up" || model.history_detail_type == "Receive Transfer") {
            holder.amountText.text = "+ Rp. " + model.amount
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.green_A700))
        } else {
            holder.amountText.text = "- Rp. " + model.amount
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.red))
        }

        holder.detailTypeText.text = model.history_detail_type
        holder.dateText.text = DateTimeFormat.changeFormatDate(model.history_datetime)
        holder.txEmoText.text = model.tx_id_emo

        if (model.tx_id == null || model.tx_id == "") {
            holder.itemLinearLayout.isEnabled = false
        } else {
            holder.itemLinearLayout.isEnabled = true
        }

        holder.itemLinearLayout.setOnClickListener { view -> listener.onClick(model) }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    internal inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var amountText: TextView = itemView.findViewById(R.id.amount_text)
        var dateText: TextView = itemView.findViewById(R.id.date_text)
        var detailTypeText: TextView = itemView.findViewById(R.id.detail_type_text)
        var txEmoText: TextView = itemView.findViewById(R.id.tx_emo_text)
        var itemLinearLayout: RelativeLayout = itemView.findViewById(R.id.item_linear_layout)

    }

    companion object {
        private val TAG = "HistoryAdapter"
    }
}
