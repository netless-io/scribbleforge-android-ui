package io.agora.board.forge.ui.component.state

import android.content.Context
import io.agora.board.forge.ui.theme.ForgeUiDefaults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class WhiteboardStateStore(context: Context) {

    private val _state = MutableStateFlow(
        WhiteboardUiState(
            drawState = ForgeUiDefaults.defaultDrawState(context),
            layoutState = LayoutState(),
        )
    )

    val state: StateFlow<WhiteboardUiState> = _state

    fun dispatch(action: WhiteboardUiAction) {
        _state.update { old ->
            reduce(old, action)
        }
    }

    private fun reduce(old: WhiteboardUiState, action: WhiteboardUiAction): WhiteboardUiState {
        return when (action) {
            is WhiteboardUiAction.ChangeTool -> {
                old.copy(
                    drawState = old.drawState.copy(toolType = action.tool)
                )
            }

            is WhiteboardUiAction.ChangeStrokeColor -> {
                old.copy(
                    drawState = old.drawState.copy(strokeColor = action.color)
                )
            }

            is WhiteboardUiAction.ChangeStrokeWidth -> {
                old.copy(
                    drawState = old.drawState.copy(strokeWidth = action.width)
                )
            }

            is WhiteboardUiAction.ChangeBackground -> {
                old.copy(
                    drawState = old.drawState.copy(backgroundColor = action.color),
                    layoutState = old.layoutState.copy(bgPickShown = false)
                )
            }

            WhiteboardUiAction.ShowToolPanel -> {
                old.copy(
                    layoutState = old.layoutState.copy(
                        toolShown = true,
                        strokeShown = false,
                        downloadShown = false,
                        bgPickShown = false
                    )
                )
            }

            WhiteboardUiAction.ToggleToolPanel -> {
                old.copy(
                    layoutState = old.layoutState.copy(
                        toolShown = !old.layoutState.toolShown,
                        strokeShown = false,
                        downloadShown = false,
                        bgPickShown = false
                    )
                )
            }

            WhiteboardUiAction.ToggleStrokePanel -> {
                old.copy(
                    layoutState = old.layoutState.copy(
                        strokeShown = !old.layoutState.strokeShown,
                        toolShown = false,
                        downloadShown = false,
                        bgPickShown = false
                    )
                )
            }

            WhiteboardUiAction.ToggleDownloadPanel -> {
                old.copy(
                    layoutState = old.layoutState.copy(
                        downloadShown = true, toolShown = false, strokeShown = false, bgPickShown = false
                    )
                )
            }

            WhiteboardUiAction.ToggleBgPanel -> {
                old.copy(
                    layoutState = old.layoutState.copy(
                        bgPickShown = !old.layoutState.bgPickShown,
                        toolShown = false,
                        strokeShown = false,
                        downloadShown = false
                    )
                )
            }

            WhiteboardUiAction.HideBgPanel -> {
                old.copy(
                    layoutState = old.layoutState.copy(
                        bgPickShown = false
                    )
                )
            }

            WhiteboardUiAction.HideAllPanel -> {
                old.copy(
                    layoutState = LayoutState(
                        toolShown = false,
                        strokeShown = false,
                        downloadShown = false,
                        bgPickShown = false
                    )
                )
            }

            WhiteboardUiAction.StartDownloading -> {
                old.copy(isDownloading = true)
            }

            WhiteboardUiAction.FinishDownloading, WhiteboardUiAction.DownloadFailed -> {
                old.copy(
                    isDownloading = false, layoutState = old.layoutState.copy(downloadShown = false)
                )
            }

            is WhiteboardUiAction.SyncToolFromWhiteboard -> {
                old.copy(
                    drawState = old.drawState.copy(toolType = action.tool)
                )
            }

            is WhiteboardUiAction.UpdateUndoRedo -> {
                old.copy(
                    drawState = old.drawState.copy(
                        undo = action.undo,
                        redo = action.redo
                    ),
                )
            }
        }
    }
}
