package io.agora.board.forge.ui.internal

import android.content.Context
import android.content.res.Configuration
import io.agora.board.forge.ui.R

object FoundationUtils {
    /**
     * 是否是平板设备
     */
    fun isTablet(context: Context): Boolean {
        return context.resources.getBoolean(R.bool.fui_isTablet)
    }

    /**
     * 屏幕竖屏
     */
    fun isPortrait(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * dp 转 px
     */
    fun dp2px(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    /**
     * dp 转 px (Float)
     */
    fun dp2pxFloat(context: Context, dp: Float): Float {
        val density = context.resources.displayMetrics.density
        return dp * density
    }

    /**
     * px 转 dp
     */
    fun px2dp(context: Context, px: Int): Float {
        val density = context.resources.displayMetrics.density
        return px / density
    }

    /**
     * px 转 dp (Int)
     */
    fun px2dpInt(context: Context, px: Int): Int {
        val density = context.resources.displayMetrics.density
        return (px / density + 0.5f).toInt()
    }
}
