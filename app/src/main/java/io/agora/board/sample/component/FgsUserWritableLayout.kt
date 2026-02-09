package io.agora.board.sample.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.board.forge.Room
import io.agora.board.forge.RoomUser
import io.agora.board.forge.SocketProvider
import io.agora.board.forge.UserListener
import io.agora.board.forge.WritableListener
import io.agora.board.forge.sample.databinding.FgsUserWritableItemBinding
import io.agora.board.forge.sample.databinding.FgsUserWritableLayoutBinding
import io.agora.board.sample.util.DebouncedUpdater

/**
 * 用户权限管理组件
 * 显示房间内所有用户的读写权限状态，并允许管理员控制权限
 */
class FgsUserWritableLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    data class UserWritableItem(
        val user: RoomUser,
        val isWritable: Boolean
    )

    private val binding = FgsUserWritableLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private val adapter = UserWritableAdapter(onWritableToggle = ::onWritableToggle)

    private val userListUpdater = DebouncedUpdater { updateUserList() }

    private var roomRef: Room? = null

    init {
        binding.rvUserList.layoutManager = LinearLayoutManager(context)
        binding.rvUserList.adapter = adapter
    }

    // private val userListener = object : UserListener {
    //     override fun onUserJoined(user: RoomUser) = userListUpdater.invoke()
    //     override fun onUserLeft(user: RoomUser) = userListUpdater.invoke()
    // }

    private val roomSocketListener = object : SocketProvider.Listener {
        override fun onUserJoined(userId: String) = userListUpdater.invoke()
        override fun onUserLeft(userId: String) = userListUpdater.invoke()
    }

    private val writableListener = object : WritableListener {
        override fun onWritableChanged(userId: String, writable: Boolean) = userListUpdater.invoke()
    }

    fun attachRoom(room: Room) {
        roomRef = room
        room.socketProvider.addListener(roomSocketListener)
        // room.userManager.addListener(userListener)
        room.addWritableListener(writableListener)
        updateUserList()
    }

    fun detachRoom() {
        roomRef?.let { room ->
            // room.userManager.removeListener(userListener)
            room.socketProvider.removeListener(roomSocketListener)
            room.removeWritableListener(writableListener)
        }
        roomRef = null
    }

    private fun onWritableToggle(item: UserWritableItem, writable: Boolean) {
        roomRef?.setWritable(item.user.id, writable)
    }

    private fun updateUserList() {
        val room = roomRef ?: return
        val users = room.userManager.getUsers().associateBy { it.id }

        val userItems = room.socketProvider.getUsers().map { userId ->
            val user = users[userId] ?: RoomUser(id = userId, online = false)
            UserWritableItem(user, room.isWritable(userId))
        }

        adapter.updateUsers(userItems)
    }
}

class UserWritableAdapter(
    private val onWritableToggle: (FgsUserWritableLayout.UserWritableItem, Boolean) -> Unit
) : RecyclerView.Adapter<UserWritableAdapter.UserViewHolder>() {

    private val items = mutableListOf<FgsUserWritableLayout.UserWritableItem>()

    inner class UserViewHolder(val binding: FgsUserWritableItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = FgsUserWritableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvUserId.text = buildString {
                append(item.user.id)
                item.user.nickName?.takeIf { it.isNotEmpty() }?.let {
                    append(" ($it)")
                }
            }
            tvUserStatus.text = if (item.user.online) "在线" else "离线"
            tvWritableStatus.text = if (item.isWritable) "可写" else "只读"
            tvWritableStatus.setTextColor(
                if (item.isWritable) 0xFF4CAF50.toInt() else 0xFFFF5722.toInt()
            )

            switchWritable.setOnCheckedChangeListener(null)
            switchWritable.isChecked = item.isWritable
            switchWritable.setOnCheckedChangeListener { _, isChecked ->
                onWritableToggle(item, isChecked)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateUsers(newItems: List<FgsUserWritableLayout.UserWritableItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
