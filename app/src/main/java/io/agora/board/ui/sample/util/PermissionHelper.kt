package io.agora.board.ui.sample.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.common.BasePermission
import io.agora.board.forge.imagerydoc.ImageryDocApplication
import io.agora.board.forge.imagerydoc.ImageryDocPermission
import io.agora.board.forge.slide.SlideApplication
import io.agora.board.forge.slide.SlidePermission
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardPermission
import io.agora.board.forge.windowmanager.WindowManagerPermission

/**
 * 权限管理帮助类，提供统一的权限管理功能
 */
object PermissionHelper {

    /**
     * 显示用户选择对话框
     */
    @SuppressLint("SetTextI18n")
    fun showUserListDialog(
        context: Context,
        userIds: List<String>,
        currentUserId: String,
        onUserSelected: (String) -> Unit
    ) {
        var dialog: AlertDialog? = null

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 16)
        }

        userIds.forEach { userId ->
            layout.addView(TextView(context).apply {
                text = userId + if (userId == currentUserId) " (自己)" else ""
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, context.dp2Px(48f)
                ).apply { setMargins(16, 8, 16, 8) }
                gravity = Gravity.CENTER
                setOnClickListener {
                    onUserSelected(userId)
                    dialog?.dismiss()
                }
            })
        }

        val scrollView = ScrollView(context).apply {
            addView(layout)
        }

        dialog = AlertDialog.Builder(context)
            .setTitle("选择用户")
            .setView(scrollView)
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

    /**
     * 显示应用选择对话框
     */
    fun showAppSelectionDialog(
        context: Context,
        room: Room,
        userId: String,
        onAppSelected: (String, String) -> Unit
    ) {
        var apps = mutableListOf<Pair<String, String>>()

        // 添加房间应用
        room.appManager.getApps().forEach { app ->
            apps.add(app.appId to app::class.java.simpleName)
        }

        // 添加窗口管理器应用
        room.windowManager?.appManager?.getApps()?.forEach { app ->
            apps.add(app.appId to "${app::class.java.simpleName}(Manager)")
        }

        // 添加窗口管理器本身
        if (room.windowManager != null) {
            apps.add("WindowManager" to "WindowManager")
        }

        if (apps.isEmpty()) {
            showToast(context, "没有可管理权限的应用")
            return
        }

        var dialog: AlertDialog? = null

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 16)
        }

        apps.forEach { (appId, appType) ->
            layout.addView(TextView(context).apply {
                text = "$appType\n$appId"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, context.dp2Px(60f)
                ).apply { setMargins(16, 8, 16, 8) }
                gravity = Gravity.CENTER
                setOnClickListener {
                    onAppSelected(appId, appType)
                    dialog?.dismiss()
                }
            })
        }

        val scrollView = ScrollView(context).apply {
            addView(layout)
        }

        dialog = AlertDialog.Builder(context)
            .setTitle("选择应用")
            .setView(scrollView)
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

    /**
     * 显示权限配置对话框
     */
    fun showPermissionConfigDialog(
        context: Context,
        room: Room,
        userId: String,
        appId: String,
        appType: String
    ) {
        when {
            appId == "WindowManager" -> showWindowManagerPermissionDialog(context, room, userId, appId)
            appType.contains("WhiteboardApplication") -> showWhiteboardPermissionDialog(context, room, userId, appId)
            appType.contains("ImageryDocApplication") -> showImageryDocPermissionDialog(context, room, userId, appId)
            appType.contains("SlideApplication") -> showSlidePermissionDialog(context, room, userId, appId)
            else -> showToast(context, "不支持的应用类型: $appType")
        }
    }

    /**
     * 直接显示特定应用的权限配置对话框（用于 item 点击）
     */
    fun showAppPermissionDialog(
        context: Context,
        room: Room,
        userId: String,
        appId: String,
        appType: String
    ) {
        showPermissionConfigDialog(context, room, userId, appId, appType)
    }

    private fun showWindowManagerPermissionDialog(context: Context, room: Room, userId: String, appId: String) {
        val windowManager = room.windowManager
        if (windowManager == null) {
            showToast(context, "WindowManager 未启用")
            return
        }

        val permissionManager = windowManager.permissionManager
        val currentPermission = permissionManager.getPermission(userId)
        val permissionItems = listOf(
            WindowManagerPermission.OPERATE to "操作窗口"
        )

        showPermissionDialog(
            context = context,
            title = "WindowManager 权限设置",
            currentPermission = currentPermission,
            permissionItems = permissionItems,
            onPermissionSet = { newPermission ->
                permissionManager.setPermission(newPermission, userId)
                showToast(context, "权限设置成功")
            }
        )
    }

    private fun showWhiteboardPermissionDialog(
        context: Context,
        room: Room,
        userId: String,
        appId: String
    ) {
        val app = room.appManager.getApp(appId) as? WhiteboardApplication
            ?: room.windowManager?.appManager?.getApp(appId) as? WhiteboardApplication

        if (app == null) {
            showToast(context, "找不到白板应用")
            return
        }

        val permissionItems = listOf(
            WhiteboardPermission.DRAW to "DRAW(绘制元素)",
            WhiteboardPermission.EDIT_SELF to "EDIT_SELF(修改自己创建的元素)",
            WhiteboardPermission.EDIT_OTHERS to "EDIT_OTHERS(修改他人创建的元素)",
            WhiteboardPermission.DELETE_SELF to "DELETE_SELF(删除自己创建的元素)",
            WhiteboardPermission.DELETE_OTHERS to "DELETE_OTHERS(删除他人创建的元素)",
            WhiteboardPermission.MAIN_VIEW to "MAIN_VIEW(修改主视角)",
            WhiteboardPermission.SET_OTHERS_VIEW to "SET_OTHERS_VIEW(修改他人视角)"
        )

        app.getPermission(userId, object : RoomCallback<WhiteboardPermission> {
            override fun onSuccess(result: WhiteboardPermission) {
                showPermissionDialog(
                    context = context,
                    title = "设置${userId}的白板权限",
                    permissionItems = permissionItems,
                    currentPermission = result
                ) { newPermission ->
                    app.setPermission(userId, newPermission)
                }
            }

            override fun onFailure(error: RoomError) {
                showToast(context, "获取白板权限失败: $error")
            }
        })
    }

    private fun showImageryDocPermissionDialog(context: Context, room: Room, userId: String, appId: String) {
        val app = room.appManager.getApp(appId) as? ImageryDocApplication
            ?: room.windowManager?.appManager?.getApp(appId) as? ImageryDocApplication

        if (app !is ImageryDocApplication) {
            showToast(context, "无法找到 ImageryDoc 应用")
            return
        }

        val permissionManager = app.permissionManager
        if (permissionManager == null) {
            showToast(context, "权限管理器未初始化")
            return
        }

        val currentPermission = permissionManager.getPermission(userId)
        val permissionItems = listOf(
            ImageryDocPermission.SWITCH_PAGE to "切换页面",
            ImageryDocPermission.CAMERA to "操作相机",
            ImageryDocPermission.SIDE_BAR to "操作侧边栏"
        )

        showPermissionDialog(
            context = context,
            title = "ImageryDoc 权限设置",
            currentPermission = currentPermission,
            permissionItems = permissionItems,
            onPermissionSet = { newPermission ->
                permissionManager.setPermission(newPermission, userId)
                showToast(context, "权限设置成功")
            }
        )
    }

    private fun showSlidePermissionDialog(context: Context, room: Room, userId: String, appId: String) {
        val app = room.appManager.getApp(appId) as? SlideApplication
            ?: room.windowManager?.appManager?.getApp(appId) as? SlideApplication

        if (app !is SlideApplication) {
            showToast(context, "无法找到 Slide 应用")
            return
        }

        val permissionManager = app.permissionManager
        if (permissionManager == null) {
            showToast(context, "权限管理器未初始化")
            return
        }

        val currentPermission = permissionManager.getPermission(userId)
        val permissionItems = listOf(
            SlidePermission.CHANGE_STEP to "操作动画步骤",
            SlidePermission.CHANGE_PAGE to "切换页码",
            SlidePermission.CLICK_ANIM to "触发点击动画"
        )

        showPermissionDialog(
            context = context,
            title = "Slide 权限设置",
            currentPermission = currentPermission,
            permissionItems = permissionItems,
            onPermissionSet = { newPermission ->
                permissionManager.setPermission(newPermission, userId)
                showToast(context, "权限设置成功")
            }
        )
    }

    private fun <T : BasePermission> showPermissionDialog(
        context: Context,
        title: String,
        permissionItems: List<Pair<Int, String>>,
        currentPermission: T,
        onPermissionSet: (T) -> Unit
    ) {
        val itemStates = BooleanArray(permissionItems.size) { index ->
            currentPermission.hasPermission(permissionItems[index].first)
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 16)
        }

        permissionItems.forEachIndexed { index, (_, itemName) ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(16, 8, 16, 8) }
            }

            row.addView(TextView(context).apply {
                text = itemName
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
                gravity = Gravity.START
            })

            row.addView(CheckBox(context).apply {
                isChecked = itemStates[index]
                setOnCheckedChangeListener { _, isChecked -> itemStates[index] = isChecked }
            })

            layout.addView(row)
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton("确定") { dialog, _ ->
                // 创建新的权限对象
                val newFlags = permissionItems.indices
                    .filter { itemStates[it] }
                    .fold(0) { acc, index -> acc or permissionItems[index].first }

                @Suppress("UNCHECKED_CAST")
                val newPermission = when (currentPermission) {
                    is WhiteboardPermission -> WhiteboardPermission(newFlags) as T
                    is WindowManagerPermission -> WindowManagerPermission(newFlags) as T
                    is ImageryDocPermission -> ImageryDocPermission(newFlags) as T
                    is SlidePermission -> SlidePermission(newFlags) as T
                    else -> currentPermission
                }

                onPermissionSet(newPermission)
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 显示权限管理主对话框（选择用户后选择应用）
     */
    fun showPermissionManagementDialog(context: Context, room: Room) {
        val userIds = room.userManager.userIds().toList()
        val currentUserId = room.userManager.userId

        if (userIds.isEmpty()) {
            showToast(context, "没有用户在房间中")
            return
        }

        showUserListDialog(context, userIds, currentUserId) { selectedUserId ->
            showAppSelectionDialog(context, room, selectedUserId) { appId, appType ->
                showPermissionConfigDialog(context, room, selectedUserId, appId, appType)
            }
        }
    }
}
