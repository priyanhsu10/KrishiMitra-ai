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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.krishimitra.mobilev2.data.RetrofitClient;
import com.krishimitra.mobilev2.data.api.CropTimelineItemDto;
import com.krishimitra.mobilev2.data.api.CropTimelineResponse;
import com.krishimitra.mobilev2.databinding.FragmentCropDetailBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CropDetailFragment extends Fragment {

    private FragmentCropDetailBinding binding;
    private String cropId;
    private String cropType;
    private String sowingDate;
    private TimelineAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCropDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        com.krishimitra.mobilev2.data.SessionManager sessionManager = new com.krishimitra.mobilev2.data.SessionManager(requireContext());
        String language = sessionManager.getLanguage();
        boolean isEnglish = "en".equalsIgnoreCase(language);

        if (getArguments() != null) {
            cropId = getArguments().getString("crop_id");
            cropType = getArguments().getString("crop_type");
            sowingDate = getArguments().getString("sowing_date");
        }

        if (!isEnglish) {
            // Use generic 'Tracking' but can be expanded for specific languages
            binding.tvCropTitle.setText(cropType + " मागोवा/Tracking");
            binding.tvSowingInfo.setText("पेरणी/Sowing: " + sowingDate);
        } else {
            binding.tvCropTitle.setText(cropType + " Tracking");
            binding.tvSowingInfo.setText("Sown on: " + sowingDate);
        }

        binding.rvTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TimelineAdapter(new ArrayList<>());
        binding.rvTimeline.setAdapter(adapter);

        loadTimeline();
    }

    private void loadTimeline() {
        if (cropId == null) return;

        RetrofitClient.INSTANCE.getFarmerApi().getCropTimeline(cropId).enqueue(new Callback<CropTimelineResponse>() {
            @Override
            public void onResponse(Call<CropTimelineResponse> call, Response<CropTimelineResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setTimeline(response.body().getTimeline());
                } else {
                    Toast.makeText(getContext(), "Failed to load timeline", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CropTimelineResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {
        private List<CropTimelineItemDto> timeline;

        TimelineAdapter(List<CropTimelineItemDto> timeline) {
            this.timeline = timeline;
        }

        void setTimeline(List<CropTimelineItemDto> timeline) {
            this.timeline = timeline;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CropTimelineItemDto item = timeline.get(position);
            holder.tvStage.setText(item.getStage());
            holder.tvDate.setText(item.getEstimatedDate());
            holder.tvDescription.setText(item.getDescription());

            holder.viewLineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
            holder.viewLineBottom.setVisibility(position == timeline.size() - 1 ? View.INVISIBLE : View.VISIBLE);
            
            if (item.getCompleted()) {
                holder.viewDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getColor(R.color.green_primary)));
            } else {
                holder.viewDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getColor(R.color.green_mid)));
            }
        }

        @Override
        public int getItemCount() {
            return timeline.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStage, tvDate, tvDescription;
            View viewLineTop, viewLineBottom, viewDot;

            ViewHolder(View view) {
                super(view);
                tvStage = view.findViewById(R.id.tv_stage);
                tvDate = view.findViewById(R.id.tv_date);
                tvDescription = view.findViewById(R.id.tv_description);
                viewLineTop = view.findViewById(R.id.view_line_top);
                viewLineBottom = view.findViewById(R.id.view_line_bottom);
                viewDot = view.findViewById(R.id.view_dot);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
