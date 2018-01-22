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
import net.dankito.deepthought.ui.presenter.FileListPresenter
import net.dankito.utils.extensions.didCollectionChange
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.Colors
import net.dankito.utils.ui.IApplicationsService
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


    val didValueChange = SimpleBooleanProperty()

    private val files = FXCollections.observableArrayList<FileLink>()

    private var originalFiles: MutableCollection<FileLink> = ArrayList()

    private val listViewHeight = SimpleDoubleProperty(90.0)

    private val fileListPresenter: FileListPresenter


    init {
        AppComponent.component.inject(this)

        fileListPresenter = FileListPresenter(fileManager, applicationsService, localization)
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

                    action { selectFileToAdd() }
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




    fun setFiles(originalFiles: MutableCollection<FileLink>) {
        this.originalFiles = originalFiles

        fileListPresenter.ensureLocalFileInfoIsSet(originalFiles)

        files.setAll(originalFiles) // make a copy to not edit original files

        updateListViewHeight()
    }

    fun getEditedFiles(): Collection<FileLink> {
        return files
    }


    private fun selectFileToAdd() {
        val fileChooserDialog = FileChooser()

        fileChooserDialog.showOpenDialog(currentStage)?.let { file ->
            addFile(file.absoluteFile)
        }
    }

    private fun addFile(file: File) {
        val localFile = fileManager.createLocalFile(file)
        files.add(localFile)

        updateListViewHeight()
        updateDidValueChange()
    }

    private fun removeFile(file: FileLink) {
        files.remove(file)

        updateListViewHeight()
        updateDidValueChange()
    }

    private fun updateListViewHeight() {
        listViewHeight.value = files.size * 80.0 // cell height is 70.0 plus a padding of 80
    }

    private fun updateDidValueChange() {
        didValueChange.value = originalFiles.didCollectionChange(files)
    }


    private fun showFile(file: FileLink?) {
        file?.let {
            fileListPresenter.showFile(file)
        }
    }

}