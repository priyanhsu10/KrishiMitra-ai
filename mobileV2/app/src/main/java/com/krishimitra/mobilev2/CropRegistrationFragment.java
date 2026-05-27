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
import com.krishimitra.mobilev2.data.api.CropRequest;
import com.krishimitra.mobilev2.data.model.CropResponse;
import com.krishimitra.mobilev2.databinding.FragmentCropRegistrationBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CropRegistrationFragment extends Fragment {

    private FragmentCropRegistrationBinding binding;
    private String farmId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCropRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            farmId = getArguments().getString("farm_id");
        }

        binding.btnSaveCrop.setOnClickListener(v -> {
            String cropType = binding.etCropType.getText().toString();
            String sowingDate = binding.etSowingDate.getText().toString();
            String stage = binding.etStage.getText().toString();

            if (!cropType.isEmpty() && !sowingDate.isEmpty() && !stage.isEmpty()) {
                saveCrop(cropType, sowingDate, stage);
            } else {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCrop(String cropType, String sowingDate, String stage) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveCrop.setEnabled(false);

        CropRequest request = new CropRequest(farmId, cropType, sowingDate, stage);

        RetrofitClient.INSTANCE.getFarmerApi().createCrop(request).enqueue(new Callback<CropResponse>() {
            @Override
            public void onResponse(Call<CropResponse> call, Response<CropResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveCrop.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Registration Complete!", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(CropRegistrationFragment.this)
                            .navigate(R.id.action_CropRegistrationFragment_to_HomeFragment);
                } else {
                    Toast.makeText(getContext(), "Failed to save crop", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CropResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveCrop.setEnabled(true);
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
