package io.agora.board.forge.ui.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.agora.board.forge.ui.databinding.FcrBoardToolBoxItemBinding

/**
 * author : fenglibin
 * date : 2024/7/2
 * description : 白板工具属性选择面板适配器
 */
class FcrBoardToolBoxAdapter(private var itemList: List<ToolBoxItem>) :
    RecyclerView.Adapter<FcrBoardToolBoxAdapter.ViewHolder>() {

    private var drawConfig: FcrBoardUiDrawConfig? = null
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FcrBoardToolBoxItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        val binding = holder.binding

        if (item.type == FcrBoardToolBoxType.Stroke) {
            binding.ivIcon.setImageResource(item.iconResId)
            binding.ivIcon.visibility = View.VISIBLE
            binding.flStroke.visibility = View.VISIBLE
            drawConfig?.let {
                binding.strokeDot.setDotColor(it.strokeColor)
                binding.strokeDot.setDotSize(it.strokeWidth.toFloat())
            }
        } else {
            binding.ivIcon.setImageResource(item.iconResId)
            binding.ivIcon.visibility = View.VISIBLE
            binding.flStroke.visibility = View.GONE
        }
        binding.vBackground.isSelected = item.isSelected

        // undo redo
        if (item.type == FcrBoardToolBoxType.Undo || item.type == FcrBoardToolBoxType.Redo) {
            binding.ivIcon.alpha = if (item.isEnabled) 1f else 0.5f
            binding.ivIcon.isEnabled = item.isEnabled
        } else {
            binding.ivIcon.alpha = 1f
            binding.ivIcon.isEnabled = true
        }

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

    fun setDrawConfig(drawConfig: FcrBoardUiDrawConfig) {
        this.drawConfig = drawConfig
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: FcrBoardToolBoxItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
