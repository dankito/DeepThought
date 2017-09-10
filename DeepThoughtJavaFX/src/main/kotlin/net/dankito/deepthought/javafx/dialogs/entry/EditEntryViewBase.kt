package net.dankito.deepthought.javafx.dialogs.entry

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.previewWithSeriesAndPublishingDate
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditEntryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import tornadofx.*
import javax.inject.Inject


abstract class EditEntryViewBase : DialogFragment() {

    // param values for Entry and EntryExtractionResult are evaluated after root has been initialized -> Entry is null at root initialization stage.
    // so i had to find a way to mitigate that Entry / EntryExtractionResult is not initialized yet

    protected val abstractPlainText = SimpleStringProperty()

    protected val referencePreview = SimpleStringProperty()

    protected val tagsPreview = SimpleStringProperty()

    protected val contentHtml = SimpleStringProperty()

    protected val hasUnsavedChanges = SimpleBooleanProperty()


    private var txtAbstract: Label by singleAssign()

    private var txtReference: Label by singleAssign()

    private var txtTags: Label by singleAssign()

    private var wbContent: WebView by singleAssign()


    private val presenter: EditEntryPresenter


    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter


    init {
        AppComponent.component.inject(this)

        presenter = EditEntryPresenter(entryPersister, readLaterArticleService, clipboardService, router)
    }


    override var root = vbox {
        prefWidth = 905.0
        prefHeight = 650.0

        hbox {
            prefHeight = 50.0
            maxHeight = 100.0
            prefWidthProperty().bind(this@vbox.widthProperty())

            label(messages["edit.entry.abstract.label"]) {
                minWidth = Control.USE_PREF_SIZE // guarantee that label keeps its calculated size
                useMaxWidth = true
            }

            txtAbstract = label {
                prefHeight = 50.0
                maxHeight = 100.0
                isWrapText = true

                textProperty().bind(abstractPlainText)
            }

            vboxConstraints {
                marginBottom = 6.0
            }
        }

        hbox {
            prefHeight = 50.0
            maxHeight = 70.0
            prefWidthProperty().bind(this@vbox.widthProperty())

            label(messages["edit.entry.reference.label"]) {
                minWidth = Control.USE_PREF_SIZE
                useMaxWidth = true
            }

            txtReference = label {
                isWrapText = false

                textProperty().bind(referencePreview)

                hgrow = Priority.ALWAYS
            }

            vboxConstraints {
                marginBottom = 6.0
            }
        }

        hbox {
            prefHeight = 50.0
            maxHeight = 70.0
            prefWidthProperty().bind(this@vbox.widthProperty())

            label(messages["edit.entry.tags.label"]) {
                minWidth = Control.USE_PREF_SIZE
                useMaxWidth = true
            }

            txtTags = label {
                isWrapText = false

                textProperty().bind(tagsPreview)

                hgrow = Priority.ALWAYS
            }

            vboxConstraints {
                marginBottom = 6.0
            }
        }

        wbContent = webview {
            vgrow = Priority.ALWAYS
            useMaxWidth = true

            prefWidthProperty().bind(this@vbox.widthProperty())

            contentHtml.onChange { engine.loadContent(contentHtml.value) }
        }

        anchorpane {

            hbox {
                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }

                button("Cancel") {
                    minHeight = 40.0
                    maxHeight = 40.0
                    prefWidth = 150.0
                    action { closeDialog() }

                    hboxConstraints {
                        marginRight = 12.0
                    }
                }

                button("Save") {
                    minHeight = 40.0
                    maxHeight = 40.0
                    prefWidth = 150.0

                    disableProperty().bind(hasUnsavedChanges)

                    action { saveEntryAndCloseDialog() }
                }
            }
        }
    }


    private fun cleanUp() {
        contentHtml.onChange { }
        wbContent.prefWidthProperty().unbind()

        // Delete cache for navigate back
        wbContent.engine.load("about:blank")
        wbContent.engine.history.entries.clear()
        // Delete cookies
        java.net.CookieHandler.setDefault(java.net.CookieManager())

        root.getChildren().remove(wbContent)

        System.gc()
    }


    protected fun showReferencePreview(reference: Reference?) {
        this.referencePreview.value = reference?.previewWithSeriesAndPublishingDate ?: ""
    }

    protected fun showTagsPreview(tags: Collection<Tag>) {
        this.tagsPreview.value = tags.sortedBy { it.name.toLowerCase() }.joinToString { it.name }
    }


    protected open fun saveEntryAndCloseDialog() {
        presenter.saveEntryAsync(getEntryForSaving(), getReferenceForSaving(), getTagsForSaving()) {
            entrySaved()

            runLater { closeDialog() }
        }
    }

    protected open fun entrySaved() {

    }

    protected open fun closeDialog() {
        cleanUp()

        close()
    }

    protected abstract fun getEntryForSaving(): Entry

    protected abstract fun getReferenceForSaving(): Reference?

    protected abstract fun getTagsForSaving(): List<Tag>

}