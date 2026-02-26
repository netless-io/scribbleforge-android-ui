package io.agora.board.ui.sample.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.board.forge.Room
import io.agora.board.forge.common.dev.DevNetworkLossSimulator
import io.agora.board.forge.ui.sample.R
import io.agora.board.forge.ui.sample.databinding.ItemSettingButtonBinding
import io.agora.board.forge.ui.sample.databinding.ItemSettingDropdownBinding
import io.agora.board.forge.ui.sample.databinding.ItemSettingRadioBinding
import io.agora.board.forge.ui.sample.databinding.ItemSettingSliderBinding
import io.agora.board.forge.ui.sample.databinding.ItemSettingSwitchBinding
import io.agora.board.forge.ui.sample.databinding.ItemSettingTextBinding
import io.agora.board.forge.ui.sample.databinding.ItemSettingTextInputBinding
import io.agora.board.forge.ui.sample.databinding.FgsWindowSettingLayoutBinding
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.windowmanager.WindowManager
import io.agora.board.ui.sample.util.KvStore
import io.agora.board.ui.sample.util.MessageRecordHelper
import io.agora.board.ui.sample.util.PermissionHelper

class FgsWindowSettingLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = FgsWindowSettingLayoutBinding.inflate(LayoutInflater.from(context), this)

    private var room: Room? = null
    private var windowManagerApplication: WindowManager? = null
    private var whiteboardApplication: WhiteboardApplication? = null

    private val windowSettings = listOf(
        SettingItem("group", "多窗口设置", SettingType.TEXT, "多窗口设置") {},
        // SettingItem("footer", "显示底部栏", SettingType.SWITCH, true) {},
        SettingItem("ratio", "选择比例", SettingType.RADIO, "16:9", options = listOf("16:9", "4:3", "9:16", "N")) {
            when (it) {
                "16:9" -> windowManagerApplication?.setContentRatio(16f / 9f)
                "4:3" -> windowManagerApplication?.setContentRatio(4f / 3f)
                "9:16" -> windowManagerApplication?.setContentRatio(9f / 16f)
                "N" -> windowManagerApplication?.setContentRatio(null)
            }
        },
        // SettingItem("click", "手动触发测试", SettingType.BUTTON, "点击测试") {
        //     // 执行按钮动作
        // },
    )

    private val whiteboardSettings: List<SettingItem>
        get() {
            return listOf(
                SettingItem("whiteboard", "白板设置", SettingType.TEXT, "白板设置") {},
                SettingItem("cameraBoundaryColor", "设置边界颜色", SettingType.BUTTON, Color.RED) {
                    whiteboardApplication?.setCameraBoundaryColor(Color.BLUE)
                },
                // SettingItem("enableCameraBoundaryHighlight", "设置边界高亮", SettingType.SWITCH, true) {
                //     whiteboardApplication?.enableCameraBoundaryHighlight(it as Boolean)
                // },
                // SettingItem("setMainCanvasVisible", "设置主Canvas可见", SettingType.SWITCH, true) {
                //     whiteboardApplication?.performPrivateFunction("setMainCanvasVisible", it as Boolean)
                // },
                // SettingItem("setDelayTranslateOut", "设置画笔消失", SettingType.TEXT_INPUT, 2000) {
                //     val value = it.toString().toIntOrNull() ?: return@SettingItem
                //     whiteboardApplication?.performPrivateFunction("setDelayTranslateOut", value)
                // },
                // SettingItem("setLiveCursorVisible", "设置Cursor", SettingType.SWITCH, true) {
                //     whiteboardApplication?.performPrivateFunction("setLiveCursorVisible", it as Int)
                // },
                // SettingItem("showWhiteboard", "显示/隐藏白板控制", SettingType.SWITCH, false) {
                //     val whiteboardController = context.getActivity()?.findViewById<View>(R.id.whiteboard_control_layout)
                //     whiteboardController?.run {
                //         isVisible = it as Boolean
                //     }
                // },
                SettingItem("setPerformanceMode", "设置新能模式", SettingType.SWITCH, false) {
                    whiteboardApplication?.setPerformanceMode(it as Boolean)
                },
            )
        }

    private val imageryDocSettings: List<SettingItem>
        get() {
            return listOf(
                SettingItem("imageryDoc", "图片文档设置", SettingType.TEXT, "图片文档设置") {},
                SettingItem(
                    "displayMode",
                    "连续浏览模式",
                    SettingType.SWITCH,
                    KvStore.isDocContinuousMode()
                ) { value ->
                    KvStore.setDocContinuousMode(value as Boolean)
                },
                SettingItem(
                    "inheritWhiteboardId",
                    "教具集成",
                    SettingType.SWITCH,
                    KvStore.isDocInheritWhiteboardId()
                ) { value ->
                    KvStore.setDocInheritWhiteboardId(value as Boolean)
                },
            )
        }

    private val slideSettings: List<SettingItem>
        get() {
            return listOf(
                SettingItem("slide", "幻灯片设置", SettingType.TEXT, "幻灯片设置") {},
                SettingItem(
                    "inheritWhiteboardId",
                    "教具集成",
                    SettingType.SWITCH,
                    KvStore.isSlideInheritWhiteboardId()
                ) { value ->
                    KvStore.setSlideInheritWhiteboardId(value as Boolean)
                },
            )
        }

    private val roomSettings = listOf(
        SettingItem("whiteboard", "房间设置", SettingType.TEXT, "房间设置") {},
        SettingItem("messageRecord", "消息录制", SettingType.BUTTON, "") {
            room?.let { room ->
                MessageRecordHelper(context, room).showRecordingMenu()
            }
        },
        SettingItem("sendDrop", "发送丢包", SettingType.SWITCH, false) {
            val enable = it as Boolean
            if (enable) {
                DevNetworkLossSimulator.enabled = true
                DevNetworkLossSimulator.Send.enabled = true
            } else {
                DevNetworkLossSimulator.Send.enabled = false
            }
        },
        SettingItem("receiveDrop", "接受丢包", SettingType.SWITCH, false) {
            val enable = it as Boolean
            if (enable) {
                DevNetworkLossSimulator.enabled = true
                DevNetworkLossSimulator.Receive.enabled = true
            } else {
                DevNetworkLossSimulator.Receive.enabled = false
            }
        },
        SettingItem("", "权限设置", SettingType.TEXT, "权限设置") {},
        SettingItem("permissionManager", "权限管理", SettingType.BUTTON, "") {
            room?.let { room ->
                PermissionHelper.showPermissionManagementDialog(context, room)
            }
        },
        SettingItem("allWritable", "设置全部用户可写", SettingType.BUTTON, "") {
            room?.let { room ->
                room.userManager.getUsers().forEach { user ->
                    room.setWritable(user.id, true)
                }
            }
        },
    )

    private val settings = mutableListOf<SettingItem>()

    init {
        binding.settingsRecyclerView.adapter = SettingsAdapter(settings)
        binding.settingsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun attachWindowManager(windowManagerApplication: WindowManager) {
        this.windowManagerApplication = windowManagerApplication
        updateSettings()
    }

    fun detachWindowManager() {
        this.windowManagerApplication = null
        updateSettings()
    }

    fun attachWhiteboard(whiteboardApplication: WhiteboardApplication) {
        this.whiteboardApplication = whiteboardApplication
        updateSettings()
    }

    fun detachWhiteboard() {
        this.whiteboardApplication = null
        updateSettings()
    }

    fun attachRoom(room: Room) {
        this.room = room
        updateSettings()
    }

    private fun updateSettings() {
        settings.clear()
        if (windowManagerApplication != null) {
            settings.addAll(windowSettings)
        }
        if (whiteboardApplication != null) {
            settings.addAll(whiteboardSettings)
        }
        if (room != null) {
            settings.addAll(imageryDocSettings)
            settings.addAll(slideSettings)

            settings.addAll(roomSettings)
        }
        binding.settingsRecyclerView.adapter?.notifyDataSetChanged()
    }
}


