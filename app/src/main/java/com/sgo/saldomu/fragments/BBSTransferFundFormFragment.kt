package com.sgo.saldomu.fragments

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.sgo.saldomu.databinding.FragmentBBSTransferFundFormBinding
import com.sgo.saldomu.models.retrofit.BBSTransModel
import com.sgo.saldomu.widgets.BaseFragment


class BBSTransferFundFormFragment : BaseFragment() {
    val TAG = "com.sgo.saldomu.fragments.BBSTransferFundForm"
    private var _binding: FragmentBBSTransferFundFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var model: BBSTransModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model = arguments?.getSerializable("data") as BBSTransModel
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBBSTransferFundFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun init() {
        binding.apply {
            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, model.cust_id_types)
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    spIdentityType.adapter = adapter

                    spIdentityType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
//                            genderSelectedId = position + 1
                        }

                    }
                }

            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, model.source_of_fund)
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    spSource.adapter = adapter

                    spSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
//                            genderSelectedId = position + 1
                        }

                    }
                }

            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, model.purpose_of_trx)
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    spDestination.adapter = adapter

                    spDestination.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
//                            genderSelectedId = position + 1
                        }

                    }
                }

            backBtn.setOnClickListener { fragManager.popBackStack() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}