package net.dankito.deepthought.android.views

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import net.dankito.deepthought.android.R
import net.dankito.utils.android.extensions.isTouchInsideView


class OpenUrlOptionsView {

    enum class OpenUrlOption {
        OpenInNewActivity,
        OpenWithOtherApp
    }


    private val optionItems = OpenUrlOption.values().toList()

    private val optionItemsStringResourceIds = HashMap<OpenUrlOption, Int>()

    private var displayedPopupWindow: PopupWindow? = null


    init {
        optionItemsStringResourceIds.put(OpenUrlOption.OpenInNewActivity, R.string.menu_open_url_open_in_new_activity)
        optionItemsStringResourceIds.put(OpenUrlOption.OpenWithOtherApp, R.string.menu_open_url_with_other_app)
    }


    fun showMenuCenter(anyViewInHierarchyJustForAnchor: View, optionSelectedListener: (OpenUrlOption) -> Unit) {
        cleanUp()

        val context = anyViewInHierarchyJustForAnchor.context
        val popupWindow = PopupWindow(context)
        val adapter = OpenUrlOptionsListAdapter { position ->
            closeMenu(popupWindow)

            optionSelectedListener(optionItems[position])
        }

        val optionsListView = ListView(context)
        optionsListView.setBackgroundColor(Color.WHITE)
        optionsListView.adapter = adapter

        optionsListView.setOnItemClickListener { _, _, position, _ -> // for newer / non-Samsung Androids
            closeMenu(popupWindow)

            optionSelectedListener(optionItems[position])
        }

        popupWindow.contentView = optionsListView
        // for Samsung devices - once again a special handling for Samsung devices - height and width has to be set, otherwise PopupWindow won't be shown
        popupWindow.height = adapter.calculateItemsHeight(anyViewInHierarchyJustForAnchor.context)
        popupWindow.width = adapter.calculateItemWidth(anyViewInHierarchyJustForAnchor.context)
        popupWindow.showAtLocation(anyViewInHierarchyJustForAnchor, Gravity.CENTER, 0, 0)

        this.displayedPopupWindow = popupWindow
    }


    fun cleanUp() {
        displayedPopupWindow?.let { closeMenu(it) }
    }

    private fun closeMenu(popupWindow: PopupWindow) {
        popupWindow.dismiss()

        displayedPopupWindow = null
    }


    fun handlesBackButtonPress(): Boolean {
        displayedPopupWindow?.let {
            closeMenu(it)

            return true
        }

        return false
    }

    fun handlesTouch(event: MotionEvent): Boolean {
        displayedPopupWindow?.let { popupWindow ->
            if(popupWindow.contentView.isTouchInsideView(event) == false) { // if menu is opened and user clicked somewhere else in the view, close menu
                closeMenu(popupWindow)

                return true
            }
        }

        return false
    }


    inner class OpenUrlOptionsListAdapter(private val itemClickedListener: (Int) -> Unit): BaseAdapter() {

        override fun getCount(): Int {
            return optionItems.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return optionItems[position]
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val option = getItem(position) as OpenUrlOption
            val optionResourceId = optionItemsStringResourceIds[option]

            val textView = convertView as? TextView ?: TextView(parent?.context)
            styleTextView(textView, parent)

            optionResourceId?.let { textView.setText(optionResourceId) }

            // ListView.setOnItemClickListener() didn't work on Samsung devices as TextView caught click - even thought isClickable and isFocusable had been set to false!
            textView.setOnClickListener { itemClickedListener(position) }

            return textView
        }

        private fun styleTextView(textView: TextView, parent: ViewGroup?) {
            val density = parent?.context?.resources?.displayMetrics?.density ?: 1f
            val height = calculateItemHeight(density)
            textView.height = height
            parent?.context?.let { textView.width = calculateItemWidth(it) }

            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            textView.gravity = Gravity.CENTER_VERTICAL
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.textAlignment = TextView.TEXT_ALIGNMENT_GRAVITY
            }

            val padding = (4 * density).toInt()
            textView.setPadding(padding, padding, padding, padding)
        }


        fun calculateItemWidth(context: Context): Int {
            return (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        }

        fun calculateItemsHeight(context: Context): Int {
            val density = context.resources.displayMetrics.density

            return count * calculateItemHeight(density) +
                    (count + 1) * (3 * density).toInt() // ListView adds a space of 1dp between two items plus a padding at top and end of ListView
        }

        private fun calculateItemHeight(density: Float) = (50 * density).toInt()

    }

}