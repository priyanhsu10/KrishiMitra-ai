package com.krishimitra.mobilev2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.krishimitra.mobilev2.data.SessionManager

class OtpFragment : Fragment() {

    private lateinit var viewModel: OtpViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sessionManager = SessionManager(requireContext())
        val mobile = arguments?.getString("mobile") ?: "9876543210"

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OtpViewModel(sessionManager) as T
            }
        })[OtpViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    OtpScreen(
                        viewModel = viewModel,
                        mobile = mobile,
                        onSuccess = { isNewUser ->
                            if (isNewUser) {
                                findNavController().navigate(R.id.action_OtpFragment_to_FarmerProfileFragment)
                            } else {
                                findNavController().navigate(R.id.action_OtpFragment_to_HomeFragment)
                            }
                        }
                    )
                }
            }
        }
    }
}
