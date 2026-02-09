package io.agora.board.sample

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import io.agora.board.forge.Room
import io.agora.board.forge.sample.BuildConfig
import io.agora.board.forge.sample.databinding.ActivityMainBinding
import io.agora.board.sample.page.BaseActivity
import io.agora.board.sample.page.apaas.ApaasSampleActivity
import io.agora.board.sample.page.app.SampleWhiteboardActivity
import io.agora.board.sample.page.app.WhiteboardUIActivity
import io.agora.board.sample.util.KvStore
import io.agora.board.sample.util.RtmTokenUpdater
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var userJob: Job? = null

    // 测试页面映射表，支持后期动态添加
    private val testPages = listOf(
        TestPage("Apaas测试", ApaasSampleActivity::class.java),
        TestPage("单白板测试", SampleWhiteboardActivity::class.java),
    )

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

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
                AlertDialog.Builder(this)
                    .setTitle("确认更新")
                    .setMessage("Room ID \n$roomId")
                    .setPositiveButton("确认") { dialog, _ ->
                        // 用户点击确认按钮
                        Constants.roomId = roomId
                        Constants.roomToken = token
                        updateView()
                        dialog.dismiss()
                    }
                    .setNegativeButton("取消") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        binding.whiteboardUi.setOnClickListener {
            gotoActivity(WhiteboardUIActivity::class.java)
        }

        // 更多按钮 - 显示测试页面选择对话框
        binding.btnMore.setOnClickListener {
            showTestPagesDialog()
        }

        binding.swWritable.setOnCheckedChangeListener { _, isChecked ->
            Constants.writable = isChecked
        }
        binding.swWritable.isChecked = Constants.writable

        updateView()
        loadUser()
    }

    /**
     * 显示测试页面选择对话框
     */
    private fun showTestPagesDialog() {
        val titles = testPages.map { it.title }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("选择测试页面")
            .setItems(titles) { dialog, which ->
                val selectedPage = testPages[which]
                gotoTestPage(selectedPage)
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 跳转到指定的测试页面
     */
    private fun gotoTestPage(testPage: TestPage) {
        if (binding.inputUserId.text.isNotEmpty()) {
            KvStore.setUserId(binding.inputUserId.text.toString())
            loadUser()
        }

        lifecycleScope.launch {
            userJob?.join()
            Log.e("MainActivity", "user ${Constants.currentUser}")

            if (testPage.activityClass != null) {
                startActivity(Intent(this@MainActivity, testPage.activityClass))
            } else if (testPage.className != null) {
                try {
                    startActivity(Intent().setClassName(packageName, testPage.className))
                } catch (e: Exception) {
                    showToast("${testPage.title} 未启用或未包含")
                }
            }
        }
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

/**
 * 测试页面数据类
 * @param title 页面标题
 * @param activityClass Activity类（优先使用）
 * @param className Activity类名（用于反射启动）
 */
data class TestPage(
    val title: String,
    val activityClass: Class<*>? = null,
    val className: String? = null
)

