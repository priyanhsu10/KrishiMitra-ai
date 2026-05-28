package com.krishimitra.mobilev2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.krishimitra.mobilev2.data.RetrofitClient;
import com.krishimitra.mobilev2.data.SessionManager;
import com.krishimitra.mobilev2.data.api.FarmRequest;
import com.krishimitra.mobilev2.data.model.FarmResponse;
import com.krishimitra.mobilev2.databinding.FragmentFarmRegistrationBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmRegistrationFragment extends Fragment {

    private FragmentFarmRegistrationBinding binding;
    private SessionManager sessionManager;
    private String[] soilTypes = {"काळी माती (Black)", "लाल माती (Red)", "वालुकामय (Sandy)", "पोयटा (Loamy)", "चिकण माती (Clay)"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFarmRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        // Setup Soil Type Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, soilTypes);
        binding.autoSoilType.setAdapter(adapter);

        // In a real app, we would use FusedLocationProviderClient here.
        binding.tvLat.setText("Lat: 18.5204");
        binding.tvLon.setText("Lon: 73.8567");

        binding.btnSaveFarm.setOnClickListener(v -> {
            String name = binding.etFarmName.getText().toString();
            String areaStr = binding.etArea.getText().toString();
            String soil = binding.autoSoilType.getText().toString();

            if (!name.isEmpty() && !areaStr.isEmpty() && !soil.isEmpty()) {
                saveFarm(name, Double.parseDouble(areaStr), soil);
            } else {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFarm(String name, double area, String soil) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveFarm.setEnabled(false);

        String farmerId = sessionManager.getFarmerId();
        FarmRequest request = new FarmRequest(farmerId, name, 18.5204, 73.8567, area, soil);

        RetrofitClient.INSTANCE.getFarmerApi().createFarm(request).enqueue(new Callback<FarmResponse>() {
            @Override
            public void onResponse(Call<FarmResponse> call, Response<FarmResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveFarm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("farm_id", response.body().getFarm_id());
                    NavHostFragment.findNavController(FarmRegistrationFragment.this)
                            .navigate(R.id.action_FarmRegistrationFragment_to_CropRegistrationFragment, bundle);
                } else {
                    Toast.makeText(getContext(), "Failed to save farm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FarmResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveFarm.setEnabled(true);
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
