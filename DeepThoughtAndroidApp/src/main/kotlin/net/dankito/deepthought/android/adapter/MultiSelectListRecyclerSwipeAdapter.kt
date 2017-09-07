package net.dankito.deepthought.android.adapter

import android.app.Activity
import android.support.v7.widget.ActionBarContextView
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.LinearLayout
import net.dankito.deepthought.android.R
import java.util.*
import kotlin.collections.HashSet
import kotlin.concurrent.schedule


abstract class MultiSelectListRecyclerSwipeAdapter<T, THolder : RecyclerView.ViewHolder>(list: List<T> = ArrayList<T>()) :
        ListRecyclerSwipeAdapter<T, THolder>(list) {

    private var actionMode: android.view.ActionMode? = null

    private var actionModeBar: ActionBarContextView? = null

    private val createdViewHolders = HashSet<THolder>()

    private val selectedItemsInContextualActionMode = LinkedHashSet<T>()


    private var activity: Activity? = null

    private var menuResourceId: Int? = null

    private var layoutToPlaceActionModeBarInResourceId: Int = R.id.appBarLayout

    var actionItemClickListener: ((mode: android.view.ActionMode, actionItem: MenuItem, selectedItems: Set<T>) -> Boolean)? = null


    fun enableMultiSelectionMode(activity: Activity, menuResourceId: Int, layoutToPlaceActionModeBarInResourceId: Int = R.id.appBarLayout,
                                 actionItemClickListener: ((mode: android.view.ActionMode, actionItem: MenuItem, selectedItems: Set<T>) -> Boolean)? = null) {
        this.activity = activity
        this.menuResourceId = menuResourceId
        this.layoutToPlaceActionModeBarInResourceId = layoutToPlaceActionModeBarInResourceId
        this.actionItemClickListener = actionItemClickListener
    }


    override fun itemLongClicked(item: T, position: Int) {
        if(isMultiSelectModeEnabled()) {
            if(actionMode == null) {
                actionMode = activity?.startActionMode(actionModeCallback)
            }

            toggleSelection(item, position)
        }

        super.itemLongClicked(item, position)
    }

    override fun itemClicked(item: T, position: Int): Boolean {
        if(actionMode != null) {
            toggleSelection(item, position)
            return true
        }
        else {
            return super.itemClicked(item, position)
        }
    }

    private fun toggleSelection(item: T, position: Int) {
        if(selectedItemsInContextualActionMode.contains(item)) {
            selectedItemsInContextualActionMode.remove(item)
        }
        else {
            selectedItemsInContextualActionMode.add(item)
        }

        notifyItemChanged(position)

        val count = selectedItemsInContextualActionMode.count()

        if(count == 0) {
            actionMode?.finish()
        }
        else {
            actionMode?.title = activity?.getString(R.string.activity_article_summary_menu_count_articles_selected, selectedItemsInContextualActionMode.size, itemCount)
            actionMode?.invalidate()
        }
    }

    override fun itemBound(viewHolder: RecyclerView.ViewHolder, item: T, position: Int) {
        super.itemBound(viewHolder, item, position)

        createdViewHolders.add(viewHolder as THolder) // overwriting onCreateViewHolder() for this makes no sense as child class generates the ViewHolder

        if(isInMultiSelectMode()) { // otherwise due to isPressed flag item would also get shown as selected
            viewHolder.itemView.isPressed = false
        }
        viewHolder.itemView.isActivated = selectedItemsInContextualActionMode.contains(item)
    }


    fun isMultiSelectModeEnabled(): Boolean {
        return activity != null && menuResourceId != null
    }

    fun isInMultiSelectMode(): Boolean {
        return actionMode != null
    }



    private val actionModeCallback = object : AbsListView.MultiChoiceModeListener {

        override fun onCreateActionMode(mode: android.view.ActionMode, menu: Menu): Boolean {
            menuResourceId?.let { mode.menuInflater.inflate(it, menu) }

            for(i in 0..menu.size() - 1) {
                val menuItem = menu.getItem(i)
                menuItem?.actionView?.setOnClickListener { onActionItemClicked(mode, menuItem) }
            }

            activity?.let { placeActionModeBarInAppBarLayout(it) }

            return true
        }

        override fun onPrepareActionMode(mode: android.view.ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onItemCheckedStateChanged(mode: android.view.ActionMode, position: Int, id: Long, checked: Boolean) {
        }

        override fun onActionItemClicked(mode: android.view.ActionMode, item: MenuItem): Boolean {
            actionItemClickListener?.let { actionItemClickListener ->
                return actionItemClickListener.invoke(mode, item, selectedItemsInContextualActionMode)
            }

            return false
        }

        override fun onDestroyActionMode(mode: android.view.ActionMode) {
            activity?.let { activity ->
                activity.findViewById(android.support.v7.appcompat.R.id.action_mode_bar)?.let { actionModeBar ->
                    actionModeBar.visibility = View.GONE // hide action_mode_bar here as otherwise it will be displayed together with toolbar

                    activity.findViewById(R.id.toolbar)?.let { it.visibility = View.VISIBLE }
                }

                selectedItemsInContextualActionMode.clear()
                actionMode = null

                notifyDataSetChanged()

                fixItemGetsShownInPressedStateBug(activity)
            }
        }

    }

    private fun fixItemGetsShownInPressedStateBug(activity: Activity) {
        // this is a real curious bug in Android (https://stackoverflow.com/questions/32559649/recyclerview-pressed-state-of-items-is-getting-messed-up):
        // after deselecting last item from multi select mode by hand in most cases an arbitrary item gets shown in pressed state (the user therefore thinks it is selected)
        // to circumvent this, we have to wait some time and unpress it with this really beautiful code. But user then sees a short blink anyway

        val timer = Timer()

        timer.schedule(100) {
            activity.runOnUiThread {
                createdViewHolders.forEach { it.itemView.isPressed = false }
            }
        }

        timer.schedule(1000) { // to be on the safe side when the 100 millisecond option doesn't work, but should actually never be needed
            activity.runOnUiThread {
                createdViewHolders.forEach { it.itemView.isPressed = false }
            }
        }
    }

    private fun placeActionModeBarInAppBarLayout(activity: Activity) {
        (activity.findViewById(android.support.v7.appcompat.R.id.action_bar_root) as? LinearLayout)?.let { actionBarRoot ->
            for(i in 0..actionBarRoot.childCount) { // starting with second call ActionBarContextView is already in correct place in appBarLayout -> won't find it in action_bar_root anymore
                val child = actionBarRoot.getChildAt(i)
                if(child is ActionBarContextView) {
                    placeActionModeBarInAppBarLayout(activity, child)
                }
            }

            activity.findViewById(R.id.toolbar)?.let { it.visibility = View.GONE }
        }
    }

    private fun placeActionModeBarInAppBarLayout(activity: Activity, actionModeBar: ActionBarContextView) {
        this.actionModeBar = actionModeBar
        actionModeBar.setBackgroundResource(R.drawable.primary_color_gradient)

        (actionModeBar.parent as? ViewGroup)?.let { parent ->
            parent.removeView(actionModeBar)

            (activity.findViewById(layoutToPlaceActionModeBarInResourceId) as? ViewGroup)?.let { layout ->
                layout.addView(actionModeBar)
            }
        }
    }

}