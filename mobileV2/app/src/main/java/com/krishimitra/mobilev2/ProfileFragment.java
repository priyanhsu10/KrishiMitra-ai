package com.krishimitra.mobilev2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.krishimitra.mobilev2.data.RetrofitClient;
import com.krishimitra.mobilev2.data.SessionManager;
import com.krishimitra.mobilev2.data.model.FarmerResponse;
import com.krishimitra.mobilev2.databinding.FragmentProfileBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        loadProfileData();

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        
        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.clear();
            NavHostFragment.findNavController(this).navigate(R.id.LanguageFragment);
        });
    }

    private void loadProfileData() {
        String name = sessionManager.getFarmerName();
        String id = sessionManager.getFarmerId();
        String state = sessionManager.getState();
        String lang = sessionManager.getLanguage();

        if (name != null) {
            binding.tvProfileName.setText(name);
            binding.tvInitials.setText(name.substring(0, Math.min(2, name.length())).toUpperCase());
        }
        
        if (id != null) binding.tvProfileId.setText("Farmer ID: " + id);
        if (state != null) binding.tvValState.setText(state);
        
        String langDisplay = "English";
        if ("mr".equalsIgnoreCase(lang)) langDisplay = "मराठी";
        else if ("hi".equalsIgnoreCase(lang)) langDisplay = "हिंदी";
        binding.tvValLang.setText(langDisplay);

        // Fetch latest from API to be sure
        if (id != null) {
            RetrofitClient.INSTANCE.getFarmerApi().getFarmer(id).enqueue(new Callback<FarmerResponse>() {
                @Override
                public void onResponse(Call<FarmerResponse> call, Response<FarmerResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        FarmerResponse farmer = response.body();
                        binding.tvProfileName.setText(farmer.getName());
                    }
                }

                @Override
                public void onFailure(Call<FarmerResponse> call, Throwable t) {
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
