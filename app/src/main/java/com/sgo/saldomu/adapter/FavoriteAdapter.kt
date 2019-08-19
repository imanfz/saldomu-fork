package com.sgo.saldomu.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.models.retrofit.FavoriteModel
import com.sgo.saldomu.models.retrofit.HistoryModel

class FavoriteAdapter(internal var listener: FavoriteListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal lateinit var context: Context
    internal var itemList: MutableList<FavoriteModel> = arrayListOf()

    interface FavoriteListener {
        fun onClick(model: HistoryModel)
        fun onShowTransferActivity()
    }

    fun updateAdapter(itemList: List<FavoriteModel>) {
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return Holder(LayoutInflater.from(context).inflate(R.layout.adapter_favorite, parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder: Holder = viewHolder as Holder

        val model: FavoriteModel = itemList.elementAt(position)

        holder.customerIdText.text = model.customer_id
        holder.messageText.text = model.notes

        holder.itemLinearLayout.setOnClickListener {
            if (model.product_type == DefineValue.P2P) {
                listener.onShowTransferActivity()
            }
        }

    }

    internal inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var customerIdText: TextView = itemView.findViewById(R.id.customer_id_text)
        var messageText: TextView = itemView.findViewById(R.id.message_text)
        var itemLinearLayout: RelativeLayout = itemView.findViewById(R.id.item_linear_layout)
    }

}