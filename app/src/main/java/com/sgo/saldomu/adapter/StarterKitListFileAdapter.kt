package com.sgo.saldomu.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.sgo.saldomu.R
import com.sgo.saldomu.models.StarterKitFileModel
import com.sgo.saldomu.models.retrofit.HistoryModel
import kotlinx.android.synthetic.main.item_file_starterkit.view.*

class StarterKitListFileAdapter(internal var listener: StarterKitListFileAdapter.StarterKitListener) : RecyclerView.Adapter<ViewHolder>() {

    lateinit var mContext: Context
    var starterKitFileArrayList: MutableList<StarterKitFileModel> = ArrayList()

    interface StarterKitListener {
        fun onClick(model: StarterKitFileModel)
    }

    fun updateData(modelList: List<StarterKitFileModel>) {
        this.starterKitFileArrayList.addAll(modelList)
        notifyDataSetChanged()
    }

    fun clearData() {
        this.starterKitFileArrayList.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return starterKitFileArrayList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_file_starterkit, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model: StarterKitFileModel = starterKitFileArrayList.elementAt(position)
        holder?.tvFileName.text = model.file_title
        holder.imgDownload.setOnClickListener{View -> listener.onClick(model)}
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvFileName = view.tv_fileName
    val imgDownload = view.img_download
}