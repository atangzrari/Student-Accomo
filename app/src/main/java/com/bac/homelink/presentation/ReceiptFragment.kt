package com.bac.homelink.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bac.homelink.R
import com.bac.homelink.databinding.ActivityReceiptBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ReceiptFragment : Fragment() {

    private var _binding: ActivityReceiptBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments
        val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        val refNumber     = args?.getString("reference_number") ?: "N/A"
        val studentName   = args?.getString("student_name") ?: ""
        val studentId     = args?.getString("student_id") ?: ""
        val listingTitle  = args?.getString("listing_title") ?: ""
        val location      = args?.getString("listing_location") ?: ""
        val monthlyRent   = args?.getInt("monthly_rent", 0) ?: 0
        val depositAmount = args?.getInt("deposit_amount", 0) ?: 0
        val payMethod     = args?.getString("payment_method") ?: ""
        val moveInDate    = args?.getString("move_in_date") ?: ""

        binding.tvRefNumber.text     = refNumber
        binding.tvStudentName.text   = studentName
        binding.tvStudentId.text     = studentId
        binding.tvListingTitle.text  = listingTitle
        binding.tvLocation.text      = location
        binding.tvMonthlyRent.text   = "BWP $monthlyRent/month"
        binding.tvDepositPaid.text   = "BWP $depositAmount"
        binding.tvPaymentMethod.text = payMethod
        binding.tvMoveInDate.text    = moveInDate
        binding.tvPaymentDate.text   = date
        binding.tvStatus.text        = "✅ PAYMENT CONFIRMED"

        binding.btnBackHome.setOnClickListener {
            findNavController().navigate(R.id.action_receipt_to_home)
        }

        binding.btnShareReceipt.setOnClickListener {
            val shareText = buildString {
                appendLine("HomeLink Receipt")
                appendLine("Ref: $refNumber")
                appendLine("Student: $studentName ($studentId)")
                appendLine("Property: $listingTitle")
                appendLine("Location: $location")
                appendLine("Deposit: BWP $depositAmount")
                appendLine("Monthly Rent: BWP $monthlyRent/month")
                appendLine("Payment: $payMethod")
                appendLine("Move-In: $moveInDate")
                appendLine("Date: $date")
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "HomeLink Reservation Receipt - $refNumber")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
