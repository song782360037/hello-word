# GitHub Actions 工作流说明

本目录包含用于自动化构建Android应用的GitHub Actions工作流配置。

## 工作流文件

### build-apk.yml

自动编译和打包Android APK文件的工作流。

#### 触发条件

1. **推送代码** - 当代码推送到以下分支时自动触发：
   - `main`
   - `master`
   - `develop`

2. **Pull Request** - 当创建或更新Pull Request到上述分支时触发

3. **手动触发** - 在GitHub仓库的Actions页面可以手动运行工作流

4. **标签发布** - 推送以`v`开头的标签（如`v1.0.0`）时会自动创建GitHub Release

#### 构建步骤

1. **检出代码** - 从仓库拉取最新代码
2. **设置JDK 17** - 配置Java开发环境（使用Temurin发行版）
3. **授予gradlew执行权限** - 确保Gradle包装器可执行
4. **创建占位符图标** - 自动生成应用图标（绿色背景带"AI"文字）
   - 为所有密度（mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi）生成图标
   - 包括标准图标和圆形图标
5. **构建Debug APK** - 编译调试版本
6. **构建Release APK** - 编译发布版本（未签名）
7. **上传构建产物** - 将APK文件作为工作流产物上传（保留30天）

#### 构建产物

构建完成后，可以在工作流运行页面的"Artifacts"部分下载以下文件：

- **app-debug** - Debug版本APK（`app-debug.apk`）
  - 用于开发和测试
  - 包含调试信息
  - 文件体积较大

- **app-release** - Release版本APK（`app-release-unsigned.apk`）
  - 用于生产环境
  - 代码经过优化
  - 需要签名才能在生产设备上安装

#### 如何下载构建的APK

1. 打开GitHub仓库页面
2. 点击顶部的"Actions"标签
3. 选择"Build Android APK"工作流
4. 点击想要下载的工作流运行记录
5. 滚动到页面底部的"Artifacts"部分
6. 点击相应的artifact名称下载ZIP文件
7. 解压ZIP文件即可获得APK

#### 如何手动触发构建

1. 打开GitHub仓库页面
2. 点击"Actions"标签
3. 在左侧选择"Build Android APK"工作流
4. 点击右侧的"Run workflow"按钮
5. 选择要构建的分支
6. 点击"Run workflow"确认

#### 如何创建Release

创建带有APK文件的GitHub Release：

```bash
# 创建并推送标签
git tag v1.0.0
git push origin v1.0.0
```

工作流会自动：
1. 检测到标签推送
2. 构建Debug和Release版本APK
3. 创建GitHub Release
4. 将APK文件附加到Release

## 环境要求

- **操作系统**: Ubuntu Latest
- **JDK版本**: 17 (Temurin)
- **Gradle**: 8.2 (通过wrapper)
- **Android SDK**: 自动通过Gradle下载

## 缓存策略

工作流使用Gradle缓存来加速构建：
- Gradle依赖包
- Gradle构建缓存
- Android SDK组件

## 故障排查

### 构建失败

1. 检查工作流日志中的错误信息
2. 确认Gradle配置正确
3. 验证依赖版本兼容性

### 图标生成失败

如果自动图标生成失败，可以：
1. 在本地使用Android Studio的Image Asset Studio生成图标
2. 将图标文件提交到仓库的`app/src/main/res/mipmap-*`目录
3. 修改工作流，跳过图标生成步骤

### APK上传失败

确认：
1. APK文件路径正确
2. Gradle构建成功完成
3. 检查工作流权限设置

## 自定义工作流

### 修改触发分支

编辑`build-apk.yml`中的`on.push.branches`和`on.pull_request.branches`：

```yaml
on:
  push:
    branches: [ main, develop, feature/* ]
  pull_request:
    branches: [ main, develop ]
```

### 添加签名配置

要构建已签名的Release APK，需要添加签名配置：

1. 在GitHub仓库的Settings > Secrets添加以下secrets：
   - `KEYSTORE_FILE`: Base64编码的keystore文件
   - `KEYSTORE_PASSWORD`: Keystore密码
   - `KEY_ALIAS`: 密钥别名
   - `KEY_PASSWORD`: 密钥密码

2. 在工作流中添加签名步骤（参考Android官方文档）

### 修改保留时间

默认构建产物保留30天，可以修改`retention-days`参数：

```yaml
- name: Upload Debug APK
  uses: actions/upload-artifact@v4
  with:
    name: app-debug
    path: app/build/outputs/apk/debug/app-debug.apk
    retention-days: 90  # 修改为90天
```

## 最佳实践

1. **定期清理旧的工作流运行** - 避免占用过多存储空间
2. **使用缓存** - 加速构建过程
3. **保护敏感信息** - 使用GitHub Secrets存储密钥和密码
4. **版本标签** - 使用语义化版本号（Semantic Versioning）
5. **测试PR** - 在合并前确保PR构建成功

## 相关链接

- [GitHub Actions文档](https://docs.github.com/en/actions)
- [Android Gradle插件文档](https://developer.android.com/studio/build)
- [签名您的应用](https://developer.android.com/studio/publish/app-signing)
