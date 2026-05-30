package com.krishimitra.mobilev2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    LoginScreen(
                        viewModel = viewModel,
                        onOtpSent = { mobile ->
                            val bundle = Bundle().apply {
                                putString("mobile", mobile)
                            }
                            findNavController().navigate(R.id.action_LoginFragment_to_OtpFragment, bundle)
                        },
                        onRegisterClick = {
                            // findNavController().navigate(R.id.action_LoginFragment_to_FarmerProfileFragment)
                        }
                    )
                }
            }
        }
    }
}
