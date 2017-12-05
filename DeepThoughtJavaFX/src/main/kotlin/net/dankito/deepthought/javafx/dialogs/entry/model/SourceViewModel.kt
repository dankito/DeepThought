package net.dankito.deepthought.javafx.dialogs.entry.model

import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.seriesAndPublishingDatePreview
import tornadofx.*


class SourceViewModel : ItemViewModel<Source>() {

    // TODO: may show preview image
    val previewImageUrl = bind { SimpleStringProperty(item?.previewImageUrl) }

    val preview = bind { SimpleStringProperty(
(if(item.seriesAndPublishingDatePreview.isNullOrBlank()) "" else item.seriesAndPublishingDatePreview + " - ") +
                    item.preview
    ) }
}