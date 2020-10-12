package com.sgo.saldomu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.models.EditFavoriteModel
import kotlinx.android.synthetic.main.edit_favorite_item.view.*

class AdapterCurrFavorite : RecyclerView.Adapter<AdapterCurrFavorite.Holder>() {

    private var isEdit = false

    var itemList: List<EditFavoriteModel>? = null
    var subItemList: List<EditFavoriteModel>? = null
    var context: Context? = null
    var listener: OnClick? = null

    fun AdapterCurrFavorite(isEdit: Boolean, context: Context?, itemList: List<EditFavoriteModel>?, subItemList: List<EditFavoriteModel>?, listener: OnClick?) {
        this.itemList = itemList
        this.subItemList = subItemList
        this.listener = listener
        this.context = context
        this.isEdit = isEdit
    }

    fun AdapterCurrFavorite(isEdit: Boolean, context: Context?, itemList: List<EditFavoriteModel>?, listener: OnClick?) {
        this.itemList = itemList
        this.listener = listener
        this.context = context
        this.isEdit = isEdit
    }

    interface OnClick {
        fun onTap(pos: Int, isEdit: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.edit_favorite_item, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemView.tv_fav_title.text = itemList!![position].getTitle()
        holder.itemView.img.setImageDrawable(ResourcesCompat.getDrawable(context!!.resources, itemList!![position].getImg()!!, null))
    }

    override fun getItemCount(): Int {
        return itemList!!.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}