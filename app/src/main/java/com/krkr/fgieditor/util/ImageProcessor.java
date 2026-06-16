package com.krkr.fgieditor.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.krkr.fgieditor.model.Layer;

import java.util.List;

public class ImageProcessor {

    /**
     * Apply opacity to a bitmap
     * @param source Source bitmap
     * @param opacity Opacity value (0-255)
     * @return Bitmap with applied opacity
     */
    public static Bitmap applyOpacity(Bitmap source, int opacity) {
        if (source == null) return null;
        if (opacity == 255) return source;

        Bitmap result = source.copy(Bitmap.Config.ARGB_8888, true);
        int width = result.getWidth();
        int height = result.getHeight();

        int[] pixels = new int[width * height];
        result.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int alpha = Color.alpha(pixels[i]);
            int newAlpha = (alpha * opacity) / 255;
            pixels[i] = Color.argb(newAlpha,
                    Color.red(pixels[i]),
                    Color.green(pixels[i]),
                    Color.blue(pixels[i]));
        }

        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * Calculate sum of all alpha values in bitmap
     * Used for layer sorting
     * @param bitmap Input bitmap
     * @return Sum of alpha values
     */
    public static long calculateAlphaSum(Bitmap bitmap) {
        if (bitmap == null) return 0;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        long sum = 0;
        for (int pixel : pixels) {
            sum += Color.alpha(pixel);
        }
        return sum;
    }

    /**
     * Merge multiple layers into one bitmap
     * @param layers List of layers to merge
     * @return Merged bitmap
     */
    public static Bitmap mergeLayers(List<Layer> layers) {
        if (layers == null || layers.isEmpty()) return null;

        // Filter out empty layers and check if all are empty
        boolean hasNonEmpty = false;
        for (Layer layer : layers) {
            if (!layer.isEmpty()) {
                hasNonEmpty = true;
                break;
            }
        }
        if (!hasNonEmpty) return null;

        // Calculate canvas bounds
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = 0;
        int bottom = 0;

        for (Layer layer : layers) {
            if (layer.isEmpty()) continue;

            left = Math.min(left, layer.x);
            top = Math.min(top, layer.y);
            right = Math.max(right, layer.x + layer.bitmap.getWidth());
            bottom = Math.max(bottom, layer.y + layer.bitmap.getHeight());
        }

        int width = right - left;
        int height = bottom - top;

        // Create result bitmap
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // Draw layers
        for (Layer layer : layers) {
            if (layer.isEmpty()) continue;

            Bitmap layerBitmap = layer.opacity == 255
                    ? layer.bitmap
                    : applyOpacity(layer.bitmap, layer.opacity);

            canvas.drawBitmap(layerBitmap,
                    layer.x - left,
                    layer.y - top,
                    null);

            // Clean up temporary bitmap
            if (layer.opacity != 255 && layerBitmap != layer.bitmap) {
                layerBitmap.recycle();
            }
        }

        return result;
    }

    /**
     * Resize bitmap to fit within bounds while maintaining aspect ratio
     * @param source Source bitmap
     * @param maxWidth Maximum width
     * @param maxHeight Maximum height
     * @return Resized bitmap
     */
    public static Bitmap resizeBitmap(Bitmap source, int maxWidth, int maxHeight) {
        if (source == null) return null;

        int width = source.getWidth();
        int height = source.getHeight();

        float widthRatio = (float) width / maxWidth;
        float heightRatio = (float) height / maxHeight;

        int newWidth, newHeight;
        if (widthRatio < heightRatio) {
            newWidth = (int) (width / heightRatio);
            newHeight = maxHeight;
        } else {
            newWidth = maxWidth;
            newHeight = (int) (height / widthRatio);
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }
}
