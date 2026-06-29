# Timetable Android

这是 `../timetable` iOS 项目的 Android 对应实现，使用 Kotlin + Jetpack Compose + Glance App Widget。

## 已实现功能

- 主 App 预览倒计时卡片
- 距离周末的天数
- 距离最近 2026 年大陆法定节假日的天数
- 多条自定义倒计时
- 自定义事件新增、删除、标题编辑、日期选择、颜色选择
- 自定义事件置顶与手动上下排序
- 本地 SharedPreferences 持久化
- Android 桌面小组件
- 小组件点击通过 `timetable://` 深链回到 App 或对应事件
- 主界面底部显示当前应用版本号
- 统一日志模块（Logcat + 内部存储文件 + 崩溃捕获），长按版本号可一键导出/分享日志

## 环境要求

- JDK 17
- Android SDK（compileSdk 35）
- 最低支持 Android 8.0（minSdk 26）

## 打开方式

用 Android Studio 打开当前目录：

```bash
open timetable_android
```

或在 Android Studio 中选择 `File > Open...`，打开 `timetable_android`。

## 构建

在配置好 JDK 17 和 Android SDK 的环境中执行：

```bash
./gradlew assembleDebug
```

运行 JVM 单元测试：

```bash
./gradlew test
```

仓库已包含 Gradle wrapper；也可以直接让 Android Studio 同步项目，它会使用 `settings.gradle.kts` 和根目录 `build.gradle.kts` 下载所需插件。

### Release 签名

对外安装或发布请使用已签名 release APK。先生成并妥善保存自己的 release keystore：

```bash
keytool -genkeypair -v \
  -keystore release-keystore.jks \
  -alias timetable \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

本机构建时，在未提交的 `local.properties` 中加入：

```properties
RELEASE_STORE_FILE=/absolute/path/to/release-keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=timetable
RELEASE_KEY_PASSWORD=your-key-password
```

然后执行：

```bash
./gradlew assembleRelease
```

产物位于 `app/build/outputs/apk/release/app-release.apk`。如果没有配置上述 4 个签名字段，release 构建会失败，避免产出无法正常安装的 `unsigned` APK。

## CI

GitHub Actions workflow 位于 `.github/workflows/android.yml`，包含两个任务：

- **build**：在 push 到 `main` 或 pull request 时执行单元测试与 debug 构建。

  ```bash
  ./gradlew test
  ./gradlew assembleDebug
  ```

- **release**：在推送 `v*` 格式的 tag 时执行，构建已签名 release APK 并自动创建 GitHub Release，附带 APK 及自动生成的更新日志。

  GitHub 仓库需要配置以下 Actions Secrets：

  - `RELEASE_KEYSTORE_BASE64`：release keystore 的 Base64 内容。macOS 可用 `base64 -i release-keystore.jks | pbcopy` 复制；Linux 可用 `base64 -w 0 release-keystore.jks` 输出。
  - `RELEASE_STORE_PASSWORD`
  - `RELEASE_KEY_ALIAS`
  - `RELEASE_KEY_PASSWORD`

## 发布新版本

1. 在 `app/build.gradle.kts` 中递增 `versionCode` 与 `versionName`，提交并推送到 `main`：

   ```bash
   git commit -am "Release vX.Y.Z"
   git push origin main
   ```

2. 打上对应的 tag 并推送，触发 release 任务：

   ```bash
   git tag vX.Y.Z
   git push origin vX.Y.Z
   ```

tag 名需以 `v` 开头才会触发发布。发布完成后可在仓库 Releases 页面下载 APK。

## 项目结构

```text
timetable_android/
├── app/
│   ├── build.gradle.kts                  # 模块构建配置（版本号、依赖）
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml       # 组件声明、deep link、小组件注册
│       │   ├── java/com/hheelo/countdown/
│       │   │   ├── MainActivity.kt           # 入口 Activity，处理 timetable:// 深链
│       │   │   ├── CountdownApp.kt           # 主界面 Compose 布局
│       │   │   ├── CountdownViewModel.kt     # UI 状态与业务逻辑，后台读写持久化
│       │   │   ├── CountdownModels.kt        # CountdownEvent / CountdownCard 数据模型
│       │   │   ├── CountdownStore.kt         # SharedPreferences 持久化与序列化
│       │   │   ├── CountdownCalculator.kt    # 周末 / 节假日 / 自定义事件天数计算
│       │   │   ├── HolidayCalendar.kt        # 内置法定节假日数据
│       │   │   ├── CountdownPreviewCards.kt  # 顶部预览卡片与 Header
│       │   │   ├── CustomEventEditor.kt      # 自定义事件编辑器
│       │   │   ├── SavePanel.kt              # 保存按钮与 Footer（含版本号）
│       │   │   ├── CountdownColors.kt        # 颜色常量与解析缓存
│       │   │   ├── CountdownTheme.kt         # Compose 主题
│       │   │   ├── ListExtensions.kt         # 不可变列表操作辅助函数
│       │   │   ├── CountdownWidget.kt        # Glance 桌面小组件
│       │   │   ├── CountdownApplication.kt    # Application 入口，初始化日志与崩溃捕获
│       │   │   └── logging/                   # 日志模块（AppLog/FileLogWriter/CrashHandler/LogExporter）
│       │   └── res/                      # 图标、布局、小组件配置等资源
│       └── test/                         # JVM 单元测试
├── .github/workflows/android.yml         # CI（build + release）
├── build.gradle.kts                      # 根构建脚本
└── settings.gradle.kts                   # 模块与插件配置
```

## 节假日数据

节假日数据位于 `app/src/main/java/com/hheelo/countdown/HolidayCalendar.kt`，当前与 iOS 项目一致，内置 2026 年大陆法定节假日。
