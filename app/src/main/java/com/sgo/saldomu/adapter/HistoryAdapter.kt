package com.sgo.saldomu.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.securepreferences.SecurePreferences
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DateTimeFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.models.retrofit.HistoryModel

class HistoryAdapter(internal var listener: HistoryListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal lateinit var context: Context
    internal var itemList: MutableList<HistoryModel> = arrayListOf()
    private var agentCOL: Boolean = false
    lateinit var sp: SecurePreferences

    interface HistoryListener {
        fun onClick(model: HistoryModel)
        fun showErrorMessage(message: String)
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

        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        agentCOL = sp.getBoolean(DefineValue.AGENT_COL, false)

        if (position == itemList.size - 1) {
            holder.dividerView.visibility = View.GONE
        }

        val model = itemList.elementAt(position)
        if (model.history_type == "I") {
            holder.amountText.text = "+ Rp. " + CurrencyFormat.format1(model.amount)
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.green_A700))
        } else {
            holder.amountText.text = "- Rp. " + CurrencyFormat.format1(model.amount)
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.red))
        }

        holder.detailTypeText.text = model.history_detail_type
        holder.dateText.text = DateTimeFormat.changeFormatDate(model.history_datetime)
        holder.txEmoText.text = model.tx_id_emo

        holder.itemLinearLayout.setOnClickListener {
            if (model.tx_id == null || model.tx_id == "") {
                listener.showErrorMessage("Detail Tidak Tersedia")
            } else {
                listener.onClick(model)
            }
        }

        if (model.end_balance == "" || model.end_balance == null) {
            if (agentCOL == true) {
                holder.endBalanceText.visibility=View.GONE
            } else
                holder.endBalanceText.text = "Rp. " + CurrencyFormat.format1(0.00)
        } else
            holder.endBalanceText.text = "Rp. " + CurrencyFormat.format1(model.end_balance)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    internal inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var amountText: TextView = itemView.findViewById(R.id.amount_text)
        var endBalanceText: TextView = itemView.findViewById(R.id.endbalance_text)
        var dateText: TextView = itemView.findViewById(R.id.date_text)
        var detailTypeText: TextView = itemView.findViewById(R.id.detail_type_text)
        var txEmoText: TextView = itemView.findViewById(R.id.tx_emo_text)
        var itemLinearLayout: RelativeLayout = itemView.findViewById(R.id.item_linear_layout)
        var dividerView: View = itemView.findViewById(R.id.divider_view)

    }

    companion object {
        private val TAG = "HistoryAdapter"
    }
}
