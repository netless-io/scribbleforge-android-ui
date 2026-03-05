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
import io.agora.board.forge.ui.databinding.FcrBoardToolBarBinding
import io.agora.board.forge.ui.whiteboard.state.WhiteboardUiState
import io.agora.board.forge.ui.internal.findForgeConfigOrNull
import io.agora.board.forge.ui.model.ToolbarAction
import io.agora.board.forge.ui.model.ToolbarItem
import io.agora.board.forge.whiteboard.WhiteboardToolType

/**
 * author : fenglibin
 * date : 2024/7/2
 * description : 白板工具选择界面
 */
class FcrBoardToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), FcrBoardToolbarAdapter.OnItemClickListener {
    companion object {
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL

        const val SCROLL_DURATION = 300
    }

    private var orientation: Int
    private val binding =
        FcrBoardToolBarBinding.inflate(LayoutInflater.from(context), this)

    private var toolbarItems = mutableListOf(
        ToolbarItem.Action(ToolbarAction.Clear, false),
        ToolbarItem.Action(ToolbarAction.Undo, false),
        ToolbarItem.Action(ToolbarAction.Redo, false),
        ToolbarItem.Tool(listOf(WhiteboardToolType.CURVE), 0, false),
        ToolbarItem.Tool(listOf(WhiteboardToolType.LASER), 0, false),
        ToolbarItem.Tool(
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
        ToolbarItem.Tool(listOf(WhiteboardToolType.POINTER, WhiteboardToolType.GRAB), 0, false),
        ToolbarItem.Tool(listOf(WhiteboardToolType.SELECTOR), 0, false),
        ToolbarItem.Tool(listOf(WhiteboardToolType.ERASER), 0, false),
        ToolbarItem.Action(ToolbarAction.Stroke, false),
        ToolbarItem.Tool(listOf(WhiteboardToolType.TEXT), 0, false),
        ToolbarItem.Action(ToolbarAction.Download, false),
        ToolbarItem.Action(ToolbarAction.Background, false),
    )

    private var toolBoxAdapter: FcrBoardToolbarAdapter =
        FcrBoardToolbarAdapter(toolbarItems).apply {
            setItemClickListener(this@FcrBoardToolbar)
        }

    private var toolBoxListener: ToolBoxListener? = null
    private var uiState: WhiteboardUiState? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FcrBoardToolbar).apply {
            try {
                orientation = getInt(R.styleable.FcrBoardToolbar_fcr_layoutOrientation, HORIZONTAL)
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
        toolBoxListener?.onToolBoxClick(toolbarItems[position], position)
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
        toolbarItems.forEach { item ->
            when (item) {
                is ToolbarItem.Tool -> {
                    val indexOf = item.tools.indexOf(state.toolType)
                    if (indexOf >= 0) {
                        item.index = indexOf
                    }
                }

                is ToolbarItem.Action -> { /* icon from provider in adapter */
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
        toolbarItems.forEach { item ->
            when (item) {
                is ToolbarItem.Tool -> item.isSelected =
                    currentTool != null && item.tools.contains(currentTool)

                is ToolbarItem.Action -> item.isSelected = when (item.action) {
                    ToolbarAction.Stroke -> strokeShown
                    ToolbarAction.Background -> bgPickShown
                    ToolbarAction.Download -> downloadShown
                    else -> false
                }
            }
        }
        toolBoxAdapter.setItems(toolbarItems)
    }

    fun animateGuide() {
        binding.rvToolBox.smoothScrollToPosition(toolbarItems.size - 1)
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
        fun onToolBoxClick(item: ToolbarItem, position: Int)
    }
}
