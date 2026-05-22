package com.bac.homelink.presentation.landlord.addlisting

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bac.homelink.core.extensions.collectWithLifecycle
import com.bac.homelink.core.extensions.showToast
import com.bac.homelink.core.ui.UiState
import com.bac.homelink.databinding.ActivityAddListingBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

@AndroidEntryPoint
class AddListingFragment : Fragment() {

    private var _binding: ActivityAddListingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddListingViewModel by viewModels()

    private var selectedDate    = ""
    private var savedImageUri   = ""
    private var savedImageUri2  = ""
    private var savedImageUri3  = ""
    private var editingListingId = 0

    private val houseTypes = listOf(
        "Single Room","2ndhalf (Outbuilding)","Bachelor Flat",
        "Room in Multi-Res","Studio Apartment","2-Bedroom Flat",
        "3-Bedroom House","4-Bedroom House","Hostel Bed (Dorm)",
        "Room to Share (2-bed flat)","Room to Share (3-bed house)",
        "Garden Cottage","Cluster Townhouse","Self-Contained Unit"
    )
    private val roomCounts = listOf(
        "1 Room","2 Rooms","3 Rooms","4+ Rooms","Shared Dorm"
    )
    private val sharingOptions = listOf(
        "Private (No Sharing)","Sharing (2 people)",
        "Sharing (3 people)","Sharing (4+ people)","Hostel Dorm (4+ people)"
    )
    private val locationAreas = listOf(
        "Block 3","Block 5","Block 6","Block 7","Block 8",
        "Phase 2","Phase 4","Mogoditshane","Tlokweng","Gaborone West",
        "Broadhurst","Bonnington","Extension 2","Extension 14","Phakalane",
        "Gaborone North","Gaborone Central","Old Naledi","Tsholofelo","Mmopane"
    )
    private val amenityOptions = listOf(
        "WiFi","Water","Electricity","Security Guard","Swimming Pool"
    )

    // Track selected amenities
    private val selectedAmenities = mutableSetOf<String>()

    // Photo pickers (main + 2 extras)
    private var activePhotoSlot = 1
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                val path = copyImageToStorage(uri)
                when (activePhotoSlot) {
                    1 -> {
                        savedImageUri = path
                        binding.ivPhotoPreview1.setImageURI(uri)
                        binding.ivPhotoPreview1.visibility = View.VISIBLE
                    }
                    2 -> {
                        savedImageUri2 = path
                        binding.ivPhotoPreview2.setImageURI(uri)
                        binding.ivPhotoPreview2.visibility = View.VISIBLE
                    }
                    3 -> {
                        savedImageUri3 = path
                        binding.ivPhotoPreview3.setImageURI(uri)
                        binding.ivPhotoPreview3.visibility = View.VISIBLE
                    }
                }
                binding.tvPhotoStatus.text = "Photo${if (activePhotoSlot > 1) " $activePhotoSlot" else ""} selected"
                binding.tvPhotoStatus.setTextColor(requireContext().getColor(com.bac.homelink.R.color.status_available))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityAddListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if we're editing an existing listing
        editingListingId = arguments?.getInt("listing_id", 0) ?: 0
        if (editingListingId > 0) {
            viewModel.loadForEditing(editingListingId)
        }

        setupDropdowns()
        setupAmenityChips()

        binding.btnPickAvailDate.setOnClickListener { showDatePicker() }

        binding.btnPickPhoto1.setOnClickListener { activePhotoSlot = 1; openGallery() }
        binding.btnPickPhoto2.setOnClickListener { activePhotoSlot = 2; openGallery() }
        binding.btnPickPhoto3.setOnClickListener { activePhotoSlot = 3; openGallery() }

        binding.btnSubmitListing.setOnClickListener { submitListing() }

        // Pre-fill fields if editing
        viewModel.existingListing.collectWithLifecycle(viewLifecycleOwner) { listing ->
            if (listing == null) return@collectWithLifecycle
            binding.etTitle.setText(listing.title)
            binding.etDescription.setText(listing.description)
            binding.etPrice.setText(listing.pricePerMonth.toString())
            binding.etDeposit.setText(listing.depositAmount.toString())
            if (listing.securityAmount > 0) binding.etSecurity.setText(listing.securityAmount.toString())
            binding.actvAccomType.setText(listing.accommodationType, false)
            binding.actvLocation.setText(listing.location, false)
            binding.etAddress.setText(listing.address)
            binding.etAdditionalNotes.setText(listing.additionalNotes)
            selectedDate = listing.availabilityDate
            binding.tvAvailDate.text = "Available: $selectedDate"
            listing.amenities.forEach { selectedAmenities.add(it) }
            savedImageUri  = listing.imageUrl
            savedImageUri2 = listing.imageUrl2
            savedImageUri3 = listing.imageUrl3
            binding.btnSubmitListing.text = "Update Listing"
            if (listing.imageUrl.isNotEmpty()) {
                val f = File(listing.imageUrl)
                if (f.exists()) binding.ivPhotoPreview1.setImageURI(Uri.fromFile(f))
                binding.ivPhotoPreview1.visibility = View.VISIBLE
            }
        }

