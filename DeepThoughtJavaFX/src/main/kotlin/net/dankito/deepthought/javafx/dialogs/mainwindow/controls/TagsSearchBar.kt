package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import net.dankito.deepthought.ui.presenter.TagsListPresenter
import tornadofx.*


class TagsSearchBar(private val presenter: TagsListPresenter) : View() {

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

            promptText = messages["search.tags.prompt.text"]

            textProperty().addListener { _, _, newValue -> presenter.searchTags(newValue) }
            setOnKeyReleased { event ->
                if(event.code == KeyCode.ESCAPE) {
                    clear()
                }
            }
        }
    }

}