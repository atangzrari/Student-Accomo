package com.bac.homelink.presentation.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bac.homelink.R
import com.bac.homelink.adapters.ListingAdapter
import com.bac.homelink.core.extensions.collectWithLifecycle
import com.bac.homelink.core.extensions.hide
import com.bac.homelink.core.extensions.show
import com.bac.homelink.core.extensions.showSnack
import com.bac.homelink.core.ui.UiState
import com.bac.homelink.databinding.FragmentHomeBinding
import com.bac.homelink.domain.model.ListingModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: ListingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        binding.tvGreeting.text = "Hello ${viewModel.userName}"
        binding.tvAppName.visibility = View.GONE
        viewModel.loadSavedFilter()

        // Search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.onSearchQuery(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Filter FAB
        binding.fabFilter.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_filter)
        }

        // Clear filter from empty state button
        binding.btnClearFilterEmpty?.setOnClickListener {
            viewModel.clearFilter()
        }

        // Pull to refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadSavedFilter()
            binding.swipeRefresh.isRefreshing = false
        }

        // Listings state
        viewModel.listings.collectWithLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.tvResultCount.text = "Loading listings…"
                }
                is UiState.Success -> showListings(state.data)
                is UiState.Empty   -> showEmpty()
                is UiState.Error   -> {
                    showEmpty()
                    binding.root.showSnack(state.message)
                }
            }
        }

        // Filter active chip
        viewModel.filterParams.collectWithLifecycle(viewLifecycleOwner) { params ->
            binding.chipFilterActive.visibility =
                if (params.isActive) View.VISIBLE else View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = ListingAdapter(
            onItemClick = { listing ->
                val bundle = android.os.Bundle().apply { putInt("listing_id", listing.id) }
                findNavController().navigate(R.id.action_home_to_detail, bundle)
            },
            onFavouriteClick = { listing -> viewModel.toggleFavourite(listing.id) }
        )
        binding.rvListings.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvListings.adapter = adapter
    }

    private fun showListings(listings: List<ListingModel>) {
        adapter.submitList(listings)
        val count = listings.size
        binding.tvResultCount.text = when {
            count == 0  -> "No listings found"
            count == 1  -> "1 listing found"
            else        -> "$count listings found"
        }
        binding.rvListings.show()
        binding.layoutEmpty.hide()
    }

    private fun showEmpty() {
        adapter.submitList(emptyList())
        binding.tvResultCount.text = "No listings match your criteria"
        binding.rvListings.hide()
        binding.layoutEmpty.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
