package io.agora.board.forge.ui.component

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.databinding.FcrBoardColorPickComponentBinding

/**
 * author : fenglibin
 * date : 2024/7/2
 * description : 白板工具属性选择面板
 */
class FcrBoardColorPickLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = FcrBoardColorPickComponentBinding.inflate(LayoutInflater.from(context), this, true)
    private val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
    private val colors = listOf(binding.color1, binding.color2, binding.color3, binding.color4, binding.color5)

    private val dotData = listOf(
        2,
        6,
        10
    )

    private val colorData = listOf(
        ContextCompat.getColor(context, R.color.fcr_whiteboard_color_red),
        ContextCompat.getColor(context, R.color.fcr_whiteboard_color_yellow),
        ContextCompat.getColor(context, R.color.fcr_whiteboard_color_green),
        ContextCompat.getColor(context, R.color.fcr_whiteboard_color_blue),
        ContextCompat.getColor(context, R.color.fcr_whiteboard_color_purple),
    )

    init {
        val orientation = context.obtainStyledAttributes(attrs, R.styleable.FcrBoardToolBox).run {
            try {
                getInt(R.styleable.FcrBoardToolBox_fcr_layoutOrientation, FcrBoardToolBoxLayout.HORIZONTAL)
            } finally {
                recycle()
            }
        }

        setupColorPickLayout(orientation)
        setupDivider(orientation)
        setupDots()
        setupColors()
    }

    private fun setupColorPickLayout(orientationMode: Int) {
        val colorPickLayout = binding.colorPickLayout
        val paddingMain = resources.getDimensionPixelSize(R.dimen.fcr_board_color_pick_padding_main)
        val panelSize = resources.getDimensionPixelSize(R.dimen.fcr_board_settings_panel_size)

        colorPickLayout.updateLayoutParams<LayoutParams> {
            if (orientationMode == FcrBoardToolBoxLayout.HORIZONTAL) {
                width = LayoutParams.WRAP_CONTENT
                height = panelSize
                colorPickLayout.apply {
                    dividerDrawable = ContextCompat.getDrawable(context, R.drawable.fcr_board_divider_width_10)
                    gravity = Gravity.CENTER_VERTICAL
                    orientation = LinearLayout.HORIZONTAL
                    showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                    setPadding(paddingMain, 0, paddingMain, 0)
                }
            } else {
                width = panelSize
                height = LayoutParams.WRAP_CONTENT
                colorPickLayout.apply {
                    dividerDrawable = ContextCompat.getDrawable(context, R.drawable.fcr_board_divider_height_10)
                    gravity = Gravity.CENTER_HORIZONTAL
                    orientation = LinearLayout.VERTICAL
                    showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                    setPadding(0, paddingMain, 0, paddingMain)
                }
            }
        }
    }

    private fun setupDivider(orientation: Int) {
        binding.divider.updateLayoutParams<LinearLayout.LayoutParams> {
            if (orientation == FcrBoardToolBoxLayout.HORIZONTAL) {
                width = resources.getDimensionPixelSize(R.dimen.fcr_v_divider_normal)
                height = resources.getDimensionPixelSize(R.dimen.fcr_board_color_pick_divider_size)
            } else {
                width = resources.getDimensionPixelSize(R.dimen.fcr_board_color_pick_divider_size)
                height = resources.getDimensionPixelSize(R.dimen.fcr_v_divider_normal)
            }
        }
    }

    private fun setupDots() {
        dots.forEachIndexed { index, dotView ->
            // setDotSize 仍然用 px
            dotView.setDotSize(dotData[index].toFloat() * context.resources.displayMetrics.density)
            // onStrokeWidthClick 传 dp
            dotView.setOnClickListener { onStrokeSettingListener?.onStrokeWidthClick(dotData[index]) }
        }
    }

    private fun setupColors() {
        colors.forEachIndexed { index, colorView ->
            colorView.imageTintList = ColorStateList.valueOf(colorData[index])
            colorView.setOnClickListener { onStrokeSettingListener?.onStrokeColorClick(colorData[index]) }
        }
    }

    interface OnStrokeSettingListener {
        fun onStrokeWidthClick(width: Int)

        fun onStrokeColorClick(@ColorInt color: Int)
    }

    private var onStrokeSettingListener: OnStrokeSettingListener? = null

    fun setOnStrokeSettingsListener(listener: OnStrokeSettingListener) {
        this.onStrokeSettingListener = listener
    }

    fun setDrawConfig(drawConfig: FcrBoardUiDrawConfig) {
        dots.forEachIndexed { index, fcrBoardDotView ->
            fcrBoardDotView.isSelected = dotData[index] == drawConfig.strokeWidth
        }

        colors.forEachIndexed { index, imageView ->
            imageView.isSelected = colorData[index] == drawConfig.strokeColor
        }
    }
}
