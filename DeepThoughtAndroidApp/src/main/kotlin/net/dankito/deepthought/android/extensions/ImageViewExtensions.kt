package net.dankito.deepthought.android.extensions

import android.widget.ImageView
import net.dankito.deepthought.android.R
import net.dankito.utils.android.extensions.setTintList


fun ImageView.setTintListToEntityIsSelectedColor() {
    setTintList(R.color.is_entity_selected_icon_color)
}