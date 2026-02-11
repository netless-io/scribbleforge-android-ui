package io.agora.board.sample

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import io.agora.board.forge.Room
import io.agora.board.forge.sample.BuildConfig
import io.agora.board.forge.sample.databinding.ActivityMainBinding
import io.agora.board.sample.page.BaseActivity
import io.agora.board.sample.page.WhiteboardUIActivity
import io.agora.board.sample.util.KvStore
import io.agora.board.sample.util.RtmTokenUpdater
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var userJob: Job? = null

    override fun inflateBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(layoutInflater)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.buildVersion.text = "v${BuildConfig.BUILD_VERSION}"
        binding.roomVersion.text = "forge-sdk:${Room.VERSION}"
        binding.inputRoomId.addTextChangedListener {
            updateView()
        }
        binding.inputRoomToken.addTextChangedListener {
            updateView()
        }
        binding.updateRoomId.setOnClickListener {
            val roomId = binding.inputRoomId.text.toString()
            val token = binding.inputRoomToken.text.toString()
            if (roomId.isNotEmpty() && token.isNotEmpty()) {
                AlertDialog.Builder(this).setTitle("确认更新").setMessage("Room ID \n$roomId")
                    .setPositiveButton("确认") { dialog, _ ->
                        // 用户点击确认按钮
                        Constants.roomId = roomId
                        Constants.roomToken = token
                        updateView()
                        dialog.dismiss()
                    }.setNegativeButton("取消") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }
        }

        binding.whiteboardUi.setOnClickListener {
            gotoActivity(WhiteboardUIActivity::class.java)
        }

        binding.swWritable.setOnCheckedChangeListener { _, isChecked ->
            Constants.writable = isChecked
        }
        binding.swWritable.isChecked = Constants.writable

        updateView()
        loadUser()
    }

    private fun gotoActivity(clazz: Class<*>) {
        if (binding.inputUserId.text.isNotEmpty()) {
            KvStore.setUserId(binding.inputUserId.text.toString())
            loadUser()
        }

        lifecycleScope.launch {
            userJob?.join()
            Log.e("MainActivity", "user ${Constants.currentUser}")
            startActivity(Intent(this@MainActivity, clazz))
        }
    }

    private fun loadUser() {
        userJob?.cancel()
        userJob = lifecycleScope.launch {
            RtmTokenUpdater(this@MainActivity).getRtmToken()
        }
        userJob?.start()
    }

    @SuppressLint("SetTextI18n")
    private fun updateView() {
        binding.roomId.text = "roomId: ${Constants.roomId}"
        binding.updateRoomId.isEnabled =
            binding.inputRoomId.text.isNotEmpty() && binding.inputRoomToken.text.isNotEmpty()
        binding.inputUserId.hint = KvStore.getUserId()
    }
}
