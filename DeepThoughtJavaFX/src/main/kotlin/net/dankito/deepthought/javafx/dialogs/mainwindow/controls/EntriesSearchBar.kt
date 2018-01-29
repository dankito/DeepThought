package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import net.dankito.deepthought.javafx.ui.controls.searchtextfield
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.utils.ui.Colors
import tornadofx.*


class EntriesSearchBar(private val entriesListView: EntriesListView, private val presenter: EntriesListPresenter) : View() {

    val didUserCreateAnItemYet = SimpleBooleanProperty(true)


    private var btnCreateItem: Button by singleAssign()

    private var createItemHintPopOver: CreateItemHintPopOver? = null


    init {
        didUserCreateAnItemYet.addListener { _, _, newValue -> didUserCreateAnItemYetChanged(newValue) }
    }


    override val root = borderpane {
        prefHeight = 40.0
        useMaxWidth = true

        center {
            hbox {
                alignment = Pos.CENTER_LEFT

                label(messages["search.textbox.label"])

                searchtextfield {
                    hboxConstraints {
                        hgrow = Priority.ALWAYS
                        marginLeftRight(6.0)
                    }

                    promptText = messages["find.items.prompt.text"]

                    textProperty().addListener { _, _, newValue -> entriesListView.searchEntities(newValue) }
                }
            }
        }

        right {
            hbox {
                btnCreateItem = button("+") {
                    prefHeight = 30.0
                    prefWidth = 50.0
                    font = Font.font(font.family, FontWeight.BOLD, 18.0)
                    textFill = Color.valueOf(Colors.AddButtonHexColor)

                    hboxConstraints {
                        marginLeft = 6.0
                        marginTopBottom(2.0)
                    }

                    action { createItem() }
                }
            }
        }
    }


    private fun createItem() {
        hideCreateItemPopOver()

        presenter.createEntry()
    }

    private fun didUserCreateAnItemYetChanged(didUserCreateAnItemYet: Boolean) {
        if(didUserCreateAnItemYet) {
            hideCreateItemPopOver()
        }
        else {
            showCreateItemPopOver()
        }
    }

    private fun showCreateItemPopOver() {
        this.createItemHintPopOver = CreateItemHintPopOver(btnCreateItem)
        this.createItemHintPopOver?.showHint()
    }

    private fun hideCreateItemPopOver() {
        createItemHintPopOver?.hide()
        createItemHintPopOver = null
    }

}