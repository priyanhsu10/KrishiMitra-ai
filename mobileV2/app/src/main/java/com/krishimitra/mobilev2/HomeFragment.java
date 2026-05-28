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
import com.krishimitra.mobilev2.data.model.CropResponse;
import com.krishimitra.mobilev2.data.model.FarmResponse;
import com.krishimitra.mobilev2.data.api.FarmListResponse;
import com.krishimitra.mobilev2.data.api.CropListResponse;
import com.krishimitra.mobilev2.databinding.FragmentHomeBinding;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SessionManager sessionManager;
    private String currentFarmId;

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
            binding.tvWelcome.setText("नमस्कार, " + name + "!");
        }

        loadFarmerProfile();
        loadFarmAndCropData();
        loadWeatherSummary();

        binding.btnDisease.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_DiseaseDetectionFragment));
        binding.btnAdvisory.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_AdvisoryChatFragment));
        binding.btnWeather.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_WeatherFragment));
        binding.btnMandi.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_MandiFragment));
        binding.btnNotifications.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_NotificationsFragment));
        binding.tvWelcome.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_ProfileFragment));
        
        binding.btnCropTracking.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_CropTrackingFragment));
        
        View.OnClickListener addCropListener = v -> {
            if (currentFarmId != null) {
                Bundle bundle = new Bundle();
                bundle.putString("farm_id", currentFarmId);
                NavHostFragment.findNavController(this).navigate(R.id.action_HomeFragment_to_CropRegistrationFragment, bundle);
            } else {
                Toast.makeText(getContext(), "शेत माहिती लोड होत आहे, कृपया थांबा...", Toast.LENGTH_SHORT).show();
            }
        };
        
        binding.btnAddCrop.setOnClickListener(addCropListener);
        binding.btnRegisterNewCrop.setOnClickListener(addCropListener);
    }

    private void loadWeatherSummary() {
        String farmerId = sessionManager.getFarmerId();
        if (farmerId == null) return;

        String language = sessionManager.getLanguage();

        RetrofitClient.INSTANCE.getWeatherApi().getWeather(farmerId, null, language).enqueue(new Callback<com.krishimitra.mobilev2.data.api.WeatherResponse>() {
            @Override
            public void onResponse(Call<com.krishimitra.mobilev2.data.api.WeatherResponse> call, Response<com.krishimitra.mobilev2.data.api.WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.krishimitra.mobilev2.data.api.WeatherResponse weather = response.body();
                    
                    if (weather.getTemperature() != null) {
                        binding.tvWeatherTemp.setText(String.format("%.1f°C", weather.getTemperature()));
                    } else {
                        binding.tvWeatherTemp.setText("--°C");
                    }

                    binding.tvWeatherDesc.setText(weather.getWeather_summary() != null ? weather.getWeather_summary() : weather.getDescription());
                    
                    if (weather.getHumidity() != null) {
                        binding.tvHumidity.setText(String.format("%d%%", weather.getHumidity().intValue()));
                    } else {
                        binding.tvHumidity.setText("--%");
                    }
                    
                    String language = sessionManager.getLanguage();
                    String advice = weather.getAdvice();
                    if (advice == null || advice.isEmpty()) {
                        advice = "mr".equalsIgnoreCase(language) ? weather.getAdvice_mr() : weather.getAdvice_en();
                    }

                    if (advice != null && !advice.isEmpty()) {
                        binding.layoutAlert.setVisibility(View.VISIBLE);
                        binding.tvAlertMessage.setText(advice);
                    } else {
                        binding.layoutAlert.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<com.krishimitra.mobilev2.data.api.WeatherResponse> call, Throwable t) {
            }
        });
    }

    private void loadFarmAndCropData() {
        String farmerId = sessionManager.getFarmerId();
        if (farmerId == null) return;

        RetrofitClient.INSTANCE.getFarmerApi().getFarms(farmerId).enqueue(new Callback<FarmListResponse>() {
            @Override
            public void onResponse(Call<FarmListResponse> call, Response<FarmListResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getFarms().isEmpty()) {
                    FarmResponse farm = response.body().getFarms().get(0);
                    currentFarmId = farm.getFarm_id();
                    binding.tvLocationInfo.setText(String.format("📍 %s • %.1f acres", farm.getName(), farm.getArea_acres()));
                    loadCrops(farm.getFarm_id());
                } else {
                    binding.tvCropInfo.setText("No farms registered.");
                }
            }

            @Override
            public void onFailure(Call<FarmListResponse> call, Throwable t) {
                binding.tvCropInfo.setText("Error loading farm data.");
            }
        });
    }

    private void loadCrops(String farmId) {
        RetrofitClient.INSTANCE.getFarmerApi().getCrops(farmId).enqueue(new Callback<CropListResponse>() {
            @Override
            public void onResponse(Call<CropListResponse> call, Response<CropListResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getCrops().isEmpty()) {
                    CropResponse crop = response.body().getCrops().get(0);
                    binding.tvCropInfo.setText(String.format("🌾 %s — %s टप्पा", crop.getCrop_type(), crop.getStage()));
                    binding.tvHealthStatus.setVisibility(View.VISIBLE);
                    binding.tvProgressLabel.setVisibility(View.VISIBLE);
                    binding.pbCropGrowth.setVisibility(View.VISIBLE);
                    binding.pbCropGrowth.setProgress(45); // Mock progress
                    binding.btnRegisterNewCrop.setVisibility(View.GONE);
                    sessionManager.saveCropType(crop.getCrop_type());
                    sessionManager.saveCropId(crop.getCrop_id());
                } else {
                    binding.tvCropInfo.setText("No active crop registered");
                    binding.btnRegisterNewCrop.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<CropListResponse> call, Throwable t) {
                binding.tvCropInfo.setText("Error loading crop data.");
            }
        });
    }

    private void loadFarmerProfile() {
        String farmerId = sessionManager.getFarmerId();
        if (farmerId == null) return;

        RetrofitClient.INSTANCE.getFarmerApi().getFarmer(farmerId).enqueue(new Callback<com.krishimitra.mobilev2.data.model.FarmerResponse>() {
            @Override
            public void onResponse(Call<com.krishimitra.mobilev2.data.model.FarmerResponse> call, Response<com.krishimitra.mobilev2.data.model.FarmerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.krishimitra.mobilev2.data.model.FarmerResponse profile = response.body();
                    sessionManager.saveFarmerName(profile.getName());
                    binding.tvWelcome.setText("Welcome, " + profile.getName() + "!");
                    // Note: If profile has state, save it. FarmerResponse currently doesn't have it in DTO but let's assume it might be added or we just rely on registration save.
                }
            }

            @Override
            public void onFailure(Call<com.krishimitra.mobilev2.data.model.FarmerResponse> call, Throwable t) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
