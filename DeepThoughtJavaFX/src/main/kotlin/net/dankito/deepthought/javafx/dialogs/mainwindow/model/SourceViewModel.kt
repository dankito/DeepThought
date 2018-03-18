package net.dankito.deepthought.javafx.dialogs.mainwindow.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.seriesAndPublishingDatePreview
import tornadofx.*


class SourceViewModel : ItemViewModel<Source>() {

    val sourcePreview = bind { SimpleStringProperty(item?.preview + (if(item == null) "" else " (" + item?.items?.size + ")")) }

    val seriesAndPublishingDatePreview = bind { SimpleStringProperty(item?.seriesAndPublishingDatePreview) }

    val hasSeriesAndPublishingDatePreview = bind { SimpleBooleanProperty(item?.seriesAndPublishingDatePreview?.isNotBlank() ?: false) }

}