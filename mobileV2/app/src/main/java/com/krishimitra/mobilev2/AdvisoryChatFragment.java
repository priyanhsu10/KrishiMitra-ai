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
import com.krishimitra.mobilev2.data.api.AdvisoryChatRequest;
import com.krishimitra.mobilev2.data.model.AdvisoryChatResponse;
import com.krishimitra.mobilev2.databinding.FragmentAdvisoryChatBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdvisoryChatFragment extends Fragment {

    private FragmentAdvisoryChatBinding binding;
    private SessionManager sessionManager;
    private StringBuilder chatHistory = new StringBuilder();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdvisoryChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        binding.btnSend.setOnClickListener(v -> {
            String question = binding.etQuestion.getText().toString();
            if (!question.isEmpty()) {
                sendMessage(question);
            }
        });
    }

    private void sendMessage(String question) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSend.setEnabled(false);

        chatHistory.append("You: ").append(question).append("\n\n");
        binding.tvChatHistory.setText(chatHistory.toString());
        binding.etQuestion.setText("");

        String farmerId = sessionManager.getFarmerId();
        String language = sessionManager.getLanguage();
        
        // Defaulting crop and stage for now, should ideally be fetched from session/last selected
        AdvisoryChatRequest request = new AdvisoryChatRequest(
                farmerId != null ? farmerId : "",
                "Soybean", 
                "Vegetative",
                language != null ? language : "en",
                question
        );

        RetrofitClient.INSTANCE.getAdvisoryApi().getAdvisory(request).enqueue(new Callback<AdvisoryChatResponse>() {
            @Override
            public void onResponse(Call<AdvisoryChatResponse> call, Response<AdvisoryChatResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSend.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    AdvisoryChatResponse advisory = response.body();
                    String answer = "marathi".equalsIgnoreCase(language) ? advisory.getAdvice_mr() : advisory.getAdvice_en();
                    chatHistory.append("AI: ").append(answer).append("\n\n");
                    binding.tvChatHistory.setText(chatHistory.toString());
                    
                    // Scroll to bottom
                    binding.scrollChat.post(() -> binding.scrollChat.fullScroll(View.FOCUS_DOWN));
                } else {
                    Toast.makeText(getContext(), "Failed to get advisory", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdvisoryChatResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSend.setEnabled(true);
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
