package net.dankito.deepthought.android.views

import android.support.v7.widget.ActionMenuView
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.contextual_action_mode_item_action_layout.view.*
import org.slf4j.LoggerFactory


class ActionBarUtil {

    companion object {
        private val log = LoggerFactory.getLogger(ActionBarUtil::class.java)
    }


    fun setupLayout(menu: Menu, onClickListener: ((MenuItem) -> Unit)? = null) {
        for(i in 0..menu.size() - 1) {
            setupLayout(menu.getItem(i), onClickListener)
        }
    }

    fun setupLayout(menuItem: MenuItem?, onClickListener: ((MenuItem) -> Unit)? = null) {
        menuItem?.actionView?.let { actionView ->
            actionView.imgActionIcon.setImageDrawable(menuItem.icon)
            actionView.txtActionTitle.text = menuItem.title

            actionView.setOnClickListener {
                onClickListener?.invoke(menuItem)
            }
        }
    }


    // we have to wait some time till actionModeBar is deflated and its children are added
    fun adjustActionModeBarLayoutDelayed(toolbarOrActionModeBar: ViewGroup, countActionItems: Int, reserveOverflow: Boolean? = null) {
        toolbarOrActionModeBar.postDelayed({
            adjustActionModeBarLayout(toolbarOrActionModeBar, countActionItems, reserveOverflow)
        }, 100L)
    }

    fun adjustActionModeBarLayout(toolbarOrActionModeBar: ViewGroup, countActionItems: Int, reserveOverflow: Boolean? = null) {
        reduceCloseButtonAndTitleViewSize(toolbarOrActionModeBar)

        setMaxActionItemsCount(toolbarOrActionModeBar, countActionItems, reserveOverflow)
    }


    fun reduceCloseButtonAndTitleViewSize(toolbarOrActionModeBar: ViewGroup) {
        for(i in 0..toolbarOrActionModeBar.childCount - 1) {
            val child = toolbarOrActionModeBar.getChildAt(i)

            if(child is ImageView) { // ImageView on newer Androids, AppCompatImageButton on older ones
                (child.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
                    layoutParams.rightMargin = 0
                    child.layoutParams = layoutParams
                }
            }
            else if(child is LinearLayout) { // LinearLayout on newer Androids, AppCompatTextView on older ones
                child.setPadding(0, 0, 0, 0)
            }
        }
    }


    fun setMaxActionItemsCount(actionMenuViewParent: ViewGroup, countActionItems: Int, reserveOverflow: Boolean? = null) {
        for(i in 0..actionMenuViewParent.childCount - 1) {
            val child = actionMenuViewParent.getChildAt(i)

            if(child is ActionMenuView) {
                try {
                    setMaxActionItemsCountInActionMenuView(child, countActionItems, reserveOverflow)
                } catch(e: Exception) { log.error("Could not set item limit", e) }
            }
        }
    }

    private fun setMaxActionItemsCountInActionMenuView(actionMenuView: ActionMenuView, countActionItems: Int, reserveOverflow: Boolean?) {
        val presenterField = actionMenuView.javaClass.getDeclaredField("mPresenter")
        presenterField.isAccessible = true
        val menuPresenter = presenterField.get(actionMenuView) // class ActionMenuPresenter is not accessible -> have to do everything via reflection

        reserveOverflow?.let {
            val setReserveOverflowMethod = menuPresenter.javaClass.getDeclaredMethod("setReserveOverflow", Boolean::class.java)
            setReserveOverflowMethod.invoke(menuPresenter, it)
        }

        val displayWidth = actionMenuView.context.resources.displayMetrics.widthPixels

        val mActionItemWidthLimitField = menuPresenter.javaClass.getDeclaredField("mActionItemWidthLimit")
        mActionItemWidthLimitField.isAccessible = true
        mActionItemWidthLimitField.set(menuPresenter, displayWidth)

        val setWidthLimitMethod = menuPresenter.javaClass.getDeclaredMethod("setWidthLimit", Int::class.java, Boolean::class.java)
        setWidthLimitMethod.invoke(menuPresenter, displayWidth, false)

        val setItemLimitMethod = menuPresenter.javaClass.getDeclaredMethod("setItemLimit", Int::class.java)
        setItemLimitMethod.invoke(menuPresenter, countActionItems)

        val flagActionItemsMethod = menuPresenter.javaClass.getDeclaredMethod("flagActionItems")
        flagActionItemsMethod.invoke(menuPresenter)

        val updateMenuViewMethod = menuPresenter.javaClass.getDeclaredMethod("updateMenuView", Boolean::class.java)
        updateMenuViewMethod.invoke(menuPresenter, true)
    }

}