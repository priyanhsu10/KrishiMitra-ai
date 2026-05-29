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
import com.krishimitra.mobilev2.data.api.LoginRequest;
import com.krishimitra.mobilev2.data.api.LoginResponse;
import com.krishimitra.mobilev2.databinding.FragmentLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSendOtp.setOnClickListener(v -> {
            String mobile = binding.etMobile.getText().toString();
            if (mobile.length() == 10) {
                sendOtp(mobile);
            } else {
                Toast.makeText(getContext(), "Enter valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOtp(String mobile) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSendOtp.setEnabled(false);

        RetrofitClient.INSTANCE.getAuthApi().login(new LoginRequest(mobile)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSendOtp.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().getOtp_sent()) {
                    Bundle bundle = new Bundle();
                    bundle.putString("mobile", mobile);
                    NavHostFragment.findNavController(LoginFragment.this)
                            .navigate(R.id.action_LoginFragment_to_OtpFragment, bundle);
                } else {
                    // Demo fallback
                    if (mobile.startsWith("8421") || mobile.equals("9876543210")) {
                        Bundle bundle = new Bundle();
                        bundle.putString("mobile", mobile);
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_LoginFragment_to_OtpFragment, bundle);
                    } else {
                        Toast.makeText(getContext(), "Failed to send OTP", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSendOtp.setEnabled(true);
                
                // Demo bypass: if network fails, allow moving to OTP screen for demo purposes
                if (mobile.startsWith("8421") || mobile.equals("9876543210")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("mobile", mobile);
                    NavHostFragment.findNavController(LoginFragment.this)
                            .navigate(R.id.action_LoginFragment_to_OtpFragment, bundle);
                } else {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
