package com.sgo.saldomu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.sgo.saldomu.Beans.CustomAdapterModel
import java.util.*

class CustomAutoCompleteAdapter(context: Context, val locList: MutableList<CustomAdapterModel> = ArrayList(), var resource : Int)
    : ArrayAdapter<CustomAdapterModel>(context, resource, locList){

    val locLists: MutableList<CustomAdapterModel>? = locList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view : View? = convertView
        if (view == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(resource, parent, false)
        }

        val text : TextView = view!!.findViewById(android.R.id.text1)

        text.text = locLists!![position].name

        return view
    }

    override fun getCount(): Int {
        return locLists!!.size
    }

    override fun getFilter(): Filter {
        return nameFilter
    }

    var nameFilter: Filter = object : Filter() {

        override fun convertResultToString(resultValue: Any): String? {
            return (resultValue as CustomAdapterModel).name
        }

        override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
            if (constraint != null) {
                val temp: MutableList<CustomAdapterModel>? = ArrayList()

                for (CustomAdapterModel in locList) {
                    if (CustomAdapterModel.name?.toLowerCase(Locale.ROOT)!!.contains(constraint.toString().toLowerCase())) {
                        temp?.add(CustomAdapterModel)
                    }
                }
                val filterResults = Filter.FilterResults()
                filterResults.values = temp
                filterResults.count = temp!!.size
                return filterResults
            } else {
                return Filter.FilterResults()
            }
        }

        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
            val filterList = results.values as ArrayList<CustomAdapterModel>
            locLists?.addAll(filterList)
            notifyDataSetChanged()
        }
    }

}