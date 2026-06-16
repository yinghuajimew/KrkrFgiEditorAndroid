package com.krkr.fgieditor.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.krkr.fgieditor.model.Layer;
import com.krkr.fgieditor.model.LayerGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CoordinateFileParser {

    /**
     * Parse coordinate file (TXT or JSON)
     * @param file Coordinate file
     * @param imageDir Directory containing layer images
     * @return List of layer groups
     */
    public static List<LayerGroup> parseFile(File file, File imageDir) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".json")) {
            return parseJsonFile(file, imageDir);
        } else {
            // Auto-detect encoding for TXT files
            Charset charset = EncodingDetector.detectEncoding(file);
            return parseTxtFile(file, charset, imageDir);
        }
    }

    /**
     * Parse TXT format coordinate file
     * Format: 0\tname\tleft\ttop\t?\t?\t?\topacity\t?\tlayerId\t[groupLayerId]
     */
    public static List<LayerGroup> parseTxtFile(File file, Charset charset, File imageDir)
            throws IOException {
        List<LayerGroup> groups = new ArrayList<>();
        LayerGroup noneGroup = new LayerGroup("(none)", -1);
        groups.add(noneGroup);

        String characterName = file.getName().replaceFirst("[.][^.]+$", ""); // Remove extension

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) continue;

                if (parts[0].equals("0")) {
                    // Layer data
                    if (parts.length >= 10) {
                        Layer layer = parseLayerLine(parts, characterName, imageDir);
                        if (layer != null) {
                            int groupId = parts.length > 10 && !parts[10].isEmpty()
                                    ? Integer.parseInt(parts[10])
                                    : -1;
                            LayerGroup group = findOrCreateGroup(groups, groupId);
                            group.layers.add(layer);
                        }
                    }
                } else if (parts[0].equals("2")) {
                    // Group layer data
                    if (parts.length >= 10) {
                        int groupId = Integer.parseInt(parts[9]);
                        LayerGroup group = findOrCreateGroup(groups, groupId);
                        group.name = parts[1];
                    }
                }
            }
        }

        return groups;
    }

    /**
     * Parse layer line from TXT file
     */
    private static Layer parseLayerLine(String[] parts, String characterName, File imageDir) {
        try {
            String name = parts[1];
            int left = Integer.parseInt(parts[2]);
            int top = Integer.parseInt(parts[3]);
            int opacity = Integer.parseInt(parts[7]);
            int layerId = Integer.parseInt(parts[9]);

            // Find image file
            Bitmap bitmap = loadLayerImage(characterName, layerId, imageDir);
            if (bitmap == null) {
                return null;
            }

            return new Layer(name, left, top, opacity, layerId, bitmap);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse JSON format coordinate file
     */
    public static List<LayerGroup> parseJsonFile(File file, File imageDir) throws IOException {
        List<LayerGroup> groups = new ArrayList<>();
        LayerGroup noneGroup = new LayerGroup("(none)", -1);
        groups.add(noneGroup);

        String characterName = file.getName()
                .replaceFirst("[.][^.]+$", "") // Remove .json
                .replaceFirst("[.][^.]+$", ""); // Remove .pbd

        // Read file content
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }

        // Parse JSON
        JsonArray items = JsonParser.parseString(jsonBuilder.toString()).getAsJsonArray();

        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();

            if (!item.has("layer_type")) continue;
            int layerType = item.get("layer_type").getAsInt();

            if (layerType == 0) {
                // Layer data
                Layer layer = parseLayerJson(item, characterName, imageDir);
                if (layer != null) {
                    int groupId = item.has("group_layer_id")
                            ? item.get("group_layer_id").getAsInt()
                            : -1;
                    LayerGroup group = findOrCreateGroup(groups, groupId);
                    group.layers.add(layer);
                }
            } else if (layerType == 2) {
                // Group data
                int groupId = item.get("layer_id").getAsInt();
                LayerGroup group = findOrCreateGroup(groups, groupId);
                group.name = item.get("name").getAsString();
            }
        }

        return groups;
    }

    /**
     * Parse layer from JSON object
     */
    private static Layer parseLayerJson(JsonObject item, String characterName, File imageDir) {
        try {
            String name = item.get("name").getAsString();
            int left = item.get("left").getAsInt();
            int top = item.get("top").getAsInt();
            int opacity = item.get("opacity").getAsInt();
            int layerId = item.get("layer_id").getAsInt();

            Bitmap bitmap = loadLayerImage(characterName, layerId, imageDir);
            if (bitmap == null) {
                return null;
            }

            return new Layer(name, left, top, opacity, layerId, bitmap);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Load layer image from file
     * Expected filename: {characterName}_{layerId}.{ext}
     */
    private static Bitmap loadLayerImage(String characterName, int layerId, File imageDir) {
        // Try common image extensions
        String[] extensions = {".png", ".jpg", ".jpeg", ".bmp"};

        for (String ext : extensions) {
            File imageFile = new File(imageDir, characterName + "_" + layerId + ext);
            if (imageFile.exists()) {
                try {
                    return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                } catch (Exception e) {
                    // Try next extension
                }
            }
        }

        return null;
    }

    /**
     * Find or create layer group by ID
     */
    private static LayerGroup findOrCreateGroup(List<LayerGroup> groups, int groupId) {
        // Check if group already exists
        for (LayerGroup group : groups) {
            if (group.groupLayerId == groupId) {
                return group;
            }
        }

        // Create new group
        LayerGroup newGroup = new LayerGroup("Group " + groupId, groupId);
        groups.add(newGroup);
        return newGroup;
    }
}
