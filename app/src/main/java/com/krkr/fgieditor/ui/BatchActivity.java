package com.krkr.fgieditor.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.krkr.fgieditor.R;
import com.krkr.fgieditor.adapter.BatchResultAdapter;
import com.krkr.fgieditor.model.BatchResult;
import com.krkr.fgieditor.model.Layer;
import com.krkr.fgieditor.model.LayerGroup;
import com.krkr.fgieditor.util.BatchDataHolder;
import com.krkr.fgieditor.util.ImageProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchActivity extends AppCompatActivity {

    private CheckBox skipEmptyCheckbox;
    private CheckBox autoSortCheckbox;
    private TextView combinationsText;
    private TextView groupsInfoText;
    private View progressCard;
    private TextView progressText;
    private ProgressBar progressBar;
    private RecyclerView resultsRecyclerView;
    private Button startButton;

    private List<LayerGroup> layerGroups;
    private String baseFilename;
    private List<BatchResult> batchResults = new ArrayList<>();
    private BatchTask currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
        loadData();
        calculateCombinations();
        setupListeners();
    }

    private void initViews() {
        skipEmptyCheckbox = findViewById(R.id.skip_empty_checkbox);
        autoSortCheckbox = findViewById(R.id.auto_sort_checkbox);
        combinationsText = findViewById(R.id.combinations_text);
        groupsInfoText = findViewById(R.id.groups_info_text);
        progressCard = findViewById(R.id.progress_card);
        progressText = findViewById(R.id.progress_text);
        progressBar = findViewById(R.id.progress_bar);
        resultsRecyclerView = findViewById(R.id.results_recycler_view);
        startButton = findViewById(R.id.start_button);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadData() {
        // Get layer groups from singleton
        BatchDataHolder holder = BatchDataHolder.getInstance();
        layerGroups = holder.getLayerGroups();
        baseFilename = holder.getBaseFilename();

        if (layerGroups == null) {
            layerGroups = new ArrayList<>();
        }
        if (baseFilename == null) {
            baseFilename = "merged";
        }
    }

    private void calculateCombinations() {
        if (layerGroups == null || layerGroups.isEmpty()) {
            combinationsText.setText(getString(R.string.total_combinations, 0));
            groupsInfoText.setText(R.string.no_layer_groups);
            startButton.setEnabled(false);
            return;
        }

        boolean skipEmpty = skipEmptyCheckbox.isChecked();
        int total = 1;
        StringBuilder info = new StringBuilder();

        for (LayerGroup group : layerGroups) {
            int count = skipEmpty ? countNonEmptyLayers(group) : group.layers.size();
            if (count == 0) {
                total = 0;
                break;
            }
            total *= count;
            info.append(group.name).append(": ").append(count).append(" layers\n");
        }

        combinationsText.setText(getString(R.string.total_combinations, total));
        groupsInfoText.setText(info.toString().trim());
        startButton.setEnabled(total > 0 && total <= 10000); // Limit to prevent crashes
    }

    private int countNonEmptyLayers(LayerGroup group) {
        int count = 0;
        for (Layer layer : group.layers) {
            if (!layer.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private void setupListeners() {
        skipEmptyCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> calculateCombinations());
        autoSortCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {});

        startButton.setOnClickListener(v -> startBatchProcess());
    }

    private void startBatchProcess() {
        if (layerGroups == null || layerGroups.isEmpty()) {
            Toast.makeText(this, R.string.no_valid_combinations, Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable UI
        startButton.setEnabled(false);
        skipEmptyCheckbox.setEnabled(false);
        autoSortCheckbox.setEnabled(false);
        progressCard.setVisibility(View.VISIBLE);

        // Start async task
        boolean skipEmpty = skipEmptyCheckbox.isChecked();
        boolean autoSort = autoSortCheckbox.isChecked();
        currentTask = new BatchTask(skipEmpty, autoSort);
        currentTask.execute();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (currentTask != null && currentTask.getStatus() == AsyncTask.Status.RUNNING) {
            currentTask.cancel(true);
            Toast.makeText(this, R.string.batch_cancelled, Toast.LENGTH_SHORT).show();
        }
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentTask != null) {
            currentTask.cancel(true);
        }
    }

    private class BatchTask extends AsyncTask<Void, Progress, List<BatchResult>> {
        private final boolean skipEmpty;
        private final boolean autoSort;
        private int totalCombinations;

        BatchTask(boolean skipEmpty, boolean autoSort) {
            this.skipEmpty = skipEmpty;
            this.autoSort = autoSort;
        }

        @Override
        protected List<BatchResult> doInBackground(Void... voids) {
            List<BatchResult> results = new ArrayList<>();

            // Generate all combinations
            List<List<Layer>> combinations = generateCombinations(layerGroups, skipEmpty);
            totalCombinations = combinations.size();

            if (totalCombinations == 0) {
                return results;
            }

            // Create output directory
            File outputDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "KrkrFgiEditor/Batch");
            outputDir.mkdirs();

            // Process each combination
            Map<Integer, Long> alphaCache = new HashMap<>();

            for (int i = 0; i < combinations.size() && !isCancelled(); i++) {
                List<Layer> combination = combinations.get(i);

                // Sort if needed
                if (autoSort) {
                    sortLayers(combination, alphaCache);
                }

                // Generate filename
                String filename = generateBatchFilename(combination, i);

                // Merge layers
                Bitmap result = ImageProcessor.mergeLayers(combination);
                if (result == null) continue;

                // Save to file
                File outputFile = new File(outputDir, filename);
                try (OutputStream out = new FileOutputStream(outputFile)) {
                    result.compress(Bitmap.CompressFormat.PNG, 100, out);

                    // Create thumbnail
                    Bitmap thumbnail = ImageProcessor.resizeBitmap(result, 128, 128);

                    // Add to results
                    BatchResult batchResult = new BatchResult(
                            filename,
                            new ArrayList<>(combination),
                            thumbnail,
                            outputFile.getAbsolutePath()
                    );
                    results.add(batchResult);

                    // Notify media scanner
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(Uri.fromFile(outputFile));
                    sendBroadcast(mediaScanIntent);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Update progress
                publishProgress(new Progress(i + 1, totalCombinations));
            }

            return results;
        }

        @Override
        protected void onProgressUpdate(Progress... values) {
            Progress progress = values[0];
            progressText.setText(getString(R.string.processing, progress.current, progress.total));
            progressBar.setMax(progress.total);
            progressBar.setProgress(progress.current);
        }

        @Override
        protected void onPostExecute(List<BatchResult> results) {
            batchResults = results;

            // Show results
            progressCard.setVisibility(View.GONE);
            resultsRecyclerView.setVisibility(View.VISIBLE);

            BatchResultAdapter adapter = new BatchResultAdapter(results, result -> {
                // Could open viewer or share
                Toast.makeText(BatchActivity.this,
                        getString(R.string.saved_to, result.filePath),
                        Toast.LENGTH_LONG).show();
            });
            resultsRecyclerView.setAdapter(adapter);

            // Update button
            startButton.setText(getString(R.string.batch_complete, results.size()));
            startButton.setEnabled(false);

            Toast.makeText(BatchActivity.this,
                    getString(R.string.batch_complete, results.size()),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled() {
            progressCard.setVisibility(View.GONE);
            startButton.setEnabled(true);
            skipEmptyCheckbox.setEnabled(true);
            autoSortCheckbox.setEnabled(true);
        }

        private List<List<Layer>> generateCombinations(List<LayerGroup> groups, boolean skipEmpty) {
            List<List<Layer>> result = new ArrayList<>();
            generateCombinationsRecursive(groups, 0, new ArrayList<>(), result, skipEmpty);
            return result;
        }

        private void generateCombinationsRecursive(List<LayerGroup> groups, int groupIndex,
                                                   List<Layer> current, List<List<Layer>> result,
                                                   boolean skipEmpty) {
            if (groupIndex >= groups.size()) {
                result.add(new ArrayList<>(current));
                return;
            }

            LayerGroup group = groups.get(groupIndex);
            for (Layer layer : group.layers) {
                if (skipEmpty && layer.isEmpty()) {
                    continue;
                }

                current.add(layer);
                generateCombinationsRecursive(groups, groupIndex + 1, current, result, skipEmpty);
                current.remove(current.size() - 1);
            }
        }

        private void sortLayers(List<Layer> layers, Map<Integer, Long> cache) {
            // Calculate alpha sums if not cached
            for (Layer layer : layers) {
                if (!cache.containsKey(layer.layerId)) {
                    long alphaSum = ImageProcessor.calculateAlphaSum(layer.bitmap);
                    long adjustedSum = (alphaSum * layer.opacity) / 255;
                    cache.put(layer.layerId, adjustedSum);
                }
            }

            // Sort by alpha sum (descending)
            Collections.sort(layers, new Comparator<Layer>() {
                @Override
                public int compare(Layer a, Layer b) {
                    long alphaA = cache.get(a.layerId);
                    long alphaB = cache.get(b.layerId);
                    return Long.compare(alphaB, alphaA);
                }
            });
        }

        private String generateBatchFilename(List<Layer> layers, int index) {
            StringBuilder name = new StringBuilder(baseFilename);

            for (Layer layer : layers) {
                if (!layer.isEmpty()) {
                    name.append("_").append(layer.layerId);
                }
            }

            name.append("_").append(String.format("%04d", index));
            name.append(".png");
            return name.toString();
        }
    }

    private static class Progress {
        int current;
        int total;

        Progress(int current, int total) {
            this.current = current;
            this.total = total;
        }
    }
}
