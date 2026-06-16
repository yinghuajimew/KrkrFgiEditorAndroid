package com.krkr.fgieditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krkr.fgieditor.R;
import com.krkr.fgieditor.model.BatchResult;

import java.util.List;

public class BatchResultAdapter extends RecyclerView.Adapter<BatchResultAdapter.ViewHolder> {

    private final List<BatchResult> results;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BatchResult result);
    }

    public BatchResultAdapter(List<BatchResult> results, OnItemClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_batch_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BatchResult result = results.get(position);
        holder.bind(result);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView filename;
        TextView layers;

        ViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.result_thumbnail);
            filename = itemView.findViewById(R.id.result_filename);
            layers = itemView.findViewById(R.id.result_layers);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(results.get(pos));
                    }
                }
            });
        }

        void bind(BatchResult result) {
            filename.setText(result.filename);
            layers.setText(result.getLayerDescription());
            if (result.thumbnail != null) {
                thumbnail.setImageBitmap(result.thumbnail);
            }
        }
    }
}
