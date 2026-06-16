package com.krkr.fgieditor.util;

import com.krkr.fgieditor.model.LayerGroup;

import java.util.List;

/**
 * Singleton to hold temporary data for batch processing
 * (workaround for passing Bitmap data between activities)
 */
public class BatchDataHolder {
    private static BatchDataHolder instance;

    private List<LayerGroup> layerGroups;
    private String baseFilename;

    private BatchDataHolder() {
    }

    public static synchronized BatchDataHolder getInstance() {
        if (instance == null) {
            instance = new BatchDataHolder();
        }
        return instance;
    }

    public void setData(List<LayerGroup> layerGroups, String baseFilename) {
        this.layerGroups = layerGroups;
        this.baseFilename = baseFilename;
    }

    public List<LayerGroup> getLayerGroups() {
        return layerGroups;
    }

    public String getBaseFilename() {
        return baseFilename;
    }

    public void clear() {
        layerGroups = null;
        baseFilename = null;
    }
}
