package net.dankito.deepthought.android.views

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.view_edit_entity_field.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
import net.dankito.deepthought.android.extensions.setTextColorToColorResource
import net.dankito.deepthought.android.extensions.setTintColor
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.service.showKeyboard
import java.util.*


open class EditEntityField : RelativeLayout {

    companion object {
        private const val FIELD_NAME_BUNDLE_EXTRA_NAME = "FIELD_NAME"
        private const val FIELD_VALUE_BUNDLE_EXTRA_NAME = "FIELD_VALUE"
    }


    protected lateinit var rootView: ViewGroup

    protected lateinit var txtEntityFieldName: TextView

    protected lateinit var edtxtEntityFieldValue: EditText

    protected lateinit var btnEntityFieldAction: ImageButton

    protected lateinit var rcySearchResult: MaxHeightRecyclerView


    var didValueChange = false

    protected var originalValue = ""

    protected var disableActionOnKeyboard = false


    var didValueChangeListener: ((didValueChange: Boolean) -> Unit)? = null

    var fieldClickedListener: (() -> Unit)? = null

    var fieldValueFocusChangedListener: ((hasFocus: Boolean) -> Unit)? = null

    var actionIconClickedListener: (() -> Unit)? = null



    constructor(context: Context) : super(context) {
        setupUI(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupUI(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setupUI(context)
    }


    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState() // we have to call it but we're not interested in its result

        val bundle = Bundle()

        bundle.putString(FIELD_NAME_BUNDLE_EXTRA_NAME, txtEntityFieldName.text.toString())
        bundle.putString(FIELD_VALUE_BUNDLE_EXTRA_NAME, getCurrentFieldValue())

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(AbsSavedState.EMPTY_STATE) // don't call with state as super.onRestoreInstanceState() doesn't like a Bundle as parameter value

        (state as? Bundle)?.let { bundle ->
            bundle.getString(FIELD_NAME_BUNDLE_EXTRA_NAME)?.let {  txtEntityFieldName.text = it }
            bundle.getString(FIELD_VALUE_BUNDLE_EXTRA_NAME)?.let {  edtxtEntityFieldValue.setText(it) }
        }
    }


    private fun setupUI(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = inflater.inflate(R.layout.view_edit_entity_field, this) as ViewGroup

        txtEntityFieldName = rootView.txtEntityFieldName

        edtxtEntityFieldValue = rootView.edtxtEntityFieldValue
        edtxtEntityFieldValue.addTextChangedListener(edtxtEntityFieldValueTextWatcher)
        edtxtEntityFieldValue.setOnEditorActionListener { _, actionId, keyEvent -> handleEditEntrySearchTagAction(actionId, keyEvent) }
        edtxtEntityFieldValue.setOnFocusChangeListener { _, hasFocus -> hasFocusChanged(hasFocus) }

        btnEntityFieldAction = rootView.btnEntityFieldAction

        rcySearchResult = rootView.findViewById(R.id.rcySearchResults) as MaxHeightRecyclerView
        rcySearchResult.addItemDecoration(HorizontalDividerItemDecoration(context))

        rootView.setOnClickListener { viewClicked() }

        doCustomUiInitialization(rootView)
    }

    private fun handleEditEntrySearchTagAction(actionId: Int, keyEvent: KeyEvent?): Boolean {
        if(actionId == EditorInfo.IME_ACTION_DONE || (actionId == EditorInfo.IME_NULL && keyEvent?.action == KeyEvent.ACTION_DOWN)) {
            if(disableActionOnKeyboard == false) {
                return handleActionPressed()
            }

            return disableActionOnKeyboard
        }

        return false
    }

    protected open fun handleActionPressed(): Boolean {
        return disableActionOnKeyboard
    }

    protected open fun hasFocusChanged(hasFocus: Boolean) {
        fieldValueFocusChangedListener?.invoke(hasFocus)
    }

    protected open fun viewClicked() {
        fieldClickedListener?.invoke() // remember: setOnClickListener() on an EditText only works if focusable has been set to  false -> call setFieldNameOnUiThread() with isEditable = false
    }

    protected open fun doCustomUiInitialization(rootView: ViewGroup) {

    }


    open fun setFieldNameOnUiThread(fieldNameResourceId: Int, isEditable: Boolean = true, didValueChangeListener: ((didValueChange: Boolean) -> Unit)?) {
        setFieldNameOnUiThread(fieldNameResourceId, isEditable)

        this.didValueChangeListener = didValueChangeListener
    }

    open fun setFieldNameOnUiThread(fieldNameResourceId: Int, isEditable: Boolean = true) {
        txtEntityFieldName.text = context.getString(fieldNameResourceId)

        setEditTextEntityFieldValueIsEditableOnUiThread(isEditable)
    }

    open fun getCurrentFieldValue(): String {
        return edtxtEntityFieldValue.text.toString()
    }

    open fun setFieldValueInputTypeOnUiThread(inputType: Int) {
        edtxtEntityFieldValue.inputType = inputType
    }

    open fun setFieldValueOnUiThread(fieldValue: String) {
        setEditTextEntityFieldValueOnUiThread(fieldValue)

        edtxtEntityFieldValue.setTypeface(null, Typeface.NORMAL)
        edtxtEntityFieldValue.setTextColorToColorResource(R.color.entity_field_value_text_color)

        originalValue = fieldValue
        updateDidValueChange(false)
    }

    open fun setOnboardingTextOnUiThread(onboardingTextResourceId: Int) {
        setEditTextEntityFieldValueOnUiThread(context.getString(onboardingTextResourceId))

        edtxtEntityFieldValue.setTypeface(null, Typeface.ITALIC)
        edtxtEntityFieldValue.setTextColorToColorResource(R.color.entity_field_onboarding_text_color)

        updateDidValueChange(false)
    }

    protected open fun setEditTextEntityFieldValueOnUiThread(fieldValue: String) {
        edtxtEntityFieldValue.removeTextChangedListener(edtxtEntityFieldValueTextWatcher)

        edtxtEntityFieldValue.setText(fieldValue)
        edtxtEntityFieldValue.setSelection(fieldValue.length)

        edtxtEntityFieldValue.addTextChangedListener(edtxtEntityFieldValueTextWatcher)
    }

    protected open fun setEditTextEntityFieldValueIsEditableOnUiThread(isEditable: Boolean) {
        edtxtEntityFieldValue.isFocusable = isEditable
        edtxtEntityFieldValue.isFocusableInTouchMode = isEditable
    }


    open fun showActionIconOnUiThread(iconResourceId: Int, useAccentColorAsTintColor: Boolean = true) {
        showActionIconOnUiThread(iconResourceId, useAccentColorAsTintColor, null)
    }

    open fun showActionIconOnUiThread(iconResourceId: Int, useAccentColorAsTintColor: Boolean = true, actionIconClickedListener: (() -> Unit)?) {
        setActionImageAndTintColor(iconResourceId, useAccentColorAsTintColor)

        btnEntityFieldAction.visibility = View.VISIBLE

        btnEntityFieldAction.setOnClickListener {
            edtxtEntityFieldValue.clearFocus() // ensure edtxtEntityFieldValue loses focus so that e.g. Source.publishingDate gets updated before PickDateDialog with not updated date gets shown
            btnEntityFieldAction.requestFocus()
            actionIconClickedListener?.invoke()
        }

        this.actionIconClickedListener = actionIconClickedListener
    }

    protected open fun setActionImageAndTintColor(iconResourceId: Int, useAccentColorAsTintColor: Boolean) {
        btnEntityFieldAction.setImageResource(iconResourceId)

        if(useAccentColorAsTintColor) {
            btnEntityFieldAction.setTintColor(R.color.colorAccent)
        }
        else {
            btnEntityFieldAction.clearColorFilter()
        }
    }

    open fun hideActionIconOnUiThread() {
        btnEntityFieldAction.setImageBitmap(null)

        btnEntityFieldAction.visibility = View.INVISIBLE

        btnEntityFieldAction.setOnClickListener(null)
    }


    open fun startEditing() {
        edtxtEntityFieldValue.requestFocus()
        edtxtEntityFieldValue.showKeyboard()
    }

    open fun stopEditing() {
        clearEditTextFocus()
        edtxtEntityFieldValue.hideKeyboard()
    }

    private fun clearEditTextFocus() {
        txtEntityFieldName.requestFocus() // to remove focus from EditText; therefore focusable is set to true on txtEntityFieldName
    }


    protected fun showEditTextEntityFieldValue() {
        edtxtEntityFieldValue.visibility = View.VISIBLE
        vwHorizontalLineWhenEditTextNotShown.visibility = View.GONE
    }

    protected fun hideEditTextEntityFieldValue() {
        edtxtEntityFieldValue.visibility = View.GONE
        vwHorizontalLineWhenEditTextNotShown.visibility = View.VISIBLE
    }

    protected fun showAsDoesNotAcceptInput() {
        edtxtEntityFieldValue.visibility = View.GONE
        vwHorizontalLineWhenEditTextNotShown.visibility = View.GONE

        rcySearchResults.visibility = View.VISIBLE
        (rcySearchResult.layoutParams as? MarginLayoutParams)?.topMargin = (-10 * context.resources.displayMetrics.density).toInt() // so that there's not such a big gap to first
        // item
    }

    protected fun showSearchResultsView() {
        rcySearchResult.visibility = View.VISIBLE
        vwSearchResultsDivider.visibility = View.VISIBLE
    }

    protected fun hideSearchResultsView() {
        rcySearchResult.visibility = View.GONE
        vwSearchResultsDivider.visibility = View.GONE
    }


    protected open val edtxtEntityFieldValueTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            enteredTextChanged(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

    }

    protected open fun enteredTextChanged(enteredText: String) {
        updateDidValueChange(enteredText != originalValue)
    }

    protected open fun updateDidValueChange(didValueChange: Boolean) {
        val previousValue = this.didValueChange

        this.didValueChange = didValueChange

        if(didValueChange != previousValue) {
            didValueChangeListener?.invoke(didValueChange)
        }
    }

    protected fun didCollectionChange(originalCollection: Collection<*>, editedCollection: Collection<*>): Boolean {
        if(originalCollection.size != editedCollection.size) {
            return true
        }

        val copy = ArrayList(editedCollection)
        copy.removeAll(originalCollection)
        return copy.size > 0
    }


    fun handlesBackButtonPress(): Boolean {
        if(rcySearchResult.visibility == View.VISIBLE) {
            stopEditing()

            return true
        }

        return false
    }


}