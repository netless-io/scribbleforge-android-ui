package io.agora.board.sample.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.isVisible
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.sample.databinding.LayoutChangeElementAttributesBinding
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardElementAttributeKey
import io.agora.board.forge.whiteboard.WhiteboardElementSelection
import io.agora.board.sample.util.dp2Px
import org.json.JSONArray

class WhiteboardElementAttributesLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutChangeElementAttributesBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private var whiteboardApp: WhiteboardApplication? = null

    init {
        visibility = View.GONE
        setOnClickListener {
            // Prevent clicks from propagating through
        }
    }

    fun setWhiteboardApp(app: WhiteboardApplication?) {
        this.whiteboardApp = app
    }

    fun showElementAttributes(selection: WhiteboardElementSelection?) {
        isVisible = selection != null

        if (selection == null) return

        val layoutMap = mapOf(
            WhiteboardElementAttributeKey.STROKE_COLOR to binding.layoutStrokeColor,
            WhiteboardElementAttributeKey.FILL_COLOR to binding.layoutFillColor,
            WhiteboardElementAttributeKey.STROKE_WIDTH to binding.layoutStrokeWidth,
            WhiteboardElementAttributeKey.FONT_SIZE to binding.layoutFontSize,
            WhiteboardElementAttributeKey.DASH_ARRAY to binding.layoutDashArray,
            WhiteboardElementAttributeKey.HEAD_ARROW to binding.layoutArrowStyle,
            WhiteboardElementAttributeKey.TAIL_ARROW to binding.layoutArrowStyle,
        )

        // Hide all layouts initially
        layoutMap.values.forEach { it.isVisible = false }

        var arrowStyle = false
        selection.attributes.forEach { key ->
            val attributeKey = WhiteboardElementAttributeKey.fromKey(key)
            layoutMap[attributeKey]?.isVisible = true

            when (attributeKey) {
                WhiteboardElementAttributeKey.STROKE_COLOR -> setupStrokeColor(selection)
                WhiteboardElementAttributeKey.FILL_COLOR -> setupFillColor(selection)
                WhiteboardElementAttributeKey.STROKE_WIDTH -> setupStrokeWidth(selection)
                WhiteboardElementAttributeKey.FONT_SIZE -> setupFontSize(selection)
                WhiteboardElementAttributeKey.DASH_ARRAY -> setupDashArray(selection)
                WhiteboardElementAttributeKey.HEAD_ARROW, WhiteboardElementAttributeKey.TAIL_ARROW -> {
                    if (!arrowStyle) {
                        arrowStyle = true
                        setupArrowStyle(selection)
                    }
                }
                else -> {}
            }
        }
    }

    private fun setupStrokeColor(selection: WhiteboardElementSelection) {
        val colors = listOf("#4CAF50", "#2196F3", "#FF9800", "#F44336", "#9E9E9E")
        val colorLayout = binding.layoutStrokeColorPalette
        colorLayout.removeAllViews()

        colors.forEach { color ->
            colorLayout.addView(createColorView(color) {
                whiteboardApp?.setElementAttribute(
                    selection.layerId,
                    selection.uuid,
                    WhiteboardElementAttributeKey.STROKE_COLOR.key,
                    color
                )
            })
        }

        // Get current stroke color and update UI
        whiteboardApp?.getElementAttribute(
            selection.layerId,
            selection.uuid,
            WhiteboardElementAttributeKey.STROKE_COLOR.key,
            object : RoomCallback<Any> {
                override fun onSuccess(result: Any) {
                    if (result is String) {
                        colors.forEachIndexed { index, color ->
                            if (color == result) {
                                colorLayout.getChildAt(index)?.isSelected = true
                            }
                        }
                    }
                }

                override fun onFailure(error: RoomError) {}
            })
    }

    private fun setupFillColor(selection: WhiteboardElementSelection) {
        val colors = listOf("#4CAF50", "#2196F3", "#FF9800", "#F44336", "#9E9E9E")
        val fillColorLayout = binding.layoutFillColorPalette
        fillColorLayout.removeAllViews()

        colors.forEach { color ->
            fillColorLayout.addView(createColorView(color) {
                whiteboardApp?.setElementAttribute(
                    selection.layerId,
                    selection.uuid,
                    WhiteboardElementAttributeKey.FILL_COLOR.key,
                    color
                )
            })
        }

        // Get current fill color and update UI
        whiteboardApp?.getElementAttribute(
            selection.layerId,
            selection.uuid,
            WhiteboardElementAttributeKey.FILL_COLOR.key,
            object : RoomCallback<Any> {
                override fun onSuccess(result: Any) {
                    if (result is String) {
                        colors.forEachIndexed { index, color ->
                            if (color == result) {
                                fillColorLayout.getChildAt(index)?.isSelected = true
                            }
                        }
                    }
                }

                override fun onFailure(error: RoomError) {}
            })
    }

    private fun setupStrokeWidth(selection: WhiteboardElementSelection) {
        val strokeWidthSlider = binding.strokeWidthSlider
        strokeWidthSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    whiteboardApp?.setElementAttribute(
                        selection.layerId,
                        selection.uuid,
                        WhiteboardElementAttributeKey.STROKE_WIDTH.key,
                        progress.toFloat()
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Get current stroke width and update UI
        whiteboardApp?.getElementAttribute(
            selection.layerId,
            selection.uuid,
            WhiteboardElementAttributeKey.STROKE_WIDTH.key,
            object : RoomCallback<Any> {
                override fun onSuccess(result: Any) {
                    if (result is Number) {
                        strokeWidthSlider.progress = result.toInt()
                    }
                }

                override fun onFailure(error: RoomError) {}
            })
    }

    private fun setupFontSize(selection: WhiteboardElementSelection) {
        val fontSizeSlider = binding.fontSizeSlider
        fontSizeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    whiteboardApp?.setElementAttribute(
                        selection.layerId,
                        selection.uuid,
                        WhiteboardElementAttributeKey.FONT_SIZE.key,
                        progress.toFloat()
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Get current font size and update UI
        whiteboardApp?.getElementAttribute(
            selection.layerId,
            selection.uuid,
            WhiteboardElementAttributeKey.FONT_SIZE.key,
            object : RoomCallback<Any> {
                override fun onSuccess(result: Any) {
                    if (result is Number) {
                        fontSizeSlider.progress = result.toInt()
                    }
                }

                override fun onFailure(error: RoomError) {}
            })
    }

    private fun setupDashArray(selection: WhiteboardElementSelection) {
        val dash1 = binding.dash1
        val dash2 = binding.dash2
        val dash3 = binding.dash3

        dash1.setOnClickListener {
            whiteboardApp?.setElementAttribute(
                selection.layerId,
                selection.uuid,
                WhiteboardElementAttributeKey.DASH_ARRAY.key,
                arrayOf(2, 2, 2)
            )
            updateDashSelection(true, false, false)
        }

        dash2.setOnClickListener {
            whiteboardApp?.setElementAttribute(
                selection.layerId,
                selection.uuid,
                WhiteboardElementAttributeKey.DASH_ARRAY.key,
                arrayOf(8, 8, 8)
            )
            updateDashSelection(false, true, false)
        }

        dash3.setOnClickListener {
            whiteboardApp?.setElementAttribute(
                selection.layerId,
                selection.uuid,
                WhiteboardElementAttributeKey.DASH_ARRAY.key,
                emptyArray<Int>()
            )
            updateDashSelection(false, false, true)
        }

        // Get current dash array and update UI
        whiteboardApp?.getElementAttribute(
            selection.layerId,
            selection.uuid,
            WhiteboardElementAttributeKey.DASH_ARRAY.key,
            object : RoomCallback<Any> {
                override fun onSuccess(result: Any) {
                    if (result is JSONArray) {
                        val dashArray = IntArray(result.length()) { index -> result.optInt(index) }
                        updateDashSelection(
                            dashArray.contentEquals(intArrayOf(2, 2, 2)),
                            dashArray.contentEquals(intArrayOf(8, 8, 8)),
                            dashArray.isEmpty()
                        )
                    }
                }

                override fun onFailure(error: RoomError) {}
            })
    }

    private fun setupArrowStyle(selection: WhiteboardElementSelection) {
        val headArrow = binding.ivArrowStyleHead
        val tailArrow = binding.ivArrowStyleTail

        // Get current arrow styles
        whiteboardApp?.getElementAttribute(
            selection.layerId,
            selection.uuid,
            WhiteboardElementAttributeKey.HEAD_ARROW.key,
            object : RoomCallback<Any> {
                override fun onSuccess(result: Any) {
                    headArrow.isSelected = result == "normal"
                }

                override fun onFailure(error: RoomError) {}
            })

        whiteboardApp?.getElementAttribute(
            selection.layerId,
            selection.uuid,
            WhiteboardElementAttributeKey.TAIL_ARROW.key,
            object : RoomCallback<Any> {
                override fun onSuccess(result: Any) {
                    tailArrow.isSelected = result == "normal"
                }

                override fun onFailure(error: RoomError) {}
            })

        headArrow.setOnClickListener {
            val selected = !headArrow.isSelected
            whiteboardApp?.setElementAttribute(
                selection.layerId,
                selection.uuid,
                WhiteboardElementAttributeKey.HEAD_ARROW.key,
                if (selected) "normal" else "none"
            )
            headArrow.isSelected = selected
        }

        tailArrow.setOnClickListener {
            val selected = !tailArrow.isSelected
            whiteboardApp?.setElementAttribute(
                selection.layerId,
                selection.uuid,
                WhiteboardElementAttributeKey.TAIL_ARROW.key,
                if (selected) "normal" else "none"
            )
            tailArrow.isSelected = selected
        }
    }

    private fun createColorView(color: String, onClick: () -> Unit): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(context.dp2Px(32f), context.dp2Px(32f))
            setBackgroundColor(Color.parseColor(color))
            setOnClickListener { onClick() }
        }
    }

    private fun updateDashSelection(dash1Selected: Boolean, dash2Selected: Boolean, dash3Selected: Boolean) {
        binding.dash1.isSelected = dash1Selected
        binding.dash2.isSelected = dash2Selected
        binding.dash3.isSelected = dash3Selected
    }

    fun hide() {
        isVisible = false
    }
} 
