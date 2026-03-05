package io.agora.board.forge.ui.whiteboard.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.agora.board.forge.ui.databinding.FcrBoardToolBoxItemBinding
import io.agora.board.forge.ui.internal.FoundationUtils
import io.agora.board.forge.ui.internal.findForgeConfigOrNull
import io.agora.board.forge.ui.model.ToolBoxAction
import io.agora.board.forge.ui.model.ToolBoxItem
import io.agora.board.forge.ui.theme.ForgeUiProvider
import io.agora.board.forge.ui.theme.ForgeUiDefaultProvider
import io.agora.board.forge.ui.whiteboard.state.WhiteboardUiState

/**
 * author : fenglibin
 * date : 2024/7/2
 * description : 白板工具属性选择面板适配器
 */
class FcrBoardToolBoxAdapter(private var itemList: List<ToolBoxItem>) :
    RecyclerView.Adapter<FcrBoardToolBoxAdapter.ViewHolder>() {

    private var uiState: WhiteboardUiState? = null
    private var onItemClickListener: OnItemClickListener? = null

    /** 由 FcrBoardToolBoxLayout 在 onAttachedToWindow 时注入，避免 item 尚未 attach 时 findForgeConfig 崩溃 */
    var provider: ForgeUiProvider? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FcrBoardToolBoxItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            is ToolBoxItem.Tool -> {
                val tool = item.tools.getOrNull(item.index) ?: item.tools.firstOrNull()
                if (tool != null) provider.toolIcon(tool) else 0
            }
            is ToolBoxItem.Action -> provider.toolActionIcon(item.action)
        }
        binding.ivIcon.setImageResource(iconResId)
        binding.ivIcon.visibility = View.VISIBLE
        binding.vBackground.isSelected = item.isSelected

        val isStrokeAction = item is ToolBoxItem.Action && item.action == ToolBoxAction.Stroke
        if (isStrokeAction) {
            binding.flStroke.visibility = View.VISIBLE
            uiState?.let {
                binding.strokeDot.setDotColor(it.strokeColor)
                binding.strokeDot.setDotSize(FoundationUtils.dp2pxFloat(context, it.strokeWidth.toFloat()))
            }
        } else {
            binding.flStroke.visibility = View.GONE
        }

        val enabled = when (item) {
            is ToolBoxItem.Action -> when (item.action) {
                ToolBoxAction.Undo -> uiState?.undo ?: false
                ToolBoxAction.Redo -> uiState?.redo ?: false
                else -> true
            }
            is ToolBoxItem.Tool -> true
        }
        binding.ivIcon.alpha = if (enabled) 1f else 0.5f
        binding.ivIcon.isEnabled = enabled

        binding.root.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    fun setItems(itemList: List<ToolBoxItem>) {
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

    class ViewHolder(val binding: FcrBoardToolBoxItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private val fallbackProvider = ForgeUiDefaultProvider()
    }
}
