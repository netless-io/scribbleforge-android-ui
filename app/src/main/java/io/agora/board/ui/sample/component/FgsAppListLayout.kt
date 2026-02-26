package io.agora.board.ui.sample.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import io.agora.board.forge.ApplicationListener
import io.agora.board.forge.DocManager
import io.agora.board.forge.Room
import io.agora.board.forge.ui.sample.databinding.FgsAppListItemBinding
import io.agora.board.forge.ui.sample.databinding.FgsAppListLayoutBinding
import io.agora.board.ui.sample.util.DebouncedUpdater
import io.agora.board.ui.sample.util.PermissionHelper

/**
 * A layout that displays a list of applications in a room.
 * It shows the app ID and type, and allows the user to close apps.
 */
class FgsAppListLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    data class AppItem(
        val appId: String,
        val appType: String,
        val source: Source,
        val sourceId: String? = null,
    ) {
        enum class Source { ROOM, MANAGER, INVALID }
    }

    private val binding = FgsAppListLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private val adapter = AppListAdapter(
        onItemCloseClick = ::onCloseClick,
        onItemPermissionClick = ::onPermissionClick
    )

    private val appListUpdater = DebouncedUpdater { updateAppList() }

    private var roomRef: Room? = null

    init {
        binding.rvAppList.adapter = adapter
    }

    private val appListener = object : ApplicationListener {
        override fun onAppLaunch(appId: String) = appListUpdater.invoke()
        override fun onAppTerminate(appId: String) = appListUpdater.invoke()
    }

    private val docListener = object : DocManager.Listener {
        override fun onDocAdded(docId: String) = appListUpdater.invoke()
        override fun onDocRemoved(docId: String) = appListUpdater.invoke()
        override fun onDocRelaunch(guid: String, publisher: String) = appListUpdater.invoke()
    }

    fun attachRoom(room: Room) {
        roomRef = room
        room.addAppListener(appListener)
        room.windowManager?.addAppListener(appListener)
        room.docManager.addListener(docListener)
    }

    fun detachRoom() {
        roomRef?.apply {
            removeAppListener(appListener)
            windowManager?.removeAppListener(appListener)
            docManager.removeListener(docListener)
        }
        roomRef = null
        adapter.updateApps(emptyList())
    }

    private fun onCloseClick(item: AppItem) {
        when (item.source) {
            AppItem.Source.ROOM -> roomRef?.appManager?.terminalApp(item.appId)
            AppItem.Source.MANAGER -> roomRef?.windowManager?.terminalApp(item.appId)
            AppItem.Source.INVALID -> roomRef?.docManager?.deleteDocAsync(item.appId)
        }
        adapter.removeApp(item.appId)
    }

    private fun onPermissionClick(item: AppItem) {
        val room = roomRef ?: return
        val userIds = room.userManager.userIds().toList()
        val currentUserId = room.userManager.userId

        PermissionHelper.showUserListDialog(context, userIds, currentUserId) { selectedUserId ->
            PermissionHelper.showPermissionConfigDialog(context, roomRef!!, selectedUserId, item.appId, item.appType)
        }
    }

    private fun updateAppList() {
        val roomApps = roomRef?.appManager?.getApps()?.map {
            AppItem(it.appId, it::class.java.simpleName, AppItem.Source.ROOM)
        }.orEmpty()

        val managerApps = roomRef?.windowManager?.appManager?.getApps()?.map {
            AppItem(it.appId, it::class.java.simpleName, AppItem.Source.MANAGER, "Manager")
        }.orEmpty()

        val invalidApps = roomRef?.docManager?.invalidSubDocIds()?.map {
            AppItem(it, "Invalid Doc", AppItem.Source.INVALID, "DocManager")
        }.orEmpty()

        adapter.updateApps(roomApps + managerApps + invalidApps)
    }
}

class AppListAdapter(
    private val onItemCloseClick: (FgsAppListLayout.AppItem) -> Unit,
    private val onItemPermissionClick: (FgsAppListLayout.AppItem) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private val items = mutableListOf<FgsAppListLayout.AppItem>()

    inner class AppViewHolder(val binding: FgsAppListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = FgsAppListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvAppId.text = item.appId
            tvAppType.text =
                item.appType + if (item.source == FgsAppListLayout.AppItem.Source.MANAGER) "(${item.sourceId})" else ""
            
            // 显示关闭按钮
            ivAppClose.visibility = View.VISIBLE
            ivAppClose.setOnClickListener { onItemCloseClick(item) }
            
            // 显示权限图标（仅对非 INVALID 类型的应用显示）
            if (item.source != FgsAppListLayout.AppItem.Source.INVALID) {
                ivAppPermission.visibility = View.VISIBLE
                ivAppPermission.setOnClickListener { onItemPermissionClick(item) }
            } else {
                ivAppPermission.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateApps(newItems: List<FgsAppListLayout.AppItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeApp(appId: String) {
        val index = items.indexOfFirst { it.appId == appId }
        if (index >= 0) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
