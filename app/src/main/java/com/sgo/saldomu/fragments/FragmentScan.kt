package com.sgo.saldomu.fragments

import com.google.zxing.Result
import com.sgo.saldomu.widgets.BaseFragment
import me.dm7.barcodescanner.zxing.ZXingScannerView

class FragmentScan : BaseFragment(), ZXingScannerView.ResultHandler {
    override fun handleResult(rawResult: Result?) {
        TODO("Not yet implemented")
    }

}