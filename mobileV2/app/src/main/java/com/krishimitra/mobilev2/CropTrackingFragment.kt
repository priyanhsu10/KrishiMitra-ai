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

class CropTrackingFragment : Fragment() {

    private lateinit var viewModel: CropTrackingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sessionManager = SessionManager(requireContext())
        
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CropTrackingViewModel(sessionManager) as T
            }
        })[CropTrackingViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    CropTrackingScreen(
                        viewModel = viewModel,
                        onCropClick = { crop ->
                            val bundle = Bundle().apply {
                                putString("crop_id", crop.crop_id)
                                putString("crop_type", crop.crop_type)
                                putString("sowing_date", crop.sowing_date)
                            }
                            findNavController().navigate(R.id.action_CropTrackingFragment_to_CropDetailFragment, bundle)
                        },
                        onAddCropClick = { farmId ->
                            val bundle = Bundle().apply {
                                putString("farm_id", farmId)
                            }
                            findNavController().navigate(R.id.action_CropTrackingFragment_to_CropRegistrationFragment, bundle)
                        }
                    )
                }
            }
        }
    }
}
