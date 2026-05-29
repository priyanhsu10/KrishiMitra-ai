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
import com.krishimitra.mobilev2.data.api.VerifyRequest;
import com.krishimitra.mobilev2.data.api.VerifyResponse;
import com.krishimitra.mobilev2.databinding.FragmentOtpBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpFragment extends Fragment {

    private FragmentOtpBinding binding;
    private String mobile;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOtpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        if (getArguments() != null) {
            mobile = getArguments().getString("mobile");
        }
        
        if (mobile != null) {
            binding.tvOtpSentTo.setText(mobile + " वर पाठवले");
        }

        binding.btnVerify.setOnClickListener(v -> {
            String otp = binding.etOtp.getText().toString();
            if (otp.length() == 6) {
                verifyOtp(otp);
            } else {
                Toast.makeText(getContext(), "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });

        // Demo shortcut
        binding.tvDemoPin.setOnClickListener(v -> {
            binding.etOtp.setText("123456");
            verifyOtp("123456");
        });
    }

    private void verifyOtp(String otp) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnVerify.setEnabled(false);

        // For demo: if network fails or for specific OTP, we can mock success if needed
        // But let's try the actual call first
        RetrofitClient.INSTANCE.getAuthApi().verify(new VerifyRequest(mobile != null ? mobile : "9876543210", otp)).enqueue(new Callback<VerifyResponse>() {
            @Override
            public void onResponse(Call<VerifyResponse> call, Response<VerifyResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnVerify.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccess(response.body());
                } else {
                    // Demo fallback: if it's the demo OTP, just proceed anyway
                    if ("123456".equals(otp)) {
                        mockSuccess();
                    } else {
                        Toast.makeText(getContext(), "Verification failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<VerifyResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnVerify.setEnabled(true);
                
                // Demo fallback: proceed if it's the demo OTP even on network failure
                if ("123456".equals(otp)) {
                    mockSuccess();
                } else {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleSuccess(VerifyResponse body) {
        sessionManager.saveAuthToken(body.getToken());
        sessionManager.saveFarmerId(body.getFarmer_id());

        if (body.is_new_user()) {
            NavHostFragment.findNavController(OtpFragment.this)
                    .navigate(R.id.action_OtpFragment_to_FarmerProfileFragment);
        } else {
            // Navigate to Home
            NavHostFragment.findNavController(OtpFragment.this)
                    .navigate(R.id.action_OtpFragment_to_HomeFragment);
        }
    }

    private void mockSuccess() {
        sessionManager.saveAuthToken("mock_demo_token");
        sessionManager.saveFarmerId("demo_farmer_id");
        // For demo, assume existing user to go to home, or new user to show profile
        // Let's go to Home if they already have a name, but for demo let's go to Home.
        NavHostFragment.findNavController(OtpFragment.this)
                .navigate(R.id.action_OtpFragment_to_HomeFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
