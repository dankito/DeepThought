package net.dankito.deepthought.javafx.dialogs.mainwindow.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.model.Tag
import tornadofx.*


class TagViewModel : ItemViewModel<Tag>() {

    val displayName = bind { SimpleStringProperty(item?.displayText) }

    val filter = bind { SimpleBooleanProperty(false) }

}