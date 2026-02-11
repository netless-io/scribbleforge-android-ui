package io.agora.board.forge.ui.component

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.model.ToolType
import io.agora.board.forge.ui.databinding.FcrBoardToolBoxComponentBinding
import io.agora.board.forge.ui.component.state.DrawState
import io.agora.board.forge.ui.model.imgResId

/**
 * author : fenglibin
 * date : 2024/7/2
 * description : 白板工具选择界面
 */
class FcrBoardToolBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), FcrBoardToolBoxAdapter.OnItemClickListener {

    companion object {
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL

        const val SCROLL_DURATION = 300
    }

    private var orientation: Int
    private val binding = FcrBoardToolBoxComponentBinding.inflate(LayoutInflater.from(context), this)

    private var toolBoxItems = mutableListOf(
        ToolBoxItem(
            FcrBoardToolBoxType.Clear,
            R.drawable.fcr_ic_clear,
        ),
        ToolBoxItem(
            FcrBoardToolBoxType.Undo,
            R.drawable.fcr_ic_undo,
        ),
        ToolBoxItem(
            FcrBoardToolBoxType.Redo,
            R.drawable.fcr_ic_redo,
        ),
        ToolBoxItem(
            FcrBoardToolBoxType.Tool,
            R.mipmap.fcr_whiteboard_pen1,
            listOf(ToolType.CURVE)
        ),
        ToolBoxItem(
            FcrBoardToolBoxType.Tool, R.mipmap.fcr_whiteboard_shap_square, listOf(
                ToolType.RECTANGLE,
                ToolType.TRIANGLE,
                ToolType.ELLIPSE,
                ToolType.STRAIGHT,
                ToolType.ARROW,
                ToolType.LASER_POINTER,
            )
        ),
        ToolBoxItem(
            FcrBoardToolBoxType.Tool,
            R.drawable.fcr_ic_tool_clicker,
            listOf(ToolType.CLICKER, ToolType.HAND)
        ),
        ToolBoxItem(
            FcrBoardToolBoxType.Tool,
            R.mipmap.fcr_whiteboard_whitechoose,
            listOf(ToolType.SELECTOR)
        ),
        ToolBoxItem(
            FcrBoardToolBoxType.Tool,
            R.mipmap.fcr_mobile_whiteboard_eraser,
            listOf(ToolType.ERASER)
        ),
        ToolBoxItem(FcrBoardToolBoxType.Stroke, 0),
        ToolBoxItem(
            FcrBoardToolBoxType.Tool,
            R.mipmap.fcr_mobile_whiteboard_text,
            listOf(ToolType.TEXT)
        ),
        ToolBoxItem(FcrBoardToolBoxType.Download, R.mipmap.fcr_download),
        ToolBoxItem(FcrBoardToolBoxType.Background, R.mipmap.fcr_whiteboard_bg),
    )

    private var toolBoxAdapter: FcrBoardToolBoxAdapter = FcrBoardToolBoxAdapter(toolBoxItems).apply {
        setItemClickListener(this@FcrBoardToolBoxLayout)
    }

