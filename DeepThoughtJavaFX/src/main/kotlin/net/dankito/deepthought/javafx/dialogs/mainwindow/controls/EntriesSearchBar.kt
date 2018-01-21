package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.utils.ui.Colors
import tornadofx.*


class EntriesSearchBar(private val entriesListView: EntriesListView, private val presenter: EntriesListPresenter) : View() {

    override val root = borderpane {
        prefHeight = 40.0
        useMaxWidth = true

        center {
            hbox {
                alignment = Pos.CENTER_LEFT

                label(messages["search.textbox.label"])

                textfield {
                    hboxConstraints {
                        hgrow = Priority.ALWAYS
                        marginLeftRight(6.0)
                    }

                    promptText = messages["find.items.prompt.text"]

                    textProperty().addListener { _, _, newValue -> entriesListView.searchEntities(newValue) }
                    setOnKeyReleased { event ->
                        if(event.code == KeyCode.ESCAPE) {
                            clear()
                        }
                    }
                }
            }
        }

        right {
            hbox {
                button("+") {
                    prefHeight = 30.0
                    prefWidth = 50.0
                    font = Font.font(font.family, FontWeight.BOLD, 18.0)
                    textFill = Color.valueOf(Colors.AddButtonHexColor)

                    hboxConstraints {
                        marginLeft = 6.0
                        marginTopBottom(2.0)
                    }

                    action { presenter.createEntry() }
                }
            }
        }
    }

}