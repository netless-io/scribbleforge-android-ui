package io.agora.board.forge.ui.whiteboard.state

import android.content.Context
import io.agora.board.forge.ui.theme.ForgeUiDefaults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class WhiteboardStateStore(context: Context) {

    private val _state = MutableStateFlow(ForgeUiDefaults.defaultWhiteboardUiState(context))

    val state: StateFlow<WhiteboardUiState> = _state

    fun dispatch(action: WhiteboardUiAction) {
        _state.update { old ->
            reduce(old, action)
        }
    }

    private fun reduce(old: WhiteboardUiState, action: WhiteboardUiAction): WhiteboardUiState {
        return when (action) {
            is WhiteboardUiAction.ChangeTool -> old.copy(toolType = action.tool)
            is WhiteboardUiAction.ChangeStrokeColor -> old.copy(strokeColor = action.color)
            is WhiteboardUiAction.ChangeStrokeWidth -> old.copy(strokeWidth = action.width)
            is WhiteboardUiAction.ChangeBackground -> old.copy(backgroundColor = action.color)
            is WhiteboardUiAction.UpdateUndoRedo -> old.copy(undo = action.undo, redo = action.redo)
            
            is WhiteboardUiAction.WritableChanged -> old.copy(canDraw = action.writable)
        }
    }
}
