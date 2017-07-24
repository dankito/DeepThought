package net.dankito.deepthought.android.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_edit_reference.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityParameters
import net.dankito.deepthought.android.di.AppComponent


class EditReferenceActivity : BaseActivity() {


    init {
        AppComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        showParameters(getParameters() as? EditReferenceActivityParameters)
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
            edtxtTitle.setText(reference.title)
            edtxtSeries.setText(reference.series)
            edtxtIssueOrPublishingDate.setText(reference.issueOrPublishingDate)
        }
    }

}