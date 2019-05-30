package net.dankito.deepthought.ui.windowdata

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.util.ItemExtractionResult


class EditReadLaterArticleWindowData(val readLaterArticle: ReadLaterArticle) : EditItemWindowDataBase() {

    private constructor() : this(ReadLaterArticle(ItemExtractionResult(Item("")))) // for object deserializers

}