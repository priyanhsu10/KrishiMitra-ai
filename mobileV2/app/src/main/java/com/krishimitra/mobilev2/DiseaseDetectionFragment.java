package com.krishimitra.mobilev2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.krishimitra.mobilev2.data.RetrofitClient;
import com.krishimitra.mobilev2.data.SessionManager;
import com.krishimitra.mobilev2.data.model.DiseaseDetectionResponse;
import com.krishimitra.mobilev2.databinding.FragmentDiseaseDetectionBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiseaseDetectionFragment extends Fragment {

    private FragmentDiseaseDetectionBinding binding;
    private SessionManager sessionManager;
    private String currentPhotoPath;
    private Uri photoURI;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    captureImage();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    binding.ivPlant.setImageURI(photoURI);
                    binding.btnDetect.setEnabled(true);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiseaseDetectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        binding.btnCapture.setOnClickListener(v -> checkPermissionAndCapture());
        binding.btnDetect.setOnClickListener(v -> detectDisease());
    }

    private void checkPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            captureImage();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void captureImage() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(getContext(), "Error creating file", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);
            takePictureLauncher.launch(photoURI);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void detectDisease() {
        if (currentPhotoPath == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnDetect.setEnabled(false);

        File file = new File(currentPhotoPath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        String farmerId = sessionManager.getFarmerId();
        String cropType = sessionManager.getCropType();
        if (farmerId == null) farmerId = "unknown";
        if (cropType == null) cropType = "Soybean";

        RetrofitClient.INSTANCE.getDiseaseApi().detectDisease(
                farmerId,
                "crop_id_placeholder", // Ideally should be from active crop session
                cropType,
                body
        ).enqueue(new Callback<DiseaseDetectionResponse>() {
            @Override
            public void onResponse(Call<DiseaseDetectionResponse> call, Response<DiseaseDetectionResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnDetect.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    DiseaseDetectionResponse res = response.body();
                    String language = sessionManager.getLanguage();
                    String remedy = "marathi".equalsIgnoreCase(language) ? res.getRemedy_mr() : res.getRemedy_en();
                    
                    binding.tvResult.setText(String.format("Disease: %s\nSeverity: %s\nConfidence: %.2f%%\n\nRemedy: %s",
                            res.getDisease(), res.getSeverity(), res.getConfidence() * 100, remedy));
                } else {
                    Toast.makeText(getContext(), "Detection failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DiseaseDetectionResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnDetect.setEnabled(true);
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
