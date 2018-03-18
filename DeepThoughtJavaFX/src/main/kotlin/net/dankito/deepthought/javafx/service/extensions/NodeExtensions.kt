package net.dankito.deepthought.javafx.service.extensions

import javafx.scene.Node
import javafx.scene.layout.AnchorPane
import tornadofx.*


fun UIComponent.setAnchorPaneOverallAnchor(anchorValue: Double) {
    this.root.setAnchorPaneOverallAnchor(anchorValue)
}

fun Node.setAnchorPaneOverallAnchor(anchorValue: Double) {
    AnchorPane.setLeftAnchor(this, anchorValue)
    AnchorPane.setTopAnchor(this, anchorValue)
    AnchorPane.setRightAnchor(this, anchorValue)
    AnchorPane.setBottomAnchor(this, anchorValue)
}