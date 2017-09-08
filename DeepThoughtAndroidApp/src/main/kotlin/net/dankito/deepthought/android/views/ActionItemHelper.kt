package net.dankito.deepthought.android.views

import android.view.MenuItem
import kotlinx.android.synthetic.main.contextual_action_mode_item_action_layout.view.*


class ActionItemHelper {

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