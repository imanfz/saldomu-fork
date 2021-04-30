package com.sgo.saldomu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.sgo.saldomu.R
import com.sgo.saldomu.models.EditMenuModel

class AdapterSubMenu(var context: Context?, var itemList: List<EditMenuModel>?, var listener: OnClick?) : RecyclerView.Adapter<AdapterSubMenu.Holder>() {

    interface OnClick {
        fun onTap(pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.edit_menu_item, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.layout.setOnClickListener { listener!!.onTap(position) }
        holder.title.text = itemList!![position].getTitle()
        holder.image.setImageDrawable(ResourcesCompat.getDrawable(context!!.resources, getImage(position), null))
        holder.status.setImageDrawable(ResourcesCompat.getDrawable(context!!.resources, R.drawable.plus_widget, null))
    }

    private fun getImage(position: Int): Int {
        when (itemList!![position].getTitle()){
            getString(R.string.menu_item_title_mandiri_lkd) -> return R.drawable.ic_mandiri
            getString(R.string.menu_item_title_trx_agent) -> return R.drawable.ic_permintaan_transaksi
            getString(R.string.title_bbs_list_account_bbs) -> return R.drawable.ic_rekening_saya
            getString(R.string.menu_item_title_onprogress_agent) -> return R.drawable.ic_dalam_proses
            getString(R.string.cash_out) -> return R.drawable.ic_tarik_tunai
            getString(R.string.cash_in) -> return R.drawable.ic_setor_tunai
            getString(R.string.menu_item_title_tagih_agent) -> return R.drawable.ic_biller
            getString(R.string.menu_item_title_collector_history) -> return R.drawable.ic_history_collector
            getString(R.string.menu_item_title_upgrade_member) -> return R.drawable.ic_upgrade
            getString(R.string.menu_title_cash_collection) -> return R.drawable.ic_cash_collection
            getString(R.string.menu_item_title_cash_collector_history) -> return R.drawable.ic_history_collector
            getString(R.string.menu_item_title_pulsa_agent) -> return R.drawable.ic_pulsa
            getString(R.string.newhome_listrik_pln) -> return R.drawable.ic_listrik_pln
            getString(R.string.newhome_data) -> return R.drawable.ic_paket_data
            getString(R.string.newhome_bpjs) -> return R.drawable.ic_bpjs
            getString(R.string.newhome_emoney) -> return R.drawable.ic_emoney
            getString(R.string.newhome_game) -> return R.drawable.ic_game
            getString(R.string.newhome_voucher) -> return R.drawable.ic_voucher
            getString(R.string.newhome_pam) -> return R.drawable.ic_pdam
            getString(R.string.menu_item_title_biller) -> return R.drawable.ic_biller
            getString(R.string.menu_item_title_scadm) -> return R.drawable.ic_menu_b2b
            getString(R.string.menu_item_search_agent_bbs) + " " + getString(R.string.cash_in) -> return R.drawable.ic_tarik_tunai
            getString(R.string.menu_item_search_agent_bbs) + " " + getString(R.string.cash_out) -> return R.drawable.ic_setor_tunai
            getString(R.string.title_cash_out_member) -> return R.drawable.ic_permintaan_transaksi
            getString(R.string.menu_item_title_ask_for_money) -> return R.drawable.ic_minta_saldo
            getString(R.string.menu_item_title_report) -> return R.drawable.ic_laporan
            getString(R.string.menu_item_history_detail) -> return R.drawable.ic_history
            getString(R.string.menu_item_lending) -> return R.drawable.ic_lending
            getString(R.string.menu_item_title_b2b_eratel)+ " "+ getString(R.string.menu_item_title_ebd_toko) -> return R.drawable.ic_b2b_eratel
            getString(R.string.menu_item_title_b2b_eratel)+ " "+ getString(R.string.menu_item_title_ebd_canvasser) -> return R.drawable.ic_b2b_eratel
            getString(R.string.menu_item_title_report_ebd) -> return R.drawable.ic_laporan
        }
        return R.drawable.ic_home_default
    }

    private fun getString(id: Int): String {
        return context!!.resources.getString(id)
    }

    override fun getItemCount(): Int {
        return itemList!!.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layout: RelativeLayout = itemView.findViewById(R.id.edit_menu_layout)
        var title: TextView = itemView.findViewById(R.id.tv_title)
        var image: ImageView = itemView.findViewById(R.id.iv_image)
        var status: ImageView = itemView.findViewById(R.id.iv_status)
    }
}