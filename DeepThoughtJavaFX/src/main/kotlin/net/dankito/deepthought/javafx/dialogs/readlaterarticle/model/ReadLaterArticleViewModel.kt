package net.dankito.deepthought.javafx.dialogs.readlaterarticle.model

import javafx.beans.property.SimpleStringProperty
import net.dankito.deepthought.extensions.entryPreview
import net.dankito.deepthought.extensions.preview
import net.dankito.deepthought.model.util.EntryExtractionResult
import tornadofx.*


class ReadLaterArticleViewModel : ItemViewModel<EntryExtractionResult>() {

    val previewImageUrl = bind { SimpleStringProperty(item?.entry?.previewImageUrl) }

    val reference = bind { SimpleStringProperty(item?.reference?.preview ?: "") }

    val summary = bind { SimpleStringProperty(item?.entry?.entryPreview ?: "") }

}