package net.dankito.deepthought.android.adapter

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.ActionBarContextView
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.LinearLayout
import net.dankito.deepthought.android.R
import net.dankito.utils.android.ui.view.ToolbarUtil
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet
import kotlin.concurrent.schedule


abstract class MultiSelectListRecyclerSwipeAdapter<T, THolder : RecyclerView.ViewHolder>(list: List<T> = ArrayList<T>()) :
        ListRecyclerSwipeAdapter<T, THolder>(list) {

    companion object {
        private const val IS_MULTI_SELECT_MODE_ENABLED_EXTRA_NAME = "IS_MULTI_SELECT_MODE_ENABLED"
        private const val SELECTED_ITEMS_IN_MULTI_SELECT_MODE_EXTRA_NAME = "SELECTED_ITEMS_IN_MULTI_SELECT_MODE"
    }


    var actionModeBar: ActionBarContextView? = null

    var actionItemClickListener: ((mode: android.view.ActionMode, actionItem: MenuItem, selectedItems: Set<T>) -> Boolean)? = null

    var actionModeBarVisibilityListener: ((isVisible: Boolean) -> Unit)? = null


    var actionMode: android.view.ActionMode? = null

    private var toolbarUtil = ToolbarUtil()

    private val selectedItemsInContextualActionMode = LinkedHashMap<Int, T>()


    private var goToMultiSelectModeOnRestore: Boolean = false

    private var selectedItemsToRestore: Collection<Int>? = null


    private var activity: Activity? = null

    private var menuResourceId: Int? = null

    private var layoutToPlaceActionModeBarInResourceId: Int = R.id.appBarLayout

    private var hideToolbar = true


    fun enableMultiSelectionMode(activity: Activity, menuResourceId: Int, layoutToPlaceActionModeBarInResourceId: Int = R.id.appBarLayout, hideToolbar: Boolean = true,
                                 actionItemClickListener: ((mode: android.view.ActionMode, actionItem: MenuItem, selectedItems: Set<T>) -> Boolean)? = null) {
        this.activity = activity
        this.menuResourceId = menuResourceId
        this.layoutToPlaceActionModeBarInResourceId = layoutToPlaceActionModeBarInResourceId
        this.hideToolbar = hideToolbar
        this.actionItemClickListener = actionItemClickListener
    }


    override fun itemLongClicked(viewHolder: RecyclerView.ViewHolder, item: T, position: Int) {
        if(isMultiSelectModeEnabled()) {
            if(actionMode == null) {
                startActionMode()
            }

            toggleSelection(item, position)
        }

        super.itemLongClicked(viewHolder, item, position)
    }

    private fun startActionMode() {
        activity?.startActionMode(actionModeCallback)
    }

    override fun itemClicked(viewHolder: RecyclerView.ViewHolder, item: T, position: Int): Boolean {
        if(actionMode != null) {
            toggleSelection(item, position)
            return true
        }
        else {
            return super.itemClicked(viewHolder, item, position)
        }
    }

    protected open fun toggleSelection(item: T, position: Int) {
        if(selectedItemsInContextualActionMode.contains(position)) {
            selectedItemsInContextualActionMode.remove(position)
        }
        else {
            selectedItemsInContextualActionMode.put(position, item)
        }

        notifyItemChanged(position)

        val count = selectedItemsInContextualActionMode.count()

        if(count == 0) {
            actionMode?.finish()
        }
        else {
            actionMode?.title = activity?.getString(R.string.contextual_action_menu_count_items_selected, selectedItemsInContextualActionMode.size, itemCount)
            actionMode?.invalidate()
        }
    }

    override fun itemBound(viewHolder: RecyclerView.ViewHolder, item: T, position: Int) {
        super.itemBound(viewHolder, item, position)

        if(isInMultiSelectMode()) { // otherwise due to isPressed flag item would also get shown as selected
            viewHolder.itemView.isPressed = false
        }
        viewHolder.itemView.isActivated = selectedItemsInContextualActionMode.contains(position)
    }


    fun isMultiSelectModeEnabled(): Boolean {
        return activity != null && menuResourceId != null
    }

    fun isInMultiSelectMode(): Boolean {
        return actionMode != null
    }


    fun onSaveInstanceState(outState: Bundle?) {
        outState?.let {
            outState.putBoolean(IS_MULTI_SELECT_MODE_ENABLED_EXTRA_NAME, isInMultiSelectMode())
            outState.putIntegerArrayList(SELECTED_ITEMS_IN_MULTI_SELECT_MODE_EXTRA_NAME, ArrayList(selectedItemsInContextualActionMode.keys))
        }
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            goToMultiSelectModeOnRestore = savedInstanceState.getBoolean(IS_MULTI_SELECT_MODE_ENABLED_EXTRA_NAME, false)

            selectedItemsToRestore = savedInstanceState.getIntegerArrayList(SELECTED_ITEMS_IN_MULTI_SELECT_MODE_EXTRA_NAME)

            if(items.isNotEmpty()) { // in same views as in ArticleSummaryActivity items are already available when restoring view
                restoreSelectedItems()
            }
        }
    }

    override fun itemsHaveBeenSet(value: List<T>) {
        super.itemsHaveBeenSet(value)

        restoreSelectedItems() // in most cases we have to wait till items are available before we can restore selected items
    }

    private fun restoreSelectedItems() {
        selectedItemsToRestore?.let {
            if(goToMultiSelectModeOnRestore) {
                startActionMode()
            }

            it.forEach { position ->
                if(position < itemCount) {
                    toggleSelection(getItem(position), position)
                }
            }

            selectedItemsToRestore = null // selected items are restored now
        }
    }


    private val actionModeCallback = object : AbsListView.MultiChoiceModeListener {

        override fun onCreateActionMode(mode: android.view.ActionMode, menu: Menu): Boolean {
            actionMode = mode
            menuResourceId?.let { mode.menuInflater.inflate(it, menu) }

            toolbarUtil.setupActionItemsLayout(menu) { menuItem: MenuItem -> onActionItemClicked(mode, menuItem) }

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
                return actionItemClickListener.invoke(mode, item, LinkedHashSet(selectedItemsInContextualActionMode.values))
            }

            return false
        }

        override fun onDestroyActionMode(mode: android.view.ActionMode) {
            activity?.let { activity ->
                activity.findViewById<View>(android.support.v7.appcompat.R.id.action_mode_bar)?.let { actionModeBar ->
                    actionModeBar.visibility = View.GONE // hide action_mode_bar here as otherwise it will be displayed together with toolbar

                    activity.findViewById<View>(R.id.toolbar)?.let { it.visibility = View.VISIBLE }
                }

                selectedItemsInContextualActionMode.clear()
                actionMode = null
                actionModeBarVisibilityListener?.invoke(false)

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
                    this.actionModeBar = child
                    placeActionModeBarInAppBarLayout(activity, child)
                }
            }

            actionModeBar?.let { toolbarUtil.adjustToolbarLayoutDelayed(it, reserveOverflow = false) }

            if(hideToolbar) {
                activity.findViewById<View>(R.id.toolbar)?.let { it.visibility = View.GONE }
            }

            actionModeBarVisibilityListener?.invoke(true)
        }
    }

    private fun placeActionModeBarInAppBarLayout(activity: Activity, actionModeBar: ActionBarContextView) {
        (actionModeBar.parent as? ViewGroup)?.let { parent ->
            parent.removeView(actionModeBar)

            (activity.findViewById(layoutToPlaceActionModeBarInResourceId) as? ViewGroup)?.let { layout ->
                layout.addView(actionModeBar)
            }
        }
    }

}