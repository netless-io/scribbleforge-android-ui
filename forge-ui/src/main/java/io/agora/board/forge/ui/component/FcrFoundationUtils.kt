package io.agora.board.forge.ui.component

import android.content.Context
import android.content.res.Configuration
import io.agora.board.forge.ui.R

enum class FcrDeviceOrientation {
    /**
     * 平板竖屏
     */
    TabletPortrait,

    /**
     * 平板横屏
     */
    TabletLandscape,

    /**
     * 手机竖屏
     */
    PhonePortrait,

    /**
     * 手机横屏
     */
    PhoneLandscape;

    companion object {
        fun get(context: Context): FcrDeviceOrientation {
            val tablet = FcrFoundationUtils.isTablet(context)
            val portrait = FcrFoundationUtils.isPortrait(context)

            return if (tablet) {
                if (portrait) TabletPortrait else TabletLandscape
            } else {
                if (portrait) PhonePortrait else PhoneLandscape
            }
        }
    }
}

object FcrFoundationUtils {
    /**
     * 是否是平板设备
     */
    fun isTablet(context: Context): Boolean {
        return context.resources.getBoolean(R.bool.isTablet)
    }

    /**
     * 屏幕竖屏
     */
    fun isPortrait(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    fun getStatusBarHeight(activity: Context): Int {
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            activity.resources.getDimensionPixelSize(resourceId)
        } else activity.resources.getDimensionPixelOffset(R.dimen.fcr_system_status_bar_height)
    }
}
