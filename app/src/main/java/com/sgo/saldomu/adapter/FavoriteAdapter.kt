package com.sgo.saldomu.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sgo.saldomu.R
import com.sgo.saldomu.models.retrofit.HistoryModel

class FavoriteAdapter(internal var listener: FavoriteListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal lateinit var context: Context

    interface FavoriteListener {
    }

    fun updateAdapter(itemList: List<HistoryModel>) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return Holder(LayoutInflater.from(context).inflate(R.layout.adapter_history, parent, false))
    }

    override fun getItemCount(): Int {
        return 0
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

    }

    internal inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var amountText: TextView = itemView.findViewById(R.id.amount_text)
        var dateText: TextView = itemView.findViewById(R.id.date_text)

    }

}