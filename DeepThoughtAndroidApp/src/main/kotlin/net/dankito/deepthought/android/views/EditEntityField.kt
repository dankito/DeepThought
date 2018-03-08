package net.dankito.deepthought.android.views

import android.content.Context
import android.graphics.Typeface
import android.os.Build
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


open class EditEntityField : RelativeLayout {

    companion object {
        private const val FIELD_NAME_BUNDLE_EXTRA_NAME = "FIELD_NAME"
        private const val FIELD_VALUE_BUNDLE_EXTRA_NAME = "FIELD_VALUE"
        private const val IS_EDITABLE_BUNDLE_EXTRA_NAME = "IS_EDITABLE"

        private const val IS_SECONDARY_INFORMATION_VISIBLE_BUNDLE_EXTRA_NAME = "IS_SECONDARY_INFORMATION_VISIBLE"
        private const val SECONDARY_INFORMATION_VALUE_BUNDLE_EXTRA_NAME = "SECONDARY_INFORMATION_VALUE"
    }


    protected lateinit var rootView: ViewGroup

    protected lateinit var txtEntityFieldName: TextView

    protected lateinit var edtxtEntityFieldValue: EditText

    protected lateinit var edtxtSecondaryInformationValue: EditText

    protected lateinit var btnEntityFieldAction: ImageButton

    protected lateinit var rcySearchResult: MaxHeightRecyclerView


    var didValueChange = false

    protected var originalValue: String? = null

    protected var disableActionOnKeyboard = false


    var didSecondaryInformationValueChange = false

    protected var originalSecondaryInformationValue = ""


    private var fieldNameResourceId: Int = 0

    private var secondaryInformationLabelResourceId: Int = 0

    private var secondaryInformationHintResourceId: Int? = null


    var didValueChangeListener: ((didValueChange: Boolean) -> Unit)? = null

    var didSecondaryInformationValueChangeListener: ((didValueChange: Boolean) -> Unit)? = null

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

        bundle.putInt(FIELD_NAME_BUNDLE_EXTRA_NAME, fieldNameResourceId)
        bundle.putString(FIELD_VALUE_BUNDLE_EXTRA_NAME, getCurrentFieldValue())
        bundle.putBoolean(IS_EDITABLE_BUNDLE_EXTRA_NAME, isFieldEditable)

        // TODO: also save and restore originalValue

        bundle.putBoolean(IS_SECONDARY_INFORMATION_VISIBLE_BUNDLE_EXTRA_NAME, isSecondaryInformationVisible())
        bundle.putString(SECONDARY_INFORMATION_VALUE_BUNDLE_EXTRA_NAME, getEditedSecondaryInformation())

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(AbsSavedState.EMPTY_STATE) // don't call with state as super.onRestoreInstanceState() doesn't like a Bundle as parameter value

