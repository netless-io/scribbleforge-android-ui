# ForgeUI Architecture

## 1. Overview

ForgeUI 是基于 ForgeSDK 的白板交互层框架（Presentation Layer）。

### Responsibilities

* 提供单白板 UI 能力
* 提供多白板调度能力
* 管理 UI 状态与交互逻辑
* 提供主题系统
* 封装 Room 与 Application 生命周期交互

## 2. Module Structure

```
io.agora.board.forge.ui
│
├── ForgeUI.kt
├── ForgeUIConfig.kt
│
├── whiteboard
│   ├── WhiteboardController.kt
│   ├── WhiteboardControllerConfig.kt
│   ├── WhiteboardLayout.kt
│   ├── WhiteboardContainer.kt
│   ├── WhiteboardControlLayout.kt
│   ├── component/
│   └── state/
│
├── multiwindow
│   ├── MultipleWhiteboardController.kt
│   ├── WhiteboardStackManager.kt
│   └── SwitcherLayout.kt
│
├── common
│   ├── component/
│   └── ext/
│
├── theme
└── internal
```

## 3. Package Responsibilities

### 3.1 whiteboard

单白板场景能力。

包含：

* WhiteboardController（生命周期与 SDK 交互）
* WhiteboardLayout（对外暴露的根 View）
* 内部 UI 组件
* UI 状态管理

### 约束

* Controller 不持有外部 View 容器
* Controller 不操作外部 View 层级
* Layout 负责 UI 组合
* State 不持有 View 引用
* Component 不直接调用 Room

### 3.2 multiwindow

多白板调度能力。

职责：

* 管理多个 WhiteboardController
* 控制窗口栈结构
* 负责窗口切换逻辑

### 约束

* 不侵入 whiteboard 内部实现
* 不直接操作 WhiteboardLayout 内部结构
* 仅通过 Controller 进行交互

### 3.3 common

跨模块可复用 UI 组件。

### 约束

* 不包含 Controller
* 不包含 State
* 不调用 ForgeSDK
* 仅用于 UI 复用

### 3.4 theme

主题与样式系统。

职责：

* 提供 UI 风格抽象

### 3.5 internal

内部工具类。

### 约束

* 不允许外部依赖
* 不包含业务逻辑
* 仅作为辅助工具

## 4. Controller Design Principles

WhiteboardController：

* 负责 Room 交互
* 监听 Application 生命周期
* 管理白板启动与销毁
* 提供 `view` 或 `getView()` 供外部添加
* 不负责 View 层级管理
