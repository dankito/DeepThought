package net.dankito.deepthought.javafx.dialogs.source

import com.sun.prism.impl.Disposer.cleanUp
import javafx.beans.property.SimpleBooleanProperty
import net.dankito.deepthought.data.ReferencePersister
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.ui.controls.DialogButtonBar
import net.dankito.deepthought.javafx.ui.controls.EditDateFieldValueView
import net.dankito.deepthought.javafx.ui.controls.EditFieldValueView
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditReferencePresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.IClipboardService
import tornadofx.*
import javax.inject.Inject


class EditSourceDialog : DialogFragment() {

    @Inject
    protected lateinit var referencePersister: ReferencePersister

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService


    private val titleField = EditFieldValueView(messages["edit.source.title"])

    private val issueField = EditFieldValueView(messages["edit.source.issue"])

    private val publishingDateField = EditDateFieldValueView(messages["edit.source.publishing.date"])

    private val webAddressField = EditFieldValueView(messages["edit.source.web.address"])


    private val presenter: EditReferencePresenter


    val source: Source by param()

    private var series: Series? = source.series

    protected val hasUnsavedChanges = SimpleBooleanProperty()


    init {
        AppComponent.component.inject(this)

        presenter = EditReferencePresenter(router, clipboardService, deleteEntityService, referencePersister)
    }


    override val root = vbox {
        prefWidth = 850.0

        setupEntityField(titleField, source.title)
        add(titleField)

        setupEntityField(issueField, source.issue ?: "")
        add(issueField)

        setupEntityField(publishingDateField, source.publishingDateString ?: "")
        add(publishingDateField)

        setupEntityField(webAddressField, source.url ?: "")
        add(webAddressField)

        val buttons = DialogButtonBar({ closeDialog() }, { saveSource(it) }, hasUnsavedChanges, messages["action.save"])
        add(buttons)
    }

    private fun setupEntityField(field: EditFieldValueView, value: String) {
        field.value = value

        field.didValueChange.addListener { _, _, _ -> setHasUnsavedChanges() }
    }

    private fun setHasUnsavedChanges() {
        hasUnsavedChanges.value = titleField.didValueChange.value or issueField.didValueChange.value or publishingDateField.didValueChange.value or
                webAddressField.didValueChange.value
    }


    private fun saveSource(done: () -> Unit) {
        source.title = titleField.value
        source.issue = if(issueField.value.isBlank()) null else issueField.value
        source.url = if(webAddressField.value.isBlank()) null else webAddressField.value

        presenter.saveReferenceAsync(source, series, null, publishingDateField.value) {
            done()
        }
    }

    private fun closeDialog() {
        runLater {
            cleanUp()

            close()
        }
    }

}