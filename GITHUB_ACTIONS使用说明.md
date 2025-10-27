# GitHub Actions APK构建使用说明

## 概述

本项目已配置GitHub Actions自动化工作流，可以在GitHub上自动编译并生成Android APK安装文件。

## 功能特性

✅ 自动构建Debug和Release版本APK  
✅ 自动生成应用图标（无需手动创建图标文件）  
✅ 支持手动触发构建  
✅ 构建产物保留30天  
✅ 支持创建GitHub Release并附带APK文件  

## 快速开始

### 1. 自动触发构建

当你将代码推送到以下分支时，GitHub Actions会自动开始构建：

- `main` 主分支
- `master` 主分支
- `develop` 开发分支

```bash
# 推送代码即可触发构建
git add .
git commit -m "你的提交信息"
git push origin main
```

### 2. 手动触发构建

如果想手动触发构建：

1. 打开你的GitHub仓库
2. 点击顶部的 **"Actions"** 标签
3. 在左侧列表中选择 **"Build Android APK"**
4. 点击右上角的 **"Run workflow"** 按钮
5. 选择要构建的分支
6. 点击绿色的 **"Run workflow"** 按钮确认

### 3. 下载构建的APK

构建完成后，按以下步骤下载APK文件：

1. 进入仓库的 **Actions** 页面
2. 点击最新的工作流运行记录（绿色✓表示成功）
3. 向下滚动到页面底部的 **"Artifacts"** 部分
4. 点击你需要的APK下载：
   - **app-debug**: 调试版本APK（用于开发测试）
   - **app-release**: 发布版本APK（未签名，用于生产）
5. 下载的是ZIP文件，解压后即可得到APK

### 4. 安装APK到手机

#### 方法一：直接安装（Debug版本）
1. 下载并解压 `app-debug` ZIP文件
2. 将 `app-debug.apk` 传输到手机
3. 在手机上打开APK文件进行安装
4. 如提示"未知来源"，需要在设置中允许安装未知应用

#### 方法二：使用ADB安装
```bash
# 解压ZIP文件后，使用ADB安装
adb install app-debug.apk
```

## 创建正式版本发布

当你准备发布新版本时：

```bash
# 1. 确保所有更改已提交
git add .
git commit -m "Release version 1.0.0"

# 2. 创建版本标签
git tag v1.0.0

# 3. 推送标签到GitHub
git push origin v1.0.0
```

GitHub Actions会自动：
- 构建Debug和Release版本APK
- 创建GitHub Release页面
- 将APK文件附加到Release
- 用户可以在Release页面直接下载

## 构建产物说明

### Debug APK (`app-debug.apk`)
- **用途**: 开发和测试
- **特点**: 
  - 包含调试符号
  - 可以直接安装到任何设备
  - 文件较大
  - 未经优化

### Release APK (`app-release-unsigned.apk`)
- **用途**: 生产环境
- **特点**:
  - 代码经过混淆和优化
  - 文件较小
  - 未签名版本需要签名后才能上传到Google Play
  - 可以直接安装用于分发测试

## 查看构建日志

如果构建失败，可以查看详细日志：

1. 进入 **Actions** 页面
2. 点击失败的工作流运行（红色✗）
3. 点击 **"build"** 作业
4. 展开各个步骤查看详细日志
5. 错误信息会用红色高亮显示

## 常见问题

### Q: 构建需要多长时间？
A: 首次构建约5-10分钟（需要下载依赖），后续构建约3-5分钟（使用缓存）。

### Q: APK文件会保留多久？
A: 默认保留30天，之后会自动删除。建议及时下载重要版本。

### Q: Release APK为什么没有签名？
A: 为了安全，密钥不应该存储在代码仓库中。你可以手动签名或配置GitHub Secrets。

### Q: 可以修改应用图标吗？
A: 可以！工作流会自动生成占位符图标。如果你想使用自定义图标，在本地使用Android Studio的Image Asset Studio生成图标，然后提交到仓库即可。

### Q: 如何获得已签名的Release APK？
A: 需要配置签名密钥。步骤：
1. 在GitHub仓库的Settings > Secrets添加签名相关的secrets
2. 修改工作流文件添加签名步骤
3. 详细教程请参考 `.github/workflows/README.md`

## 工作流配置文件

主要配置文件位于：
- `.github/workflows/build-apk.yml` - 工作流定义
- `.github/workflows/README.md` - 详细技术文档

## 技术支持

如果遇到问题：
1. 查看 `.github/workflows/README.md` 中的故障排查部分
2. 检查Actions页面的构建日志
3. 确认Gradle配置和依赖版本正确

## 最佳实践

1. ✅ 每次重要功能完成后创建版本标签
2. ✅ 在合并PR前检查构建是否成功
3. ✅ 定期清理旧的构建产物
4. ✅ 使用语义化版本号（如v1.0.0, v1.1.0）
5. ✅ 在Release说明中记录主要更改

---

**注意**: 自动生成的应用图标仅为占位符（绿色背景带"AI"文字），建议在正式发布前替换为设计好的图标。
