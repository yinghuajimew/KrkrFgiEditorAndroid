package com.krkr.fgieditor.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krkr.fgieditor.R;
import com.krkr.fgieditor.model.Layer;
import com.krkr.fgieditor.util.ImageProcessor;

import java.util.List;

public class LayerAdapter extends RecyclerView.Adapter<LayerAdapter.LayerViewHolder> {

    private List<Layer> layers;
    private OnLayerClickListener listener;
    private int thumbnailSize = 100;

    public interface OnLayerClickListener {
        void onLayerClick(Layer layer);
    }

    public LayerAdapter(List<Layer> layers, OnLayerClickListener listener) {
        this.layers = layers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layer, parent, false);
        return new LayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LayerViewHolder holder, int position) {
        Layer layer = layers.get(position);
        holder.bind(layer);
    }

    @Override
    public int getItemCount() {
        return layers.size();
    }

    class LayerViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailView;
        TextView nameView;

        LayerViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.layer_thumbnail);
            nameView = itemView.findViewById(R.id.layer_name);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLayerClick(layers.get(position));
                }
            });
        }

        void bind(Layer layer) {
            nameView.setText(layer.name);

            if (layer.bitmap != null) {
                Bitmap thumbnail = ImageProcessor.resizeBitmap(
                        layer.bitmap, thumbnailSize, thumbnailSize);
                thumbnailView.setImageBitmap(thumbnail);
            } else {
                thumbnailView.setImageResource(R.drawable.ic_empty_layer);
            }
        }
    }
}
