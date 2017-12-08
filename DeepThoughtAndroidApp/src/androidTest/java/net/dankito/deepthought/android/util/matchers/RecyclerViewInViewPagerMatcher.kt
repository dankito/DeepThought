package net.dankito.deepthought.android.util.matchers

import android.support.v7.widget.RecyclerView
import android.view.View
import org.hamcrest.Matcher


class RecyclerViewInViewPagerMatcher(private val inFragmentWithTag: Any, private val recyclerViewId: Int) : RecyclerViewMatcher(recyclerViewId) {

    companion object {

        @JvmStatic
        fun withRecyclerView(inFragmentWithTag: Any, recyclerViewId: Int) : RecyclerViewInViewPagerMatcher {
            return RecyclerViewInViewPagerMatcher(inFragmentWithTag, recyclerViewId)
        }
    }

    override fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {
        return ItemPositionInRecyclerViewInFragmentMatcher(position, targetViewId)
    }


    inner class ItemPositionInRecyclerViewInFragmentMatcher(position: Int, targetViewId: Int) : ItemPositionInRecyclerViewMatcher(position, targetViewId) {

        override fun findRecyclerView(view: View): RecyclerView? {
            val fragmentView = view.findViewWithTag(inFragmentWithTag)

            if(fragmentView != null) {
                return fragmentView.findViewById(recyclerViewId) as? RecyclerView
            }

            return super.findRecyclerView(view)
        }
    }

}