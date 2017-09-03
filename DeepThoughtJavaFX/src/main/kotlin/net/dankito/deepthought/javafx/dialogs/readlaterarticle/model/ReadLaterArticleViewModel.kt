package net.dankito.deepthought.javafx.dialogs.readlaterarticle.model

import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.extensions.previewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.ReadLaterArticle
import tornadofx.*


class ReadLaterArticleViewModel : ItemViewModel<ReadLaterArticle>() {

    val previewImageUrl = bind { SimpleStringProperty(item?.entryExtractionResult?.reference?.previewImageUrl) }

    val reference = bind { SimpleStringProperty(item?.entryExtractionResult?.reference?.previewWithSeriesAndPublishingDate ?: "") }

    val summary = bind { SimpleStringProperty(item?.entryExtractionResult?.entry?.preview ?: "") }

}