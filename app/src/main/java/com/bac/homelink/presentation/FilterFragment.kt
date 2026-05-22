package com.bac.homelink.presentation

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bac.homelink.core.extensions.showToast
import com.bac.homelink.core.utils.SessionManager
import com.bac.homelink.databinding.ActivityFilterBinding
import com.bac.homelink.domain.model.FilterParams
import com.bac.homelink.presentation.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class FilterFragment : Fragment() {

    private var _binding: ActivityFilterBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    @Inject lateinit var session: SessionManager

    private val locations = listOf(
        "All Areas", "Block 3", "Block 5", "Block 6", "Block 7", "Block 8",
        "Phase 2", "Phase 4", "Mogoditshane", "Tlokweng", "Gaborone West",
        "Broadhurst", "Bonnington", "Extension 2", "Extension 14", "Phakalane",
        "Gaborone North", "Gaborone Central", "Old Naledi", "Tsholofelo", "Mmopane"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        loadCurrentFilters()

        binding.sliderPrice.addOnChangeListener { slider, _, _ ->
            updatePriceLabel(slider.values)
        }

        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    binding.tvSelectedDate.text = String.format("%04d-%02d-%02d", year, month + 1, day)
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnApplyFilter.setOnClickListener {
            val params = buildFilterParams()
            homeViewModel.applyFilter(params)
            homeViewModel.saveFilterPreferences(params, binding.switchAlerts.isChecked)
            findNavController().popBackStack()
        }

        binding.btnSavePreferences.setOnClickListener {
            val params = buildFilterParams()
            homeViewModel.saveFilterPreferences(params, binding.switchAlerts.isChecked)
            showToast("Preferences saved")
        }

        binding.btnClearFilter.setOnClickListener {
            homeViewModel.clearFilter()
            findNavController().popBackStack()
        }
    }

    private fun buildFilterParams(): FilterParams {
        val rawLoc = binding.actvLocation.text.toString()
        val rawDate = binding.tvSelectedDate.text.toString()

        return FilterParams(
            maxPrice = (binding.sliderPrice.values.getOrNull(1) ?: 5000f).toInt(),
            location = if (rawLoc == "All Areas") "" else rawLoc,
            availabilityDate = if (rawDate == "Tap to select date") "" else rawDate
        )
    }

    private fun setupDropdowns() {
        fun setupDropdown(
            view: com.google.android.material.textfield.MaterialAutoCompleteTextView,
            items: List<String>
        ) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
            view.setAdapter(adapter)
            view.threshold = 0
            view.setOnClickListener { view.showDropDown() }
            view.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) view.showDropDown() }
        }

        setupDropdown(binding.actvLocation, locations)
    }

    private fun loadCurrentFilters() {
        val params = homeViewModel.filterParams.value

        if (params.maxPrice > 0) {
            binding.sliderPrice.values = listOf(500f, params.maxPrice.toFloat().coerceIn(500f, 5000f))
        }
        updatePriceLabel(binding.sliderPrice.values)

        binding.actvLocation.setText(
            if (params.location.isEmpty()) "All Areas" else params.location, false
        )
        if (params.availabilityDate.isNotEmpty()) {
            binding.tvSelectedDate.text = params.availabilityDate
        }
        binding.switchAlerts.isChecked = homeViewModel.savedFilterAlerts()
    }

    private fun updatePriceLabel(values: List<Float>) {
        val min = (values.getOrNull(0) ?: 500f).toInt()
        val max = (values.getOrNull(1) ?: 5000f).toInt()
        binding.tvPriceValue.text = "Price: BWP $min - $max/month"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
