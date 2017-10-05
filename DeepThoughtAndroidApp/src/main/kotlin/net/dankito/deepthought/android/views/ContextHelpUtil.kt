package net.dankito.deepthought.android.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.TextView
import net.dankito.deepthought.android.R


class ContextHelpUtil {

    companion object {
        private const val AnimationDurationMillis = 250L
    }


    fun showContextHelp(lytContextHelp: View, helpTextResourceId: Int) {
        showContextHelp(lytContextHelp, lytContextHelp.context.getString(helpTextResourceId))
    }

    fun showContextHelp(lytContextHelp: View, helpText: String) {
        val txtContextHelpText = lytContextHelp.findViewById(R.id.txtContextHelpText) as TextView
        txtContextHelpText.text = helpText

        val btnDismissContextHelp = lytContextHelp.findViewById(R.id.btnDismissContextHelp) as Button
        btnDismissContextHelp.setOnClickListener { animateHideContextHelp(lytContextHelp) }

        lytContextHelp.visibility = View.VISIBLE

        animateShowContextHelp(lytContextHelp)
    }

    // TODO: animation currently only works on top off screen, not in other places
    private fun animateShowContextHelp(lytContextHelp: View) {
        if (lytContextHelp.measuredHeight == 0) { // in this case we have to wait till height is determined -> set OnGlobalLayoutListener
            var layoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null // have to do it that complicated otherwise in OnGlobalLayoutListener we cannot access layoutListener variable
            layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                removeOnGlobalLayoutListener(lytContextHelp, layoutListener)

                animateShowContextHelpAfterMeasuringHeight(lytContextHelp)
            }

            lytContextHelp.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        } else {
            animateShowContextHelpAfterMeasuringHeight(lytContextHelp)
        }
    }

    private fun animateShowContextHelpAfterMeasuringHeight(lytContextHelp: View) {
        playAnimation(lytContextHelp, - lytContextHelp.measuredHeight.toFloat(), 0f)
    }

    private fun animateHideContextHelp(lytContextHelp: View) {
        playAnimation(lytContextHelp, 0f, - lytContextHelp.measuredHeight.toFloat()) { lytContextHelp.visibility = View.GONE }
    }

    private fun playAnimation(lytContextHelp: View, yStart: Float, yEnd: Float, animationEndListener: (() -> Unit)? = null) {
        val yAnimator = ObjectAnimator
                .ofFloat(lytContextHelp, View.Y, yStart, yEnd)
                .setDuration(AnimationDurationMillis)
        yAnimator.interpolator = AccelerateInterpolator()

        yAnimator.addListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animation: Animator) { }

            override fun onAnimationRepeat(animation: Animator) { }

            override fun onAnimationCancel(animation: Animator) { }

            override fun onAnimationEnd(animation: Animator) {
                animationEndListener?.invoke()
            }

        })

        yAnimator.start()
    }


    private fun removeOnGlobalLayoutListener(lytContextHelp: View, layoutListener: ViewTreeObserver.OnGlobalLayoutListener?) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            lytContextHelp.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        }
        else {
            lytContextHelp.viewTreeObserver.removeGlobalOnLayoutListener(layoutListener)
        }
    }

}