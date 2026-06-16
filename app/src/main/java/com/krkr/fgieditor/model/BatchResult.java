package com.krkr.fgieditor.model;

import android.graphics.Bitmap;

import java.util.List;

public class BatchResult {
    public String filename;
    public List<Layer> layers;
    public Bitmap thumbnail;
    public String filePath;

    public BatchResult(String filename, List<Layer> layers, Bitmap thumbnail, String filePath) {
        this.filename = filename;
        this.layers = layers;
        this.thumbnail = thumbnail;
        this.filePath = filePath;
    }

    public String getLayerDescription() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < layers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(layers.get(i).name);
        }
        return sb.toString();
    }
}
