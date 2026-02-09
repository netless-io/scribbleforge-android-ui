package io.agora.board.forge.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.databinding.FcrBoardBgPickLayoutBinding

/**
 * author : fenglibin
 * date : 2024/8/9
 * description : 白板背景选择布局
 */
class FcrBoardBgPickLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private val binding = FcrBoardBgPickLayoutBinding.inflate(LayoutInflater.from(context), this)

    companion object {
        const val TAG = "FcrBoardSceneDownloadingLayout"
    }

    private var listener: BoardBgPickListener? = null

    val flBoards = listOf(binding.flWhiteBoard, binding.flBlackBoard, binding.flGreenBoard)
    val flSelects = listOf(binding.flWhiteSelect, binding.flBlackSelect, binding.flGreenSelect)
    val colors = listOf(
        ContextCompat.getColor(context, R.color.fcr_whiteboard_bg_white),
        ContextCompat.getColor(context, R.color.fcr_whiteboard_bg_black),
        ContextCompat.getColor(context, R.color.fcr_whiteboard_bg_green)
    )
    val toasts = listOf(
        R.string.fcr_board_toast_change_bg_white,
        R.string.fcr_board_toast_change_bg_black,
        R.string.fcr_board_toast_change_bg_green
    )

    init {
        flBoards.forEachIndexed { index, v ->
            v.setOnClickListener {
                listener?.onBoardBgPicked(colors[index], toasts[index])
            }
        }
    }

    fun setBoardBackgroundColor(color: Int) {
        flSelects.forEachIndexed { index, v ->
            v.isVisible = colors[index] == color
        }
    }

    fun setBoardBgPickListener(listener: BoardBgPickListener) {
        this.listener = listener
    }

    interface BoardBgPickListener {
        fun onBoardBgPicked(@ColorInt color: Int, @StringRes toast: Int)
    }
}
