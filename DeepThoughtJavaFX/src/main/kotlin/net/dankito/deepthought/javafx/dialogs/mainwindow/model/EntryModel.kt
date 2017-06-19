package net.dankito.deepthought.javafx.dialogs.mainwindow.model

import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.model.Entry
import tornadofx.*


class EntryModel : ItemViewModel<Entry>() {

    val index = bind { SimpleLongProperty(item?.entryIndex ?: 0) }

    val reference = bind { SimpleStringProperty(item?.reference?.toString() ?: "") }

    val preview = bind { SimpleStringProperty(item?.content ?: "") }

}