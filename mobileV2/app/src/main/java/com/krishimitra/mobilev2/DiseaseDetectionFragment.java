package com.krishimitra.mobilev2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private String[] cropOptions = {"Soybean", "Cotton", "Rice", "Wheat", "Sugarcane", "Onion", "Other"};

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

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    photoURI = uri;
                    binding.ivPlant.setImageURI(uri);
                    try {
                        File file = copyUriToFile(uri);
                        currentPhotoPath = file.getAbsolutePath();
                        binding.btnDetect.setEnabled(true);
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
                    }
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

        setupCropDropdown();

        binding.btnCapture.setOnClickListener(v -> checkPermissionAndCapture());
        binding.btnGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        binding.btnDetect.setOnClickListener(v -> detectDisease());
    }

    private void setupCropDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, cropOptions);
        binding.autoCropType.setAdapter(adapter);

        // Pre-select based on session if available
        String sessionCrop = sessionManager.getCropType();
        if (sessionCrop != null) {
            binding.autoCropType.setText(sessionCrop, false);
        }

        binding.autoCropType.setOnItemClickListener((parent, view, position, id) -> {
            String selected = cropOptions[position];
            if ("Other".equals(selected)) {
                binding.etOtherCrop.setVisibility(View.VISIBLE);
            } else {
                binding.etOtherCrop.setVisibility(View.GONE);
            }
        });
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

    private File copyUriToFile(Uri uri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new IOException("Unable to open input stream");
        File tempFile = createImageFile();
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
    }

    private File compressImage(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            int MAX_SIZE = 1024;
            int width = options.outWidth;
            int height = options.outHeight;

            int inSampleSize = 1;
            if (width > MAX_SIZE || height > MAX_SIZE) {
                int halfWidth = width / 2;
                int halfHeight = height / 2;
                while ((halfWidth / inSampleSize) >= MAX_SIZE && (halfHeight / inSampleSize) >= MAX_SIZE) {
                    inSampleSize *= 2;
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            File compressedFile = new File(requireContext().getCacheDir(), "compressed_" + file.getName());
            FileOutputStream out = new FileOutputStream(compressedFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();

            return compressedFile;
        } catch (Exception e) {
            e.printStackTrace();
            return file;
        }
    }

    private void detectDisease() {
        if (currentPhotoPath == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnDetect.setEnabled(false);

        File originalFile = new File(currentPhotoPath);
        File fileToUpload = compressImage(originalFile);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), fileToUpload);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileToUpload.getName(), requestFile);

        String farmerId = sessionManager.getFarmerId();
        
        String selectedCropType = binding.autoCropType.getText().toString();
        if ("Other".equals(selectedCropType)) {
            selectedCropType = binding.etOtherCrop.getText().toString();
        }
        if (selectedCropType.isEmpty()) selectedCropType = "Soybean";
        
        String cropId = sessionManager.getCropId();
        String language = sessionManager.getLanguage();
        
        if (farmerId == null) farmerId = "unknown";
        // if (cropType == null) cropType = "Soybean"; // Removed old cropType usage
        if (language == null) language = "en";

        RequestBody farmerIdPart = RequestBody.create(MediaType.parse("text/plain"), farmerId);
        RequestBody cropIdPart = cropId != null ? 
                RequestBody.create(MediaType.parse("text/plain"), cropId) : null;
        RequestBody cropTypePart = RequestBody.create(MediaType.parse("text/plain"), selectedCropType);
        RequestBody languagePart = RequestBody.create(MediaType.parse("text/plain"), language);

        RetrofitClient.INSTANCE.getDiseaseApi().detectDisease(
                farmerIdPart,
                cropIdPart,
                cropTypePart,
                languagePart,
                body
        ).enqueue(new Callback<DiseaseDetectionResponse>() {
            @Override
            public void onResponse(Call<DiseaseDetectionResponse> call, Response<DiseaseDetectionResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnDetect.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    DiseaseDetectionResponse res = response.body();
                    String language = sessionManager.getLanguage();
                    boolean isMarathi = "mr".equalsIgnoreCase(language);
                    
                    String diseaseName = isMarathi && res.getDisease_mr() != null ? res.getDisease_mr() : res.getDisease();
                    String remedy = isMarathi ? res.getRemedy_mr() : res.getRemedy_en();
                    String cause = isMarathi && res.getCause_mr() != null ? res.getCause_mr() : res.getCause_en();
                    String severity = res.getSeverity();
                    
                    // Simple severity translation
                    if (isMarathi) {
                        if ("high".equalsIgnoreCase(severity)) severity = "उच्च (High)";
                        else if ("medium".equalsIgnoreCase(severity)) severity = "मध्यम (Medium)";
                        else if ("low".equalsIgnoreCase(severity)) severity = "कमी (Low)";
                    }

                    String resultText = isMarathi ? 
                        String.format("रोग: %s\nगंभीरता: %s\nविश्वासार्हता: %.2f%%\n\nकारण: %s\n\nउपाय: %s", 
                            diseaseName, severity, res.getConfidence() * 100, 
                            cause != null ? cause : "माहिती उपलब्ध नाही", remedy) :
                        String.format("Disease: %s\nSeverity: %s\nConfidence: %.2f%%\n\nCause: %s\n\nRemedy: %s",
                            diseaseName, severity, res.getConfidence() * 100, 
                            cause != null ? cause : "No info available", remedy);
                    
                    binding.tvResult.setText(resultText);
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
