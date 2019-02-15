package net.dankito.deepthought.javafx.dialogs.source

import com.sun.prism.impl.Disposer.cleanUp
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Parent
import net.dankito.deepthought.data.SourcePersister
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.dialogs.source.controls.EditSourcePublishingDateField
import net.dankito.deepthought.javafx.dialogs.source.controls.EditSourceSeriesField
import net.dankito.deepthought.javafx.service.events.EditingSourceDoneEvent
import net.dankito.deepthought.javafx.ui.controls.DialogButtonBar
import net.dankito.deepthought.javafx.ui.controls.EditEntityField
import net.dankito.deepthought.javafx.ui.controls.EditEntityFilesField
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditSourcePresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.datetime.asLocalDate
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService
import tornadofx.*
import javax.inject.Inject


class EditSourceDialog : DialogFragment() {

    companion object {
        private val SeriesNullObject = Series("")
    }


    @Inject
    protected lateinit var sourcePersister: SourcePersister

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var eventBus: IEventBus


    private val titleField = EditEntityField(messages["edit.source.title"])

    private val editSeriesField = EditSourceSeriesField()

    private val issueField = EditEntityField(messages["edit.source.issue"])

    private val lengthField = EditEntityField(messages["edit.source.length"])

    private val publishingDateField: EditSourcePublishingDateField

    private val webAddressField = EditEntityField(messages["edit.source.web.address"])

    private val editFilesField = EditEntityFilesField()


    private val presenter: EditSourcePresenter


    val source: Source by param()

    val seriesParam: Series? by param(SeriesNullObject) // by param() doesn't seem to like when passing null - on calling get() an exception gets thrown

    val editedSourceTitle: String? by param<String?>(source.title)

    protected val hasUnsavedChanges = SimpleBooleanProperty()

    private var didPostResult = false


    init {
        AppComponent.component.inject(this)

        presenter = EditSourcePresenter(router, dialogService, clipboardService, deleteEntityService, sourcePersister)

        publishingDateField = EditSourcePublishingDateField(presenter, source.publishingDate.asLocalDate())
    }


    override val root = vbox {
        prefWidth = 850.0

        scrollpane {
            isFitToWidth = true
            isFitToHeight = true

            vbox {
                setupEntityField(titleField, editedSourceTitle ?: source.title, this)

                add(editSeriesField)
                editSeriesField.didEntityChange.addListener { _, _, _ -> updateHasUnsavedChanges() }
                editSeriesField.didTitleChange.addListener { _, _, _ -> updateHasUnsavedChanges() }
                editSeriesField.setSeriesToEdit(if(seriesParam != SeriesNullObject) seriesParam else source.series)

                setupEntityField(issueField, source.issue ?: "", this)

                setupEntityField(lengthField, source.length ?: "", this)

                setupEntityField(publishingDateField, source.publishingDateString ?: "", this)

                setupEntityField(webAddressField, source.url ?: "", this)

                editFilesField.didValueChange.addListener { _, _, _ -> updateHasUnsavedChanges() }
                editFilesField.setFiles(source.attachedFiles, source)
                add(editFilesField)
            }
        }

        val buttons = DialogButtonBar({ closeDialog() }, { saveSource(it) }, hasUnsavedChanges, messages["action.save"])
        add(buttons)
    }

    private fun setupEntityField(entityField: EditEntityField, value: String, pane: Parent) {
        entityField.value = value

        entityField.didValueChange.addListener { _, _, _ -> updateHasUnsavedChanges() }

        pane.add(entityField)
    }

    private fun updateHasUnsavedChanges() {
        hasUnsavedChanges.value = titleField.didValueChange.value or
                editSeriesField.didEntityChange.value or editSeriesField.didTitleChange.value or
                issueField.didValueChange.value or lengthField.didValueChange.value or
                publishingDateField.didValueChange.value or webAddressField.didValueChange.value or editFilesField.didValueChange.value
    }


    private fun saveSource(done: () -> Unit) {
        source.title = titleField.value
        source.issue = if(issueField.value.isBlank()) null else issueField.value
        source.length = if(lengthField.value.isBlank()) null else lengthField.value
        source.url = if(webAddressField.value.isBlank()) null else webAddressField.value

        val series = updateSeries()

        presenter.saveSourceAsync(source, series, publishingDateField.selectedDate.asUtilDate(), publishingDateField.value, editFilesField.getEditedFiles()) {
            postResult(EditingSourceDoneEvent(true, source))
            done()
        }
    }

    private fun updateSeries(): Series? {
        var series = editSeriesField.seriesToEdit

        if(editSeriesField.didTitleChange.value) {
            series?.title = editSeriesField.enteredTitle
        }

        if(series?.isPersisted() == false && editSeriesField.enteredTitle.isNullOrBlank()) {
            series = null
        }

        return series
    }

    private fun closeDialog() {
        runLater {
            cleanUp()

            close()

            postResult(EditingSourceDoneEvent(false))
        }
    }

    private fun postResult(event: EditingSourceDoneEvent) {
        if(didPostResult == false) {
            eventBus.postAsync(event)
        }

        didPostResult = true
    }

}