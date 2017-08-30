package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import tornadofx.*


class EntriesSearchBar(private val entriesListView: EntriesListView) : View() {

    override val root = hbox {
        prefHeight = 40.0
        useMaxWidth = true
        alignment = Pos.CENTER_LEFT

        label(messages["search.textbox.label"])

        textfield {
            hboxConstraints {
                hgrow = Priority.ALWAYS
                marginLeftRight(6.0)
            }

            promptText = messages["search.entries.prompt.text"]

            textProperty().addListener { _, _, newValue -> entriesListView.searchEntities(newValue) }
            setOnKeyReleased { event ->
                if(event.code == KeyCode.ESCAPE) {
                    clear()
                }
            }
        }
    }

}