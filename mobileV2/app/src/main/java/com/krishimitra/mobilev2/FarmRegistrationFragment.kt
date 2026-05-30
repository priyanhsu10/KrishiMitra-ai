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

class FarmRegistrationFragment : Fragment() {

    private lateinit var viewModel: FarmRegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sessionManager = SessionManager(requireContext())
        
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FarmRegistrationViewModel(sessionManager) as T
            }
        })[FarmRegistrationViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    FarmRegistrationScreen(
                        viewModel = viewModel,
                        onSuccess = { farmId ->
                            val bundle = Bundle().apply {
                                putString("farm_id", farmId)
                            }
                            findNavController().navigate(R.id.action_FarmRegistrationFragment_to_CropRegistrationFragment, bundle)
                        }
                    )
                }
            }
        }
    }
}
