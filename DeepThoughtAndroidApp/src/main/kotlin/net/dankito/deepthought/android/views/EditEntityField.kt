package net.dankito.deepthought.android.views

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
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


    private lateinit var txtEntityFieldName: TextView

    private lateinit var edtxtEntityFieldValue: EditText

    private lateinit var btnEntityFieldAction: ImageButton


    private var didValueChange = false

    private var originalValue = ""


    var didValueChangeListener: ((didValueChange: Boolean) -> Unit)? = null

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


    private fun setupUI(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_edit_entity_field, this)

        txtEntityFieldName = rootView.txtEntityFieldName

        edtxtEntityFieldValue = rootView.edtxtEntityFieldValue
        edtxtEntityFieldValue.addTextChangedListener(edtxtEntityFieldValueTextWatcher)
        edtxtEntityFieldValue.setOnFocusChangeListener { _, hasFocus -> fieldValueFocusChangedListener?.invoke(hasFocus) }

        btnEntityFieldAction = rootView.btnEntityFieldAction
    }


    fun setFieldNameAndValueChangeListenerOnUiThread(fieldNameResourceId: Int, didValueChangeListener: ((didValueChange: Boolean) -> Unit)?) {
        setFieldNameOnUiThread(fieldNameResourceId)

        this.didValueChangeListener = didValueChangeListener
    }

    fun setFieldNameOnUiThread(fieldNameResourceId: Int) {
        txtEntityFieldName.text = context.getString(fieldNameResourceId)
    }

    fun getCurrentFieldValue(): String {
        return edtxtEntityFieldValue.text.toString()
    }

    fun setFieldValueOnUiThread(fieldValue: String, isEditable: Boolean = true) {
        setEditTextEntityFieldValueOnUiThread(fieldValue)
        setEditTextEntityFieldValueIsEditableOnUiThread(isEditable)

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


    fun showActionIconOnUiThread(iconResourceId: Int) {
        btnEntityFieldAction.setImageResource(iconResourceId)

        btnEntityFieldAction.visibility = View.VISIBLE

        btnEntityFieldAction.setOnClickListener { actionIconClickedListener?.invoke() }
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