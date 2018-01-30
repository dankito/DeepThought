package net.dankito.deepthought.javafx.ui.controls.cell

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.SourceViewModel
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.Source
import tornadofx.*


class SourceListCellFragment : ListCellFragment<Source>() {

    val source = SourceViewModel().bindTo(this)


    override val root = vbox {
        cellProperty.addListener { _, _, newValue -> // so that the graphic always has cell's width
            newValue?.let { prefWidthProperty().bind(it.widthProperty().subtract(16)) }
        }

        minHeight = 50.0
        maxHeight = 74.0
        useMaxHeight = true
        alignment = Pos.CENTER_LEFT

        label(source.sourcePreview) {
            vgrow = Priority.ALWAYS

            isWrapText = true
        }

        label(source.seriesAndPublishingDatePreview) {
            maxHeight = 20.0
            visibleProperty().bind(source.hasSeriesAndPublishingDatePreview)
            FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

            vboxConstraints {
                marginTop = 6.0
            }
        }
    }

}