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
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditSourcePresenter
import net.dankito.deepthought.ui.windowdata.EditSourceWindowData
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.datetime.asLocalDate
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService
import tornadofx.*
import javax.inject.Inject


class EditSourceDialog : DialogFragment() {

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


    private lateinit var editSourceWindowData: EditSourceWindowData

    protected val hasUnsavedChanges = SimpleBooleanProperty()

    private var didPostResult = false


    init {
        AppComponent.component.inject(this)

        presenter = EditSourcePresenter(router, dialogService, clipboardService, deleteEntityService, sourcePersister)

        publishingDateField = EditSourcePublishingDateField(presenter)

        (windowData as? EditSourceWindowData)?.let { editSourceWindowData ->
            this.editSourceWindowData = editSourceWindowData

            initFieldValues(editSourceWindowData)

            restoreEditedValues(editSourceWindowData)
        }
    }


    override val root = vbox {
        prefWidth = 850.0

        scrollpane {
            isFitToWidth = true
            isFitToHeight = true

            vbox {
                setupEntityField(titleField, this)

                add(editSeriesField)
                editSeriesField.didEntityChange.addListener { _, _, _ -> updateHasUnsavedChanges() }
                editSeriesField.didTitleChange.addListener { _, _, _ -> updateHasUnsavedChanges() }

                setupEntityField(issueField, this)

                setupEntityField(lengthField, this)

                setupEntityField(publishingDateField, this)

                setupEntityField(webAddressField,  this)

                editFilesField.didValueChange.addListener { _, _, _ -> updateHasUnsavedChanges() }
                editFilesField.setFiles(mutableListOf())
                add(editFilesField)
            }
        }

        val buttons = DialogButtonBar({ closeDialog() }, { saveSource(it) }, hasUnsavedChanges, messages["action.save"])
        add(buttons)
    }

    private fun setupEntityField(entityField: EditEntityField, pane: Parent) {
        entityField.didValueChange.addListener { _, _, _ -> updateHasUnsavedChanges() }

        pane.add(entityField)
    }

    private fun initFieldValues(windowData: EditSourceWindowData) {
        val source = windowData.source

        titleField.value = windowData.editedSourceTitle ?: source.title

        editSeriesField.setSeriesToEdit(windowData.series ?: source.series)

        issueField.value = source.issue ?: ""

        lengthField.value = source.length ?: ""

        publishingDateField.value = source.publishingDateString ?: ""

        webAddressField.value = source.url ?: ""

        source.publishingDate.asLocalDate()?.let { publishingDateField.selectedDate = it }

        editFilesField.setFiles(source.attachedFiles, source)
    }

    private fun restoreEditedValues(windowData: EditSourceWindowData) {
        windowData.editedTitle?.let {
            titleField.setCurrentValue(it)
        }

        windowData.editedSeriesTitle?.let {
            editSeriesField.enteredTitle = it
        }

        windowData.editedIssue?.let {
            issueField.setCurrentValue(it)
        }

        windowData.editedLength?.let {
            lengthField.setCurrentValue(it)
        }

        windowData.editedPublishingDateString?.let {
            publishingDateField.setCurrentValue(it)
        }

        windowData.editedUrl?.let {
            webAddressField.setCurrentValue(it)
        }

        windowData.editedFiles?.let {
            editFilesField.setEditedFiles(it.toMutableList())
        }
    }

    private fun updateHasUnsavedChanges() {
        hasUnsavedChanges.value = titleField.didValueChange.value or
                editSeriesField.didEntityChange.value or editSeriesField.didTitleChange.value or
                issueField.didValueChange.value or lengthField.didValueChange.value or
                publishingDateField.didValueChange.value or webAddressField.didValueChange.value or editFilesField.didValueChange.value
    }


    private fun saveSource(done: () -> Unit) {
        val source = editSourceWindowData.source

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


    override val windowDataClass = EditSourceWindowData::class.java

    override fun getCurrentWindowData(): Any? {
        editSourceWindowData.editedTitle = titleField.value

        editSourceWindowData.editedSeriesTitle = editSeriesField.enteredTitle

        editSourceWindowData.editedIssue = issueField.value

        editSourceWindowData.editedLength = lengthField.value

        editSourceWindowData.editedPublishingDateString = publishingDateField.value

        editSourceWindowData.editedUrl = webAddressField.value

        editSourceWindowData.editedFiles = editFilesField.getEditedFiles()


        return editSourceWindowData
    }

}