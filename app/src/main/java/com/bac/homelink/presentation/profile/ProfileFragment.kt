package com.bac.homelink.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bac.homelink.R
import com.bac.homelink.core.extensions.collectWithLifecycle
import com.bac.homelink.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.collectWithLifecycle(viewLifecycleOwner) { user ->
            if (user == null) return@collectWithLifecycle

            binding.tvName.text        = user.fullName
            binding.tvEmail.text       = user.email
            binding.tvPhone.text       = "Phone: ${user.phone}"
            binding.tvInstitution.text = "Institution: ${user.institution}"
            binding.tvRole.text        = "Student"

            val initials = user.fullName
                .split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
            binding.tvInitials.text = initials

        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ -> viewModel.logout() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        viewModel.logoutEvent.collectWithLifecycle(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_profile_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
