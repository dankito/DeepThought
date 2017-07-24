package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.view_entry_fields_preview.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.extensions.abstractPlainText
import net.dankito.deepthought.extensions.preview
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.EntryField
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult


class EntryFieldsPreview : RelativeLayout {

    var entry: Entry? = null

    var readLaterArticle: ReadLaterArticle? = null

    var entryExtractionResult: EntryExtractionResult? = null

    var tagsOnEntry: Collection<Tag> = ArrayList()


    private lateinit var txtEntryAbstract: TextView

    private lateinit var txtEntryReference: TextView

//    private lateinit var txtTagsOnEntry: TextView


    var fieldClickedListener: ((EntryField) -> Unit)? = null



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
        val rootView = inflater.inflate(R.layout.view_entry_fields_preview, this)

        txtEntryAbstract = rootView.txtEntryAbstract
        rootView.lytAbstract.setOnClickListener { callFieldClickedListener(EntryField.Abstract) }

        txtEntryReference = rootView.txtEntryReference
        rootView.lytReference.setOnClickListener { callFieldClickedListener(EntryField.Reference) }

//        txtTagsOnEntry = rootView.txtTagsOnEntry
        rootView.lytTagsOnEntry.setOnClickListener { callFieldClickedListener(EntryField.Tags) }
    }

    private fun callFieldClickedListener(field: EntryField) {
        fieldClickedListener?.invoke(field)
    }


    fun setAbstractPreviewOnUIThread() {
        entry?.let { txtEntryAbstract.text = it.abstractPlainText }

        readLaterArticle?.entryExtractionResult?.entry?.let { txtEntryAbstract.text = it.abstractPlainText }

        entryExtractionResult?.entry?.let { txtEntryAbstract.text = it.abstractPlainText }
    }

    fun setReferencePreviewOnUIThread() {
        entry?.reference?.let { txtEntryReference.text = it.preview }

        readLaterArticle?.entryExtractionResult?.reference?.let { txtEntryReference.text = it.preview }

        entryExtractionResult?.reference?.let { txtEntryReference.text = it.preview }
    }

    fun setTagsOnEntryPreviewOnUIThread() {
        if(txtTagsOnEntry != null) {
            txtTagsOnEntry.text = tagsOnEntry.sortedBy { it.name.toLowerCase() }.joinToString { it.name }
        }
    }


}