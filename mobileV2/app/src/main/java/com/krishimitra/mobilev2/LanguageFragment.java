package com.krishimitra.mobilev2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.krishimitra.mobilev2.data.SessionManager;
import com.krishimitra.mobilev2.databinding.FragmentLanguageBinding;

public class LanguageFragment extends Fragment {

    private FragmentLanguageBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLanguageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        // Check if language is already selected and user is logged in
        if (sessionManager.getAuthToken() != null) {
            NavHostFragment.findNavController(LanguageFragment.this)
                    .navigate(R.id.action_LanguageFragment_to_HomeFragment);
            return;
        }

        binding.btnContinue.setOnClickListener(v -> {
            String selectedLang = "en";
            if (binding.rbMarathi.isChecked()) selectedLang = "mr";
            else if (binding.rbHindi.isChecked()) selectedLang = "hi";
            
            selectLanguage(selectedLang);
        });
    }

    private void selectLanguage(String lang) {
        sessionManager.saveLanguage(lang);
        NavHostFragment.findNavController(LanguageFragment.this)
                .navigate(R.id.action_LanguageFragment_to_LoginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
