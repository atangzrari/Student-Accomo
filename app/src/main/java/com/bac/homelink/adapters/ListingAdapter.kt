package com.bac.homelink.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bac.homelink.R
import com.bac.homelink.databinding.ItemListingCardBinding
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.model.ListingStatus
import com.bumptech.glide.Glide
import java.io.File

class ListingAdapter(
    private val onItemClick: (ListingModel) -> Unit,
    private val onFavouriteClick: (ListingModel) -> Unit
) : ListAdapter<ListingModel, ListingAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListingCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemListingCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(listing: ListingModel) {
            binding.tvTitle.text        = listing.title
            binding.tvLocation.text     = listing.location
            binding.tvPrice.text        = "BWP ${listing.pricePerMonth}/mo"
            binding.tvType.text         = listing.accommodationType
            binding.tvAvailability.text = listing.availabilityDate
            binding.tvSharing?.text     = listing.sharingArrangement

            // Status colour
            val (statusText, statusColour) = when (listing.status) {
                ListingStatus.AVAILABLE   -> "Available" to binding.root.context.getColor(R.color.status_available)
                ListingStatus.RESERVED    -> "Reserved"  to binding.root.context.getColor(R.color.status_reserved)
                ListingStatus.UNAVAILABLE -> "N/A"       to binding.root.context.getColor(R.color.status_unavailable)
            }
            binding.tvStatus.text = statusText
            binding.tvStatus.setTextColor(statusColour)

            // Favourite icon
            binding.btnFavourite?.apply {
                setImageResource(
                    if (listing.isFavourite) R.drawable.ic_heart_filled
                    else R.drawable.ic_heart_outline
                )
                setOnClickListener { onFavouriteClick(listing) }
            }

            // Image — supports local file path or remote URL
            val localFile = File(listing.imageUrl)
            Glide.with(binding.root.context)
                .load(if (localFile.exists()) localFile else listing.imageUrl)
                .placeholder(R.drawable.placeholder_house)
                .centerCrop()
                .into(binding.ivThumbnail)

            binding.root.setOnClickListener { onItemClick(listing) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ListingModel>() {
        override fun areItemsTheSame(oldItem: ListingModel, newItem: ListingModel) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ListingModel, newItem: ListingModel) =
            oldItem == newItem
    }
}
