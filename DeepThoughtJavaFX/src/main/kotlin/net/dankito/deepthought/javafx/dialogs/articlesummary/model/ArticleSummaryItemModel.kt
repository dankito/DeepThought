package net.dankito.deepthought.javafx.dialogs.articlesummary.model

import javafx.beans.property.SimpleStringProperty
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryItemModel : ItemViewModel<ArticleSummaryItem>() {

    val previewImageUrl = bind { SimpleStringProperty(item?.previewImageUrl) }

    val title = bind { SimpleStringProperty(item?.title) }

    val summary = bind { SimpleStringProperty(item?.summary ?: "") }

}