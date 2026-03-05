package io.agora.board.forge.ui.whiteboard.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.agora.board.forge.ui.databinding.FcrBoardToolBarItemBinding
import io.agora.board.forge.ui.internal.FoundationUtils
import io.agora.board.forge.ui.internal.findForgeConfigOrNull
import io.agora.board.forge.ui.model.ToolbarAction
import io.agora.board.forge.ui.model.ToolbarItem
import io.agora.board.forge.ui.theme.ForgeUiDefaultProvider
import io.agora.board.forge.ui.theme.ForgeUiProvider
import io.agora.board.forge.ui.whiteboard.state.WhiteboardUiState

/**
 * author : fenglibin
 * date : 2024/7/2
 * description : 白板工具属性选择面板适配器
 */
class FcrBoardToolbarAdapter(private var itemList: List<ToolbarItem>) :
    RecyclerView.Adapter<FcrBoardToolbarAdapter.ViewHolder>() {

    private var uiState: WhiteboardUiState? = null
    private var onItemClickListener: OnItemClickListener? = null

    /** 由 FcrBoardToolbar 在 onAttachedToWindow 时注入，避免 item 尚未 attach 时 findForgeConfig 崩溃 */
    var provider: ForgeUiProvider? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            FcrBoardToolBarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        val binding = holder.binding
        val context = binding.root.context
        val provider = this.provider
            ?: binding.root.findForgeConfigOrNull()?.provider
            ?: fallbackProvider

        val iconResId = when (item) {
            is ToolbarItem.Tool -> {
                val tool = item.tools.getOrNull(item.index) ?: item.tools.firstOrNull()
                if (tool != null) provider.toolIcon(tool) else 0
            }

            is ToolbarItem.Action -> provider.toolActionIcon(item.action)
        }
        binding.ivIcon.setImageResource(iconResId)
        binding.ivIcon.visibility = View.VISIBLE
        binding.vBackground.isSelected = item.isSelected

        val isStrokeAction = item is ToolbarItem.Action && item.action == ToolbarAction.Stroke
        if (isStrokeAction) {
            binding.flStroke.visibility = View.VISIBLE
            uiState?.let {
                binding.strokeDot.setDotColor(it.strokeColor)
                binding.strokeDot.setDotSize(
                    FoundationUtils.dp2pxFloat(
                        context,
                        it.strokeWidth.toFloat()
                    )
                )
            }
        } else {
            binding.flStroke.visibility = View.GONE
        }

        val enabled = when (item) {
            is ToolbarItem.Action -> when (item.action) {
                ToolbarAction.Undo -> uiState?.undo ?: false
                ToolbarAction.Redo -> uiState?.redo ?: false
                else -> true
            }

            is ToolbarItem.Tool -> true
        }
        binding.ivIcon.alpha = if (enabled) 1f else 0.5f
        binding.ivIcon.isEnabled = enabled

        binding.root.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    fun setItems(itemList: List<ToolbarItem>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setUiState(state: WhiteboardUiState) {
        this.uiState = state
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: FcrBoardToolBarItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private val fallbackProvider = ForgeUiDefaultProvider()
    }
}
