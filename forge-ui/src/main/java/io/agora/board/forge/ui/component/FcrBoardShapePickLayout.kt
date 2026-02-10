package io.agora.board.forge.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.model.model.ToolType
import io.agora.board.forge.ui.databinding.FcrBoardShapePickComponentBinding
import io.agora.board.forge.ui.databinding.FcrBoardSubToolItemBinding


/**
 * author : fenglibin
 * date : 2024/5/27
 * description : 白板形状选择
 */
class FcrBoardShapePickLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = FcrBoardShapePickComponentBinding.inflate(LayoutInflater.from(context), this, true)
    private var toolList = listOf<ToolType>()
    private var selectedTool = ToolType.CURVE
    private var shapePickListener: ShapePickListener? = null
    private var layoutOrientation: Int

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FcrBoardShapePick).apply {
            try {
                layoutOrientation = getInt(
                    R.styleable.FcrBoardShapePick_fcr_layoutOrientation, FcrBoardToolBoxLayout.HORIZONTAL
                )
            } finally {
                recycle()
            }
        }

        setupShapeLayout(context)
    }

    private fun setupShapeLayout(context: Context) {
        val isHorizontal = layoutOrientation == FcrBoardToolBoxLayout.HORIZONTAL
        val padding = resources.getDimensionPixelSize(R.dimen.fcr_board_shape_pick_padding_main)
        val panelSize = resources.getDimensionPixelSize(R.dimen.fcr_board_settings_panel_size)

        binding.llShapeLayout.orientation = layoutOrientation
        binding.llShapeLayout.apply {
            layoutParams = if (isHorizontal) {
                LayoutParams(LayoutParams.WRAP_CONTENT, panelSize)
            } else {
                LayoutParams(panelSize, LayoutParams.WRAP_CONTENT)
            }
            gravity = if (isHorizontal) Gravity.CENTER_VERTICAL else Gravity.CENTER_HORIZONTAL
            setPadding(
                if (isHorizontal) padding else 0,
                if (isHorizontal) 0 else padding,
                if (isHorizontal) padding else 0,
                if (isHorizontal) 0 else padding
            )
        }
        binding.llShapeLayout.dividerDrawable = ContextCompat.getDrawable(
            context, if (isHorizontal) R.drawable.fcr_board_divider_width_10 else R.drawable.fcr_board_divider_height_10
        )
        binding.llShapeLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
    }

    fun setTools(toolTypes: List<ToolType>) {
        if (toolList != toolTypes) {
            toolList = toolTypes
            rebuildToolLayout()
        }
        updateSelectedTool()
    }

    private fun rebuildToolLayout() {
        binding.llShapeLayout.removeAllViews()

        toolList.forEach { toolType ->
            binding.llShapeLayout.addView(createToolItemView(toolType))
        }
    }

    private fun createToolItemView(toolType: ToolType): View {
        val toolItemBinding = FcrBoardSubToolItemBinding.inflate(
            LayoutInflater.from(context), this, false
        )
        toolItemBinding.ivTool.setImageResource(toolType.imgResId())
        toolItemBinding.root.tag = toolType
        toolItemBinding.root.setOnClickListener { shapePickListener?.onToolClick(toolType) }
        return toolItemBinding.root
    }

    fun selectTool(toolType: ToolType) {
        selectedTool = toolType
        updateSelectedTool()
    }

    private fun updateSelectedTool() {
        binding.llShapeLayout.children.forEach {
            it.isSelected = it.tag == selectedTool
        }
    }

    interface ShapePickListener {
        fun onToolClick(toolType: ToolType)
    }

    fun setShapePickListener(listener: ShapePickListener) {
        this.shapePickListener = listener
    }
}
