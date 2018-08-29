package net.dankito.deepthought.android.views

import android.content.Context
import android.support.v7.widget.ActionBarContextView
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.contextual_action_mode_item_action_layout.view.*
import net.dankito.deepthought.android.R
import net.dankito.utils.android.extensions.setRightMargin
import org.slf4j.LoggerFactory


class ToolbarUtil {

    companion object {
        const val DefaultCountActionItems = 4

        private val log = LoggerFactory.getLogger(ToolbarUtil::class.java)
    }


    fun setupActionItemsLayout(menu: Menu, onClickListener: ((MenuItem) -> Unit)? = null) {
        for(i in 0..menu.size() - 1) {
            setupActionItemsLayout(menu.getItem(i), onClickListener)
        }
    }

    fun setupActionItemsLayout(menuItem: MenuItem?, onClickListener: ((MenuItem) -> Unit)? = null) {
        menuItem?.let {
            when(menuItem.actionView) {
                is SearchView -> setupSearchView(menuItem.actionView as SearchView, menuItem)
                is RelativeLayout -> setupActionLayoutItem(menuItem.actionView as RelativeLayout, menuItem, onClickListener)
            }
        }
    }

    private fun setupActionLayoutItem(actionView: RelativeLayout, menuItem: MenuItem, onClickListener: ((MenuItem) -> Unit)?) {
        setTitleAndIcon(actionView, menuItem)

        actionView.setOnClickListener {
            onClickListener?.invoke(menuItem)
        }
    }

    fun updateMenuItemView(menuItem: MenuItem?) {
        menuItem?.actionView?.let { actionView ->
            setTitleAndIcon(actionView, menuItem)
        }
    }

    private fun setTitleAndIcon(actionView: View, menuItem: MenuItem) {
        actionView.imgActionIcon.setImageDrawable(menuItem.icon)
        actionView.txtActionTitle.text = menuItem.title
    }


    private fun setupSearchView(searchView: SearchView, menuItem: MenuItem) {
        (searchView.findViewById(android.support.v7.appcompat.R.id.search_button) as? ImageView)?.let { searchIcon ->
            setupSearchView(searchView, menuItem, searchIcon)
        }
    }

    private fun setupSearchView(searchView: SearchView, menuItem: MenuItem, searchIcon: ImageView) {
        val searchIconIndex = (searchIcon.parent as ViewGroup).indexOfChild(searchIcon)
        (searchIcon.parent as ViewGroup).removeView(searchIcon) // remove search icon and place it in an ActionLayout

        val inflater = searchView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val actionLayout = inflater.inflate(R.layout.contextual_action_mode_item_action_layout, null) as ViewGroup
        searchView.addView(actionLayout, searchIconIndex)

        actionLayout.layoutParams?.width = searchView.context.resources.getDimension(R.dimen.action_item_width).toInt()
        searchView.layoutParams?.width = actionLayout.layoutParams?.width

        val imgActionIcon = actionLayout.imgActionIcon
        val index = actionLayout.indexOfChild(imgActionIcon)
        actionLayout.addView(searchIcon, if(index >= 0) index else 0, imgActionIcon.layoutParams)
        actionLayout.removeView(imgActionIcon)

        searchIcon.layoutParams.width = imgActionIcon.layoutParams.width
        searchIcon.scaleType = ImageView.ScaleType.FIT_CENTER

        val txtActionTitle = actionLayout.txtActionTitle
        txtActionTitle.text = menuItem.title

        actionLayout.setOnClickListener { searchIcon.callOnClick() }
    }


    // we have to wait some time till actionModeBar is deflated and its children are added
    fun adjustToolbarLayoutDelayed(toolbarOrActionModeBar: ViewGroup, countActionItems: Int = DefaultCountActionItems, reserveOverflow: Boolean? = null) {
        toolbarOrActionModeBar.postDelayed({
            adjustToolbarLayout(toolbarOrActionModeBar, countActionItems, reserveOverflow)
        }, 100L)
    }

    fun adjustToolbarLayout(toolbarOrActionModeBar: ViewGroup, countActionItems: Int = DefaultCountActionItems, reserveOverflow: Boolean? = null) {
        reduceCloseButtonAndTitleViewSize(toolbarOrActionModeBar)

        setMaxActionItemsCount(toolbarOrActionModeBar, countActionItems, reserveOverflow)
    }


    fun reduceCloseButtonAndTitleViewSize(toolbarOrActionModeBar: ViewGroup) {
        val density = toolbarOrActionModeBar.context.resources.displayMetrics.density

        for(i in 0..toolbarOrActionModeBar.childCount - 1) {
            val child = toolbarOrActionModeBar.getChildAt(i)

            if(child is ImageButton || (toolbarOrActionModeBar is ActionBarContextView && child is ImageView)) { // home / close button at left of toolbar / actionModeBar
                (child.layoutParams as? ViewGroup.MarginLayoutParams)?.setRightMargin(0)
                child.setPadding(0, 0, 0, 0)

                val newWidth = (45 * density).toInt()
                child.minimumWidth = newWidth
                child.layoutParams?.width = newWidth
            }
            else if(child is LinearLayout) { // toolbar / actionModeBar's title
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