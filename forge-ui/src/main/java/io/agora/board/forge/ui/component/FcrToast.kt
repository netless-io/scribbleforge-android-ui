package io.agora.board.forge.ui.component

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import io.agora.board.forge.ui.R

/**
 * author : felix
 * date : 2024/4/07
 * description : 用于显示居中Toast提示。
 */
internal object FcrToast {
    private const val LEVEL_NORMAL = 1
    private const val LEVEL_INFO = 2
    private const val LEVEL_ALARM = 3

    private var BASE_CORNER = 8f

    const val LENGTH_SHORT = 0

    fun normal(context: Context, textResId: Int, duration: Int = LENGTH_SHORT) {
        showToast(context.applicationContext, LEVEL_NORMAL, context.getString(textResId), duration)
    }

    fun normal(context: Context, text: String, duration: Int = LENGTH_SHORT) {
        showToast(context.applicationContext, LEVEL_NORMAL, text, duration)
    }

    fun info(context: Context, textResId: Int, duration: Int = LENGTH_SHORT) {
        showToast(context.applicationContext, LEVEL_INFO, context.getString(textResId), duration)
    }

    fun info(context: Context, text: String, duration: Int = LENGTH_SHORT) {
        showToast(context.applicationContext, LEVEL_INFO, text, duration)
    }

    fun alarm(context: Context, textResId: Int, duration: Int = LENGTH_SHORT) {
        showToast(context.applicationContext, LEVEL_ALARM, context.getString(textResId), duration)
    }

    fun alarm(context: Context, text: String, duration: Int = LENGTH_SHORT) {
        showToast(context.applicationContext, LEVEL_ALARM, text, duration)
    }

    private fun showToast(context: Context, level: Int, text: String, duration: Int) {
        BASE_CORNER = context.resources.getDimension(R.dimen.fcr_cornerradius_round)

        ContextCompat.getMainExecutor(context).execute {
            val toastLayout = LayoutInflater.from(context).inflate(
                R.layout.fcr_ui_toast, null, false
            )

            toastLayout.findViewById<ImageView>(R.id.agora_toast_icon)?.let { icon ->
                getToastIconRes(level)?.let { iconRes ->
                    icon.visibility = View.VISIBLE
                    icon.setImageResource(iconRes)
                }
            }

            toastLayout.findViewById<TextView>(R.id.agora_toast_message)?.let { msgView ->
                msgView.setTextColor(ContextCompat.getColor(context, R.color.fcr_ui_scene_inverse))
                msgView.text = text
            }

            toastLayout.findViewById<ViewGroup>(R.id.agora_toast_layout)?.let { layout ->
                // val screenDensity = context.resources.displayMetrics.density
                // val minWidth = (100 * screenDensity).toInt()
                // layout.minimumWidth = minWidth

                buildToastBgDrawable(context, level)?.let { drawable ->
                    layout.background = drawable
                }
            }

            val toast = Toast(context.applicationContext)
            toast.view = toastLayout
            toast.duration = duration
            toast.setGravity(Gravity.FILL, 0, 0)
            toast.show()
        }
    }

    private fun buildToastBgDrawable(context: Context, level: Int): Drawable? {
        val bgColor: Int = when (level) {
            LEVEL_NORMAL -> ContextCompat.getColor(context, R.color.fcr_mobile_ui_scene_toast1)
            LEVEL_INFO -> ContextCompat.getColor(context, R.color.fcr_ui_scene_ramp_brand6)
            LEVEL_ALARM -> ContextCompat.getColor(context, R.color.fcr_ui_scene_ramp_red6)
            else -> return null
        }

        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(bgColor)
            cornerRadius = BASE_CORNER
        }
    }

    private fun getToastIconRes(level: Int): Int? = when (level) {
        LEVEL_NORMAL -> null
        LEVEL_INFO -> null
        LEVEL_ALARM -> null
        else -> null
    }
}
