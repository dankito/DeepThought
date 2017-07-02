package net.dankito.deepthought.android.views

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup


/**
 * Copied from Travis Castillo, https://stackoverflow.com/a/35904421
 */
class MoveUpwardBehavior : CoordinatorLayout.Behavior<View> {

    val childOriginalMarginTop = HashMap<View, Int>()


    constructor() : super() {

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }


    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View, dependency: View): Boolean {
        if(dependency is Snackbar.SnackbarLayout) {
            if(childOriginalMarginTop.containsKey(child) == false) { // only store top margin on first call as otherwise stored margin grows by dependency.height after each call to onDependentViewChanged
                (child.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
                    childOriginalMarginTop.put(child, layoutParams.topMargin)
                }
            }

            return true
        }

        return false
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View, dependency: View): Boolean {
        (child.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
            // setTranslationY() moves child behind its previous sibling -> increase margin top by dependency.height so that it's not being partially covered by previous sibling
            layoutParams.topMargin = (childOriginalMarginTop[child] ?: 0) + dependency.height
            child.layoutParams = layoutParams
        }

        val translationY = Math.min(0f, ViewCompat.getTranslationY(dependency) - dependency.height)
        ViewCompat.setTranslationY(child, translationY)

        return true
    }

    //you need this when you swipe the snackbar(thanx to ubuntudroid's comment)
    override fun onDependentViewRemoved(parent: CoordinatorLayout?, child: View, dependency: View) {
        (child.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
            layoutParams.topMargin = childOriginalMarginTop[child] ?: 0
            child.layoutParams = layoutParams
        }

        ViewCompat.animate(child).translationY(0f).start()
    }

}
