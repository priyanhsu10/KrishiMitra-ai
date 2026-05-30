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

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sessionManager = SessionManager(requireContext())
        
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(sessionManager) as T
            }
        })[HomeViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    HomeScreen(
                        viewModel = viewModel,
                        onActionClick = { action ->
                            when (action) {
                                "disease" -> findNavController().navigate(R.id.action_HomeFragment_to_DiseaseDetectionFragment)
                                "advisory" -> findNavController().navigate(R.id.action_HomeFragment_to_AdvisoryChatFragment)
                                "weather" -> findNavController().navigate(R.id.action_HomeFragment_to_WeatherFragment)
                                "mandi" -> findNavController().navigate(R.id.action_HomeFragment_to_MandiFragment)
                                "notifications" -> findNavController().navigate(R.id.action_HomeFragment_to_NotificationsFragment)
                                "profile" -> findNavController().navigate(R.id.action_HomeFragment_to_ProfileFragment)
                                "crop_tracking" -> findNavController().navigate(R.id.action_HomeFragment_to_CropTrackingFragment)
                                "add_crop" -> {
                                    val farmId = viewModel.activeCrop.value?.farm_id ?: viewModel.activeCrop.value?.farm_id
                                    // If no active crop, we need farmId. In ViewModel I should probably store it.
                                    // For now, let's just use what's available.
                                    val currentFarmId = viewModel.activeCrop.value?.farm_id
                                    if (currentFarmId != null) {
                                        val bundle = Bundle().apply { putString("farm_id", currentFarmId) }
                                        findNavController().navigate(R.id.action_HomeFragment_to_CropRegistrationFragment, bundle)
                                    } else {
                                        // Need to handle case where no farm is found yet
                                        Toast.makeText(context, "Farm ID not found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }
}
