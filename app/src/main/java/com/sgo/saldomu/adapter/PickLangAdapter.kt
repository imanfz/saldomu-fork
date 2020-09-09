package com.sgo.saldomu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import java.util.*

class PickLangAdapter(var context: FragmentActivity?, var list: ArrayList<String>) : RecyclerView.Adapter<PickLangAdapter.holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): holder {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        return holder(LayoutInflater.from(context).inflate(R.layout.pick_language_item, parent, false))
    }

    override fun getItemCount(): Int {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return list.size
    }

    override fun onBindViewHolder(holder: holder, position: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        holder.langItem.text = list.get(position)
    }


    class holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var arrow : TextView = itemView.findViewById(R.id.arrow)
        var langItem : TextView = itemView.findViewById(R.id.lang_item)
}
}