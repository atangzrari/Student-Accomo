package com.bac.homelink.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bac.homelink.databinding.ItemReservationBinding
import com.bac.homelink.domain.model.ReservationModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReservationAdapter(
    private val reservations: List<ReservationModel>
) : RecyclerView.Adapter<ReservationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReservationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reservations[position])
    }

    override fun getItemCount() = reservations.size

    inner class ViewHolder(private val binding: ItemReservationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reservation: ReservationModel) {
            binding.tvListingTitle.text  = reservation.listingTitle
            binding.tvLocation.text      = reservation.listingLocation
            binding.tvRefNumber.text     = "Ref: ${reservation.referenceNumber}"
            binding.tvDepositPaid.text   = "BWP ${reservation.depositAmountPaid}"
            binding.tvMoveIn.text        = reservation.moveInDate
            binding.tvPaymentMethod.text = reservation.paymentMethod
            binding.tvStatus.text        =
                if (reservation.status == "CONFIRMED") "Confirmed" else "Cancelled"
            binding.tvDate.text          = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(reservation.createdAt))
        }
    }
}
