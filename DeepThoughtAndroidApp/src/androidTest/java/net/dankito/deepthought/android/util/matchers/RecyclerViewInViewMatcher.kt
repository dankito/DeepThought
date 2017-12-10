package net.dankito.deepthought.android.util.matchers

import android.support.v7.widget.RecyclerView
import android.view.View
import org.hamcrest.Matcher

class RecyclerViewInViewMatcher(private val inViewWithId: Int, private val recyclerViewId: Int) : RecyclerViewMatcher(recyclerViewId) {

    companion object {

        @JvmStatic
        fun withRecyclerView(inViewWithId: Int, recyclerViewId: Int) : RecyclerViewInViewMatcher {
            return RecyclerViewInViewMatcher(inViewWithId, recyclerViewId)
        }
    }

    override fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {
        return ItemPositionInRecyclerViewInViewMatcher(position, targetViewId)
    }


    inner class ItemPositionInRecyclerViewInViewMatcher(position: Int, targetViewId: Int) : ItemPositionInRecyclerViewMatcher(position, targetViewId) {

        override fun findRecyclerView(view: View): RecyclerView? {
            val parentView = view.findViewById(inViewWithId)

            if(parentView != null) {
                return parentView.findViewById(recyclerViewId) as? RecyclerView
            }

            return super.findRecyclerView(view)
        }
    }

}