enum class SettingType {
    SWITCH, SLIDER, DROPDOWN, RADIO, BUTTON, TEXT, TEXT_INPUT
}

data class SettingItem(
    val id: String,
    val title: String,
    val type: SettingType,
    val value: Any,
    val description: String? = null,
    val options: List<Any>? = null,
    val min: Int = 0, // For SeekBar
    val max: Int = 100, // For SeekBar
    val onClick: (() -> Unit)? = null,
    val onValueChanged: ((Any) -> Unit)? = null
)

class SettingsAdapter(
    private val items: List<SettingItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (SettingType.values()[viewType]) {
            SettingType.SWITCH -> {
                val binding = ItemSettingSwitchBinding.inflate(inflater, parent, false)
                SwitchViewHolder(binding)
            }

            SettingType.SLIDER -> {
                val binding = ItemSettingSliderBinding.inflate(inflater, parent, false)
                SliderViewHolder(binding)
            }

            SettingType.DROPDOWN -> {
                val binding = ItemSettingDropdownBinding.inflate(inflater, parent, false)
                DropdownViewHolder(binding)
            }

            SettingType.RADIO -> {
                val binding = ItemSettingRadioBinding.inflate(inflater, parent, false)
                RadioViewHolder(binding)
            }

            SettingType.BUTTON -> {
                val binding = ItemSettingButtonBinding.inflate(inflater, parent, false)
                ButtonViewHolder(binding)
            }

            SettingType.TEXT -> {
                val binding = ItemSettingTextBinding.inflate(inflater, parent, false)
                TextViewHolder(binding)
            }

            SettingType.TEXT_INPUT -> {
                val binding = ItemSettingTextInputBinding.inflate(inflater, parent, false)
                TextInputViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SwitchViewHolder -> holder.bind(item)
            is SliderViewHolder -> holder.bind(item)
            is DropdownViewHolder -> holder.bind(item)
            is RadioViewHolder -> holder.bind(item)
            is ButtonViewHolder -> holder.bind(item)
            is TextViewHolder -> holder.bind(item)
            is TextInputViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class SwitchViewHolder(private val binding: ItemSettingSwitchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.title.text = item.title
            binding.switchView.setOnCheckedChangeListener(null)
            binding.switchView.isChecked = item.value as? Boolean ?: false
            binding.switchView.setOnCheckedChangeListener { _, isChecked ->
                item.onValueChanged?.invoke(isChecked)
            }
        }
    }

    class SliderViewHolder(private val binding: ItemSettingSliderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            val value = item.value as? Int ?: item.min
            binding.title.text = item.title
            binding.seekbar.max = item.max - item.min
            binding.seekbar.progress = value - item.min
            binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    item.onValueChanged?.invoke(progress + item.min)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    class DropdownViewHolder(private val binding: ItemSettingDropdownBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.title.text = item.title
            val options = item.options ?: emptyList()
            val adapter = ArrayAdapter(binding.root.context, R.layout.layout_user_spinner_item, options)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter

            val currentValue = item.value.toString()
            val position = options.indexOf(currentValue).takeIf { it >= 0 } ?: 0
            binding.spinner.setSelection(position)

            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, pos: Int, id: Long
                ) {
                    item.onValueChanged?.invoke(options[pos])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    class RadioViewHolder(private val binding: ItemSettingRadioBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.title.text = item.title
            binding.radioGroup.removeAllViews()
            val currentValue = item.value.toString()
            val context = binding.root.context

            item.options?.forEachIndexed { index, option ->
                val radioButton = RadioButton(context).apply {
                    text = option as? String
                    id = index
                }
                binding.radioGroup.addView(radioButton)
                if (option == currentValue) {
                    radioButton.isChecked = true
                }
            }

            binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                item.options?.getOrNull(checkedId)?.let {
                    item.onValueChanged?.invoke(it)
                }
            }
        }
    }

    class ButtonViewHolder(private val binding: ItemSettingButtonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.button.text = item.title
            binding.button.setOnClickListener {
                item.onValueChanged?.invoke(Unit)
            }
        }
    }

    class TextViewHolder(private val binding: ItemSettingTextBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.text.text = item.title
        }
    }

    class TextInputViewHolder(private val binding: ItemSettingTextInputBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.title.text = item.title
            binding.editText.setText(item.value.toString())
            binding.confirmButton.setOnClickListener {
                val text = binding.editText.text.toString()
                item.onValueChanged?.invoke(text)
            }
        }
    }
}
