package com.sgo.saldomu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.models.EBDCatalogModel
import com.sgo.saldomu.models.retrofit.ItemModel
import java.util.*

class AdapterListAddItemGRCanvasser(var context: Context, var listener: Listener) : RecyclerView.Adapter<AdapterListAddItemGRCanvasser.Holder>(), Filterable {

    var itemList = ArrayList<EBDCatalogModel>()
    var originalList = ArrayList<EBDCatalogModel>()
    var startDegress = -90f
    var endDegress = 0f

    interface Listener {
        fun onChangeQty(itemCode: String, qty: Int, qtyType: String)
    }

    fun updateAdapter(itemList: ArrayList<EBDCatalogModel>) {
        this.itemList = itemList
        this.originalList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.item_catalog_ebd, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val itemImage = itemList[position].itemImage
        val itemCode = itemList[position].itemCode
        val itemName = itemList[position].itemName
        val description = itemList[position].description
        val price = itemList[position].price
        val discAmount = itemList[position].discAmount
        val itemNettPrice = itemList[position].nettPrice
        val unit = itemList[position].unit
        val remarkList = itemList[position].remarkMappingUnit
        if (itemImage != "") {
            holder.itemImage.visibility = View.VISIBLE
            Glide.with(context)
                    .load(itemImage)
                    .into(holder.itemImage)
        } else
            holder.itemImage.visibility = View.GONE

        holder.favoriteIcon.visibility = View.GONE

        holder.itemName.text = itemName
        holder.description.text = description
        holder.itemPrice.text = context.getString(R.string.currency) + CurrencyFormat.format(price)
        holder.itemNettPrice.text = context.getString(R.string.currency) + CurrencyFormat.format(itemNettPrice)

        if (discAmount > 0) {
            holder.itemPrice.paintFlags = holder.itemPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemNettPrice.visibility = View.VISIBLE
            holder.discountIcon.visibility = View.VISIBLE
        } else {
            holder.itemPrice.paintFlags = holder.itemPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemNettPrice.visibility = View.INVISIBLE
            holder.discountIcon.visibility = View.GONE
        }

        if (remarkList.isNotEmpty()) {
            holder.itemRemark.text = remarkList[0] + " | " + remarkList[1]
            holder.itemRemark.visibility = View.GONE

            holder.arrowRemark.setOnClickListener {
                startDegress += 180
                endDegress += 180
                val anim = RotateAnimation(startDegress, endDegress, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f)

                anim.interpolator = LinearInterpolator()
                anim.repeatCount = 0
                anim.fillAfter = true
                anim.duration = 300

                if (holder.itemRemark.visibility == View.VISIBLE)
                    holder.itemRemark.visibility = View.GONE
                else
                    holder.itemRemark.visibility = View.VISIBLE

                holder.arrowRemark.startAnimation(anim)
            }
        } else {
            holder.itemRemark.visibility = View.GONE
            holder.layoutRemark.visibility = View.GONE
        }

        if (itemList[position].formatQtyItem.size > 0) {
            holder.itemQty1.setText(itemList[position].formatQtyItem[0].mapping_qty.toString())
            holder.itemQty2.setText(itemList[position].formatQtyItem[1].mapping_qty.toString())
            holder.itemQty3.setText(itemList[position].formatQtyItem[2].mapping_qty.toString())
        }
        holder.favoriteIcon.visibility = View.GONE
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

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView = itemView.findViewById(R.id.adapter_item_image)
        var itemName: TextView = itemView.findViewById(R.id.adapter_item_name_field)
        var description: TextView = itemView.findViewById(R.id.adapter_description_field)
        var itemPrice: TextView = itemView.findViewById(R.id.adapter_item_price_field)
        var itemNettPrice: TextView = itemView.findViewById(R.id.adapter_item_nett_price_field)
        var itemRemark: TextView = itemView.findViewById(R.id.adapter_item_remark_field)
        var discountIcon: ImageView = itemView.findViewById(R.id.adapter_discount_icon)
        var favoriteIcon: ToggleButton = itemView.findViewById(R.id.adapter_favorite_icon)

        var arrowRemark: ImageView = itemView.findViewById(R.id.arrow_desc)
        var layoutRemark: RelativeLayout = itemView.findViewById(R.id.layout_remark_arrow)

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