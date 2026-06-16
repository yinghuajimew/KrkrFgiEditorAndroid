# Krkr Fgi Editor - Android Version

Android port of the Kirikiri Figure Image Editor (吉里吉里立绘合成工具).

## Features

- **Single Image Composition**: Merge multiple layers into one image
- **Automatic Encoding Detection**: Supports Shift-JIS, UTF-8, and Unicode
- **Smart Layer Sorting**: Automatically sort layers by alpha channel sum
- **Real-time Preview**: See the result before saving
- **Batch Composition**: ✅ Generate all layer combinations automatically

## Supported Formats

### Coordinate Files
- **TXT format**: Tab-separated values with layer information
- **JSON format**: JSON array with layer objects

### Layer Images
Images should be named: `{characterName}_{layerId}.{ext}`

Example: `芳乃a_1.png`, `寧々a_0.png`

## Requirements

- Android 7.0 (API 24) or higher
- Storage permissions for reading coordinate files and images

## Build

```bash
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Usage

### Single Composition
1. Open the app
2. Tap the "Open File" button in the toolbar
3. Select a coordinate file (.txt or .json)
4. Select layers from the available list
5. View the preview
6. Tap the save button to export the merged image

### Batch Composition
1. Open the app and load a coordinate file
2. Tap the "Batch Merge" button
3. Configure options:
   - Skip Empty Layers: Exclude layers with no image
   - Auto Sort: Sort layers by alpha channel sum
4. View the total number of combinations
5. Tap "Start Batch Merge" to begin processing
6. Wait for completion (progress bar shows current status)
7. View results with thumbnails
8. Find saved images in `Pictures/KrkrFgiEditor/Batch/`

## Technical Details

### Architecture

```
com.krkr.fgieditor
├── model/          # Data models (Layer, LayerGroup)
├── util/           # Utility classes (ImageProcessor, EncodingDetector, Parser)
├── adapter/        # RecyclerView adapters
└── ui/             # Activities and UI components
```

### Key Algorithms

1. **Encoding Detection**: Counts characters outside Japanese/English ranges
2. **Layer Sorting**: Sorts by alpha sum (higher alpha = bottom layer)
3. **Image Merging**: Uses Android Canvas to composite layers with opacity

## Original Project

This is a Java/Android port of the C# WinForms application:
https://github.com/[original-repo]/krkrFgiEditor

## License

[Specify license]

## Credits

- Original Windows version by [original author]
- Android port created with Claude Code
