package net.dankito.deepthought.javafx.dialogs.mainwindow.model

import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.extensions.entryPreview
import net.dankito.deepthought.extensions.referencePreview
import net.dankito.deepthought.model.Entry
import tornadofx.*


class EntryModel : ItemViewModel<Entry>() {

    val index = bind { SimpleLongProperty(item?.entryIndex ?: 0) }

    val reference = bind { SimpleStringProperty(item?.referencePreview ?: "") }

    val preview = bind { SimpleStringProperty(item?.entryPreview ?: "") }

    val createdOn = bind { SimpleLongProperty(item?.createdOn?.time ?: 0) }

    val modifiedOn = bind { SimpleLongProperty(item?.modifiedOn?.time ?: 0) }

}