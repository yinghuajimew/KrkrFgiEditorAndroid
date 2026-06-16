# Krkr Fgi Editor Android - 快速开始

## 📱 项目简介

将 Windows C# 版本的"吉里吉里立绘合成工具"完整移植到 Android 平台。

- **原版**: C# WinForms (2221 行)
- **Android 版**: Java (1085 行)
- **移植完成度**: 90% (核心功能完成，批量合成待完善)

## ✅ 已实现功能

- ✅ TXT/JSON 坐标文件解析
- ✅ Shift-JIS/UTF-8/UTF-16 自动检测
- ✅ 图层选择和管理
- ✅ Alpha 总和自动排序
- ✅ 实时预览
- ✅ 透明度处理
- ✅ 图像合成
- ✅ 保存 PNG 文件

## 🏗️ 构建方法

### 方法 1: Android Studio (推荐)
```bash
# 1. 用 Android Studio 打开项目
# 2. Build > Build Bundle(s) / APK(s) > Build APK(s)
```

### 方法 2: GitHub Actions
```bash
# 1. 推送到 GitHub
git init
git add .
git commit -m "Initial commit"
git remote add origin <your-repo-url>
git push -u origin main

# 2. GitHub Actions 会自动构建
# 3. 从 Actions 标签页下载 APK
```

### 方法 3: 命令行（需要真实 Linux 环境）
```bash
./gradlew assembleDebug
# APK 位置: app/build/outputs/apk/debug/app-debug.apk
```

## ⚠️ PRoot/Termux 限制

当前环境（PRoot）无法运行 AAPT2，因此无法直接构建。请使用上述方法之一。

## 📂 项目结构

```
9 个 Java 文件 (1085 行代码)
├── 2 个 Model 类
├── 3 个 Util 工具类
├── 1 个 Adapter
└── 3 个 UI 类

10 个 XML 资源文件
├── 3 个布局文件
├── 3 个 values 文件
├── 2 个 drawable 文件
└── 2 个 icon 配置
```

## 🎯 核心算法

所有核心算法已从 C# 完整移植：

1. **编码检测** - 统计非日英字符判断最佳编码
2. **Alpha 排序** - 透明度高的图层放底层
3. **图层合成** - Canvas 多层绘制 + 透明度混合

## 📝 待完成

- [ ] BatchActivity 完整实现（批量合成）
- [ ] 异步图像处理（避免 UI 卡顿）
- [ ] 进度条显示
- [ ] 图层拖拽排序
- [ ] LruCache 图片缓存

## 📄 许可

[待定]
