package net.dankito.deepthought.javafx.dialogs.entry

import javafx.geometry.Pos
import net.dankito.deepthought.model.Source
import tornadofx.*


class SourceListCellFragment : ListCellFragment<Source>() {

    val source = SourceViewModel().bindTo(this)


    override val root = hbox {
        cellProperty.addListener { _, _, newValue -> // so that the graphic always has cell's width
            newValue?.let { prefWidthProperty().bind(it.widthProperty().subtract(16)) }
        }

        alignment = Pos.CENTER_LEFT

        label(source.preview) {
            minHeight = 32.0
            maxHeight = 32.0

            vboxConstraints {
                marginTopBottom(6.0)
            }
        }

    }
}