# AI Chat - Android应用

支持多个AI提供商的Android聊天应用，具有流式输出和WebDAV同步功能。

## 功能特性

- **多提供商支持**: OpenAI、Gemini、Anthropic
- **流式输出**: 实时显示AI响应
- **本地存储**: 无需登录的临时记录
- **WebDAV同步**: 手动保存和恢复配置及对话记录
- **安全加密**: 使用Android Keystore加密敏感信息

## 技术栈

- Kotlin
- Jetpack Compose
- MVVM架构
- Room数据库
- OkHttp + SSE
- Android Keystore

## 编译说明

### 本地编译

1. 安装Android Studio Hedgehog或更新版本
2. 打开项目
3. 等待Gradle同步完成
4. **重要**: 需要为应用生成图标PNG文件，或者使用Android Studio的Image Asset Studio工具生成图标到各个mipmap目录
5. 运行应用

### GitHub Actions自动构建

项目已配置GitHub Actions工作流，可自动编译打包APK文件：

1. **触发方式**:
   - 推送代码到 `main`、`master` 或 `develop` 分支时自动触发
   - 创建Pull Request时自动触发
   - 在Actions页面手动触发（workflow_dispatch）

2. **构建产物**:
   - `app-debug.apk`: Debug版本APK（用于开发测试）
   - `app-release-unsigned.apk`: Release版本APK（未签名）

3. **下载APK**:
   - 在GitHub仓库的"Actions"标签页找到对应的工作流运行记录
   - 点击进入具体的运行详情
   - 在"Artifacts"部分下载编译好的APK文件

4. **自动生成应用图标**: 工作流会自动生成占位符应用图标（绿色背景带"AI"文字）

5. **创建Release**: 当推送标签（如 `v1.0.0`）时，会自动创建GitHub Release并附带APK文件

## 最低要求

- Android 8.0 (API 26)
- 目标版本: Android 14 (API 34)

## 配置

首次使用时，需要在设置页面配置：

1. 选择提供商（OpenAI/Gemini/Anthropic）
2. 输入API Key
3. 设置Base URL
4. 指定模型名称

## WebDAV同步

可选配置WebDAV实现数据同步：

1. 输入WebDAV服务器URL
2. 提供用户名和密码
3. 设置远程路径
4. 手动上传/下载数据

**注意**: 默认不上传API Keys以保护安全。

## 许可证

本项目为MVP演示版本。
