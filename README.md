# Forge UI Android

Forge UI Android 是基于 Agora 白板 SDK 的二次封装，提供了一套开箱即用的白板 UI 组件，方便开发者快速集成白板功能。

## 环境配置

### 最低支持版本

- Android 5.0 (API level 21) 及以上
- Android Tools Build >= 4.1.0

### Maven 配置

在项目的 `build.gradle` 文件中添加以下依赖：

```groovy
// settings.gradle
repositories {
    mavenCentral()
}

// app/build.gradle
dependencies {
    implementation 'io.github.duty-os.forge:forge-ui:0.1.0'
}
``` 

## 快速接入

具体代码示例请参考 [WhiteboardUIActivity.kt](app/src/main/java/io/agora/board/ui/sample/page/WhiteboardUIActivity.kt)
，以下是核心步骤：

### 1. 创建 Controller

```kotlin
val controller = WhiteboardController(
    context = this,
    config = WhiteboardControllerConfig(
        appId = "MainWhiteboard"
    )
)
```

### 2. 添加视图到布局

```kotlin
controller.attach(binding.whiteboardContainer)
```

### 3. 启动白板

```kotlin
val room = Room(roomOptions)
controller.start(room)
```

### 4. 释放资源

```kotlin
override fun onDestroy() {
    super.onDestroy()
    controller.stop()
}
```