        (state as? Bundle)?.let { bundle ->
            setFieldNameOnUiThread(bundle.getInt(FIELD_NAME_BUNDLE_EXTRA_NAME), bundle.getBoolean(IS_EDITABLE_BUNDLE_EXTRA_NAME, true))
            bundle.getString(FIELD_VALUE_BUNDLE_EXTRA_NAME)?.let {  edtxtEntityFieldValue.setText(it) } // TODO: this calls text change listener -> valueToEdit gets set to a false value

            if(secondaryInformationLabelResourceId > 0) { // still didn't figure out how that works that secondaryInformationLabelResourceId and secondaryInformationHintResourceId get automatically restored
                setupSecondaryInformation(secondaryInformationLabelResourceId, secondaryInformationHintResourceId)
            }

            if(bundle.getBoolean(IS_SECONDARY_INFORMATION_VISIBLE_BUNDLE_EXTRA_NAME)) {
                showSecondaryInformationOnUiThread()
            }

            val secondaryInformation = bundle.getString(SECONDARY_INFORMATION_VALUE_BUNDLE_EXTRA_NAME)
            setEditTextSecondaryInformationValueOnUiThread(secondaryInformation)
            postDelayed({ // don't know why but simply calling setEditTextSecondaryInformationValueOnUiThread() doesn't work, has to be done delayed
                setEditTextSecondaryInformationValueOnUiThread(secondaryInformation)
            }, 250)
        }
    }


    private fun setupUI(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = inflater.inflate(R.layout.view_edit_entity_field, this) as ViewGroup

        txtEntityFieldName = rootView.txtEntityFieldName

        edtxtEntityFieldValue = rootView.edtxtEntityFieldValue
        edtxtEntityFieldValue.addTextChangedListener(edtxtEntityFieldValueTextWatcher)
        edtxtEntityFieldValue.setOnEditorActionListener { _, actionId, keyEvent -> handleEditItemSearchTagAction(actionId, keyEvent) }
        edtxtEntityFieldValue.setOnFocusChangeListener { _, hasFocus -> hasFocusChanged(hasFocus) }

        edtxtSecondaryInformationValue = rootView.edtxtSecondaryInformationValue
        edtxtSecondaryInformationValue.addTextChangedListener(edtxtSecondaryInformationValueTextWatcher)

        btnEntityFieldAction = rootView.btnEntityFieldAction

        rcySearchResult = rootView.findViewById(R.id.rcySearchResults) as MaxHeightRecyclerView
        rcySearchResult.addItemDecoration(HorizontalDividerItemDecoration(context))

        rootView.setOnClickListener { viewClicked() }

        doCustomUiInitialization(rootView)
    }

    private fun handleEditItemSearchTagAction(actionId: Int, keyEvent: KeyEvent?): Boolean {
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
        // may be overwritten in sub classes
    }

    protected open fun doCustomUiInitialization(rootView: ViewGroup) {

    }


    open fun viewBecomesVisible() {

    }

    open fun viewGetsHidden() {

    }


    open fun setFieldNameOnUiThread(fieldNameResourceId: Int, isEditable: Boolean = true, didValueChangeListener: ((didValueChange: Boolean) -> Unit)?) {
        setFieldNameOnUiThread(fieldNameResourceId, isEditable)

        this.didValueChangeListener = didValueChangeListener
    }

    open fun setFieldNameOnUiThread(fieldNameResourceId: Int, isEditable: Boolean = true) {
        txtEntityFieldName.text = context.getString(fieldNameResourceId)
        this.fieldNameResourceId = fieldNameResourceId

        setEditTextEntityFieldValueIsEditableOnUiThread(isEditable)
    }

    open fun getCurrentFieldValue(): String {
        return edtxtEntityFieldValue.text.toString()
    }

    open fun setFieldValueInputTypeOnUiThread(inputType: Int) {
        edtxtEntityFieldValue.inputType = inputType
    }

    open fun setFieldValueOnUiThread(fieldValue: String?) {
        setEditTextEntityFieldValueOnUiThread(fieldValue ?: "")

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

    protected val isFieldEditable: Boolean
        get() = edtxtEntityFieldValue.isFocusable && edtxtEntityFieldValue.isFocusableInTouchMode


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

    protected fun showAsHasNoInputField() {
        edtxtEntityFieldValue.visibility = View.GONE
        vwHorizontalLineWhenEditTextNotShown.visibility = View.VISIBLE

        (vwHorizontalLineWhenEditTextNotShown.layoutParams as? MarginLayoutParams)?.topMargin = 0

        rcySearchResults.visibility = View.VISIBLE
        (rcySearchResult.layoutParams as? MarginLayoutParams)?.topMargin = (-10 * context.resources.displayMetrics.density).toInt() // so that there's not such a big gap to first item

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            (btnEntityFieldAction.parent as? ViewGroup)?.translationZ = 100f // so that button is shown above rcySearchResult which overlap a bit due to setting negative topMargin above
        }
    }

    protected fun showSearchResultsView() {
        rcySearchResult.visibility = View.VISIBLE
        vwSearchResultsDivider.visibility = View.VISIBLE
    }

    protected fun hideSearchResultsView() {
        rcySearchResult.visibility = View.GONE
        vwSearchResultsDivider.visibility = View.GONE
    }


    protected fun isSecondaryInformationVisible(): Boolean {
        return lytSecondaryInformation.visibility == View.VISIBLE
    }

    protected fun setupSecondaryInformation(labelResourceId: Int, hintResourceId: Int? = null) {
        this.secondaryInformationLabelResourceId = labelResourceId
        this.secondaryInformationHintResourceId = hintResourceId

        txtSecondaryInformationLabel.setText(labelResourceId)

        hintResourceId?.let {
            edtxtSecondaryInformationValue.setHint(hintResourceId)
        }
    }

    protected fun showSecondaryInformationOnUiThread() {
        lytSecondaryInformation.visibility = View.VISIBLE

        (edtxtEntityFieldValue.layoutParams as? RelativeLayout.LayoutParams)?.let { layoutParams ->
            layoutParams.addRule(RelativeLayout.BELOW, R.id.lytSecondaryInformation)
        }
    }

    protected fun showAndFocusSecondaryInformationOnUiThread() {
        showSecondaryInformationOnUiThread()

        edtxtSecondaryInformationValue.showKeyboard()
        postDelayed({ // wait till edtxtSecondaryInformationValue is displayed
            edtxtSecondaryInformationValue.showKeyboard()
        }, 250)
    }

    fun showSecondaryInformationValueOnUiThread(secondaryInformationValue: String) {
        showSecondaryInformationOnUiThread()

        setEditTextSecondaryInformationValueOnUiThread(secondaryInformationValue)

        originalSecondaryInformationValue = secondaryInformationValue
        updateDidValueChange(false)
    }

    protected fun setEditTextSecondaryInformationValueOnUiThread(secondaryInformationValue: String) {
        edtxtSecondaryInformationValue.removeTextChangedListener(edtxtSecondaryInformationValueTextWatcher)

        edtxtSecondaryInformationValue.setText(secondaryInformationValue)
        edtxtSecondaryInformationValue.setSelection(secondaryInformationValue.length)

        edtxtSecondaryInformationValue.addTextChangedListener(edtxtSecondaryInformationValueTextWatcher)
    }


    protected open val edtxtEntityFieldValueTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            enteredTextChanged(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

    }

    protected open fun enteredTextChanged(enteredText: String) {
        val valueDidNotChange = enteredText == originalValue || (originalValue == null && enteredText.isEmpty())
        updateDidValueChange( ! valueDidNotChange)
    }

    protected open fun updateDidValueChange(didValueChange: Boolean) {
        val previousValue = this.didValueChange

        this.didValueChange = didValueChange

        if(didValueChange != previousValue) {
            didValueChangeListener?.invoke(didValueChange)
        }
    }


    protected open val edtxtSecondaryInformationValueTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            enteredSecondaryInformationChanged(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

    }

    protected open fun enteredSecondaryInformationChanged(enteredText: String) {
        updateDidSecondaryInformationValueChange(enteredText != originalSecondaryInformationValue)
    }

    protected open fun updateDidSecondaryInformationValueChange(didSecondaryInformationValueChange: Boolean) {
        val previousValue = this.didSecondaryInformationValueChange

        this.didSecondaryInformationValueChange = didSecondaryInformationValueChange

        if(didSecondaryInformationValueChange != previousValue) {
            didSecondaryInformationValueChangeListener?.invoke(didSecondaryInformationValueChange)
        }
    }


    fun handlesBackButtonPress(): Boolean {
        if(rcySearchResult.visibility == View.VISIBLE) {
            stopEditing()

            return true
        }

        return false
    }

    fun getEditedSecondaryInformation() = edtxtSecondaryInformationValue.text.toString()


}