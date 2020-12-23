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
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.models.EBDCatalogModel
import com.sgo.saldomu.models.retrofit.ItemModel
import java.util.*

class AdapterListAddItemGRCanvasser(var context: Context, var listener: Listener) : RecyclerView.Adapter<AdapterListAddItemGRCanvasser.Holder>(), Filterable {

    var itemList = ArrayList<ItemModel>()

    interface Listener {
        fun onChangeQty(itemCode: String, qty: Int, qtyType: String)
    }

    fun updateAdapter(itemList: ArrayList<ItemModel>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.item_catalog_ebd, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val itemCode = itemList[position].itemCode
        val itemName = itemList[position].itemName
        val price = itemList[position].price
        val unit = itemList[position].unit
//        val maxQty = itemList[position].maxQty
        holder.itemCode.text = itemCode
        holder.itemName.text = itemName
        holder.itemPrice.text = context.getString(R.string.currency) + CurrencyFormat.format(price) + " / " + unit

//        if (itemList[position].formatQty.isNotEmpty()) {
            holder.itemQty1.setText(itemList[position].formatQty[0].mapping_qty.toString())
            holder.itemQty2.setText(itemList[position].formatQty[1].mapping_qty.toString())
            holder.itemQty3.setText(itemList[position].formatQty[2].mapping_qty.toString())
//        } else {
//            holder.itemQty1.setText("")
//            holder.itemQty2.setText("")
//            holder.itemQty3.setText("")
//        }

        holder.itemQty1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var qty = 0
                if (s.toString() != "")
                    qty = s.toString().toInt()
//                if (s.toString() == "0" || s.toString().equals("")) {
//                    holder.itemQty1.removeTextChangedListener(this)
//                    holder.itemQty1.setText("0")
//                    holder.itemQty1.addTextChangedListener(this)
//                }

                listener.onChangeQty(itemCode, qty, DefineValue.BAL)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }
        })

        holder.itemQty2.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        var qty = 0
                        if (s.toString() != "")
                            qty = s.toString().toInt()
//                        if (s.toString() == "0" || s.toString().equals("")) {
//                            holder.itemQty2.removeTextChangedListener(this)
//                            holder.itemQty2.setText("0")
//                            holder.itemQty2.addTextChangedListener(this)
//                        }

                        listener.onChangeQty(itemCode, qty, DefineValue.SLOP)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int,
                                                   count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence, start: Int,
                                               before: Int, count: Int) {

                    }
                })

        holder.itemQty3.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        var qty = 0
                        if (s.toString() != "")
                            qty = s.toString().toInt()
//                        if (s.toString() == "0" || s.toString().equals("")) {
//                            holder.itemQty3.removeTextChangedListener(this)
//                            holder.itemQty3.setText("0")
//                            holder.itemQty3.addTextChangedListener(this)
//                        }

                        listener.onChangeQty(itemCode, qty, DefineValue.PACK)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int,
                                                   count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence, start: Int,
                                               before: Int, count: Int) {
                    }
                })
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemCode: TextView = itemView.findViewById(R.id.adapter_item_id_field)
        var itemName: TextView = itemView.findViewById(R.id.adapter_item_name_field)
        var itemPrice: TextView = itemView.findViewById(R.id.adapter_item_price_field)

        //BAL
        var itemQty1: EditText = itemView.findViewById(R.id.adapter_item_et_qty_1)

        //SLOP
        var itemQty2: EditText = itemView.findViewById(R.id.adapter_item_et_qty_2)

        //PACK
        var itemQty3: EditText = itemView.findViewById(R.id.adapter_item_et_qty_3)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString().toLowerCase(Locale.ROOT)
                val temp = ArrayList<ItemModel>()
                if (charString.isEmpty()) temp.addAll(itemList) else for (model in itemList) {
                    if (model.itemName!!.toLowerCase(Locale.ROOT).contains(charString)) temp.add(model)
                }
                val filterResults = FilterResults()
                filterResults.values = temp
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                itemList = ArrayList(results.values as ArrayList<ItemModel>)
                notifyDataSetChanged()
            }
        }
    }
}