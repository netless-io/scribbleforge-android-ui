package io.agora.board.sample.page.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.WritableListener
import io.agora.board.forge.rtm.RtmSocketProvider
import io.agora.board.forge.sample.databinding.ActivitySampleWritableBinding
import io.agora.board.forge.windowmanager.WindowManagerOption
import io.agora.board.sample.Constants
import io.agora.board.sample.util.RtmHelper
import kotlinx.coroutines.launch

class SampleWritableActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SampleWritableActivity"
        private const val TEST_USER_ID = "testUser123"
    }

    private val binding: ActivitySampleWritableBinding by lazy { 
        ActivitySampleWritableBinding.inflate(layoutInflater) 
    }
    private var rtmHelper: RtmHelper = RtmHelper()
    private var room: Room? = null
    private var writableListener: WritableListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupUI()
        setupWritableListener()

        lifecycleScope.launch {
            rtmHelper.login()
            joinRoom()
        }
    }

    private fun setupUI() {
        // 加入房间按钮
        binding.joinRoom.setOnClickListener {
            if (rtmHelper.isLogin()) {
                joinRoom()
            } else {
                showToast("RTM not logged in")
            }
        }

        // 检查自己的可写权限
        binding.checkSelfWritable.setOnClickListener {
            room?.let { room ->
                val isWritable = room.isWritable()
                updateWritableStatus("Self", isWritable)
                showToast("Self writable: $isWritable")
            } ?: showToast("Room not joined")
        }

        // 检查指定用户的可写权限
        binding.checkUserWritable.setOnClickListener {
            room?.let { room ->
                val userId = binding.userIdInput.text.toString().trim()
                if (userId.isNotEmpty()) {
                    val isWritable = room.isWritable(userId)
                    updateWritableStatus(userId, isWritable)
                    showToast("User $userId writable: $isWritable")
                } else {
                    showToast("Please enter user ID")
                }
            } ?: showToast("Room not joined")
        }

        // 设置自己为可写
        binding.setSelfWritable.setOnClickListener {
            room?.setWritable(writable = true, callback = object : RoomCallback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    showToast("Set self writable: success")
                    Log.d(TAG, "Set self writable success: $result")
                }

                override fun onFailure(error: RoomError) {
                    showToast("Set self writable failed: ${error.message}")
                    Log.e(TAG, "Set self writable failed", error)
                }
            }) ?: showToast("Room not joined")
        }

        // 设置自己为只读
        binding.setSelfReadonly.setOnClickListener {
            room?.setWritable(writable = false, callback = object : RoomCallback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    showToast("Set self readonly: success")
                    Log.d(TAG, "Set self readonly success: $result")
                }

                override fun onFailure(error: RoomError) {
                    showToast("Set self readonly failed: ${error.message}")
                    Log.e(TAG, "Set self readonly failed", error)
                }
            }) ?: showToast("Room not joined")
        }

        // 设置指定用户为可写
        binding.setUserWritable.setOnClickListener {
            room?.let { room ->
                val userId = binding.userIdInput.text.toString().trim()
                if (userId.isNotEmpty()) {
                    room.setWritable(userId = userId, writable = true, callback = object : RoomCallback<Boolean> {
                        override fun onSuccess(result: Boolean) {
                            showToast("Set user $userId writable: success")
                            Log.d(TAG, "Set user $userId writable success: $result")
                        }

                        override fun onFailure(error: RoomError) {
                            showToast("Set user $userId writable failed: ${error.message}")
                            Log.e(TAG, "Set user $userId writable failed", error)
                        }
                    })
                } else {
                    showToast("Please enter user ID")
                }
            } ?: showToast("Room not joined")
        }

        // 设置指定用户为只读
        binding.setUserReadonly.setOnClickListener {
            room?.let { room ->
                val userId = binding.userIdInput.text.toString().trim()
                if (userId.isNotEmpty()) {
                    room.setWritable(userId = userId, writable = false, callback = object : RoomCallback<Boolean> {
                        override fun onSuccess(result: Boolean) {
                            showToast("Set user $userId readonly: success")
                            updateWritableStatus(userId, result)
                            Log.d(TAG, "Set user $userId readonly success: $result")
                        }

                        override fun onFailure(error: RoomError) {
                            showToast("Set user $userId readonly failed: ${error.message}")
                            Log.e(TAG, "Set user $userId readonly failed", error)
                        }
                    })
                } else {
                    showToast("Please enter user ID")
                }
            } ?: showToast("Room not joined")
        }

        // 清空日志
        binding.clearLog.setOnClickListener {
            binding.logOutput.text = ""
        }

        // 设置默认测试用户ID
        binding.userIdInput.setText(TEST_USER_ID)
    }

    private fun setupWritableListener() {
        writableListener = object : WritableListener {
            override fun onWritableChanged(userId: String, writable: Boolean) {
                runOnUiThread {
                    val message = "User $userId writable changed to: $writable"
                    appendLog(message)
                    showToast(message)
                    updateWritableStatus(userId, writable)
                    Log.d(TAG, message)
                }
            }
        }
    }

    private fun joinRoom() {
        val roomOptions = RoomOptions(
            context = applicationContext,
            roomId = Constants.roomId,
            roomToken = Constants.roomToken,
            userId = Constants.userId
        ).apply {
            socketProvider(RtmSocketProvider(rtmHelper.rtmClient()))
            region(Constants.BOARD_REGION)
            appIdentifier("WritableTestApp")
            writable(true) // 初始设置为可写
            windowManagerOptions(WindowManagerOption(ratio = 16f / 9))
        }

        room = Room(roomOptions)

        // 添加可写权限监听器
        writableListener?.let { listener ->
            room?.addWritableListener(listener)
        }

        room?.joinRoom(object : RoomCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                showToast("Join room success")
                appendLog("Room joined successfully")
                updateConnectionStatus("Connected")
                
                // 显示初始状态
                room?.let { room ->
                    val isWritable = room.isWritable()
                    updateWritableStatus("Self", isWritable)
                    appendLog("Initial self writable status: $isWritable")
                }
            }

            override fun onFailure(error: RoomError) {
                showToast("Join room failure: ${error.message}")
                appendLog("Room join failed: ${error.message}")
                updateConnectionStatus("Failed")
                Log.e(TAG, "Join room failed", error)
            }
        })
    }

    private fun updateConnectionStatus(status: String) {
        binding.connectionStatus.text = "Connection: $status"
    }

    private fun updateWritableStatus(userId: String, isWritable: Boolean) {
        val status = if (isWritable) "Writable" else "ReadOnly"
        val color = if (isWritable) android.graphics.Color.GREEN else android.graphics.Color.RED
        
        if (userId == "Self" || userId == Constants.userId) {
            binding.selfWritableStatus.text = "Self: $status"
            binding.selfWritableStatus.setTextColor(color)
        } else {
            binding.userWritableStatus.text = "$userId: $status"
            binding.userWritableStatus.setTextColor(color)
        }
    }

    private fun appendLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message\n"
        binding.logOutput.append(logMessage)
        
        // 自动滚动到底部
        binding.scrollView.post {
            binding.scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // 移除监听器
        writableListener?.let { listener ->
            room?.removeWritableListener(listener)
        }
        
        // 离开房间
        room?.leaveRoom()
        
        Log.d(TAG, "Activity destroyed")
    }
}
