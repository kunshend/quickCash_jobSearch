package com.example.quickcash.utilities;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quickcash.R;
import com.example.quickcash.entities.ApplicationData;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

/**
 * RecyclerView adapter for displaying application data.
 * Uses the unified ApplicationData model.
 */
public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {

    private final List<ApplicationData> applicationList;
    private final OnApplicationClickListener listener;

    /**
     * Interface for handling application click events.
     */
    public interface OnApplicationClickListener {
        void onApplicationClick(ApplicationData application);
    }

    /**
     * Constructor for the adapter.
     *
     * @param applicationList List of application data to display
     * @param listener Listener for click events
     */
    public ApplicationAdapter(List<ApplicationData> applicationList, OnApplicationClickListener listener) {
        this.applicationList = applicationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        ApplicationData application = applicationList.get(position);
        if (application != null) {
            // Set job name or display placeholder
            if (application.getJobName() != null && !application.getJobName().isEmpty()) {
                holder.jobTitleTextView.setText(application.getJobName());
            } else if ("No applications...".equals(application.getId())) {
                holder.jobTitleTextView.setText("No applications...");
            } else {
                holder.jobTitleTextView.setText("Unknown Job");
            }

            // Application status (open, accepted, rejected)
            String statusText = (application.getStatus() != null && !application.getStatus().isEmpty())
                    ? application.getStatus()
                    : "unknown";
            holder.statusTextView.setText(statusText);

            // Style application status pill
            String status = statusText.toLowerCase();
            switch (status) {
                case "accepted":
                    holder.statusTextView.setBackgroundResource(R.drawable.status_accepted_bg);
                    break;
                case "rejected":
                    holder.statusTextView.setBackgroundResource(R.drawable.status_rejected_bg);
                    break;
                case "open":
                    holder.statusTextView.setBackgroundResource(R.drawable.status_open_bg);
                    break;
                default:
                    holder.statusTextView.setBackgroundColor(Color.TRANSPARENT);
                    break;
            }

            // Show job status pill if "Completed"
            if ("Completed".equalsIgnoreCase(application.getJobStatus())) {
                holder.jobStatusTextView.setText("Completed");
                holder.jobStatusTextView.setBackgroundResource(R.drawable.status_completed_bg);
                holder.jobStatusTextView.setVisibility(View.VISIBLE);
            } else {
                holder.jobStatusTextView.setVisibility(View.GONE);
            }

            // Message
            if (application.getMessage() != null && !application.getMessage().isEmpty()) {
                holder.messageTextView.setText(application.getMessage());
            } else {
                holder.messageTextView.setText("");
            }

            // Click listener (unless placeholder)
            holder.itemView.setOnClickListener(v -> {
                if (listener != null && !"No applications...".equals(application.getId())) {
                    listener.onApplicationClick(application);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    /**
     * ViewHolder class for application list items.
     */
    public static class ApplicationViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitleTextView;
        TextView statusTextView;
        TextView jobStatusTextView;
        TextView messageTextView;

        public ApplicationViewHolder(View itemView) {
            super(itemView);
            jobTitleTextView = itemView.findViewById(R.id.jobTitleTextView);
            statusTextView = itemView.findViewById(R.id.applicationStatusTextView);
            jobStatusTextView = itemView.findViewById(R.id.jobStatusTextView);
            messageTextView = itemView.findViewById(R.id.jobDescriptionTextView);
        }
    }
}
