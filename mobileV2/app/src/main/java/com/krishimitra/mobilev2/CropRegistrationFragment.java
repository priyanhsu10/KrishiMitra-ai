package com.krishimitra.mobilev2;

import android.app.DatePickerDialog;
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
import com.krishimitra.mobilev2.data.api.CropRequest;
import com.krishimitra.mobilev2.data.model.CropResponse;
import com.krishimitra.mobilev2.databinding.FragmentCropRegistrationBinding;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CropRegistrationFragment extends Fragment {

    private FragmentCropRegistrationBinding binding;
    private String farmId;
    private String selectedCrop = "Soybean";
    private String[] cropStages = {
            "Planting/Seedling (लागवड/रोप अवस्था)",
            "Vegetative Growth (वाढ)",
            "Tillering (फुटवे येणे)",
            "Flowering (फुलोरा)",
            "Fruit/Grain Filling (फळ धारणा/दाणे भरणे)",
            "Maturity (पक्वता)",
            "Harvesting (कापणी)"
    };

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

        // Setup Crop Stage Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, cropStages);
        binding.autoCropStage.setAdapter(adapter);

        binding.optionSoybean.setOnClickListener(v -> selectCrop("Soyabean", binding.optionSoybean));
        binding.optionCotton.setOnClickListener(v -> selectCrop("Cotton", binding.optionCotton));
        binding.optionRice.setOnClickListener(v -> selectCrop("Rice", binding.optionRice));
        binding.optionWheat.setOnClickListener(v -> selectCrop("Wheat", binding.optionWheat));
        binding.optionSugarcane.setOnClickListener(v -> selectCrop("Sugarcane", binding.optionSugarcane));
        binding.optionOnion.setOnClickListener(v -> selectCrop("Onion", binding.optionOnion));
        
        binding.etSowingDate.setOnClickListener(v -> showDatePicker());

        // Initial selection
        binding.etCropType.setText(selectedCrop);

        binding.btnSaveCrop.setOnClickListener(v -> {
            String cropType = binding.etCropType.getText().toString();
            String sowingDate = binding.etSowingDate.getText().toString();
            String stage = binding.autoCropStage.getText().toString();

            if (!cropType.isEmpty() && !sowingDate.isEmpty() && !stage.isEmpty()) {
                saveCrop(cropType, sowingDate, stage);
            } else {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectCrop(String crop, View view) {
        selectedCrop = crop;
        binding.optionSoybean.setBackgroundResource(R.drawable.bg_lang_option);
        binding.optionCotton.setBackgroundResource(R.drawable.bg_lang_option);
        binding.optionRice.setBackgroundResource(R.drawable.bg_lang_option);
        binding.optionWheat.setBackgroundResource(R.drawable.bg_lang_option);
        binding.optionSugarcane.setBackgroundResource(R.drawable.bg_lang_option);
        binding.optionOnion.setBackgroundResource(R.drawable.bg_lang_option);
        view.setBackgroundResource(R.drawable.bg_lang_option_selected);
        binding.etCropType.setText(crop);
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    binding.etSowingDate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveCrop(String cropType, String sowingDate, String stage) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveCrop.setEnabled(false);

        String language = new com.krishimitra.mobilev2.data.SessionManager(requireContext()).getLanguage();
        CropRequest request = new CropRequest(farmId, cropType, sowingDate, stage, language);

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
