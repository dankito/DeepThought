package net.dankito.deepthought.android.views

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.util.AttributeSet
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.extensions.setTintColor


abstract class EditEntityEntityReferenceField : EditEntityField {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    protected var valueToEdit: String? = null

    protected var valueToShowWhenNotEditing: String? = null

    protected var showEditDetailsMenuItem = true


    override fun doCustomUiInitialization(rootView: ViewGroup) {
        super.doCustomUiInitialization(rootView)

        this.disableActionOnKeyboard = true

        showMoreVerticalIconInButtonEntityFieldAction(rootView)
    }

    private fun showMoreVerticalIconInButtonEntityFieldAction(rootView: ViewGroup) {
        showActionIconOnUiThread(R.drawable.ic_more_vert_white_48dp, false) {
            showOptionsPopupMenu()
        }

        btnEntityFieldAction.setTintColor(R.color.gray)
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

    override fun enteredTextChanged(enteredText: String) {
        updateValueToEdit()

        super.enteredTextChanged(enteredText)
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

        hideSearchResultsView()
    }

    protected open fun updateValueToEdit() {
        valueToEdit = getCurrentFieldValue()
    }

    protected open fun setDisplayedValue(hasFocus: Boolean = edtxtEntityFieldValue.hasFocus()) {
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

        popup.menu.findItem(R.id.mnEditDetails)?.isVisible = showEditDetailsMenuItem

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