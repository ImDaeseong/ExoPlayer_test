package com.daeseong.simple1exoplayer

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import java.util.*

class MarqueeTask(private val textView: TextView) : TimerTask() {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var isFlag = false

    override fun run() {
        handler.post { marquee(textView, isFlag) }
    }

    private fun marquee(view: View, isFlag: Boolean) {
        val animator1: ObjectAnimator
        val animator2: ObjectAnimator

        if (isFlag) {
            animator1 = ObjectAnimator.ofFloat(view, "translationX", 0f, -1000f)
            animator2 = ObjectAnimator.ofFloat(view, "translationX", 1000f, 0f)
        } else {
            animator1 = ObjectAnimator.ofFloat(view, "translationX", 0f, 1000f)
            animator2 = ObjectAnimator.ofFloat(view, "translationX", -1000f, 0f)
        }

        animator1.duration = 500
        animator2.duration = 500

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(animator1, animator2)
        animatorSet.start()

        this.isFlag = !isFlag
    }
}
