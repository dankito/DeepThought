package net.dankito.deepthought.android.views

import android.content.Context
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.util.AttributeSet
import org.slf4j.LoggerFactory


/**
 * Kudos for disabling shift mode go to Przemysław Piechota. kibao (https://stackoverflow.com/a/40189977)
 */
class AdvancedBottomNavigationView : BottomNavigationView {

    companion object {
        private val log = LoggerFactory.getLogger(AdvancedBottomNavigationView::class.java)
    }


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    fun disableShiftMode() {
        try {
            val mMenuViewField = BottomNavigationView::class.java.getDeclaredField("mMenuView")
            mMenuViewField.isAccessible = true
            val menuView = mMenuViewField.get(this) as BottomNavigationMenuView
            mMenuViewField.isAccessible = false

            val shiftingMode = menuView.javaClass.getDeclaredField("mShiftingMode")
            shiftingMode.isAccessible = true
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.isAccessible = false

            for(i in 0..menuView.childCount - 1) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView

                item.setShiftingMode(false)

                // set once again checked value, so view will be updated
                item.setChecked(item.itemData.isChecked)
            }
        } catch (e: Exception) {
            log.error("Could not disable shift mode", e)
        }
    }


    /**
     * Calling setEnabled() an a BottomNavigationView has no effect (seems to me like a bug).
     * We have to call setEnabled() on all MenuItems explicitly.
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        try {
            val mMenuViewField = BottomNavigationView::class.java.getDeclaredField("mMenuView")
            mMenuViewField.isAccessible = true
            val menuView = mMenuViewField.get(this) as BottomNavigationMenuView
            mMenuViewField.isAccessible = false

            menuView.isEnabled = enabled

            for(i in 0..menuView.childCount - 1) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView

                item.isEnabled = enabled
            }
        } catch (e: Exception) {
            log.error("Could not set enabled to $enabled", e)
        }
    }
}