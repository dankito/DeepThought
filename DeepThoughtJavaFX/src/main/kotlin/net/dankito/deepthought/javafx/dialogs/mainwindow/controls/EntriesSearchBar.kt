package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import net.dankito.deepthought.javafx.ui.controls.searchtextfield
import net.dankito.deepthought.model.LocalSettings
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.presenter.ItemsListPresenter
import net.dankito.utils.ui.Colors
import tornadofx.*


class EntriesSearchBar(private val entriesListView: EntriesListView, private val presenter: ItemsListPresenter, private val dataManager: DataManager) : View() {


    private var btnCreateItem: Button by singleAssign()

    private var createItemHintPopOver: CreateItemHintPopOver? = null


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

        checkDidUserCreateDataEntity()
    }


    private fun createItem() {
        hideCreateItemPopOver()

        presenter.createItem()
    }


    private fun checkDidUserCreateDataEntity() {
        dataManager.addInitializationListener {
            runLater {
                checkDidUserCreateDataEntityAfterInitializingDataManagerOnUiThread()
            }
        }
    }

    private fun checkDidUserCreateDataEntityAfterInitializingDataManagerOnUiThread() {
        didUserCreateDataEntityChangedOnUiThread(dataManager.localSettings.didUserCreateDataEntity)

        if(dataManager.localSettings.didUserCreateDataEntity == false) {
            listenToDidUserCreateDataEntityChanges()
        }
    }

    private fun listenToDidUserCreateDataEntityChanges() {
        var listener: ((LocalSettings) -> Unit)? = null

        listener = { localSettings ->
            if(localSettings.didUserCreateDataEntity) {
                listener?.let { dataManager.removeLocalSettingsChangedListener(it) }

                runLater {
                    didUserCreateDataEntityChangedOnUiThread(true)
                }
            }
        }

        dataManager.addLocalSettingsChangedListener(listener)
    }

    private fun didUserCreateDataEntityChangedOnUiThread(didUserCreateAnItemYet: Boolean) {
        if(didUserCreateAnItemYet) {
            hideCreateItemPopOver()
        }
        else {
            showCreateItemPopOver()
        }
    }

    private fun showCreateItemPopOver() {
        this.createItemHintPopOver = CreateItemHintPopOver(btnCreateItem) { createItemHintPopOver = null }

        this.createItemHintPopOver?.showHint()
    }

    private fun hideCreateItemPopOver() {
        createItemHintPopOver?.hideHint()
    }

}