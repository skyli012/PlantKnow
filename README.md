# PlantKnow 🌿

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.0-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Coroutines-orange.svg)](https://developer.android.com/topic/architecture)
[![API](https://img.shields.io/badge/API-21%2B-yellow.svg)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

**基于云端AI的智能植物识别Android应用**

*发现自然，识别万物——用科技探索植物世界*

[项目展示](#-项目展示) • [功能特性](#-功能特性) • [技术架构](#-技术架构) • [快速开始](#-快速开始) • [项目结构](#-项目结构)

</div>

## 📸 项目展示

| 识别主页 | 拍照识别 | 识别结果 | 百科详情 |
|---------|----------|----------|----------|
| ![主页](https://via.placeholder.com/200x400/4CAF50/FFFFFF?text=主页) | ![拍照](https://via.placeholder.com/200x400/2196F3/FFFFFF?text=拍照) | ![结果](https://via.placeholder.com/200x400/FF9800/FFFFFF?text=结果) | ![详情](https://via.placeholder.com/200x400/9C27B0/FFFFFF?text=详情) |

## ✨ 功能特性

### 🌟 核心功能
- **📷 智能图像采集** - 支持系统相机拍照与相册选择
- **🤖 云端AI识别** - 基于百度AI平台，识别准确率超97%
- **🌿 植物百科** - 详细的植物信息与百科知识展示
- **⚡ 极速响应** - 核心识别流程平均耗时<800ms

### 🎯 技术亮点
- **💫 现代化UI** - 采用Jetpack Compose，Material Design 3设计语言
- **🏗️ 清晰架构** - MVVM架构 + 模块化设计，代码高度可维护
- **🚀 性能优化** - 智能图片压缩，内存优化，异步处理
- **🔧 健壮性** - 完善的错误处理与状态管理

## 🛠️ 技术架构

### 技术栈
**语言与框架**
- `Kotlin` - 首选开发语言
- `Jetpack Compose` - 声明式UI框架
- `Coroutines & Flow` - 异步处理与数据流

**架构模式**
- `MVVM` - 模型-视图-视图模型架构
- `Repository Pattern` - 数据仓库模式
- `Dependency Injection` - 依赖注入

**Jetpack组件**
- `ViewModel` - 生命周期感知的数据管理
- `LiveData/StateFlow` - 响应式数据流
- `Navigation` - 页面导航

**第三方库**
- `Retrofit` - 类型安全的HTTP客户端
- `Coil` - Kotlin首选的图片加载库
- `Accompanist` - Compose扩展库

## 🚀 快速开始

### 环境要求
- Android Studio Flamingo | 2022.2.1 或更高版本
- JDK 17
- Android SDK API 33+
- Gradle 8.0+

### 构建步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/PlantKnow.git
   cd PlantKnow
