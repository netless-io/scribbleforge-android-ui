package io.agora.board.forge.ui.component

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible

fun View.animateHide(animationResId: Int) {
    if (!isVisible) return
    // 此处需要设置为可见，否则动画不会执行，导致显示状态不正确
    this.isVisible = true

    val animation = AnimationUtils.loadAnimation(context, animationResId)
    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {}

        override fun onAnimationEnd(animation: Animation) {
            this@animateHide.isVisible = false
        }

        override fun onAnimationRepeat(animation: Animation) {}
    })
    this.startAnimation(animation)
}

fun View.animateShow(animationResId: Int) {
    if (isVisible) return
    // 此处需要设置为可见，否则动画不会执行，导致显示状态不正确
    this.isVisible = true

    val animation = AnimationUtils.loadAnimation(context, animationResId)
    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {}

        override fun onAnimationEnd(animation: Animation) {}

        override fun onAnimationRepeat(animation: Animation) {}
    })
    this.startAnimation(animation)
}
