package com.krishimitra.mobilev2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.krishimitra.mobilev2.data.RetrofitClient;
import com.krishimitra.mobilev2.data.SessionManager;
import com.krishimitra.mobilev2.data.api.CropListResponse;
import com.krishimitra.mobilev2.data.api.FarmListResponse;
import com.krishimitra.mobilev2.data.model.CropResponse;
import com.krishimitra.mobilev2.data.model.FarmResponse;
import com.krishimitra.mobilev2.databinding.FragmentCropTrackingBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CropTrackingFragment extends Fragment {

    private FragmentCropTrackingBinding binding;
    private SessionManager sessionManager;
    private String currentFarmId;
    private CropAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCropTrackingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        binding.rvCrops.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CropAdapter(new ArrayList<>());
        binding.rvCrops.setAdapter(adapter);

        loadFarmData();

        binding.btnAddCrop.setOnClickListener(v -> {
            if (currentFarmId != null) {
                Bundle bundle = new Bundle();
                bundle.putString("farm_id", currentFarmId);
                NavHostFragment.findNavController(this).navigate(R.id.action_CropTrackingFragment_to_CropRegistrationFragment, bundle);
            } else {
                Toast.makeText(getContext(), "Farm data not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFarmData() {
        String farmerId = sessionManager.getFarmerId();
        if (farmerId == null) return;

        RetrofitClient.INSTANCE.getFarmerApi().getFarms(farmerId).enqueue(new Callback<FarmListResponse>() {
            @Override
            public void onResponse(Call<FarmListResponse> call, Response<FarmListResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getFarms().isEmpty()) {
                    FarmResponse farm = response.body().getFarms().get(0);
                    currentFarmId = farm.getFarm_id();
                    loadCrops(currentFarmId);
                }
            }

            @Override
            public void onFailure(Call<FarmListResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading farm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCrops(String farmId) {
        RetrofitClient.INSTANCE.getFarmerApi().getCrops(farmId).enqueue(new Callback<CropListResponse>() {
            @Override
            public void onResponse(Call<CropListResponse> call, Response<CropListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setCrops(response.body().getCrops());
                }
            }

            @Override
            public void onFailure(Call<CropListResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading crops", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class CropAdapter extends RecyclerView.Adapter<CropAdapter.ViewHolder> {
        private List<CropResponse> crops;

        CropAdapter(List<CropResponse> crops) {
            this.crops = crops;
        }

        void setCrops(List<CropResponse> crops) {
            this.crops = crops;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CropResponse crop = crops.get(position);
            holder.tvType.setText(crop.getCrop_type());
            holder.tvStage.setText(crop.getStage());
            holder.tvDate.setText("Sown: " + crop.getSowing_date());
        }

        @Override
        public int getItemCount() {
            return crops.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvStage, tvDate;

            ViewHolder(View view) {
                super(view);
                tvType = view.findViewById(R.id.tv_crop_type);
                tvStage = view.findViewById(R.id.tv_crop_stage);
                tvDate = view.findViewById(R.id.tv_sowing_date);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
