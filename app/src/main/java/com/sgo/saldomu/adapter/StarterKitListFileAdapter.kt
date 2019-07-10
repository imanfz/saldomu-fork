package com.sgo.saldomu.adapter

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgo.saldomu.R
import com.sgo.saldomu.models.StarterKitFileModel
import kotlinx.android.synthetic.main.item_file_starterkit.view.*
import java.util.*

class StarterKitListFileAdapter() : RecyclerView.Adapter<ViewHolder>() {

    lateinit var mContext: Context
    lateinit var starterKitFileArrayList:ArrayList<StarterKitFileModel>

    fun updateAdapter(modelList : List<StarterKitFileModel>) {
        this.starterKitFileArrayList = starterKitFileArrayList
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
        var model : StarterKitFileModel = starterKitFileArrayList.get(position)
        holder?.tvFileName.text = model.FILE_TITLE
        holder?.imgDownload
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tvFileName = view.tv_fileName
    val imgDownload = view.img_download
}