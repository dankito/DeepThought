package net.dankito.deepthought.javafx.dialogs.readlaterarticle.model

import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.model.ReadLaterArticle
import tornadofx.*


class ReadLaterArticleViewModel : ItemViewModel<ReadLaterArticle>() {

    val previewImageUrl = bind { SimpleStringProperty(item?.previewImageUrl) }

    val source = bind { SimpleStringProperty(item?.sourcePreview ?: "") }

    val summary = bind { SimpleStringProperty(item?.itemPreview ?: "") }

}