package com.mobcleaner.mcapp.transition

import android.animation.ObjectAnimator
import android.widget.ProgressBar

fun ProgressBar.animateProgress(toProgress: Int, duration: Long = 700) {
    val animator = ObjectAnimator.ofInt(this, "progress", this.progress, toProgress)
    animator.duration = duration
    animator.start()
}