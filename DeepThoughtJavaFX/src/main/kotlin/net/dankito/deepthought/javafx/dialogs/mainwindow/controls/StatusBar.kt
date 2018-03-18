package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import tornadofx.*


class StatusBar : View() {

    private var statusLabel: Label by singleAssign()

    private var statusLabelCountItems: Label by singleAssign()


    override val root = gridpane {
        row {
            minHeight = 10.0
            vgrow = Priority.SOMETIMES

            statusLabel = label {
                gridpaneConstraints {
                    hgrow = Priority.ALWAYS
                    vAlignment = VPos.CENTER
                    marginTopBottom(2.0)
                    marginLeftRight(2.0)
                }

                gridpaneColumnConstraints {
                    hgrow = Priority.ALWAYS
                }
            }

            statusLabelCountItems = label {
                textAlignment = TextAlignment.RIGHT

                gridpaneConstraints {
                    columnIndex = 1
                    vAlignment = VPos.CENTER
                    hAlignment = HPos.RIGHT
                    marginTopBottom(2.0)
                    marginRight = 2.0
                    marginLeft = 6.0
                }

                gridpaneColumnConstraints {
                    halignment = HPos.RIGHT
                }
            }
        }
    }


    fun showStatusOnUiThread(status: String) {
        statusLabel.text = status
    }

    fun showCountDisplayedItemsOnUiThread(countItems: Int) {
        statusLabelCountItems.text = String.format(messages["main.window.status.bar.count.displayed.items"], countItems)
    }

}