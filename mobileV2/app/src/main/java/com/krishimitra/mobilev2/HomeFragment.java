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
import com.krishimitra.mobilev2.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        String name = sessionManager.getFarmerName();
        if (name != null) {
            binding.tvWelcome.setText("Welcome, " + name + "!");
        }

        binding.btnDisease.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_DiseaseDetectionFragment));
        binding.btnAdvisory.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_AdvisoryChatFragment));
        binding.btnWeather.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_WeatherFragment));
        binding.btnMandi.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_MandiFragment));
        binding.btnNotifications.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_NotificationsFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
