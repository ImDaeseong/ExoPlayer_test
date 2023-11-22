package com.daeseong.simple1exoplayer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import java.util.TimerTask;

public class MarqueeTask extends TimerTask {

    private Handler handler;
    private TextView textView;
    private boolean isFlag = false;

    public MarqueeTask(TextView textView) {
        this.textView = textView;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {
        handler.post(() -> marquee(textView, isFlag));
    }

    private void marquee(View view, boolean isFlag) {
        ObjectAnimator animator1, animator2;

        if (isFlag) {
            animator1 = ObjectAnimator.ofFloat(view, "translationX", 0f, -1000f);
            animator2 = ObjectAnimator.ofFloat(view, "translationX", 1000f, 0f);
        } else {
            animator1 = ObjectAnimator.ofFloat(view, "translationX", 0f, 1000f);
            animator2 = ObjectAnimator.ofFloat(view, "translationX", -1000f, 0f);
        }

        animator1.setDuration(500);
        animator2.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animator1, animator2);
        animatorSet.start();

        this.isFlag = !isFlag;
    }
}
