package io.agora.board.forge.ui.component

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.databinding.FcrBoardSceneDownloadingLayoutBinding

/**
 * author : fenglibin
 * date : 2024/7/11
 * description : 白板场景画布下载中布局
 */
class FcrBoardDownloadingLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private val binding = FcrBoardSceneDownloadingLayoutBinding.inflate(LayoutInflater.from(context), this)

    init {
        binding.root.setSwipeDownListener { hide() }
    }

    fun setProgress(progress: Int) {
        binding.tvDownloadingProgress.text = context.getString(
            R.string.fcr_board_scene_downloading_progress, progress.toString()
        )
        binding.pbProgress.setProgress(progress, true)
    }

    fun setDownloadState(state: FcrBoardUiDownloadingState) {
        when (state) {
            FcrBoardUiDownloadingState.DOWNLOADING -> {
                binding.tvDownloading.text = context.getString(R.string.fcr_board_scene_downloading)
                binding.pbProgress.setProgress(0, false)
                binding.pbProgress.setTint(R.color.fcr_ui_scene_ramp_brand6, R.color.fcr_ui_scene_ramp_brand1)
                binding.ivIcon.setTint(R.color.fcr_ui_scene_icontext1)
            }

            FcrBoardUiDownloadingState.FAILURE -> {
                binding.tvDownloading.text = context.getString(R.string.fcr_board_scene_downloading_failure)
                binding.pbProgress.setTint(R.color.fcr_ui_scene_ramp_red6, R.color.fcr_ui_scene_ramp_red1)
                binding.ivIcon.setTint(R.color.fcr_ui_scene_ramp_red6)
            }

            FcrBoardUiDownloadingState.SUCCESS -> {
                binding.tvDownloading.text = context.getString(R.string.fcr_board_scene_downloading_success)
            }
        }
    }

    fun show() {
        binding.root.animateShow(R.anim.fcr_board_slide_in_bottom_fade_in)
    }

    fun hide() {
        binding.root.animateHide(R.anim.fcr_board_slide_out_bottom_fade_out)
    }

    private fun View.setSwipeDownListener(onSwipeDown: () -> Unit) {
        val gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean {
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val deltaY = e2.y - (e1?.y ?: 0f)
                if (deltaY > 0) {
                    onSwipeDown()
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

        this.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun ProgressBar.setTint(progressTintResId: Int, backgroundTintResId: Int) {
        this.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context, progressTintResId))
        this.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, backgroundTintResId))
    }

    private fun ImageView.setTint(tintResId: Int) {
        this.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, tintResId))
    }
}

enum class FcrBoardUiDownloadingState {
    DOWNLOADING, FAILURE, SUCCESS
}
