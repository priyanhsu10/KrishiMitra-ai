package com.krishimitra.mobilev2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.krishimitra.mobilev2.data.RetrofitClient;
import com.krishimitra.mobilev2.data.SessionManager;
import com.krishimitra.mobilev2.data.api.WeatherResponse;
import com.krishimitra.mobilev2.databinding.FragmentWeatherBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherFragment extends Fragment {

    private FragmentWeatherBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWeatherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        loadWeather();
    }

    private void loadWeather() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String farmerId = sessionManager.getFarmerId();

        if (farmerId == null) {
            Toast.makeText(getContext(), "Farmer ID not found", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        String language = sessionManager.getLanguage();

        RetrofitClient.INSTANCE.getWeatherApi().getWeather(farmerId, null, language).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    binding.tvTemp.setText(String.format("%.1f°C", weather.getTemperature()));
                    
                    String summary = weather.getWeather_summary();
                    String advice = weather.getAdvice();
                    if (advice == null || advice.isEmpty()) {
                        advice = "mr".equalsIgnoreCase(language) ? weather.getAdvice_mr() : weather.getAdvice_en();
                    }
                    
                    binding.tvSummary.setText(summary);
                    binding.tvAdvice.setText(advice);
                } else {
                    binding.tvSummary.setText("Failed to load weather data");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvSummary.setText("Error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
