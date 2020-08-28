package com.sgo.saldomu.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.models.retrofit.FavoriteModel
import timber.log.Timber

class FavoriteAdapter(internal var listener: FavoriteListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal lateinit var context: Context
    internal var itemList: MutableList<FavoriteModel> = arrayListOf()

    interface FavoriteListener {
        fun onShowBillerActivity(model: FavoriteModel)
        fun onShowTransferActivity(model: FavoriteModel)
        fun onStartBBSActivity(model: FavoriteModel)
        fun onDeleteFavorite(model: FavoriteModel)
    }

    fun updateAdapter(itemList: List<FavoriteModel>) {
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    fun clearAdapter() {
        this.itemList.clear()
        notifyDataSetChanged()
    }

    fun removeItem(model: FavoriteModel) {
        this.itemList.remove(model)
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
        holder.customerNameText.text = model.item_name

        if (model.tx_favorite_type == "BBS") {
            if (model.product_type == "CTA") {
                holder.customerNameText.text = context.getText(R.string.cash_in)
            } else {
                holder.customerNameText.text = context.getText(R.string.cash_out)
            }
        }

        Timber.e(model.product_type)

        holder.itemLinearLayout.setOnClickListener {
            if (model.tx_favorite_type == "TRF") {
                listener.onShowTransferActivity(model)
            } else if (model.tx_favorite_type == "BIL") {
                listener.onShowBillerActivity(model)
            } else if (model.tx_favorite_type == "BBS") {
                listener.onStartBBSActivity(model)
            }
        }

        holder.itemLinearLayout.setOnLongClickListener {
            Log.e("model.product_type : ", "s")
            listener.onDeleteFavorite(model)
            return@setOnLongClickListener true
        }

    }

    internal inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var customerIdText: TextView = itemView.findViewById(R.id.customer_id_text)
        var messageText: TextView = itemView.findViewById(R.id.message_text)
        var customerNameText: TextView = itemView.findViewById(R.id.customer_name)
        var itemLinearLayout: RelativeLayout = itemView.findViewById(R.id.item_linear_layout)
    }

}