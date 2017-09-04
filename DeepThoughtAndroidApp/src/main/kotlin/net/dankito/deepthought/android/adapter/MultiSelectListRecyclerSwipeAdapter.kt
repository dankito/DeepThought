package net.dankito.deepthought.android.adapter

import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.ActionBarContextView
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.LinearLayout
import net.dankito.deepthought.android.R


abstract class MultiSelectListRecyclerSwipeAdapter<T, THolder : RecyclerView.ViewHolder>(private val activity: AppCompatActivity, list: List<T> = ArrayList<T>()) :
        ListRecyclerSwipeAdapter<T, THolder>(list) {

    private var actionMode: android.view.ActionMode? = null

    private var actionModeBar: ActionBarContextView? = null

    private val selectedItemsInContextualActionMode = LinkedHashSet<T>()


    var actionItemClickListener: ((mode: android.view.ActionMode, actionItem: MenuItem, selectedItems: Set<T>) -> Boolean)? = null


    override fun itemLongClicked(item: T, position: Int) {
        if(actionMode == null) {
            actionMode = activity.startActionMode(actionModeCallback)
        }

        toggleSelection(item, position)

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
            actionMode?.title = activity.getString(R.string.activity_article_summary_menu_count_articles_selected, selectedItemsInContextualActionMode.size, itemCount)
            actionMode?.invalidate()
        }
    }

    override fun itemBound(viewHolder: RecyclerView.ViewHolder, item: T, position: Int) {
        super.itemBound(viewHolder, item, position)

        if(isInMultiSelectMode()) { // otherwise due to isPressed flag item would also get shown as selected
            viewHolder.itemView.isPressed = false
        }
        viewHolder.itemView.isActivated = selectedItemsInContextualActionMode.contains(item)
    }


    fun isInMultiSelectMode(): Boolean {
        return actionMode != null
    }



    private val actionModeCallback = object : AbsListView.MultiChoiceModeListener {

        override fun onCreateActionMode(mode: android.view.ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.activity_article_summary_contextual_action_menu, menu)

            val viewArticleItem = menu.findItem(R.id.mnViewArticle)
            viewArticleItem?.actionView?.setOnClickListener { onActionItemClicked(mode, viewArticleItem) }

            val saveArticleForLaterReadingItem = menu.findItem(R.id.mnSaveArticleForLaterReading)
            saveArticleForLaterReadingItem?.actionView?.setOnClickListener { onActionItemClicked(mode, saveArticleForLaterReadingItem) }

            val saveArticleItem = menu.findItem(R.id.mnSaveArticle)
            saveArticleItem?.actionView?.setOnClickListener { onActionItemClicked(mode, saveArticleItem) }

            placeActionModeBarInAppBarLayout()

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
            activity.findViewById(android.support.v7.appcompat.R.id.action_mode_bar)?.let { actionModeBar ->
                actionModeBar.visibility = View.GONE // hide action_mode_bar here as otherwise it will be displayed together with toolbar

                activity.findViewById(R.id.toolbar)?.let { it.visibility = View.VISIBLE }
            }

            selectedItemsInContextualActionMode.clear()
            actionMode = null
            notifyDataSetChanged()
        }

    }

    private fun placeActionModeBarInAppBarLayout() {
        (activity.findViewById(android.support.v7.appcompat.R.id.action_bar_root) as? LinearLayout)?.let { actionBarRoot ->
            for(i in 0..actionBarRoot.childCount) {
                val child = actionBarRoot.getChildAt(i)
                if(child is ActionBarContextView) {
                    placeActionModeBarInAppBarLayout(child)
                }
            }
        }
    }

    private fun placeActionModeBarInAppBarLayout(actionModeBar: ActionBarContextView) {
        this.actionModeBar = actionModeBar

        (actionModeBar.parent as? ViewGroup)?.let { parent ->
            parent.removeView(actionModeBar)

            (activity.findViewById(R.id.appBarLayout) as? AppBarLayout)?.let { appBarLayout ->
                appBarLayout.addView(actionModeBar)
            }
            activity.findViewById(R.id.toolbar)?.let { it.visibility = View.GONE }
        }
    }

}