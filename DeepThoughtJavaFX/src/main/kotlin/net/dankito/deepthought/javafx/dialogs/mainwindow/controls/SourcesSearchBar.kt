package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.ui.controls.searchtextfield
import tornadofx.*


class SourcesSearchBar(private val sourcesListView: SourcesListView) : View() {

    override val root = hbox {
        prefHeight = 40.0
        useMaxWidth = true
        alignment = Pos.CENTER_LEFT

        label(messages["search.textbox.label"])

        searchtextfield {
            hboxConstraints {
                hgrow = Priority.ALWAYS
                marginLeftRight(6.0)
            }

            promptText = messages["find.sources.prompt.text"]

            textProperty().addListener { _, _, newValue -> sourcesListView.searchEntities(newValue) }
        }
    }

}