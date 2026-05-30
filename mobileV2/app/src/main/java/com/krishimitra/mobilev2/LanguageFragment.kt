package com.krishimitra.mobilev2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.krishimitra.mobilev2.data.SessionManager

class LanguageFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sessionManager = SessionManager(requireContext())

        // Check if language is already selected and user is logged in
        if (sessionManager.getAuthToken() != null) {
            findNavController().navigate(R.id.action_LanguageFragment_to_HomeFragment)
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    LanguageScreen { selectedLang ->
                        selectLanguage(selectedLang)
                    }
                }
            }
        }
    }

    private fun selectLanguage(lang: String) {
        sessionManager.saveLanguage(lang)
        findNavController().navigate(R.id.action_LanguageFragment_to_LoginFragment)
    }
}
