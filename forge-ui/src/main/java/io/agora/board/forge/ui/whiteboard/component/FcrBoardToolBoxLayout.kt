package io.agora.board.forge.ui.whiteboard.component

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
import io.agora.board.forge.ui.databinding.FcrBoardToolBoxComponentBinding
import io.agora.board.forge.ui.whiteboard.state.WhiteboardUiState
import io.agora.board.forge.ui.internal.findForgeConfigOrNull
import io.agora.board.forge.ui.model.ToolBoxAction
import io.agora.board.forge.ui.model.ToolBoxItem
import io.agora.board.forge.whiteboard.WhiteboardToolType

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
    private val binding =
        FcrBoardToolBoxComponentBinding.inflate(LayoutInflater.from(context), this)

    private var toolBoxItems = mutableListOf(
        ToolBoxItem.Action(ToolBoxAction.Clear, false),
        ToolBoxItem.Action(ToolBoxAction.Undo, false),
        ToolBoxItem.Action(ToolBoxAction.Redo, false),
        ToolBoxItem.Tool(listOf(WhiteboardToolType.CURVE), 0, false),
        ToolBoxItem.Tool(listOf(WhiteboardToolType.LASER), 0, false),
        ToolBoxItem.Tool(
            listOf(
                WhiteboardToolType.RECTANGLE,
                WhiteboardToolType.TRIANGLE,
                WhiteboardToolType.ELLIPSE,
                WhiteboardToolType.LINE,
                WhiteboardToolType.ARROW,
            ),
            0,
            false
        ),
        ToolBoxItem.Tool(listOf(WhiteboardToolType.POINTER, WhiteboardToolType.GRAB), 0, false),
        ToolBoxItem.Tool(listOf(WhiteboardToolType.SELECTOR), 0, false),
        ToolBoxItem.Tool(listOf(WhiteboardToolType.ERASER), 0, false),
        ToolBoxItem.Action(ToolBoxAction.Stroke, false),
        ToolBoxItem.Tool(listOf(WhiteboardToolType.TEXT), 0, false),
        ToolBoxItem.Action(ToolBoxAction.Download, false),
        ToolBoxItem.Action(ToolBoxAction.Background, false),
    )

    private var toolBoxAdapter: FcrBoardToolBoxAdapter =
        FcrBoardToolBoxAdapter(toolBoxItems).apply {
            setItemClickListener(this@FcrBoardToolBoxLayout)
        }

    private var toolBoxListener: ToolBoxListener? = null
    private var uiState: WhiteboardUiState? = null

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
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val paddingMain =
                    resources.getDimensionPixelSize(R.dimen.fcr_board_toolbox_item_space_main)
                val paddingCross =
                    resources.getDimensionPixelSize(R.dimen.fcr_board_toolbox_item_space_cross)
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findForgeConfigOrNull()?.provider?.let { toolBoxAdapter.provider = it }
    }

    override fun onItemClick(position: Int) {
        toolBoxListener?.onToolBoxClick(toolBoxItems[position], position)
    }

    fun setToolBoxListener(listener: ToolBoxListener) {
        toolBoxListener = listener
    }

    fun setUiState(state: WhiteboardUiState) {
        this.uiState = state
        applyUiState()
    }

    private fun applyUiState() {
        val state = uiState ?: return
        toolBoxItems.forEach { item ->
            when (item) {
                is ToolBoxItem.Tool -> {
                    val indexOf = item.tools.indexOf(state.toolType)
                    if (indexOf >= 0) {
                        item.index = indexOf
                    }
                }

                is ToolBoxItem.Action -> { /* icon from provider in adapter */
                }
            }
        }
        toolBoxAdapter.setUiState(state)
    }

    fun setSelectionType(
        strokeShown: Boolean,
        bgPickShown: Boolean,
        downloadShown: Boolean,
        currentTool: WhiteboardToolType?
    ) {
        toolBoxItems.forEach { item ->
            when (item) {
                is ToolBoxItem.Tool -> item.isSelected =
                    currentTool != null && item.tools.contains(currentTool)

                is ToolBoxItem.Action -> item.isSelected = when (item.action) {
                    ToolBoxAction.Stroke -> strokeShown
                    ToolBoxAction.Background -> bgPickShown
                    ToolBoxAction.Download -> downloadShown
                    else -> false
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