    private var toolBoxListener: ToolBoxListener? = null
    private var toolBoxType: FcrBoardToolBoxType = FcrBoardToolBoxType.Tool
    private var drawState: DrawState? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FcrBoardToolBox).apply {
            try {
                orientation = getInt(R.styleable.FcrBoardToolBox_fcr_layoutOrientation, HORIZONTAL)
            } finally {
                recycle()
            }
        }

        val rvToolBox = binding.rvToolBox
        rvToolBox.adapter = toolBoxAdapter
        rvToolBox.layoutManager = ToolBoxLayoutManager(context, orientation)
        rvToolBox.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val paddingMain = resources.getDimensionPixelSize(R.dimen.fcr_board_toolbox_item_space_main)
                val paddingCross = resources.getDimensionPixelSize(R.dimen.fcr_board_toolbox_item_space_cross)
                if (orientation == HORIZONTAL) {
                    outRect.set(paddingMain, paddingCross, paddingMain, paddingCross)
                } else {
                    outRect.set(paddingCross, paddingMain, paddingCross, paddingMain)
                }

                val position = parent.getChildAdapterPosition(view)
                val itemCount = state.itemCount
                if (position == 0) {
                    if (orientation == HORIZONTAL) outRect.left += paddingMain else outRect.top += paddingMain
                } else if (position == itemCount - 1) {
                    if (orientation == HORIZONTAL) outRect.right += paddingMain else outRect.bottom += paddingMain
                }
            }
        })
    }

    override fun onItemClick(position: Int) {
        toolBoxListener?.onToolBoxClick(toolBoxItems[position], position)
    }

    fun setToolBoxListener(listener: ToolBoxListener) {
        toolBoxListener = listener
    }

    fun setDrawConfig(drawState: DrawState) {
        this.drawState = drawState
        updateDrawConfig()
    }

    private fun updateDrawConfig() {
        val drawConfig = drawState ?: return
        toolBoxItems.forEach { toolBoxItem ->
            val indexOf = toolBoxItem.tools.indexOf(drawConfig.toolType)
            if (indexOf >= 0) {
                toolBoxItem.index = indexOf
                toolBoxItem.iconResId = drawConfig.toolType.imgResId()
            }
        }
        toolBoxAdapter.setDrawConfig(drawConfig)
    }

    fun setSelectionType(type: FcrBoardToolBoxType) {
        when (type) {
            FcrBoardToolBoxType.Stroke -> {
                toolBoxItems.forEach { it.isSelected = it.type == FcrBoardToolBoxType.Stroke }
            }

            FcrBoardToolBoxType.Background -> {
                toolBoxItems.forEach { it.isSelected = it.type == FcrBoardToolBoxType.Background }
            }

            FcrBoardToolBoxType.Download -> {
                toolBoxItems.forEach { it.isSelected = it.type == FcrBoardToolBoxType.Download }
            }

            else -> {
                toolBoxItems.forEach {
                    val indexOf = it.tools.indexOf(drawState?.toolType)
                    it.isSelected = indexOf >= 0
                }
            }
        }
        toolBoxAdapter.setItems(toolBoxItems)
    }

    fun animateGuide() {
        binding.rvToolBox.smoothScrollToPosition(toolBoxItems.size - 1)
        postDelayed({
            binding.rvToolBox.smoothScrollToPosition(0)
        }, SCROLL_DURATION.toLong())
    }

    class ToolBoxLayoutManager(
        context: Context,
        orientation: Int,
        private val scrollDuration: Int = SCROLL_DURATION,
    ) : LinearLayoutManager(context, orientation, false) {

        override fun smoothScrollToPosition(
            recyclerView: RecyclerView,
            state: RecyclerView.State?,
            position: Int
        ) {
            val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                override fun calculateTimeForScrolling(dx: Int): Int {
                    return scrollDuration
                }
            }
            smoothScroller.targetPosition = position
            startSmoothScroll(smoothScroller)
        }
    }

    interface ToolBoxListener {
        fun onToolBoxClick(item: ToolBoxItem, position: Int)
    }
}

data class ToolBoxItem(
    val type: FcrBoardToolBoxType,
    var iconResId: Int,
    val tools: List<ToolType> = listOf(),
    var index: Int = 0,
    var isSelected: Boolean = false,
)

/**
 * author : fenglibin
 * date : 2024/7/2
 * description : 白板布局 ToolBoxItem 类型
 */
enum class FcrBoardToolBoxType {
    /**
     * 工具
     */
    Tool,

    /**
     * 属性选择
     */
    Stroke,

    /**
     * 清除
     */
    Clear,

    Undo,

    Redo,

    /**
     * 下载
     */
    Download,

    /**
     * 背景选择
     */
    Background,
}
