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

## 打开方式

用 Android Studio 打开当前目录：

```bash
open timetable_android
```

或在 Android Studio 中选择 `File > Open...`，打开 `timetable_android`。

## 构建

在配置好 JDK 和 Android SDK 的环境中执行：

```bash
./gradlew assembleDebug
```

如果没有 Gradle wrapper，也可以直接让 Android Studio 同步项目；它会使用 `settings.gradle.kts` 和根目录 `build.gradle.kts` 下载所需插件。

## 节假日数据

节假日数据位于 `app/src/main/java/com/hheelo/countdown/HolidayCalendar.kt`，当前与 iOS 项目一致，内置 2026 年大陆法定节假日。
