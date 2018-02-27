package net.dankito.deepthought.javafx.ui.controls.viewmodel

import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.model.DeepThoughtFileLink
import tornadofx.*


class FileViewModel : ItemViewModel<DeepThoughtFileLink>() {

    val name = bind { SimpleStringProperty(item?.name) }

    val fileSize = bind { SimpleLongProperty(item?.fileSize ?: 0) }

}