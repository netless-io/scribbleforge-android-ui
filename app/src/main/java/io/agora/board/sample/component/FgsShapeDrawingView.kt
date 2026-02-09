package io.agora.board.sample.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

enum class ShapeType {
    TRIANGLE, CIRCLE, RECTANGLE, OVAL
}

data class ShapeConfig(
    val xPercent: Float, // 0.0 ~ 1.0
    val yPercent: Float, // 0.0 ~ 1.0
    val shapeType: ShapeType, // 图形类型
    val size: Float = 0.1f, // 使用比例值，默认0.1
    val color: Int = Color.BLUE // 新增颜色字段，默认蓝色
)

class FgsShapeDrawingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var shapeList: List<ShapeConfig> = emptyList()
        set(value) {
            field = value
            invalidate() // 触发重绘
        }

    private val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        for (shape in shapeList) {
            val cx = shape.xPercent * width
            val cy = shape.yPercent * height
            val size = shape.size * minOf(width, height)

            paint.color = shape.color

            when (shape.shapeType) {
                ShapeType.CIRCLE -> {
                    canvas.drawCircle(cx, cy, size / 2, paint)
                }

                ShapeType.RECTANGLE -> {
                    canvas.drawRect(
                        cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2, paint
                    )
                }

                ShapeType.OVAL -> {
                    val rect = RectF(cx - size, cy - size / 2, cx + size, cy + size / 2)
                    canvas.drawOval(rect, paint)
                }

                ShapeType.TRIANGLE -> {
                    val path = Path().apply {
                        moveTo(cx, cy - size / 2) // top
                        lineTo(cx - size / 2, cy + size / 2) // bottom-left
                        lineTo(cx + size / 2, cy + size / 2) // bottom-right
                        close()
                    }
                    canvas.drawPath(path, paint)
                }
            }
        }
    }
}
