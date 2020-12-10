package com.sgo.saldomu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.models.EBDCatalogModel
import java.util.*

class AdapterEBDCatalogList(var context: Context, var itemList: List<EBDCatalogModel>, var listener: Listener) : RecyclerView.Adapter<AdapterEBDCatalogList.Holder>(), Filterable {

    val originalList = itemList

    interface Listener {
        fun onChangeQty(itemCode: String, itemName: String, qty: Int, price: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.adapter_denom_item_list, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val itemCode = itemList[position].itemCode
        val itemName = itemList[position].itemName
        val price = itemList[position].price.toInt()
        val maxQty = itemList[position].maxQty.toInt()
        holder.itemCode.text = itemCode
        holder.itemName.text = itemName
        holder.itemPrice.text = context.getString(R.string.currency) + " " + CurrencyFormat.format(price)

        holder.itemQty.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val qty: Int = p0.toString().toInt()
                if (qty <= maxQty)
                    listener.onChangeQty(itemCode, itemName, qty, price)
                else {
                    listener.onChangeQty(itemCode, itemName, maxQty, price)
                    holder.itemQty.setText(maxQty)
                }
            }

        })

        if (position == 0)
            holder.border.visibility = View.VISIBLE
        else
            holder.border.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var border: View = itemView.findViewById(R.id.border)
        var itemCode: TextView = itemView.findViewById(R.id.adapter_item_id_field)
        var itemName: TextView = itemView.findViewById(R.id.adapter_item_name_field)
        var itemPrice: TextView = itemView.findViewById(R.id.adapter_item_price_field)
        var itemQty: EditText = itemView.findViewById(R.id.adapter_item_et_qty)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString().toLowerCase(Locale.ROOT)
                val temp = ArrayList<EBDCatalogModel>()
                if (charString.isEmpty()) temp.addAll(originalList) else for (model in originalList) {
                    if (model.itemName.toLowerCase(Locale.ROOT).contains(charString)) temp.add(model)
                }
                val filterResults = FilterResults()
                filterResults.values = temp
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                itemList = ArrayList(results.values as ArrayList<EBDCatalogModel>)
                notifyDataSetChanged()
            }
        }
    }
}