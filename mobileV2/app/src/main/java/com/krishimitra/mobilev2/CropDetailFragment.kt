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
import com.krishimitra.mobilev2.data.SessionManager

class CropDetailFragment : Fragment() {

    private lateinit var viewModel: CropDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sessionManager = SessionManager(requireContext())
        val cropId = arguments?.getString("crop_id") ?: ""
        val cropType = arguments?.getString("crop_type") ?: ""
        val sowingDate = arguments?.getString("sowing_date") ?: ""
        val language = sessionManager.getLanguage()

        viewModel = ViewModelProvider(this)[CropDetailViewModel::class.java]
        viewModel.loadTimeline(cropId)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    CropDetailScreen(
                        viewModel = viewModel,
                        cropType = cropType,
                        sowingDate = sowingDate,
                        language = language
                    )
                }
            }
        }
    }
}
