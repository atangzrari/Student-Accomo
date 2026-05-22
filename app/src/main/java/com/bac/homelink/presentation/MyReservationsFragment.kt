package com.bac.homelink.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bac.homelink.adapters.ReservationAdapter
import com.bac.homelink.core.extensions.collectWithLifecycle
import com.bac.homelink.core.utils.SessionManager
import com.bac.homelink.data.dao.ReservationDao
import com.bac.homelink.data.repository.toDomain
import com.bac.homelink.databinding.FragmentReservationsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MyReservationsFragment : Fragment() {

    private var _binding: FragmentReservationsBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var reservationDao: ReservationDao
    @Inject lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reservationDao.getReservationsByUser(session.getUserId())
            .map { list -> list.map { it.toDomain() } }
            .collectWithLifecycle(viewLifecycleOwner) { reservations ->
                binding.tvCount.text = "${reservations.size} Reservation(s)"

                binding.rvReservations.layoutManager = LinearLayoutManager(requireContext())
                binding.rvReservations.adapter = ReservationAdapter(reservations)

                binding.layoutEmpty.visibility =
                    if (reservations.isEmpty()) View.VISIBLE else View.GONE
                binding.rvReservations.visibility =
                    if (reservations.isEmpty()) View.GONE else View.VISIBLE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
