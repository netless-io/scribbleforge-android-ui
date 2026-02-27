package io.agora.board.forge.ui.internal

import android.content.Context

enum class DeviceOrientation {
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
        fun get(context: Context): DeviceOrientation {
            val tablet = FoundationUtils.isTablet(context)
            val portrait = FoundationUtils.isPortrait(context)

            return if (tablet) {
                if (portrait) TabletPortrait else TabletLandscape
            } else {
                if (portrait) PhonePortrait else PhoneLandscape
            }
        }
    }
}
