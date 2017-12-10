package net.dankito.deepthought.android.views

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.util.AttributeSet
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration


abstract class EditEntityEntityReferenceField : EditEntityField {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    protected var valueToEdit: String? = null

    protected var valueToShowWhenNotEditing: String? = null

    protected lateinit var rcySearchResult: MaxHeightRecyclerView


    override fun doCustomUiInitialization(rootView: ViewGroup) {
        super.doCustomUiInitialization(rootView)

        rcySearchResult = rootView.findViewById(R.id.rcySearchResults) as MaxHeightRecyclerView
        rcySearchResult.addItemDecoration(HorizontalDividerItemDecoration(context))

        showActionIconOnUiThread(R.drawable.ic_settings_white_48dp) {
            showOptionsPopupMenu()
        }
    }


    open fun setFieldValueOnUiThread(valueToEdit: String, valueToShowWhenNotEditing: String) {
        setValues(valueToEdit, valueToShowWhenNotEditing)

        super.setFieldValueOnUiThread(valueToEdit) // to set originalValue

        setDisplayedValue()
    }

    protected open fun setValues(valueToEdit: String, valueToShowWhenNotEditing: String) {
        this.valueToEdit = valueToEdit
        this.valueToShowWhenNotEditing = valueToShowWhenNotEditing
    }

    override fun updateDidValueChange(didValueChange: Boolean) {
        updateValueToEdit()

        super.updateDidValueChange(didValueChange)
    }

    override fun hasFocusChanged(hasFocus: Boolean) {
        if(hasFocus == false) {
            editTextLostFocus()
        }

        setDisplayedValue(hasFocus)

        if(hasFocus) {
            startEditing()
        }

        super.hasFocusChanged(hasFocus)
    }

    protected open fun editTextLostFocus() {
        updateValueToEdit()
    }

    protected open fun updateValueToEdit() {
        valueToEdit = getCurrentFieldValue()
    }

    protected open fun setDisplayedValue(hasFocus: Boolean = hasFocus()) {
        if(hasFocus) {
            setEditTextEntityFieldValueOnUiThread(valueToEdit ?: "")
        }
        else {
            setEditTextEntityFieldValueOnUiThread(valueToShowWhenNotEditing ?: "")
        }
    }


    private fun showOptionsPopupMenu() {
        val popup = PopupMenu(context, btnEntityFieldAction)

        popup.menuInflater.inflate(R.menu.edit_entity_reference_field_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.mnEditDetails -> editDetails()
                R.id.mnCreateNewEntity -> createNewEntity()
                R.id.mnRemoveEntity -> removeEntity()
            }
            true
        }

        popup.show()
    }

    abstract fun editDetails()

    abstract fun createNewEntity()

    abstract fun removeEntity()


    fun getEditedValue() = valueToEdit

}