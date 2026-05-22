package com.bac.homelink.presentation.auth.login

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
import com.bac.homelink.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                email    = binding.etEmail.text.toString().trim(),
                password = binding.etPassword.text.toString()
            )
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        viewModel.loginState.collectWithLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "Logging in…"
                }
                is UiState.Error -> {
                    binding.progressBar.hide()
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Login"
                    showToast(state.message)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Login"
                }
                else -> {
                    binding.progressBar.hide()
                    binding.btnLogin.isEnabled = true
                }
            }
        }

        viewModel.navEvent.collectWithLifecycle(viewLifecycleOwner) { event ->
            val actionId = when (event) {
                LoginNavEvent.ToLandlordDashboard -> R.id.action_login_to_landlord
                LoginNavEvent.ToHome              -> R.id.action_login_to_home
            }
            findNavController().navigate(actionId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
