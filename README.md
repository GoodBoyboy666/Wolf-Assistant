# 小狼助手 (Wolf Assistant)

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/GoodBoyboy666/Wolf-Assistant)](https://github.com/GoodBoyboy666/Wolf-Assistant/releases)
[![Build Status](https://github.com/GoodBoyboy666/Wolf-Assistant/actions/workflows/android-release.yml/badge.svg)](https://github.com/GoodBoyboy666/Wolf-Assistant/actions/workflows/android-release.yml)
[![License](https://img.shields.io/github/license/GoodBoyboy666/Wolf-Assistant)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-28-green)](https://developer.android.com/about/versions/pie)
[![Repo Size](https://img.shields.io/github/repo-size/GoodBoyboy666/Wolf-Assistant)](https://github.com/GoodBoyboy666/Wolf-Assistant)
[![Last Commit](https://img.shields.io/github/last-commit/GoodBoyboy666/Wolf-Assistant)](https://github.com/GoodBoyboy666/Wolf-Assistant/commits/main)
[![Issues](https://img.shields.io/github/issues/GoodBoyboy666/Wolf-Assistant)](https://github.com/GoodBoyboy666/Wolf-Assistant/issues)

**小狼助手** 是一款第三方智慧工大校园服务APP，旨在提供便捷的工具和服务。

## ✨ 功能特性

- **课表服务**：支持查看普通课表与实验课表。
- **服务中心**：轻松使用智慧工大APP服务中心的各项服务。
- **公告服务**：支持接收智慧工大APP公告与学工系统通知。
- **现代化 UI**：基于 Jetpack Compose 构建的现代化用户界面。

## 📥 下载与安装

### 正式版

从 [Releases](https://github.com/GoodBoyboy666/Wolf-Assistant/releases) 页面下载最新的 Release 版本 APK。

> [!TIP]
> 请选择对应设备的 CPU 架构的安装包进行安装。若不清楚设备架构，请选择带有 `universal` 字段的安装包。

### 测试版

如果需要体验最新功能，可以下载带有 `beta`、`preview` 等后缀的 APK 文件。

> [!WARNING]
> 测试版本可能会包含未修复的 Bug，请谨慎用于生产环境。

### 系统要求

- **Android 9.0 (API Level 28)** 及以上版本。

## 🛠️ 本地开发

如果你想在本地编译或运行本项目，请按照以下步骤操作：

1. **克隆仓库**

```bash
git clone https://github.com/GoodBoyboy666/Wolf-Assistant.git
cd Wolf-Assistant
```

1. **构建 Release 包**

```bash
./gradlew assembleRelease
```

### 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **数据库框架**：Room
- **网络请求框架**：Retrofit
- **依赖注入**: Hilt
- **代码规范**: KtLint

## 🤝 参与贡献

非常欢迎各种形式的贡献！如果你对贡献代码感兴趣，欢迎向仓库提交 Pull Request。

> [!IMPORTANT]
> 为保证代码风格一致性，请在提交 PR 前运行以下 Gradle 任务，并确保单元测试通过：
>
> ```bash
> ./gradlew ktlintCheck ktlintFormat
> ```

## 📄 开源协议

本项目采用 **GNU General Public License v3.0** 协议开源。

```text
Wolf Assistant
Copyright (C) 2025-2026  Liang Huaxia

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```
