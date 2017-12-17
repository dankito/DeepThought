package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.view_edit_entity_field.view.*


abstract class EditEntityCollectionField : EditEntityField {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    protected lateinit var lytPreview: FlexboxLayout


    private var lastSearchTerm = ""


    override fun doCustomUiInitialization(rootView: ViewGroup) {
        super.doCustomUiInitialization(rootView)

        this.disableActionOnKeyboard = true

        lytPreview = rootView.lytPreview

        hideEditTextEntityFieldValue()
    }


    override fun viewClicked() {
        if(edtxtEntityFieldValue.visibility != View.VISIBLE) {
            startEditing()
        }

        super.viewClicked()
    }

    override fun startEditing() {
        showEditTextEntityFieldValue()
        searchEntities(lastSearchTerm)

        super.startEditing()
    }

    override fun stopEditing() {
        hideEditTextEntityFieldValue()

        super.stopEditing()
    }

    override fun hasFocusChanged(hasFocus: Boolean) {
        if(hasFocus) {
            setFieldValueOnUiThread(lastSearchTerm)
            startEditing()
        }
        else {
            hideEditTextEntityFieldValue()
            hideSearchResultsView()
        }

        super.hasFocusChanged(hasFocus)
    }

    override fun enteredTextChanged(enteredText: String) {
        // do not call super.enteredTextChanged(enteredText) in this case as this would call updateDidValueChange()
//        super.enteredTextChanged(enteredText)

        lastSearchTerm = enteredText

        if(edtxtEntityFieldValue.hasFocus()) {
            searchEntities(enteredText)
        }
    }

    abstract fun searchEntities(searchTerm: String)

}