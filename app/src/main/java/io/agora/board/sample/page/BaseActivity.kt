package io.agora.board.sample.page

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.agora.board.forge.sample.R

open class BaseActivity : AppCompatActivity() {
    private var loadingDialog: Dialog? = null

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupFullScreen()
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * It's a test include all operation, reorganize in production environment
     */
    private fun setupFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    protected fun lockLandscape() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun showLoadingDialog(message: String, cancelable: Boolean = true) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.fgs_dialog_loading, null)
        val messageTextView = dialogView.findViewById<TextView>(R.id.tv_loading_message)
        messageTextView.text = message

        loadingDialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(cancelable).create().apply {
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                show()
            }
    }

    protected fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}
