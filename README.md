# Forge UI Android

Forge UI Android 是基于 Agora 白板 SDK 的二次封装，提供了一套开箱即用的白板 UI 组件，方便开发者快速集成白板功能。

## 使用方式

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
