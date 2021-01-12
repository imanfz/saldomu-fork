package com.sgo.saldomu.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.models.PromoCodeModel

class PromoCodeTokoAdapter(var context: Context?, var promoList: List<PromoCodeModel>?, var listener: Listener) : RecyclerView.Adapter<PromoCodeTokoAdapter.Holder>() {

    interface Listener {
        fun onChangePromoCode(position: Int, promoCode: String)
        fun onDelete(position: Int)
    }

    fun updateAdapter(promoList: List<PromoCodeModel>) {
        this.promoList = promoList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.input_promo_code_toko_item, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (promoList!![position].status == "1") {
            holder.etPromoCodeName.error = "Kode Tidak Valid"
            holder.ivDelete.visibility = View.VISIBLE
        } else {
            holder.imgStatus.visibility = View.GONE
        }

        holder.etPromoCodeName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                listener.onChangePromoCode(position, p0.toString())
            }
        })
        if (promoList!!.size == 1) {
            holder.ivDelete.visibility = View.INVISIBLE
            holder.ivDelete.setOnClickListener(null)
        } else
            holder.ivDelete.setOnClickListener { listener.onDelete(position) }
    }

    override fun getItemCount(): Int {
        return promoList!!.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var etPromoCodeName: EditText = itemView.findViewById(R.id.et_promo_code_name)
        var ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)
        var imgStatus: ImageView = itemView.findViewById(R.id.img_status)
    }
}