package net.dankito.deepthought.android.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_edit_reference.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityParameters
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Reference
import net.dankito.service.data.ReferenceService
import javax.inject.Inject


class EditReferenceActivity : BaseActivity() {

    companion object {
        private const val REFERENCE_ID_BUNDLE_EXTRA_NAME = "REFERENCE_ID"
        private const val REFERENCE_TITLE_BUNDLE_EXTRA_NAME = "REFERENCE_TITLE"
        private const val REFERENCE_SERIES_BUNDLE_EXTRA_NAME = "REFERENCE_SERIES"
        private const val REFERENCE_ISSUE_OR_PUBLISHING_DATE_BUNDLE_EXTRA_NAME = "REFERENCE_ISSUE_OR_PUBLISHING_DATE"
    }



    @Inject
    protected lateinit var referenceService: ReferenceService


    private var reference: Reference? = null


    init {
        AppComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        showParameters(getParameters() as? EditReferenceActivityParameters)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(REFERENCE_ID_BUNDLE_EXTRA_NAME)?.let { referenceId -> showReference(referenceId) }

        savedInstanceState.getString(REFERENCE_TITLE_BUNDLE_EXTRA_NAME)?.let { edtxtTitle.setText(it) }
        savedInstanceState.getString(REFERENCE_SERIES_BUNDLE_EXTRA_NAME)?.let { edtxtSeries.setText(it) }
        savedInstanceState.getString(REFERENCE_ISSUE_OR_PUBLISHING_DATE_BUNDLE_EXTRA_NAME)?.let { edtxtIssueOrPublishingDate.setText(it) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            outState.putString(REFERENCE_ID_BUNDLE_EXTRA_NAME, reference?.id)

            outState.putString(REFERENCE_TITLE_BUNDLE_EXTRA_NAME, edtxtTitle.text.toString())
            outState.putString(REFERENCE_SERIES_BUNDLE_EXTRA_NAME, edtxtSeries.text.toString())
            outState.putString(REFERENCE_ISSUE_OR_PUBLISHING_DATE_BUNDLE_EXTRA_NAME, edtxtIssueOrPublishingDate.text.toString())
        }
    }

    private fun setupUI() {
        setContentView(R.layout.activity_edit_reference)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = ""
    }


    private fun showParameters(parameters: EditReferenceActivityParameters?) {
        parameters?.reference?.let { reference ->
            showReference(reference)
        }
    }

    private fun showReference(referenceId: String) {
        referenceService.retrieve(referenceId)?.let { reference ->
            showReference(reference)
        }
    }

    private fun showReference(reference: Reference) {
        this.reference = reference

        edtxtTitle.setText(reference.title)
        edtxtSeries.setText(reference.series)
        edtxtIssueOrPublishingDate.setText(reference.issueOrPublishingDate)
    }

}