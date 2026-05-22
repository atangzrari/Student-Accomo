package com.bac.homelink.presentation.landlord.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bac.homelink.R
import com.bac.homelink.core.extensions.collectWithLifecycle
import com.bac.homelink.core.extensions.showToast
import com.bac.homelink.core.ui.UiState
import com.bac.homelink.databinding.ActivityLandlordDashboardBinding
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LandlordDashboardFragment : Fragment() {

    private var _binding: ActivityLandlordDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LandlordViewModel by viewModels()
    @Inject lateinit var authRepo: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityLandlordDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGreeting.text = "Hello ${viewModel.landlordName}"
        binding.tvSubtitle.visibility = View.GONE

        val adapter = LandlordListingAdapter(
            onItemClick = { listing ->
                val bundle = Bundle().apply { putInt("listing_id", listing.id) }
                findNavController().navigate(R.id.action_landlord_to_detail, bundle)
            },
            onMenuClick = { listing, anchorView ->
                showListingMenu(listing, anchorView)
            }
        )
        binding.rvMyListings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyListings.adapter = adapter

        viewModel.myListings.collectWithLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.tvCount.text = "Loading…"
                }
                is UiState.Success -> {
                    adapter.submitList(state.data)
                    val count = state.data.size
                    binding.tvCount.text = "$count Active Listing${if (count != 1) "s" else ""}"
                }
                is UiState.Empty -> {
                    adapter.submitList(emptyList())
                    binding.tvCount.text = "No listings yet — tap + to add one"
                }
                is UiState.Error -> {
                    binding.tvCount.text = "Error loading listings"
                }
            }
        }

        viewModel.toastEvent.collectWithLifecycle(viewLifecycleOwner) { msg ->
            showToast(msg)
        }

        binding.fabAddListing.setOnClickListener {
            findNavController().navigate(R.id.action_landlord_to_add)
        }

        binding.btnViewStudents.setOnClickListener {
            findNavController().navigate(R.id.action_landlord_to_conversations)
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    lifecycleScope.launch {
                        authRepo.logout()
                        findNavController().navigate(R.id.action_landlord_to_login)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showListingMenu(listing: ListingModel, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.listing_options_menu, popup.menu)

        // Toggle availability label
        val toggleItem = popup.menu.findItem(R.id.menu_toggle_availability)
        toggleItem?.title = if (listing.status == com.bac.homelink.domain.model.ListingStatus.AVAILABLE)
            "Mark as Unavailable" else "Mark as Available"

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit_listing -> {
                    val bundle = Bundle().apply { putInt("listing_id", listing.id) }
                    findNavController().navigate(R.id.action_landlord_to_add, bundle)
                    true
                }
                R.id.menu_toggle_availability -> {
                    viewModel.toggleAvailability(listing)
                    true
                }
                R.id.menu_delete_listing -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Listing")
                        .setMessage("Are you sure you want to delete \"${listing.title}\"? This cannot be undone.")
                        .setPositiveButton("Delete") { _, _ -> viewModel.deleteListing(listing) }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
