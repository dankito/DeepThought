package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.view_entry_field_preview.view.*
import net.dankito.deepthought.android.R


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


    fun setFieldOnUIThread(fieldName: String, fieldValue: String) {
        txtEntryFieldName.text = fieldName

        txtEntryFieldValue.text = fieldValue
    }


}