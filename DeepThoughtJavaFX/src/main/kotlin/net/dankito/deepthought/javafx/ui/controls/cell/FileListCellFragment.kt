package net.dankito.deepthought.javafx.ui.controls.cell

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.OverrunStyle
import net.dankito.deepthought.javafx.ui.controls.viewmodel.FileViewModel
import net.dankito.deepthought.model.DeepThoughtFileLink
import net.dankito.deepthought.ui.presenter.FileListPresenter
import tornadofx.*


class FileListCellFragment : ListCellFragment<DeepThoughtFileLink>() {

    val file = FileViewModel().bindTo(this)

    private val formattedFileSize = SimpleStringProperty("")

    private val fileUriOrSynchronizationState = SimpleStringProperty("")

    private var presenter: FileListPresenter? = null


    init {
        file.itemProperty.addListener { _, _, _ -> updateItem() }
    }

    override val root = vbox {
        cellProperty.addListener { _, _, newValue ->
            // so that the graphic always has cell's width
            newValue?.let { prefWidthProperty().bind(it.widthProperty().subtract(16)) }

            this@FileListCellFragment.presenter = newValue?.listView?.userData as? FileListPresenter
            updateItem()
        }

        borderpane {
            alignment = Pos.CENTER_LEFT

            left {
                label(file.name) {
                    alignment = Pos.CENTER_LEFT
                    textOverrun = OverrunStyle.CENTER_ELLIPSIS

                    minHeight = 32.0
                    maxHeight = 32.0

                    borderpaneConstraints {
                        marginRight = 12.0
                    }
                }
            }

            right {
                label(formattedFileSize) {
                    alignment = Pos.CENTER_LEFT

                    minHeight = 32.0
                    maxHeight = 32.0
                    useMaxWidth = true
                }
            }
        }

        label(fileUriOrSynchronizationState) {
            alignment = Pos.CENTER_LEFT
            isWrapText = true
            textOverrun = OverrunStyle.CENTER_ELLIPSIS

            minHeight = 32.0
            maxHeight = 32.0 // 32.0 is enough for two lines

            vboxConstraints {
                marginTop = 6.0
            }
        }

        updateItem()
    }


    private fun updateItem() {
        updateFileSize()
        updateFileUri()
    }

    private fun updateFileSize() {
        formattedFileSize.value = presenter?.formatFileSize(file.fileSize.value.toLong()) ?: ""
    }

    private fun updateFileUri() {
        fileUriOrSynchronizationState.value = presenter?.getUriOrSynchronizationState(file.item) ?: ""
    }

}