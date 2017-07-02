package net.dankito.deepthought.android.views

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View


/**
 * Copied from Travis Castillo, https://stackoverflow.com/a/35904421
 */
class MoveUpwardBehavior : CoordinatorLayout.Behavior<View> {


    constructor() : super() {

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }


    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?, dependency: View): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dependency: View): Boolean {
        val translationY = Math.min(0f, ViewCompat.getTranslationY(dependency) - dependency.height)
        ViewCompat.setTranslationY(child, translationY)
        return true
    }

    //you need this when you swipe the snackbar(thanx to ubuntudroid's comment)
    override fun onDependentViewRemoved(parent: CoordinatorLayout?, child: View?, dependency: View) {
        ViewCompat.animate(child).translationY(0f).start()
    }

}
