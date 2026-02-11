package io.agora.board.forge.ui.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.agora.board.forge.ui.databinding.FcrBoardToolBoxItemBinding
import io.agora.board.forge.ui.component.state.DrawState

/**
 * author : fenglibin
 * date : 2024/7/2
 * description : 白板工具属性选择面板适配器
 */
class FcrBoardToolBoxAdapter(private var itemList: List<ToolBoxItem>) :
    RecyclerView.Adapter<FcrBoardToolBoxAdapter.ViewHolder>() {

    private var drawState: DrawState? = null
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FcrBoardToolBoxItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        val binding = holder.binding

        binding.ivIcon.setImageResource(item.iconResId)
        binding.ivIcon.visibility = View.VISIBLE
        binding.vBackground.isSelected = item.isSelected

        if (item.type == FcrBoardToolBoxType.Stroke) {
            binding.flStroke.visibility = View.VISIBLE
            drawState?.let {
                binding.strokeDot.setDotColor(it.strokeColor)
                binding.strokeDot.setDotSize(it.strokeWidth.toFloat())
            }
        } else {
            binding.flStroke.visibility = View.GONE
        }

        val enabled = when (item.type) {
            FcrBoardToolBoxType.Undo -> drawState?.undo ?: false
            FcrBoardToolBoxType.Redo -> drawState?.redo ?: false
            else -> true
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

    fun setDrawConfig(drawState: DrawState) {
        this.drawState = drawState
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: FcrBoardToolBoxItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
