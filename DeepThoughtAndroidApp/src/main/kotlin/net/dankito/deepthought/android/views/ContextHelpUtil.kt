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
import net.dankito.deepthought.android.service.StringUtil


class ContextHelpUtil {

    companion object {
        private const val AnimationDurationMillis = 250L
    }


    val stringUtil = StringUtil()


    fun showContextHelp(lytContextHelp: View, helpTextResourceId: Int, helpDismissedListener: (() -> Unit)? = null) {
        showContextHelp(lytContextHelp, lytContextHelp.context.getText(helpTextResourceId).toString(), helpDismissedListener)
    }

    fun showContextHelp(lytContextHelp: View, helpText: String, helpDismissedListener: (() -> Unit)? = null) {
        val txtContextHelpText = lytContextHelp.findViewById(R.id.txtContextHelpText) as TextView
        txtContextHelpText.text = stringUtil.getSpannedFromHtml(helpText)

        val btnDismissContextHelp = lytContextHelp.findViewById(R.id.btnDismissContextHelp) as Button
        btnDismissContextHelp.setOnClickListener {
            animateHideContextHelp(lytContextHelp) {
                helpDismissedListener?.invoke()
            }
        }

        lytContextHelp.visibility = View.VISIBLE

        animateShowContextHelp(lytContextHelp)
    }


    fun showAsConfirmation(lytContextHelp: View, confirmationTextResourceId: Int, didConfirm: () -> Unit) {
        showAsConfirmation(lytContextHelp, lytContextHelp.context.getText(confirmationTextResourceId).toString(), didConfirm)
    }

    fun showAsConfirmation(lytContextHelp: View, confirmationText: String, didConfirm: () -> Unit) {
        val txtContextHelpText = lytContextHelp.findViewById(R.id.txtContextHelpText) as TextView
        txtContextHelpText.text = stringUtil.getSpannedFromHtml(confirmationText)

        lytContextHelp.findViewById(R.id.btnDismissContextHelp)?.visibility = View.GONE

        lytContextHelp.findViewById(R.id.lytConfirmButtons)?.visibility = View.VISIBLE

        val btnDeny = lytContextHelp.findViewById(R.id.btnDeny) as Button
        btnDeny.setOnClickListener { hide(lytContextHelp) }

        val btnConfirm = lytContextHelp.findViewById(R.id.btnConfirm) as Button
        btnConfirm.setOnClickListener {
            didConfirm()
            hide(lytContextHelp)
        }

        lytContextHelp.visibility = View.VISIBLE
    }


    // TODO: animation currently only works on top off screen, not in other places
    private fun animateShowContextHelp(lytContextHelp: View) {
        if(lytContextHelp.measuredHeight == 0) { // in this case we have to wait till height is determined -> set OnGlobalLayoutListener
            var layoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null // have to do it that complicated otherwise in OnGlobalLayoutListener we cannot access layoutListener variable
            layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                removeOnGlobalLayoutListener(lytContextHelp, layoutListener)

                animateShowContextHelpAfterMeasuringHeight(lytContextHelp)
            }

            lytContextHelp.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        }
        else {
            animateShowContextHelpAfterMeasuringHeight(lytContextHelp)
        }
    }

    private fun animateShowContextHelpAfterMeasuringHeight(lytContextHelp: View) {
        // this may works
//        playAnimation(lytContextHelp, lytContextHelp.y - lytContextHelp.measuredHeight.toFloat(), lytContextHelp.y)
        if(isAlmostAtBottom(lytContextHelp)) {
            playAnimation(lytContextHelp, lytContextHelp.bottom.toFloat(), lytContextHelp.bottom.toFloat() - lytContextHelp.measuredHeight)
        }
        else {
            playAnimation(lytContextHelp, -lytContextHelp.measuredHeight.toFloat(), 0f)
        }
    }

    fun animateHideContextHelp(lytContextHelp: View, helpDismissedListener: (() -> Unit)? = null) {
        // does not work
//        playAnimation(lytContextHelp, lytContextHelp.y, lytContextHelp.y - lytContextHelp.measuredHeight.toFloat()) { lytContextHelp.visibility = View.GONE }
        if(isAlmostAtBottom(lytContextHelp)) {
            playAnimation(lytContextHelp, lytContextHelp.top.toFloat(), lytContextHelp.bottom.toFloat()) { hide(lytContextHelp, helpDismissedListener) }
        }
        else {
            playAnimation(lytContextHelp, 0f, -lytContextHelp.measuredHeight.toFloat()) { hide(lytContextHelp, helpDismissedListener) }
        }
    }

    private fun isAlmostAtBottom(lytContextHelp: View): Boolean {
        val displayHeight = lytContextHelp.context.resources.displayMetrics.heightPixels
        return lytContextHelp.bottom / displayHeight.toFloat() > 0.7
    }

    private fun hide(lytContextHelp: View, animationEndListener: (() -> Unit)? = null) {
        lytContextHelp.visibility = View.GONE

        animationEndListener?.invoke()
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