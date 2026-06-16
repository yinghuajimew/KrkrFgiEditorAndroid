package com.krkr.fgieditor.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.krkr.fgieditor.R;
import com.krkr.fgieditor.adapter.LayerAdapter;
import com.krkr.fgieditor.model.Layer;
import com.krkr.fgieditor.model.LayerGroup;
import com.krkr.fgieditor.util.BatchDataHolder;
import com.krkr.fgieditor.util.CoordinateFileParser;
import com.krkr.fgieditor.util.ImageProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OPEN_FILE = 1001;

    private Spinner groupSpinner;
    private RecyclerView layerRecyclerView;
    private RecyclerView selectedLayerRecyclerView;
    private ImageView previewImageView;
    private FloatingActionButton saveFab;
    private Button batchButton;

    private List<LayerGroup> layerGroups = new ArrayList<>();
    private List<Layer> selectedLayers = new ArrayList<>();
    private LayerAdapter layerAdapter;
    private LayerAdapter selectedLayerAdapter;
    private Map<Integer, Long> layerAlphaCache = new HashMap<>();

    private File currentCoordinateFile;
    private File currentImageDir;
    private boolean autoSort = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
        setupListeners();
    }

    private void initViews() {
        groupSpinner = findViewById(R.id.group_spinner);
        layerRecyclerView = findViewById(R.id.layer_recycler_view);
        selectedLayerRecyclerView = findViewById(R.id.selected_layer_recycler_view);
        previewImageView = findViewById(R.id.preview_image);
        saveFab = findViewById(R.id.save_fab);
        batchButton = findViewById(R.id.batch_button);

        // Setup RecyclerViews
        layerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedLayerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup adapters
        layerAdapter = new LayerAdapter(new ArrayList<>(), this::onLayerSelected);
        selectedLayerAdapter = new LayerAdapter(selectedLayers, null);

        layerRecyclerView.setAdapter(layerAdapter);
        selectedLayerRecyclerView.setAdapter(selectedLayerAdapter);

        // Initially disabled
        saveFab.setEnabled(false);
        batchButton.setEnabled(false);
    }

    private void setupListeners() {
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < layerGroups.size()) {
                    updateLayerList(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        saveFab.setOnClickListener(v -> saveImage());

        batchButton.setOnClickListener(v -> {
            // Store data in singleton
            String baseFilename = "merged";
            if (currentCoordinateFile != null) {
                baseFilename = currentCoordinateFile.getName()
                        .replaceFirst("[.][^.]+$", "");
            }
            BatchDataHolder.getInstance().setData(layerGroups, baseFilename);

            // Start batch activity
            Intent intent = new Intent(MainActivity.this, BatchActivity.class);
            startActivity(intent);
        });

        selectedLayerRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, selectedLayerRecyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                // Click to preview
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                // Long click to remove
                                removeSelectedLayer(position);
                            }
                        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open) {
            openFile();
            return true;
        } else if (id == R.id.action_auto_sort) {
            item.setChecked(!item.isChecked());
            autoSort = item.isChecked();
            if (autoSort && !selectedLayers.isEmpty()) {
                sortSelectedLayers();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/plain", "application/json"});
        startActivityForResult(intent, REQUEST_OPEN_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OPEN_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                loadCoordinateFile(uri);
            }
        }
    }

    private void loadCoordinateFile(Uri uri) {
        try {
            // Get file path (simplified - in production use proper URI handling)
            String path = uri.getPath();
            if (path == null) {
                Toast.makeText(this, R.string.cannot_access_file, Toast.LENGTH_SHORT).show();
                return;
            }

            currentCoordinateFile = new File(path);
            currentImageDir = currentCoordinateFile.getParentFile();

            // Parse file
            layerGroups = CoordinateFileParser.parseFile(currentCoordinateFile, currentImageDir);

            if (layerGroups.isEmpty() || layerGroups.get(0).layers.isEmpty()) {
                Toast.makeText(this, R.string.no_layers_found, Toast.LENGTH_SHORT).show();
                return;
            }

            // Update UI
            updateGroupSpinner();
            batchButton.setEnabled(true);

            Toast.makeText(this, R.string.loaded_successfully, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_loading_file, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateGroupSpinner() {
        List<String> groupNames = new ArrayList<>();
        for (LayerGroup group : layerGroups) {
            groupNames.add(group.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, groupNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        if (!groupNames.isEmpty()) {
            groupSpinner.setSelection(0);
        }
    }

    private void updateLayerList(int groupIndex) {
        LayerGroup group = layerGroups.get(groupIndex);
        layerAdapter = new LayerAdapter(group.layers, this::onLayerSelected);
        layerRecyclerView.setAdapter(layerAdapter);
    }

    private void onLayerSelected(Layer layer) {
        // Check if already selected
        for (Layer selected : selectedLayers) {
            if (selected.layerId == layer.layerId) {
                Toast.makeText(this, R.string.layer_already_selected, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        selectedLayers.add(layer);

        if (autoSort) {
            sortSelectedLayers();
        } else {
            selectedLayerAdapter.notifyDataSetChanged();
        }

        updatePreview();
        saveFab.setEnabled(true);
    }

    private void removeSelectedLayer(int position) {
        if (position >= 0 && position < selectedLayers.size()) {
            selectedLayers.remove(position);
            selectedLayerAdapter.notifyDataSetChanged();
            updatePreview();

            if (selectedLayers.isEmpty()) {
                saveFab.setEnabled(false);
            }
        }
    }

    private void sortSelectedLayers() {
        // Calculate alpha sums if not cached
        for (Layer layer : selectedLayers) {
            if (!layerAlphaCache.containsKey(layer.layerId)) {
                long alphaSum = ImageProcessor.calculateAlphaSum(layer.bitmap);
                long adjustedSum = (alphaSum * layer.opacity) / 255;
                layerAlphaCache.put(layer.layerId, adjustedSum);
            }
        }

        // Sort by alpha sum (descending - higher alpha goes to bottom)
        Collections.sort(selectedLayers, new Comparator<Layer>() {
            @Override
            public int compare(Layer a, Layer b) {
                long alphaA = layerAlphaCache.get(a.layerId);
                long alphaB = layerAlphaCache.get(b.layerId);
                return Long.compare(alphaB, alphaA);
            }
        });

        selectedLayerAdapter.notifyDataSetChanged();
        updatePreview();
    }

    private void updatePreview() {
        if (selectedLayers.isEmpty()) {
            previewImageView.setImageBitmap(null);
            return;
        }

        // Merge layers in background (simplified - should use AsyncTask/Thread)
        Bitmap preview = ImageProcessor.mergeLayers(selectedLayers);
        if (preview != null) {
            // Resize for preview
            Bitmap resized = ImageProcessor.resizeBitmap(preview,
                    previewImageView.getWidth(),
                    previewImageView.getHeight());
            previewImageView.setImageBitmap(resized);
        }
    }

    private void saveImage() {
        if (selectedLayers.isEmpty()) return;

        // Generate filename
        String fileName = generateFileName();

        // Merge and save
        Bitmap result = ImageProcessor.mergeLayers(selectedLayers);
        if (result == null) {
            Toast.makeText(this, R.string.failed_to_merge, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Save to Pictures directory
            File picturesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File outputFile = new File(picturesDir, "KrkrFgiEditor/" + fileName);
            outputFile.getParentFile().mkdirs();

            try (OutputStream out = new FileOutputStream(outputFile)) {
                result.compress(Bitmap.CompressFormat.PNG, 100, out);
            }

            Toast.makeText(this, getString(R.string.saved_image, fileName), Toast.LENGTH_LONG).show();

            // Notify media scanner
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(outputFile));
            sendBroadcast(mediaScanIntent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.save_failed, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    }

    private String generateFileName() {
        StringBuilder name = new StringBuilder();

        if (currentCoordinateFile != null) {
            String baseName = currentCoordinateFile.getName()
                    .replaceFirst("[.][^.]+$", "");
            name.append(baseName);
        } else {
            name.append("merged");
        }

        for (Layer layer : selectedLayers) {
            if (!layer.isEmpty()) {
                name.append("_").append(layer.layerId);
            }
        }

        name.append(".png");
        return name.toString();
    }
}
