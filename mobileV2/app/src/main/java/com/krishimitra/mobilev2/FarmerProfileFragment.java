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
import com.krishimitra.mobilev2.data.api.FarmerRequest;
import com.krishimitra.mobilev2.data.model.FarmerResponse;
import com.krishimitra.mobilev2.databinding.FragmentFarmerProfileBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmerProfileFragment extends Fragment {

    private FragmentFarmerProfileBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFarmerProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etName.getText().toString();
            String village = binding.etVillage.getText().toString();
            String state = binding.etState.getText().toString();

            if (!name.isEmpty() && !village.isEmpty() && !state.isEmpty()) {
                saveProfile(name, village, state);
            } else {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile(String name, String village, String state) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        String language = sessionManager.getLanguage();
        String farmerId = sessionManager.getFarmerId();
        FarmerRequest request = new FarmerRequest(name, language, village, state, farmerId);

        RetrofitClient.INSTANCE.getFarmerApi().createFarmer(request).enqueue(new Callback<FarmerResponse>() {
            @Override
            public void onResponse(Call<FarmerResponse> call, Response<FarmerResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.saveFarmerName(response.body().getName());
                    sessionManager.saveState(state);
                    NavHostFragment.findNavController(FarmerProfileFragment.this)
                            .navigate(R.id.action_FarmerProfileFragment_to_FarmRegistrationFragment);
                } else {
                    Toast.makeText(getContext(), "Failed to save profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FarmerResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
