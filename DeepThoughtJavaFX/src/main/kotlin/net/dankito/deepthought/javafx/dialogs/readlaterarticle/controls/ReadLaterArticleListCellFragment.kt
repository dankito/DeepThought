package net.dankito.deepthought.javafx.dialogs.readlaterarticle.controls

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import net.dankito.deepthought.javafx.dialogs.readlaterarticle.model.ReadLaterArticleViewModel
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.utils.javafx.util.FXUtils
import tornadofx.*


class ReadLaterArticleListCellFragment : ListCellFragment<ReadLaterArticle>() {

    val article = ReadLaterArticleViewModel().bindTo(this)


    override val root = hbox {
        cellProperty.addListener { _, _, newValue -> // so that the graphic always has cell's width
            newValue?.let { prefWidthProperty().bind(it.widthProperty().subtract(16)) }
        }

        alignment = Pos.CENTER
        minHeight = 100.0
        prefHeight = 100.0

        imageview(article.previewImageUrl) {
            isPreserveRatio = true
            fitHeight = 100.0
            fitWidth = 120.0
        }

        vbox {
            useMaxHeight = true
            prefHeight = Region.USE_COMPUTED_SIZE
            alignment = Pos.CENTER_LEFT

            hboxConstraints {
                hgrow = Priority.ALWAYS
                marginLeftRight(6.0)
            }

            label(article.source) {
                maxHeight = 20.0
                FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

                vboxConstraints {
                    marginBottom = 6.0
                }
            }

            label(article.summary) {
                vgrow = Priority.ALWAYS

                isWrapText = true
            }
        }
    }

}