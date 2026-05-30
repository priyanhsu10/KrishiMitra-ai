package com.krishimitra.mobilev2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.krishimitra.mobilev2.data.SessionManager

class CropRegistrationFragment : Fragment() {

    private lateinit var viewModel: CropRegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sessionManager = SessionManager(requireContext())
        val farmId = arguments?.getString("farm_id") ?: ""
        
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CropRegistrationViewModel(sessionManager) as T
            }
        })[CropRegistrationViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    CropRegistrationScreen(
                        viewModel = viewModel,
                        farmId = farmId,
                        onSuccess = {
                            Toast.makeText(context, "Registration Complete!", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_CropRegistrationFragment_to_HomeFragment)
                        }
                    )
                }
            }
        }
    }
}
