package net.dankito.deepthought.javafx.dialogs.readlaterarticle.model

import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import tornadofx.*


class ReadLaterArticleViewModel : ItemViewModel<ReadLaterArticle>() {

    val previewImageUrl = bind { SimpleStringProperty(item?.previewImageUrl) }

    val reference = bind { SimpleStringProperty(item?.referencePreview ?: "") }

    val summary = bind { SimpleStringProperty(item?.entryPreview ?: "") }

}