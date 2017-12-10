package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet


open class EditEntityEntityReferenceField : EditEntityField {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    private var valueToEdit: String? = null

    private var valueToShowWhenNotEditing: String? = null


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


    fun getEditedValue() = valueToEdit

}