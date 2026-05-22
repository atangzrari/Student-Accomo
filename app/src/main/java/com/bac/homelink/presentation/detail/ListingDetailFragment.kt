package com.bac.homelink.presentation.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bac.homelink.R
import com.bac.homelink.core.extensions.collectWithLifecycle
import com.bac.homelink.core.extensions.hide
import com.bac.homelink.core.extensions.show
import com.bac.homelink.core.extensions.showToast
import com.bac.homelink.core.ui.UiState
import com.bac.homelink.core.utils.SessionManager
import com.bac.homelink.databinding.ActivityListingDetailBinding
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.model.ListingStatus
import com.bac.homelink.domain.repository.ChatRepository
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ListingDetailFragment : Fragment() {

    private var _binding: ActivityListingDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()

    @Inject lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityListingDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.listing.collectWithLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> Unit
                is UiState.Success -> bindListing(state.data)
                is UiState.Error   -> {
                    showToast(state.message)
                    findNavController().popBackStack()
                }
                else -> Unit
            }
        }
    }

    private fun bindListing(listing: ListingModel) {
        // Image
        val localFile = File(listing.imageUrl)
        Glide.with(this)
            .load(if (localFile.exists()) localFile else listing.imageUrl)
            .placeholder(R.drawable.placeholder_house)
            .centerCrop()
            .into(binding.ivListingImage)

        binding.tvTitle.text        = listing.title
        binding.tvLocation.text     = "${listing.location} - ${listing.address}"
        binding.tvPrice.text        = "BWP ${listing.pricePerMonth}/month"
        binding.tvDeposit.text      = buildDepositText(listing)
        binding.tvType.text         = "Type: ${listing.accommodationType}"
        binding.tvRoomCount.text    = "Rooms: ${listing.roomCount}"
        binding.tvSharing.text      = "Sharing: ${listing.sharingArrangement}"
        binding.tvBeds.text         = "${listing.bedroomsCount} Bedroom · ${listing.bathroomsCount} Bathroom"
        binding.tvAmenities.text    = listing.amenities.joinToString(" · ")
        binding.tvAvailability.text = "Available from: ${listing.availabilityDate}"
        binding.tvDescription.text  = listing.description
        val isProvider = session.getUserRole() == "PROVIDER"
        val isListingOwner = listing.landlordEmail.equals(session.getUserEmail(), ignoreCase = true)
        val isLandlordSide = isProvider || isListingOwner
        binding.cardLandlordContact.visibility = if (isLandlordSide) View.GONE else View.VISIBLE
        binding.tvLandlord.text     = "${listing.landlordName} · ${listing.landlordPhone}"

        if (isLandlordSide && listing.status == ListingStatus.RESERVED) {
            val student = listing.reservedStudentName.ifBlank { "Student" }
            val studentId = listing.reservedStudentId.ifBlank { "ID unavailable" }
            val reference = listing.reservationReference.ifBlank { "No reference" }
            val moveIn = listing.reservedMoveInDate.ifBlank { listing.availabilityDate }
            binding.tvReservationInfoDetail.visibility = View.VISIBLE
            binding.tvReservationInfoDetail.text = "Reserved by $student - $studentId\nRef: $reference - Move-in: $moveIn"
        } else {
            binding.tvReservationInfoDetail.visibility = View.GONE
            binding.tvReservationInfoDetail.text = ""
        }

        val studentCanContactLandlord = !isLandlordSide && listing.status == ListingStatus.AVAILABLE
        binding.btnCallLandlord.isEnabled = studentCanContactLandlord
        binding.btnChat.isEnabled = studentCanContactLandlord
        binding.btnCallLandlord.alpha = if (studentCanContactLandlord) 1f else 0.45f
        binding.btnChat.alpha = if (studentCanContactLandlord) 1f else 0.45f
        binding.btnCallLandlord.text = if (listing.status == ListingStatus.RESERVED) "Reserved" else "Call"
        binding.btnChat.text = if (listing.status == ListingStatus.RESERVED) "Reserved" else "Chat"

        if (listing.additionalNotes.isNotEmpty()) {
            binding.tvAdditionalNotes.show()
            binding.tvAdditionalNotes.text = listing.additionalNotes
        } else {
            binding.tvAdditionalNotes.hide()
        }

        // Status chip
        when (listing.status) {
            ListingStatus.AVAILABLE -> {
                binding.chipStatus.text = "Available"
                binding.chipStatus.setChipBackgroundColorResource(R.color.status_available)
                binding.btnReserve.isEnabled = !isLandlordSide
                binding.btnReserve.text = if (isLandlordSide) "Your Listing" else "Reserve Room"
            }
            ListingStatus.RESERVED -> {
                binding.chipStatus.text = "Reserved"
                binding.chipStatus.setChipBackgroundColorResource(R.color.status_reserved)
                binding.btnReserve.isEnabled = false
                binding.btnReserve.text = "Already Reserved"
            }
            else -> {
                binding.chipStatus.text = "Unavailable"
                binding.chipStatus.setChipBackgroundColorResource(R.color.status_unavailable)
                binding.btnReserve.isEnabled = false
                binding.btnReserve.text = "Not Available"
            }
        }

        binding.btnReserve.visibility = if (isLandlordSide) View.GONE else View.VISIBLE

        binding.btnReserve.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("listing_id", listing.id)
                putString("listing_title", listing.title)
                putString("listing_location", listing.location)
                putInt("deposit_amount", listing.depositAmount)
                putInt("monthly_rent", listing.pricePerMonth)
                putString("move_in_date", listing.availabilityDate)
            }
            findNavController().navigate(R.id.action_detail_to_reservation, bundle)
        }

        binding.btnCallLandlord.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.landlordPhone}")))
        }

        binding.btnChat.setOnClickListener {
            // Use deterministic room ID so student & landlord share the same room
            val studentEmail = session.getUserEmail()
            val chatRoomId   = ChatRepository.buildChatRoomId(listing.id, studentEmail)
            val bundle = Bundle().apply {
                putInt("listing_id", listing.id)
                putString("chat_room_id", chatRoomId)
                putString("landlord_name", listing.landlordName)
                putString("landlord_email", listing.landlordEmail)
            }
            findNavController().navigate(R.id.action_detail_to_chat, bundle)
        }
    }

    private fun buildDepositText(listing: ListingModel): String =
        if (listing.securityAmount > 0)
            "Deposit: BWP ${listing.depositAmount}   |   Security: BWP ${listing.securityAmount}"
        else
            "Deposit: BWP ${listing.depositAmount}"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
