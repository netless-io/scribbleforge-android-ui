package io.agora.board.forge.ui

import io.agora.board.forge.ui.theme.ForgeUiDefaultProvider
import io.agora.board.forge.ui.theme.ForgeUiProvider

data class ForgeUIConfig(
    val provider: ForgeUiProvider = ForgeUiDefaultProvider()
)
