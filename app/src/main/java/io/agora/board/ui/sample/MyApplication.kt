package io.agora.board.ui.sample

import android.app.Application
import android.os.Build
import android.webkit.WebView
import com.tencent.bugly.crashreport.CrashReport
import io.agora.board.ui.sample.util.KvStore

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        WebView.setWebContentsDebuggingEnabled(true)
        KvStore.init(this)

        if (!isRunningOnEmulator()) {
            CrashReport.initCrashReport(this, "876fa5c68c", true)
        }
    }

    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.lowercase().contains("vbox") // VirtualBox
            || Build.FINGERPRINT.lowercase().contains("test-keys")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
            || Build.PRODUCT.contains("google_sdk")
            || Build.HARDWARE.contains("goldfish") // QEMU
            || Build.HARDWARE.contains("ranchu")   // QEMU (newer Android)
            || Build.HARDWARE.contains("vbox86")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator"))
    }
}
