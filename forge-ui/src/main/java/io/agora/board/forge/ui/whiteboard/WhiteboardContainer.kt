package io.agora.board.forge.ui.whiteboard

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.ForgeUIConfig
import io.agora.board.forge.ui.internal.setForgeConfig

class WhiteboardContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, forgeUIConfig: ForgeUIConfig) : this(context) {
        setForgeConfig(forgeUIConfig)
    }

    internal val whiteboardViewContainer: FrameLayout
    internal val whiteboardControlLayout: WhiteboardControlLayout

    init {
        inflate(context, R.layout.fui_whiteboard_container, this)

        whiteboardViewContainer = findViewById(R.id.whiteboard_view_container)
        whiteboardControlLayout = findViewById(R.id.whiteboard_control_layout)
    }

    fun addWhiteboardView(view: View) {
        whiteboardViewContainer.removeAllViews()
        whiteboardViewContainer.addView(
            view, LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
            )
        )
    }
}
