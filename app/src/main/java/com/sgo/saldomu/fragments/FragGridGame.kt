package com.sgo.saldomu.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.res.ResourcesCompat
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.BillerActivity
import com.sgo.saldomu.adapter.GridMenu
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.models.BillerItem
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_grid.*

class FragGridGame : BaseFragment() {

    private val TAG: String = "FragGridGame"

    private val menuStrings = ArrayList<String>()
    private val menuDrawables = ArrayList<Drawable>()
    private var billerData: ArrayList<BillerItem>? = null
    private var adapter: GridMenu? = null

    private var billerTypeCode: String? = null
    private var billerActivity: BillerActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag_grid, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        billerActivity = activity as BillerActivity
        billerActivity!!.setToolbarTitle(getString(R.string.newhome_game))
        billerTypeCode = arguments!!.getString(DefineValue.BILLER_TYPE)
        billerData = arguments!!.getSerializable(DefineValue.BILLER_DATA) as ArrayList<BillerItem>?
        initializeData()
        setTitleAndIcon()
    }

    private fun initializeData() {
        if (billerData != null) {
            adapter = GridMenu(context!!, menuStrings, menuDrawables)
            grid.adapter = adapter
            grid.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                changeToInputBiller(billerData!![position])
            }
        }
    }

    private fun setTitleAndIcon() {
        menuStrings.clear()
        menuDrawables.clear()
        for (i in billerData!!.indices) {
            menuStrings.add(billerData!![i].commName)
            menuDrawables.add(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_game,
                    null
                )!!
            )
//            when {
//                billerData!![i].commName.contains("Mobile Legend",ignoreCase = true) -> menuDrawables.add(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.icon_game_mobile_legends,
//                        null
//                    )!!
//                )
//                billerData!![i].commName.contains("Eternal Love",ignoreCase = true) -> menuDrawables.add(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.icon_game_ragnarok_m,
//                        null
//                    )!!
//                )
//                billerData!![i].commName.contains("Midnight Party",ignoreCase = true) -> menuDrawables.add(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.icon_game_ragnarok_m,
//                        null
//                    )!!
//                )
//                billerData!![i].commName.contains("AOV",ignoreCase = true) -> menuDrawables.add(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.icon_game_aov,
//                        null
//                    )!!
//                )
//                billerData!![i].commName.contains("Free Fire",ignoreCase = true) -> menuDrawables.add(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.icon_game_free_fire,
//                        null
//                    )!!
//                )
//                billerData!![i].commName.contains("Speed Drifters",ignoreCase = true) -> menuDrawables.add(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.icon_game_speed_drifters,
//                        null
//                    )!!
//                )
//                billerData!![i].commName.contains("Era of Celestials",ignoreCase = true) -> menuDrawables.add(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.icon_game_era_of_celestials,
//                        null
//                    )!!
//                )
//                billerData!![i].commName.contains("Bleach Mobile 3D",ignoreCase = true) -> menuDrawables.add(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.icon_game_bleach_mobile_3d,
//                        null
//                    )!!
//                )
//                billerData!![i].commName.contains("Dragon Nest",ignoreCase = true) -> menuDrawables.add(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.icon_game_dragon_nest_m,
//                        null
//                    )!!
//                )
//                else -> menuDrawables.add(
//                    ResourcesCompat.getDrawable(
//                        resources,
//                        R.drawable.ic_game,
//                        null
//                    )!!
//                )
//            }
        }
    }

    private fun changeToInputBiller(billerItem: BillerItem) {
        val bundle = Bundle()
        bundle.putParcelable(DefineValue.BILLER_ITEM, billerItem);
        bundle.putString(DefineValue.BILLER_TYPE, billerTypeCode)

        val fragment = BillerInputGame()
        fragment.arguments = bundle
        billerActivity!!.switchContent1(fragment, billerItem.commName, TAG)
    }
}