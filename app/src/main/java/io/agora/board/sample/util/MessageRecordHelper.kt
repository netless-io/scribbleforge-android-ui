package io.agora.board.sample.util

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError

class MessageRecordHelper(private val context: Context, private val room: Room?) {

    private val devManager get() = room?.devManager

    fun showRecordingMenu() {
        if (devManager == null) {
            showToast("录制功能未初始化（需要开启 debugMode）")
            return
        }

        val isRecording = devManager?.isRecording() ?: false

        val options = if (isRecording) {
            arrayOf(
                "停止录制并保存",
                "取消录制",
                "录制状态 (${devManager?.getRecordedMessagesCount()} 条消息)"
            )
        } else {
            arrayOf(
                "开始录制",
                "查看录制列表"
            )
        }

        AlertDialog.Builder(context)
            .setTitle("消息录制")
            .setItems(options) { _, which ->
                when {
                    isRecording -> {
                        when (which) {
                            0 -> stopRecording()
                            1 -> cancelRecording()
                            2 -> {} // 只是显示状态
                        }
                    }

                    else -> {
                        when (which) {
                            0 -> startRecording()
                            1 -> showRecordingList()
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 开始录制
     */
    private fun startRecording() {
        val editText = android.widget.EditText(context)
        editText.hint = "录制名称（可选）"

        AlertDialog.Builder(context)
            .setTitle("开始录制")
            .setMessage("请输入录制名称（留空则使用时间戳）")
            .setView(editText)
            .setPositiveButton("开始") { _, _ ->
                val recordName = editText.text.toString().trim()
                devManager?.startRecording(recordName, object : RoomCallback<String> {
                    override fun onSuccess(result: String) {
                        showToast("开始录制: $result")
                    }

                    override fun onFailure(error: RoomError) {
                        showToast("录制失败: ${error.message}")
                    }
                })
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 停止录制
     */
    private fun stopRecording() {
        devManager?.stopRecording(object : RoomCallback<String> {
            override fun onSuccess(result: String) {
                showToast("录制已保存: $result")
            }

            override fun onFailure(error: RoomError) {
                showToast("保存失败: ${error.message}")
            }
        })
    }

    /**
     * 取消录制
     */
    private fun cancelRecording() {
        AlertDialog.Builder(context)
            .setTitle("取消录制")
            .setMessage("确定要取消录制吗？所有数据将丢失。")
            .setPositiveButton("确定") { _, _ ->
                devManager?.cancelRecording()
                showToast("录制已取消")
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 显示录制列表
     */
    private fun showRecordingList() {
        val manager = devManager
        if (manager == null) {
            showToast("录制功能未初始化")
            return
        }

        val recordings = manager.listRecordings()
        if (recordings.isEmpty()) {
            showToast("暂无录制文件")
            return
        }

        AlertDialog.Builder(context)
            .setTitle("录制列表")
            .setItems(recordings.toTypedArray()) { _, which ->
                showRecordingActions(recordings[which])
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    /**
     * 显示录制文件操作
     */
    private fun showRecordingActions(recordName: String) {
        AlertDialog.Builder(context)
            .setTitle(recordName)
            .setItems(arrayOf("查看信息", "删除")) { _, which ->
                when (which) {
                    0 -> showRecordingInfo(recordName)
                    1 -> deleteRecording(recordName)
                }
            }
            .setNegativeButton("返回", null)
            .show()
    }

    /**
     * 显示录制信息
     */
    private fun showRecordingInfo(recordName: String) {
        val manager = devManager ?: return
        val data = manager.loadRecording(recordName)

        if (data == null) {
            showToast("无法加载录制文件")
            return
        }

        val info = """
            录制名称: ${data.recordName}
            快照大小: ${data.snapshot.size} 字节
            消息数量: ${data.messages.size} 条
            
            消息列表:
            ${
            data.messages.take(10).joinToString("\n") {
                "${it.timestamp}ms - ${it.messageType} (${it.publisherId})"
            }
        }
            ${if (data.messages.size > 10) "\n... 还有 ${data.messages.size - 10} 条消息" else ""}
        """.trimIndent()

        AlertDialog.Builder(context)
            .setTitle("录制信息")
            .setMessage(info)
            .setPositiveButton("确定", null)
            .show()
    }

    /**
     * 删除录制
     */
    private fun deleteRecording(recordName: String) {
        AlertDialog.Builder(context)
            .setTitle("删除录制")
            .setMessage("确定要删除录制 \"$recordName\" 吗？")
            .setPositiveButton("删除") { _, _ ->
                val manager = devManager
                if (manager?.deleteRecording(recordName) == true) {
                    showToast("已删除")
                } else {
                    showToast("删除失败")
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
