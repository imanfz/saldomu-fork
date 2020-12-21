package com.sgo.saldomu.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.core.content.res.ResourcesCompat
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.*
import com.sgo.saldomu.adapter.GridMenu
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_b2b.*
import kotlinx.android.synthetic.main.frag_b2b_canvasser_menu.*
import kotlinx.android.synthetic.main.frag_grid.*

class FragB2BCanvasserMenu : BaseFragment() {

    private var B2BCanvasserActivity: B2BCanvasserActivity? = null
    private val menuStrings = ArrayList<String>()
    private val menuDrawables = ArrayList<Drawable>()
    private var adapter: GridMenu? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_grid, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        B2BCanvasserActivity = activity as B2BCanvasserActivity
        B2BCanvasserActivity!!.initializeToolbar(getString(R.string.menu_item_title_ebd))

        menuStrings.clear()
        menuDrawables.clear()
        menuStrings.add(getString(R.string.purchase_order))
        menuStrings.add(getString(R.string.good_receipt_title))
        menuStrings.add(getString(R.string.invoice_title))
        menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_biller, null)!!)
        menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_biller, null)!!)
        menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_biller, null)!!)

        adapter = GridMenu(context!!, menuStrings, menuDrawables)
        grid.adapter = adapter
        grid.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            if (menuStrings[i] == getString(R.string.purchase_order))
            {

            }
            else if (menuStrings[i] == getString(R.string.good_receipt_title)) {
                val i = Intent(activity, CanvasserGoodReceiptActivity::class.java)
                switchActivity(i)
            } else if (menuStrings[i] == getString(R.string.invoice_title)){
                val i = Intent(activity, CanvasserInvoiceActivity::class.java)
                switchActivity(i)
            }
        }

    }

    private fun switchActivity(mIntent: Intent) {
        if (activity == null) return
        val fca = activity as B2BCanvasserActivity?
        fca!!.switchActivity(mIntent, MainPage.ACTIVITY_RESULT)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity!!.menuInflater.inflate(R.menu.ab_notification, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.favorite).isVisible = true
        menu.findItem(R.id.notifications).isVisible = false
        menu.findItem(R.id.settings).isVisible = false
        menu.findItem(R.id.search).isVisible = false
        menu.findItem(R.id.cancel).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.favorite) {
            val i = Intent(activity, FavoriteActivity::class.java)
            i.putExtra(DefineValue.IS_FAV_B2B, true)
            switchActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }

}