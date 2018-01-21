package net.dankito.deepthought.javafx.ui.controls.viewmodel

import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.model.FileLink
import tornadofx.*


class FileViewModel : ItemViewModel<FileLink>() {

    val name = bind { SimpleStringProperty(item?.name) }

    val uri = bind { SimpleStringProperty(item?.localFileInfo?.path) }

    val fileSize = bind { SimpleLongProperty(item?.fileSize ?: 0) }

}