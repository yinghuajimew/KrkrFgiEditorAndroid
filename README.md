# Krkr Fgi Editor - Android Version

[English](#english) | [中文](#中文)

---

<a name="english"></a>
## English

### 📱 Overview

Android port of the Kirikiri Figure Image Editor (吉里吉里立绘合成工具) - A tool for merging character sprite layers for visual novels.

**Download**: [Latest APK](https://pan.axooo.cn/app-debug.apk) (5.4 MB)

### ✨ Features

- **Single Image Composition**: Merge multiple layers into one image
- **Batch Composition**: Automatically generate all layer combinations (Cartesian product)
- **Automatic Encoding Detection**: Supports Shift-JIS, UTF-8, and UTF-16
- **Smart Layer Sorting**: Sort layers by alpha channel sum for optimal compositing
- **Real-time Preview**: See the result before saving
- **Multiple Formats**: Supports TXT and JSON coordinate files

### 📋 Requirements

- Android 7.0 (API 24) or higher
- Storage permissions for file access

### 🚀 Quick Start

#### Single Composition
1. Tap **Open File** in the toolbar
2. Select a coordinate file (.txt or .json)
3. Choose layers from the available list
4. Preview the composition
5. Tap the **Save** button

#### Batch Composition
1. Load a coordinate file
2. Tap **Batch Merge**
3. Configure options:
   - **Skip Empty Layers**: Exclude placeholder layers
   - **Auto Sort**: Sort by alpha channel sum
4. Review combination count
5. Tap **Start Batch Merge**
6. Find results in `Pictures/KrkrFgiEditor/Batch/`

### 📝 File Format

#### Coordinate Files
**TXT Format** (Tab-separated):
```
芳乃a_0.png	0	0	255
芳乃a_1.png	10	20	255
```

**JSON Format**:
```json
[
  {"filename": "芳乃a_0.png", "x": 0, "y": 0, "opacity": 255},
  {"filename": "芳乃a_1.png", "x": 10, "y": 20, "opacity": 255}
]
```

#### Layer Images
Files should be named: `{characterName}_{layerId}.{ext}`

Example: `芳乃a_1.png`, `寧々a_0.png`

### 🔧 Build from Source

```bash
git clone https://github.com/yinghuajimew/KrkrFgiEditorAndroid.git
cd KrkrFgiEditorAndroid
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

### 📖 Documentation

- [Technical Details](docs/TECHNICAL_DETAILS.md)
- [Development History](docs/DEVELOPMENT_HISTORY.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)

### 🙏 Credits

- Original Windows version by the Kirikiri community
- Android port created with Claude Code

### 📄 License

MIT License

---

<a name="中文"></a>
## 中文

### 📱 项目简介

吉里吉里立绘合成工具的 Android 移植版 - 用于视觉小说角色立绘图层合成的工具。

**下载**: [最新 APK](https://pan.axooo.cn/app-debug.apk) (5.4 MB)

### ✨ 功能特性

- **单图合成**: 将多个图层合并为一张图片
- **批量合成**: 自动生成所有图层组合（笛卡尔积）
- **自动编码检测**: 支持 Shift-JIS、UTF-8 和 UTF-16
- **智能图层排序**: 按透明度总和排序，优化合成效果
- **实时预览**: 保存前预览合成结果
- **多格式支持**: 支持 TXT 和 JSON 坐标文件

### 📋 系统要求

- Android 7.0 (API 24) 或更高版本
- 存储权限用于文件访问

### 🚀 快速开始

#### 单图合成
1. 点击工具栏的**打开文件**
2. 选择坐标文件（.txt 或 .json）
3. 从列表中选择图层
4. 预览合成效果
5. 点击**保存**按钮

#### 批量合成
1. 加载坐标文件
2. 点击**批量合成**
3. 配置选项：
   - **跳过空图层**: 排除占位图层
   - **自动排序**: 按透明度总和排序
4. 查看组合总数
5. 点击**开始批量合成**
6. 在 `相册/KrkrFgiEditor/Batch/` 中查看结果

### 📝 文件格式

#### 坐标文件
**TXT 格式**（制表符分隔）：
```
芳乃a_0.png	0	0	255
芳乃a_1.png	10	20	255
```

**JSON 格式**：
```json
[
  {"filename": "芳乃a_0.png", "x": 0, "y": 0, "opacity": 255},
  {"filename": "芳乃a_1.png", "x": 10, "y": 20, "opacity": 255}
]
```

#### 图层图片
文件命名规则：`{角色名}_{图层ID}.{扩展名}`

示例：`芳乃a_1.png`、`寧々a_0.png`

### 🔧 从源码构建

```bash
git clone https://github.com/yinghuajimew/KrkrFgiEditorAndroid.git
cd KrkrFgiEditorAndroid
./gradlew assembleDebug
```

APK 位置：`app/build/outputs/apk/debug/app-debug.apk`

### 📖 文档

- [技术细节](docs/TECHNICAL_DETAILS.md)
- [开发历史](docs/DEVELOPMENT_HISTORY.md)
- [故障排除](docs/TROUBLESHOOTING.md)

### 🙏 致谢

- 原 Windows 版本由吉里吉里社区开发
- Android 移植版使用 Claude Code 开发

### 📄 许可证

MIT License
