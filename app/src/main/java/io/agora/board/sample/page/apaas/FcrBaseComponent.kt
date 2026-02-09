package io.agora.board.sample.page.apaas

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import io.agora.board.forge.ui.component.FcrFoundationUtils
import io.agora.board.forge.ui.contract.BoardRoomControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * author : felix
 * date : 2024/3/26
 * description :
 */
abstract class FcrBaseComponent : FrameLayout, IComponent<IUIProvider> {
    protected lateinit var provider: IUIProvider
    protected lateinit var boardRoomControl: BoardRoomControl
    protected var isPortrait = FcrFoundationUtils.isPortrait(context)
    protected val mainScope = CoroutineScope(Dispatchers.Main)

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    override fun initView(provider: IUIProvider) {
        this.provider = provider
        this.boardRoomControl = provider.getBoardControl()!!
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        isPortrait = newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT
    }
}

interface IUIProvider {
    fun getActivityPage(): FragmentActivity

    fun getBoardControl(): BoardRoomControl?
}

interface IComponent<T> {
    fun initView(provider: T)

    fun release()
}
