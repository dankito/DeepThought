package net.dankito.deepthought.android.views

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.AbsSavedState
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.view_edit_entity_field.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.extensions.setTextColorToColorResource


class EditEntityField : RelativeLayout {

    companion object {
        private const val FIELD_NAME_BUNDLE_EXTRA_NAME = "FIELD_NAME"
        private const val FIELD_VALUE_BUNDLE_EXTRA_NAME = "FIELD_VALUE"
    }


    private lateinit var txtEntityFieldName: TextView

    private lateinit var edtxtEntityFieldValue: EditText

    private lateinit var btnEntityFieldAction: ImageButton


    private var didValueChange = false

    private var originalValue = ""


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
        val rootView = inflater.inflate(R.layout.view_edit_entity_field, this)

        txtEntityFieldName = rootView.txtEntityFieldName
        txtEntityFieldName.setOnClickListener { fieldClickedListener?.invoke() }

        edtxtEntityFieldValue = rootView.edtxtEntityFieldValue
        edtxtEntityFieldValue.addTextChangedListener(edtxtEntityFieldValueTextWatcher)
        edtxtEntityFieldValue.setOnClickListener { fieldClickedListener?.invoke() } // remember: setOnClickListener() on an EditText only works if focusable has been set to  false -> call setFieldNameOnUiThread() with isEditable = false
        edtxtEntityFieldValue.setOnFocusChangeListener { _, hasFocus -> fieldValueFocusChangedListener?.invoke(hasFocus) }

        btnEntityFieldAction = rootView.btnEntityFieldAction
    }


    fun setFieldNameOnUiThread(fieldNameResourceId: Int, isEditable: Boolean = true, didValueChangeListener: ((didValueChange: Boolean) -> Unit)?) {
        setFieldNameOnUiThread(fieldNameResourceId, isEditable)

        this.didValueChangeListener = didValueChangeListener
    }

    fun setFieldNameOnUiThread(fieldNameResourceId: Int, isEditable: Boolean = true) {
        txtEntityFieldName.text = context.getString(fieldNameResourceId)

        setEditTextEntityFieldValueIsEditableOnUiThread(isEditable)
    }

    fun getCurrentFieldValue(): String {
        return edtxtEntityFieldValue.text.toString()
    }

    fun setFieldValueInputTypeOnUiThread(inputType: Int) {
        edtxtEntityFieldValue.inputType = inputType
    }

    fun setFieldValueOnUiThread(fieldValue: String) {
        setEditTextEntityFieldValueOnUiThread(fieldValue)

        edtxtEntityFieldValue.setTypeface(null, Typeface.NORMAL)
        edtxtEntityFieldValue.setTextColorToColorResource(R.color.entity_field_value_text_color)

        originalValue = fieldValue
        updateDidValueChange(false)
    }

    fun setOnboardingTextOnUiThread(onboardingTextResourceId: Int) {
        setEditTextEntityFieldValueOnUiThread(context.getString(onboardingTextResourceId))

        edtxtEntityFieldValue.setTypeface(null, Typeface.ITALIC)
        edtxtEntityFieldValue.setTextColorToColorResource(R.color.entity_field_onboarding_text_color)

        updateDidValueChange(false)
    }

    private fun setEditTextEntityFieldValueOnUiThread(fieldValue: String) {
        edtxtEntityFieldValue.removeTextChangedListener(edtxtEntityFieldValueTextWatcher)

        edtxtEntityFieldValue.setText(fieldValue)

        edtxtEntityFieldValue.addTextChangedListener(edtxtEntityFieldValueTextWatcher)
    }

    private fun setEditTextEntityFieldValueIsEditableOnUiThread(isEditable: Boolean) {
        edtxtEntityFieldValue.isFocusable = isEditable
        edtxtEntityFieldValue.isFocusableInTouchMode = isEditable
    }


    fun showActionIconOnUiThread(iconResourceId: Int, actionIconClickedListener: (() -> Unit)?) {
        showActionIconOnUiThread(iconResourceId)

        this.actionIconClickedListener = actionIconClickedListener
    }

    fun showActionIconOnUiThread(iconResourceId: Int) {
        btnEntityFieldAction.setImageResource(iconResourceId)

        btnEntityFieldAction.visibility = View.VISIBLE

        btnEntityFieldAction.setOnClickListener {
            edtxtEntityFieldValue.clearFocus() // ensure edtxtEntityFieldValue loses focus so that e.g. Reference.publishingDate gets updated before PickDateDialog with not updated date gets shown
            btnEntityFieldAction.requestFocus()
            actionIconClickedListener?.invoke()
        }
    }

    fun hideActionIconOnUiThread() {
        btnEntityFieldAction.visibility = View.INVISIBLE

        btnEntityFieldAction.setOnClickListener(null)
    }


    private val edtxtEntityFieldValueTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            updateDidValueChange(editable.toString() != originalValue)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

    }

    private fun updateDidValueChange(didValueChange: Boolean) {
        val previousValue = this.didValueChange

        this.didValueChange = didValueChange

        if(didValueChange != previousValue) {
            didValueChangeListener?.invoke(didValueChange)
        }
    }


}