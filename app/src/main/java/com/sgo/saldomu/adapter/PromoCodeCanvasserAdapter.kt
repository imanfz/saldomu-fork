package com.sgo.saldomu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.models.PromoCodeBATModel

class PromoCodeCanvasserAdapter(var context: Context?, var promoList: List<PromoCodeBATModel>, var listener: Listener) : RecyclerView.Adapter<PromoCodeCanvasserAdapter.Holder>() {

    interface Listener {
        fun onCheck(position: Int)
        fun onUncheck(position: Int)
    }

    fun updateAdapter(promoList: List<PromoCodeBATModel>) {
        this.promoList = promoList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.input_promo_code_toko_item, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.layout.setOnClickListener {
            if (promoList[position].checked) {
                holder.layout.background = ResourcesCompat.getDrawable(context!!.resources, R.drawable.rounded_background_outline, null)
                listener.onUncheck(position)
            } else {
                holder.layout.background = ResourcesCompat.getDrawable(context!!.resources, R.drawable.rounded_background_outline_primary, null)
                listener.onCheck(position)
            }
        }
        holder.tvDesc.text = promoList[position].desc
        holder.tvCode.text = promoList[position].code

        when (promoList[position].status) {
            "0" -> {
                holder.ivStatus.setImageDrawable(ResourcesCompat.getDrawable(context!!.resources, R.drawable.icon_check_green_round, null))
            }
            "1" -> {
                holder.ivStatus.setImageDrawable(ResourcesCompat.getDrawable(context!!.resources, R.drawable.icon_cancel, null))
            }
            else -> {
                holder.ivStatus.visibility = View.GONE
            }
        }
    }

    private fun onCheck(holder: Holder, position: Int) {
        holder.layout.background = ResourcesCompat.getDrawable(context!!.resources, R.drawable.rounded_background_outline_primary, null)
        listener.onCheck(position)
    }

    private fun unCheck(holder: Holder, position: Int) {
        holder.layout.background = ResourcesCompat.getDrawable(context!!.resources, R.drawable.rounded_background_outline, null)
        listener.onUncheck(position)
    }

    override fun getItemCount(): Int {
        return promoList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layout: LinearLayout = itemView.findViewById(R.id.layout)
        var tvDesc: TextView = itemView.findViewById(R.id.tv_promo_desc)
        var tvCode: TextView = itemView.findViewById(R.id.tv_promo_code)
        var ivStatus: ImageView = itemView.findViewById(R.id.iv_status)
    }
}