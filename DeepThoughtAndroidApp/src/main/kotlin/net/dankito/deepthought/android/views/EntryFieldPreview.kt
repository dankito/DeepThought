package net.dankito.deepthought.android.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.view_entry_field_preview.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.extensions.setTextColorToColorResource


class EntryFieldPreview : LinearLayout {


    private lateinit var txtEntryFieldName: TextView

    private lateinit var txtEntryFieldValue: TextView



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
        val rootView = inflater.inflate(R.layout.view_entry_field_preview, this)

        txtEntryFieldName = rootView.txtEntryFieldName

        txtEntryFieldValue = rootView.txtEntryFieldValue
    }


    fun setFieldNameOnUiThread(fieldName: String) {
        txtEntryFieldName.text = fieldName
    }

    fun setFieldValueOnUiThread(fieldValue: String) {
        txtEntryFieldValue.text = fieldValue

        txtEntryFieldValue.setTypeface(null, Typeface.NORMAL)
        txtEntryFieldValue.setTextColorToColorResource(R.color.entity_field_value_text_color)
    }

    fun setOnboardingTextOnUiThread(onboardingTextResourceId: Int) {
        txtEntryFieldValue.text = context.getString(onboardingTextResourceId)

        txtEntryFieldValue.setTypeface(null, Typeface.ITALIC)
        txtEntryFieldValue.setTextColorToColorResource(R.color.entity_field_onboarding_text_color)
    }


}