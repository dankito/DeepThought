package net.dankito.deepthought.android.views

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


class OpenUrlOptionsView {

    enum class OpenUrlOption {
        OpenInSameActivity,
        OpenInNewActivity,
        OpenWithOtherApp
    }


    private val optionItems = OpenUrlOption.values().toList()

    private val optionItemsStringResourceIds = HashMap<OpenUrlOption, Int>()

    private var displayedPopupWindow: PopupWindow? = null


    init {
        optionItemsStringResourceIds.put(OpenUrlOption.OpenInSameActivity, R.string.menu_open_url_open_in_same_activity)
        optionItemsStringResourceIds.put(OpenUrlOption.OpenInNewActivity, R.string.menu_open_url_open_in_new_activity)
        optionItemsStringResourceIds.put(OpenUrlOption.OpenWithOtherApp, R.string.menu_open_url_with_other_app)
    }


    fun showMenuCenter(anyViewInHierarchyJustForAnchor: View, optionSelectedListener: (OpenUrlOption) -> Unit) {
        displayedPopupWindow?.let { closeMenu(it) }

        val context = anyViewInHierarchyJustForAnchor.context

        val optionsListView = ListView(context)
        optionsListView.setBackgroundColor(Color.WHITE)
        optionsListView.adapter = OpenUrlOptionsListAdapter()

        val popupWindow = PopupWindow(context)
        popupWindow.contentView = optionsListView
        popupWindow.showAtLocation(anyViewInHierarchyJustForAnchor, Gravity.CENTER, 0, 0)
        this.displayedPopupWindow = popupWindow

        optionsListView.setOnItemClickListener { _, _, position, _ ->
            closeMenu(popupWindow)

            optionSelectedListener(optionItems[position])
        }
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


    inner class OpenUrlOptionsListAdapter: BaseAdapter() {

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

            return textView
        }

        private fun styleTextView(textView: TextView, parent: ViewGroup?) {
            val density = parent?.context?.resources?.displayMetrics?.density ?: 1f
            val height = (50 * density).toInt()
            textView.height = height
            textView.width = parent?.width ?: 500

            textView.layoutParams?.let { layoutParams ->
                layoutParams.height = height
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            }

            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            textView.gravity = Gravity.CENTER_VERTICAL
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.textAlignment = TextView.TEXT_ALIGNMENT_GRAVITY
            }

            val padding = (4 * density).toInt()
            textView.setPadding(padding, padding, padding, padding)
        }

    }

}