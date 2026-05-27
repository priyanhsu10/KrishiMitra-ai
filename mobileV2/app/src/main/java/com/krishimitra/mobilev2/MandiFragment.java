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
import com.krishimitra.mobilev2.data.api.MandiPrice;
import com.krishimitra.mobilev2.data.api.MandiResponse;
import com.krishimitra.mobilev2.databinding.FragmentMandiBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MandiFragment extends Fragment {

    private FragmentMandiBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMandiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        loadMandiPrices();
    }

    private void loadMandiPrices() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        String cropVal = sessionManager.getCropType();
        String stateVal = sessionManager.getState();

        if (cropVal == null) cropVal = "Soybean";
        if (stateVal == null) stateVal = "Maharashtra";

        final String crop = cropVal;
        final String state = stateVal;

        RetrofitClient.INSTANCE.getMandiApi().getMandiPrices(crop, state).enqueue(new Callback<MandiResponse>() {
            @Override
            public void onResponse(Call<MandiResponse> call, Response<MandiResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    MandiResponse mandi = response.body();
                    displayPrices(mandi);
                } else {
                    binding.tvMandiInfo.setText("No mandi prices found for " + crop + " in " + state);
                }
            }

            @Override
            public void onFailure(Call<MandiResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvMandiInfo.setText("Error loading mandi prices: " + t.getMessage());
            }
        });
    }

    private void displayPrices(MandiResponse mandi) {
        StringBuilder sb = new StringBuilder();
        sb.append("Crop: ").append(mandi.getCrop()).append("\n");
        sb.append("Best time to sell: ").append(mandi.getBest_time_to_sell()).append("\n\n");
        
        for (MandiPrice price : mandi.getPrices()) {
            sb.append("Market: ").append(price.getMarket()).append(" (").append(price.getDistrict()).append(")\n");
            sb.append("Avg Price: ₹").append(price.getAverage_price()).append("\n");
            sb.append("Range: ₹").append(price.getMin_price()).append(" - ₹").append(price.getMax_price()).append("\n\n");
        }
        
        binding.tvMandiInfo.setText(sb.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
