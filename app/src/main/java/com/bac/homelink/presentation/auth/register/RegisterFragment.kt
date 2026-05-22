package com.bac.homelink.presentation.auth.register

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
import com.bac.homelink.databinding.ActivityRegisterBinding
import com.bac.homelink.domain.model.UserRole
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()
    private var selectedRole = UserRole.STUDENT

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRoleStudent.setOnClickListener {
            selectedRole = UserRole.STUDENT
            binding.tilStudentId.hide()
            binding.btnRoleStudent.backgroundTintList =
                requireContext().getColorStateList(R.color.royal_blue)
            binding.btnRoleStudent.setTextColor(requireContext().getColor(R.color.white))
            binding.btnRoleLandlord.backgroundTintList =
                requireContext().getColorStateList(R.color.gray_200)
            binding.btnRoleLandlord.setTextColor(requireContext().getColor(R.color.text_primary))
        }

        binding.btnRoleLandlord.setOnClickListener {
            selectedRole = UserRole.PROVIDER
            binding.tilStudentId.hide()
            binding.btnRoleLandlord.backgroundTintList =
                requireContext().getColorStateList(R.color.royal_blue)
            binding.btnRoleLandlord.setTextColor(requireContext().getColor(R.color.white))
            binding.btnRoleStudent.backgroundTintList =
                requireContext().getColorStateList(R.color.gray_200)
            binding.btnRoleStudent.setTextColor(requireContext().getColor(R.color.text_primary))
        }

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                fullName        = binding.etFullName.text.toString().trim(),
                studentId       = "",
                email           = binding.etEmail.text.toString().trim(),
                phone           = binding.etPhone.text.toString().trim(),
                password        = binding.etPassword.text.toString(),
                confirmPassword = binding.etConfirmPassword.text.toString(),
                role            = selectedRole
            )
        }

        binding.tvLogin.setOnClickListener { findNavController().popBackStack() }

        viewModel.registerState.collectWithLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                    binding.btnRegister.isEnabled = false
                }
                is UiState.Error -> {
                    binding.progressBar.hide()
                    binding.btnRegister.isEnabled = true
                    showToast(state.message)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.btnRegister.isEnabled = true
                }
                else -> {
                    binding.progressBar.hide()
                    binding.btnRegister.isEnabled = true
                }
            }
        }

        viewModel.navEvent.collectWithLifecycle(viewLifecycleOwner) { event ->
            val actionId = when (event) {
                RegisterNavEvent.ToLandlordDashboard -> R.id.action_register_to_landlord
                RegisterNavEvent.ToHome              -> R.id.action_register_to_home
            }
            findNavController().navigate(actionId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
