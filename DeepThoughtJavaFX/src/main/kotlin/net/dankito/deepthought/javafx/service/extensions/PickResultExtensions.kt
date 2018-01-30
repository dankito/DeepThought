package net.dankito.deepthought.javafx.service.extensions

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.input.PickResult


fun <T> PickResult?.findClickedListCell(): ListCell<T>? {
    var parent = this?.intersectedNode
    while(parent != null) {
        (parent as? ListCell<T>)?.let { listCell ->
            return listCell
        }

        if(parent is ListView<*>) {
            break // we already reached ListView -> we won't find a ListCell
        }

        parent = parent.parent
    }

    return null
}

fun <T> PickResult?.findClickedTableRow(): TableRow<T>? {
    var parent = this?.intersectedNode
    while(parent != null) {
        (parent as? TableRow<T>)?.let { tableRow ->
            return tableRow
        }

        if(parent is TableView<*>) {
            break // we already reached TableView -> we won't find a TableCell
        }

        parent = parent.parent
    }

    return null
}