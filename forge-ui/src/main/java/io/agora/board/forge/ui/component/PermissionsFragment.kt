package io.agora.board.forge.ui.component

import android.Manifest
import android.R
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

/**
 * author : fenglibin
 * date : 2024/8/5
 * description : 权限申请 fragment
 */
internal class PermissionsFragment : Fragment() {

    private lateinit var permissionsRequired: Array<String>

    private var onGranted: (() -> Unit)? = null

    private var onDenied: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions(requireContext(), permissionsRequired)) {
            activityResultLauncher.launch(permissionsRequired)
        } else {
            onGranted?.invoke()
            removeFragment()
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionGranted = permissions.entries.all { it.value }
            if (!permissionGranted) {
                onDenied?.invoke()
            } else {
                onGranted?.invoke()
            }
            removeFragment()
        }

    private fun removeFragment() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    companion object {
        fun hasPermissions(context: Context, permissions: Array<String>) = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        fun show(
            activity: FragmentActivity,
            permissions: Array<String>,
            onGranted: () -> Unit,
            onDenied: () -> Unit,
        ) {
            val fragment = PermissionsFragment()
            fragment.permissionsRequired = permissions
            fragment.onGranted = onGranted
            fragment.onDenied = onDenied
            val ref = WeakReference(activity)
            val fragmentTransaction = ref.get()?.supportFragmentManager?.beginTransaction()
            fragmentTransaction?.let {
                fragmentTransaction.add(R.id.content, fragment).addToBackStack(null).commitAllowingStateLoss()
            }
        }

        fun hasSaveImagePermissions(context: Context) = Build.VERSION.SDK_INT > Build.VERSION_CODES.P || hasPermissions(
            context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        )

        fun showSaveImagePermission(
            activity: FragmentActivity,
            onGranted: () -> Unit,
            onDenied: () -> Unit,
        ) {
            show(
                activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), onGranted, onDenied
            )
        }
    }
}
