package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import tornadofx.*


class EntriesSearchBar(private val presenter: EntriesListPresenter) : View() {

    override val root = hbox {
        prefHeight = 40.0
        useMaxWidth = true
        alignment = Pos.CENTER_LEFT

        label("Search:")

        textfield {
            hboxConstraints {
                hgrow = Priority.ALWAYS
                marginLeftRight(6.0)
            }

            promptText = "TODO"

            textProperty().addListener { _, _, newValue -> presenter.searchEntries(newValue) }
            setOnKeyReleased { event ->
                if(event.code == KeyCode.ESCAPE) {
                    clear()
                }
            }
        }
    }

}