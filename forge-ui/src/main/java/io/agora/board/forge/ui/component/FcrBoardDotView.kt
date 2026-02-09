package io.agora.board.forge.ui.component


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import io.agora.board.forge.ui.R
import kotlin.math.min

/**
 * author : fenglibin
 * date : 2024/7/2
 * description : 白板工具属性选择面板中的颜色选择圆点
 */
class FcrBoardDotView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dotColor: Int
    private var dotSize: Float
    private var circleColor: Int
    private var circleWidth: Float
    private var isSelected: Boolean = false

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FcrBoardStrokeWidthView).apply {
            try {
                dotColor = getColor(R.styleable.FcrBoardStrokeWidthView_dotColor, Color.BLACK)
                dotSize = getDimension(R.styleable.FcrBoardStrokeWidthView_dotSize, 10f)
                circleColor = getColor(R.styleable.FcrBoardStrokeWidthView_circleColor, Color.CYAN)
                circleWidth = getDimension(R.styleable.FcrBoardStrokeWidthView_circleWidth, 2f)
            } finally {
                recycle()
            }
        }

        dotPaint.color = dotColor
        circlePaint.color = circleColor
        circlePaint.strokeWidth = circleWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f

        canvas.drawCircle(centerX, centerY, dotSize / 2f, dotPaint)

        if (isSelected) {
            val radius = min(width / 2f, height / 2f) - 8f - circleWidth / 2f
            canvas.drawCircle(centerX, centerY, radius, circlePaint)
        }
    }

    fun setDotColor(color: Int) {
        dotColor = color
        dotPaint.color = dotColor
        invalidate()
    }

    fun getDotColor(): Int = dotColor

    fun setDotSize(size: Float) {
        dotSize = size
        invalidate()
    }

    fun getDotSize(): Float = dotSize

    fun setCircleColor(color: Int) {
        circleColor = color
        circlePaint.color = circleColor
        invalidate()
    }

    override fun setSelected(selected: Boolean) {
        isSelected = selected
        invalidate()
    }
}
