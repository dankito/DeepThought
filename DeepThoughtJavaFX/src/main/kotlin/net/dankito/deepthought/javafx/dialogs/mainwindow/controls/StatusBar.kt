package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import tornadofx.*


class StatusBar : View() {

    private var statusLabel: Label by singleAssign()

    private var statusLabelCountEntries: Label by singleAssign()


    override val root = gridpane {
        row {
            minHeight = 10.0
            vgrow = Priority.SOMETIMES

            statusLabel = label {
                gridpaneConstraints {
                    hgrow = Priority.ALWAYS
                    vAlignment = VPos.CENTER
                }

                gridpaneColumnConstraints {
                    hgrow = Priority.ALWAYS
                }
            }

            statusLabelCountEntries = label {
                prefWidth = 100.0
                textAlignment = TextAlignment.RIGHT

                gridpaneConstraints {
                    vAlignment = VPos.CENTER
                    columnIndex = 1
                }

                gridpaneColumnConstraints {
                    halignment = HPos.RIGHT
                }
            }
        }
    }

}