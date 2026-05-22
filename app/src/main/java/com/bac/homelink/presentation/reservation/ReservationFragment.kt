package com.bac.homelink.presentation.reservation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bac.homelink.R
import com.bac.homelink.core.extensions.collectWithLifecycle
import com.bac.homelink.core.extensions.showToast
import com.bac.homelink.core.ui.UiState
import com.bac.homelink.databinding.ActivityReservationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReservationFragment : Fragment() {

    private var _binding: ActivityReservationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReservationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityReservationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listingId       = arguments?.getInt("listing_id", -1) ?: -1
        val listingTitle    = arguments?.getString("listing_title", "") ?: ""
        val listingLocation = arguments?.getString("listing_location", "") ?: ""
        val depositAmount   = arguments?.getInt("deposit_amount", 0) ?: 0
        val monthlyRent     = arguments?.getInt("monthly_rent", 0) ?: 0
        val moveInDate      = arguments?.getString("move_in_date", "") ?: ""

        binding.tvListingTitle.text    = listingTitle
        binding.tvListingLocation.text = listingLocation
        binding.tvDepositAmount.text   = "BWP $depositAmount"
        binding.tvMoveInDate.text      = "Move-in: $moveInDate"

        // ── Auto-fill student details ──
        viewModel.studentDetails.collectWithLifecycle(viewLifecycleOwner) { details ->
            if (details != null) {
                binding.tvStudentNameDisplay.text  = details.fullName
                binding.tvStudentIdDisplay.text    = details.studentId
                binding.tvStudentEmailDisplay.text = details.email
                binding.tvStudentPhoneDisplay.text = details.phone
            }
        }

        // ── Show/hide card fields based on payment method ──
        binding.layoutCardDetails.visibility = View.VISIBLE

        // ── Card number auto-formatting (XXXX XXXX XXXX XXXX) ──
        binding.etCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digits = s.toString().replace(" ", "")
                val formatted = StringBuilder()
                digits.forEachIndexed { i, c ->
                    if (i > 0 && i % 4 == 0 && formatted.length < 19) formatted.append(' ')
                    formatted.append(c)
                }
                s?.replace(0, s.length, formatted.toString())
                isFormatting = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ── Expiry auto-formatting (MM/YY) ──
        binding.etCardExpiry.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digits = s.toString().replace("/", "")
                val formatted = if (digits.length >= 3)
                    "${digits.substring(0,2)}/${digits.substring(2)}"
                else digits
                s?.replace(0, s.length, formatted)
                isFormatting = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnConfirmPayment.setOnClickListener {
            if (!validateCardDetails()) return@setOnClickListener

            viewModel.submitReservation(
                listingId       = listingId,
                listingTitle    = listingTitle,
                listingLocation = listingLocation,
                depositAmount   = depositAmount,
                moveInDate      = moveInDate,
                paymentMethod   = "Card",
                cardNumber      = binding.etCardNumber.text.toString(),
                cardExpiry      = binding.etCardExpiry.text.toString(),
                cardCvv         = binding.etCardCvv.text.toString()
            )
        }

        viewModel.reservationState.collectWithLifecycle(viewLifecycleOwner) { state ->
            binding.progressBar.visibility =
                if (state is UiState.Loading) View.VISIBLE else View.GONE
            binding.btnConfirmPayment.isEnabled = state !is UiState.Loading
            binding.btnConfirmPayment.text =
                if (state is UiState.Loading) "Processing..." else "Confirm & Pay Deposit"
            if (state is UiState.Error) showToast(state.message)
        }

        viewModel.navEvent.collectWithLifecycle(viewLifecycleOwner) { refNumber ->
            val details = viewModel.studentDetails.value
            val bundle = Bundle().apply {
                putString("reference_number", refNumber)
                putString("listing_title", listingTitle)
                putString("listing_location", listingLocation)
                putInt("deposit_amount", depositAmount)
                putInt("monthly_rent", monthlyRent)
                putString("student_name",    details?.fullName ?: "")
                putString("student_id",      details?.studentId ?: "")
                putString("payment_method",  "Card")
                putString("move_in_date", moveInDate)
            }
            findNavController().navigate(R.id.action_reservation_to_receipt, bundle)
        }
    }

    private fun validateCardDetails(): Boolean {
        binding.tilCardNumber.error = null
        binding.tilCardExpiry.error = null
        binding.tilCardCvv.error = null

        val cardNumber = binding.etCardNumber.text.toString().filter { it.isDigit() }
        val expiry = binding.etCardExpiry.text.toString().trim()
        val cvv = binding.etCardCvv.text.toString().filter { it.isDigit() }

        var isValid = true
        if (cardNumber.length != 16) {
            binding.tilCardNumber.error = "Enter 16 numbers"
            isValid = false
        }
        if (expiry.isBlank()) {
            binding.tilCardExpiry.error = "Expiry required"
            isValid = false
        }
        if (cvv.length != 3) {
            binding.tilCardCvv.error = "Enter 3 numbers"
            isValid = false
        }
        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