        viewModel.state.collectWithLifecycle(viewLifecycleOwner) { state ->
            binding.btnSubmitListing.isEnabled = state !is UiState.Loading
            binding.progressBar.visibility = if (state is UiState.Loading) View.VISIBLE else View.GONE
            if (state is UiState.Error) showToast(state.message)
        }

        viewModel.successEvent.collectWithLifecycle(viewLifecycleOwner) {
            val msg = if (editingListingId > 0) "Listing updated" else "Listing added"
            showToast(msg)
            findNavController().popBackStack()
        }
    }

    private fun setupDropdowns() {
        fun setup(view: com.google.android.material.textfield.MaterialAutoCompleteTextView, items: List<String>) {
            view.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items))
            view.threshold = 0
            view.setOnClickListener { view.showDropDown() }
            view.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) view.showDropDown() }
        }
        setup(binding.actvAccomType, houseTypes)
        setup(binding.actvRoomCount, roomCounts)
        setup(binding.actvSharing, sharingOptions)
        setup(binding.actvLocation, locationAreas)
    }

    private fun setupAmenityChips() {
        val chipGroup = binding.chipGroupAmenities
        chipGroup.removeAllViews()
        amenityOptions.forEach { amenity ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = amenity
                isCheckable = true
                isChecked = selectedAmenities.contains(amenity)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selectedAmenities.add(amenity)
                    else selectedAmenities.remove(amenity)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoPickerLauncher.launch(intent)
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
            binding.tvAvailDate.text = "Available: $selectedDate"
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun submitListing() {
        if (selectedDate.isEmpty()) { showToast("Please pick an availability date"); return }
        val accType = binding.actvAccomType.text.toString().trim()
        if (accType.isEmpty()) { showToast("Please select accommodation type"); return }
        val location = binding.actvLocation.text.toString().trim()
        if (location.isEmpty()) { showToast("Please select a location"); return }
        val priceStr = binding.etPrice.text.toString().trim()
        if (priceStr.isEmpty()) { showToast("Please enter a monthly price"); return }
        val address = binding.etAddress.text.toString().trim()
        val fullAddress = buildAddress(address, location)

        val amenitiesStr = if (selectedAmenities.isNotEmpty())
            selectedAmenities.joinToString(", ")
        else "Water, Electricity"

        val defaultImage = "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800"
        val listing = viewModel.buildListing(
            id          = editingListingId,
            title       = binding.etTitle.text.toString().trim(),
            description = binding.etDescription.text.toString().trim(),
            price       = priceStr.toIntOrNull() ?: 0,
            deposit     = binding.etDeposit.text.toString().toIntOrNull() ?: 0,
            security    = binding.etSecurity.text.toString().toIntOrNull() ?: 0,
            location    = location,
            address     = fullAddress,
            accType     = accType,
            roomCount   = "1 Room",
            sharing     = "Private (No Sharing)",
            amenities   = amenitiesStr,
            date        = selectedDate,
            imageUrl    = if (savedImageUri.isNotEmpty()) savedImageUri else defaultImage,
            imageUrl2   = if (savedImageUri2.isNotEmpty()) savedImageUri2 else "",
            imageUrl3   = if (savedImageUri3.isNotEmpty()) savedImageUri3 else "",
            notes       = binding.etAdditionalNotes.text.toString().trim()
        )
        viewModel.submitListing(listing)
    }

    private fun copyImageToStorage(uri: Uri): String {
        return try {
            val inputStream  = requireContext().contentResolver.openInputStream(uri) ?: return ""
            val fileName     = "listing_${System.currentTimeMillis()}.jpg"
            val file         = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close(); outputStream.close()
            file.absolutePath
        } catch (e: Exception) { "" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private fun buildAddress(address: String, location: String): String {
            val base = address.ifBlank { location }.trim()
            if (base.isBlank()) return ""
            val hasGaborone = base.contains("gaborone", ignoreCase = true)
            val hasBotswana = base.contains("botswana", ignoreCase = true)
            return buildString {
                append(base)
                if (location.isNotBlank() && !base.contains(location, ignoreCase = true)) {
                    append(", ").append(location)
                }
                if (!hasGaborone) append(", Gaborone")
                if (!hasBotswana) append(", Botswana")
            }
        }
    }
}
