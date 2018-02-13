package net.dankito.deepthought.javafx.ui.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.ui.controls.cell.FileListCellFragment
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.FileListPresenter
import net.dankito.service.data.messages.FileChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.extensions.didCollectionChange
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.Colors
import net.dankito.utils.ui.IApplicationsService
import net.engio.mbassy.listener.Handler
import tornadofx.*
import java.io.File
import javax.inject.Inject


class EditEntityFilesField : View() {

    @Inject
    protected lateinit var fileManager: FileManager

    @Inject
    protected lateinit var applicationsService: IApplicationsService

    @Inject
    protected lateinit var localization: Localization

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var eventBus: IEventBus


    val didValueChange = SimpleBooleanProperty()

    private val files = FXCollections.observableArrayList<FileLink>()

    private var originalFiles: MutableCollection<FileLink> = ArrayList()

    private var sourceForFile: Source? = null

    private val listViewHeight = SimpleDoubleProperty(90.0)

    private val fileListPresenter: FileListPresenter

    private val eventBusListener = EventBusListener()


    init {
        AppComponent.component.inject(this)

        fileListPresenter = FileListPresenter(fileManager, applicationsService, localization, router)

        eventBus.register(eventBusListener)
    }


    override val root = vbox {

        borderpane {
            left {
                label(messages["edit.entity.files.field.files.label"]) {
                    borderpaneConstraints {
                        alignment = Pos.CENTER_LEFT
                    }
                }
            }

            right {
                button("+") {
                    prefHeight = 20.0
                    prefWidth = prefHeight
                    font = Font.font(font.family, FontWeight.BOLD, 16.0)
                    textFill = Color.valueOf(Colors.AddButtonHexColor)

                    action { selectFilesToAdd() }
                }
            }
        }

        listview(files) {
            minHeightProperty().bind(listViewHeight)
            maxHeightProperty().bind(listViewHeight)

            userData = fileListPresenter // bad code design, but found no other way to pass fileListPresenter on ArticleSummaryItemListCellFragment

            cellFragment(FileListCellFragment::class)

            onDoubleClick { showFile(selectionModel.selectedItem) }

            vboxConstraints {
                marginTop = 4.0
            }

            contextmenu {
                item(messages["edit.entity.files.field.files.context.menu.open.containing.directory"]) {
                    action {
                        selectedItem?.let { fileListPresenter.openContainingDirectoryOfFile(it) }
                    }
                }

                separator()

                item(messages["edit.entity.files.field.files.context.menu.remove.file"]) {
                    action {
                        selectedItem?.let { removeFile(it) }
                    }
                }
            }
        }
    }




    fun setFiles(originalFiles: MutableCollection<FileLink>, sourceForFile: Source? = null) {
        this.originalFiles = originalFiles
        this.sourceForFile = sourceForFile

        fileListPresenter.forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(originalFiles)

        files.setAll(originalFiles) // make a copy to not edit original files

        updateListViewHeight()
    }

    fun getEditedFiles(): Collection<FileLink> {
        return files
    }


    private fun selectFilesToAdd() {
        val fileChooserDialog = FileChooser()

        fileChooserDialog.showOpenMultipleDialog(currentStage)?.let { files ->
            files.forEach { file ->
                addLocalFile(file.absoluteFile)
            }
        }
    }

    private fun addLocalFile(file: File) {
        val localFile = fileManager.createLocalFile(file)

        addFile(localFile)
    }

    private fun addFile(file: FileLink) {
        files.add(file)

        updateListViewHeight()
        updateDidValueChange()
    }

    private fun removeFile(file: FileLink) {
        files.remove(file)

        updateListViewHeight()
        updateDidValueChange()
    }

    private fun updateListViewHeight() {
        var height = files.size * 80.0 // cell height is 70.0 plus a padding of 80
        if(height > 200.0) {
            height = 200.0
        }

        listViewHeight.value = height
    }

    private fun updateDidValueChange() {
        didValueChange.value = originalFiles.didCollectionChange(files)
    }


    private fun showFile(file: FileLink?) {
        file?.let {
            fileListPresenter.showFile(file, sourceForFile)
        }
    }


    inner class EventBusListener {

        @Handler
        fun fileChanged(change: FileChanged) {
            if(files.contains(change.entity)) {
                runLater {
                    val backup = ArrayList(files)
                    files.clear()
                    files.setAll(backup)
                }
            }
        }

    }

}