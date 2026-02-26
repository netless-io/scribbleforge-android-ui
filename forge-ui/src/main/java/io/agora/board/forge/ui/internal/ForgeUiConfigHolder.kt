package io.agora.board.forge.ui.internal

import android.view.View
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.ForgeUIConfig

private val CONFIG_TAG_KEY = R.id.forge_ui_config_key

fun View.setForgeConfig(config: ForgeUIConfig) {
    setTag(CONFIG_TAG_KEY, config)
}

fun View.findForgeConfig(): ForgeUIConfig {
    var current: View? = this
    while (current != null) {
        val scope = current.getTag(CONFIG_TAG_KEY) as? ForgeUIConfig
        if (scope != null) return scope
        current = current.parent as? View
    }
    error("ForgeScope not found")
}
