<div align="center">

# MediaCompress

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-blue.svg?logo=kotlin)](https://kotlinlang.org) [![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.4-brightgreen.svg)](https://developer.android.com/jetpack/compose) [![Architecture](https://img.shields.io/badge/Architecture-MVVM%2BCoroutines-orange.svg)](https://developer.android.com/topic/architecture) [![API](https://img.shields.io/badge/API-24%2B-yellow.svg)](https://android-arsenal.com/api?level=24)

</div>

<div align="center">
  <img src="docs/icons/ic_launcher.webp" alt="MediaCompress App Icon" width="120" />
</div>

> 一款基于 **Android** 的高性能媒体压缩应用，使用 **Kotlin + Jetpack Compose + MVVM** 架构打造，支持拍照选取、相册导入、智能压缩与本地存储，全流程流畅高效。

---

## 📱 项目简介

**MediaCompress** 是一款独立设计与开发的高性能媒体压缩应用。  
它实现了从 **媒体选取 → 智能压缩 → 本地存储 → 结果展示** 的完整闭环，支持图片和视频的全方位压缩优化，在保证画质的前提下，文件体积可减少 **60%~80%+**，并在各种网络环境下保持稳定、高效的响应速度。

---

## ✨ 核心功能与亮点

### 🖼️ 智能图像压缩引擎
- **多格式支持**：原格式保留 / JPEG / PNG / WebP，灵活选择。
- **三层压缩机制**：Bitmap 采样 + 分辨率缩放 + 质量控制，图片体积减少 **70%+**。
- **EXIF 保护**：自动识别和维护图片旋转信息。
- **批量处理**：支持多张图片统一处理，并发优化。

### 🎬 专业视频压缩方案
- **硬件加速编码**：基于 Android MediaCodec 的 H.264 编码器，高效省电。
- **FFmpeg 驱动**：集成 FFmpeg-Kit，支持复杂视频处理。
- **自适应分辨率**：支持自定义缩放（如 1280x720）。
- **灵活配置**：质量控制 + 分辨率缩放 + 可选音频移除。

### ⚡ 现代化架构与流畅体验
- 基于 **Jetpack Compose + MVVM + StateFlow** 的响应式架构。
- 使用 **Kotlin 协程** 实现全链路异步操作，主线程全程无阻塞。
- **StateFlow 状态管理**：压缩中 / 完成 / 失败 / 重试，UI 实时反馈。
- 整体压缩流程平均耗时 **<800ms**。

### 🧩 系统性优化与兼容性设计
- 全面优化 **内存管理** 与 **OOM防护**，大图加载安全。
- **Room 本地数据库**：完整的压缩历史记录，支持查询与删除。
- 自适应布局，完美兼容不同屏幕尺寸与 Android 版本。
- 内置 **错误处理机制** 与 **异常恢复策略**。

## 🧰 技术栈

| 模块 | 技术 |
|------|------|
| **语言** | Kotlin |
| **架构** | MVVM + Clean Architecture |
| **UI框架** | Jetpack Compose + Material3 |
| **异步编程** | Kotlin Coroutines + StateFlow |
| **本地数据** | Room Database + DAO |
| **图像处理** | Bitmap API + EXIF 处理 |
| **视频处理** | FFmpeg-Kit + Android MediaCodec |
| **图像加载** | Glide / Coil |
| **导航框架** | Jetpack Navigation Compose |
| **构建工具** | Gradle + Kotlin DSL |

---

## 🖼️ 应用界面预览

| 主页 | 压缩处理 | 历史记录 |  
|------|-----------|------------------|
| ![screenshot1](docs/show/home_page.png) | ![screenshot2](docs/show/compression.png) | ![screenshot3](docs/show/history_page.png) |

---

## 💡 核心算法与流程

### 图片压缩
```
原始图片 → Bitmap 采样 → EXIF 处理 → 分辨率缩放 → 质量压缩 → 保存输出
```

### 视频压缩
```
原始视频 → FFmpeg 命令组装 → H.264 硬件编码 → 分辨率缩放 → 完成处理
```

---

## 📂 项目架构

```
└── app/src/main/java/com/hailong/mediacompress/
    ├── MainActivity.kt                  # 主 Activity，导航容器
    ├── model/
    │   └── MediaItem.kt                 # 媒体数据模型
    ├── repository/
    │   └── MediaRepository.kt           # 数据访问层
    ├── viewmodel/
    │   └── MediaViewModel.kt            # UI 状态管理
    ├── processor/
    │   ├── ImageProcessor.kt            # 图片压缩核心
    │   └── VideoProcessor.kt            # 视频压缩核心
    ├── data/
    │   └── AppDatabase.kt               # Room 数据库
    └── ui/
        ├── screens/                     # 功能页面
        └── theme/                       # 主题配置
```

## 🚀 快速开始

### 环境要求
- Android Studio Koala 或更高版本
- Android SDK 24+ (Android 7.0)
- Kotlin 1.9.20+
- Gradle 8.0+

### 编译与运行
```bash
# 克隆项目
git clone https://github.com/your-repo/MediaCompress.git
cd MediaCompress

# 构建项目
./gradlew build

# 运行应用
./gradlew installDebug
```

---

> 💡 **MediaCompress** 旨在让媒体压缩更智能高效，让存储空间充分利用。
