package net.dankito.deepthought.javafx.dialogs.mainwindow.model

import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.model.extensions.previewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.Item
import tornadofx.*


class ItemViewModel : ItemViewModel<Item>() {

    val index = bind { SimpleLongProperty(item?.itemIndex ?: 0) }

    val source = bind { SimpleStringProperty(item?.source.previewWithSeriesAndPublishingDate ?: "") }

    val preview = bind { SimpleStringProperty(item?.preview ?: "") }

    val createdOn = bind { SimpleLongProperty(item?.createdOn?.time ?: 0) }

    val modifiedOn = bind { SimpleLongProperty(item?.modifiedOn?.time ?: 0) }

}