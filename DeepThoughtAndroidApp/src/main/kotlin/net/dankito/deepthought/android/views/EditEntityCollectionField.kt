package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.view_edit_entity_field.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration


abstract class EditEntityCollectionField : EditEntityField {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    protected lateinit var rcySearchResult: MaxHeightRecyclerView

    protected lateinit var lytCollectionPreview: FlexboxLayout


    private var lastSearchTerm = ""


    override fun doCustomUiInitialization(rootView: ViewGroup) {
        super.doCustomUiInitialization(rootView)

        this.disableActionOnKeyboard = true

        rcySearchResult = rootView.findViewById(R.id.rcySearchResults) as MaxHeightRecyclerView
        rcySearchResult.addItemDecoration(HorizontalDividerItemDecoration(context))

        lytCollectionPreview = rootView.lytCollectionPreview
        lytCollectionPreview.visibility = View.VISIBLE
    }


    override fun hasFocusChanged(hasFocus: Boolean) {
        if(hasFocus) {
            setFieldValueOnUiThread(lastSearchTerm)
            startEditing()
        }
        else {
            rcySearchResult.visibility = View.GONE
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

    abstract fun searchEntities(query: String)

}