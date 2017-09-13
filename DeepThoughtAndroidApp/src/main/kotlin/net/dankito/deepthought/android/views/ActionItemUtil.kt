package net.dankito.deepthought.android.views

import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.contextual_action_mode_item_action_layout.view.*


class ActionItemUtil {

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

}