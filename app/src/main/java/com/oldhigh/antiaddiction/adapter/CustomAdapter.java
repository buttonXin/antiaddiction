package com.oldhigh.antiaddiction.adapter;


import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.oldhigh.antiaddiction.R;
import com.oldhigh.antiaddiction.bean.AppInfo;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    public static final String TAG = CustomAdapter.class.getName();
    private List<AppInfo> localApps;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = view.findViewById(R.id.tv);
            imageView = view.findViewById(R.id.iv);
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public CustomAdapter(List<AppInfo> dataSet) {
        localApps = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.all_app_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTextView().setText(localApps.get(position).appName);
        viewHolder.getImageView().setImageDrawable(localApps.get(position).icon);
        if (localApps.get(position).selected) {
            viewHolder.getTextView().setTextColor(ContextCompat.getColor(
                    viewHolder.getTextView().getContext(), R.color.red));
        } else {
            viewHolder.getTextView().setTextColor(ContextCompat.getColor(
                    viewHolder.getTextView().getContext(), R.color.black));
        }

        viewHolder.itemView.setOnClickListener(view -> {
            if (localApps.get(position).selected) {
                localApps.get(position).selected = false;
            } else {
                localApps.get(position).selected = true;

            }
            notifyItemChanged(position);
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localApps.size();
    }
}
