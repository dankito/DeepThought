package net.dankito.deepthought.javafx.service.extensions

import javafx.scene.control.ListCell
import javafx.scene.input.PickResult


fun <T> PickResult?.findClickedListCell(): ListCell<T>? {
    return (this?.intersectedNode as? ListCell<T>) ?: this?.intersectedNode?.parent as? ListCell<T>
}