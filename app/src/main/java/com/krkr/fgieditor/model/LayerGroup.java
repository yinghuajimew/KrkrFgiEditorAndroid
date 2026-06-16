package com.krkr.fgieditor.model;

import java.util.ArrayList;
import java.util.List;

public class LayerGroup {
    public String name;
    public int groupLayerId;
    public List<Layer> layers;

    public LayerGroup(String name, int groupLayerId) {
        this.name = name;
        this.groupLayerId = groupLayerId;
        this.layers = new ArrayList<>();
    }

    public boolean hasEmptyLayer() {
        for (Layer layer : layers) {
            if (layer.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllEmpty() {
        for (Layer layer : layers) {
            if (!layer.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
