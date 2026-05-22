package com.bac.homelink.presentation.landlord.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bac.homelink.R
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.model.ListingStatus
import com.bumptech.glide.Glide
import java.io.File

class LandlordListingAdapter(
    private val onItemClick: (ListingModel) -> Unit,
    private val onMenuClick: (ListingModel, View) -> Unit
) : ListAdapter<ListingModel, LandlordListingAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_landlord_listing, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView   = itemView.findViewById(R.id.iv_listing_thumb)
        private val tvTitle: TextView    = itemView.findViewById(R.id.tv_title)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_location)
        private val tvPrice: TextView    = itemView.findViewById(R.id.tv_price)
        private val tvType: TextView     = itemView.findViewById(R.id.tv_type)
        private val tvStatus: TextView   = itemView.findViewById(R.id.tv_status)
        private val tvReservationInfo: TextView = itemView.findViewById(R.id.tv_reservation_info)
        private val btnMenu: ImageButton = itemView.findViewById(R.id.btn_more)

        fun bind(listing: ListingModel) {
            tvTitle.text    = listing.title
            tvLocation.text = listing.location
            tvPrice.text    = "BWP ${listing.pricePerMonth}/month"
            tvType.text     = listing.accommodationType

            val (statusText, statusColour) = when (listing.status) {
                ListingStatus.AVAILABLE   -> "Available"   to itemView.context.getColor(R.color.status_available)
                ListingStatus.RESERVED    -> "Reserved"    to itemView.context.getColor(R.color.status_reserved)
                ListingStatus.UNAVAILABLE -> "Unavailable" to itemView.context.getColor(R.color.status_unavailable)
            }
            tvStatus.text = statusText
            tvStatus.setTextColor(statusColour)
            if (listing.status == ListingStatus.RESERVED) {
                val student = listing.reservedStudentName.ifBlank { "Student" }
                val studentId = listing.reservedStudentId.ifBlank { "ID unavailable" }
                val reference = listing.reservationReference.ifBlank { "No reference" }
                val moveIn = listing.reservedMoveInDate.ifBlank { listing.availabilityDate }
                tvReservationInfo.visibility = View.VISIBLE
                tvReservationInfo.text = "Reserved by $student - $studentId\nRef: $reference - Move-in: $moveIn"
            } else {
                tvReservationInfo.visibility = View.GONE
                tvReservationInfo.text = ""
            }

            val localFile = File(listing.imageUrl)
            Glide.with(itemView.context)
                .load(if (localFile.exists()) localFile else listing.imageUrl)
                .placeholder(R.drawable.placeholder_house)
                .centerCrop()
                .into(ivImage)

            itemView.setOnClickListener { onItemClick(listing) }
            btnMenu.setOnClickListener { onMenuClick(listing, btnMenu) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ListingModel>() {
            override fun areItemsTheSame(a: ListingModel, b: ListingModel) = a.id == b.id
            override fun areContentsTheSame(a: ListingModel, b: ListingModel) = a == b
        }
    }
}
