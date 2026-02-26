package io.agora.board.forge.ui.whiteboard.component


import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.cardview.widget.CardView

/**
 * author : fenglibin
 * date : 2024/6/28
 * description : 兼容 Android 29（Android Q）以前，View ClipToOutline 底层实现不处理 radius 超过一半宽高情况。
 */
class FcrCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val maxRadius = minOf(w.toFloat() / 2, h.toFloat() / 2)
            if (radius > maxRadius) {
                radius = maxRadius
            }
        }
    }

    private fun minOf(a: Float, b: Float): Float {
        return if (a < b) a else b
    }
}
