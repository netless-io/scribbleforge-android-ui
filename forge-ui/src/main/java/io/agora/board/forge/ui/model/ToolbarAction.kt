package io.agora.board.forge.ui.model

/**
 * 白板工具箱操作类型（非绘图工具）
 */
enum class ToolbarAction {
    /** 清除 */
    Clear,

    Undo,

    Redo,

    /** 笔画属性选择 */
    Stroke,

    /** 下载 */
    Download,

    /** 背景选择 */
    Background,
}
