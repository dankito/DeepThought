package net.dankito.deepthought.ui.windowdata

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.util.ItemExtractionResult


class EditItemExtractionResultWindowData(val itemExtractionResult: ItemExtractionResult) : EditItemWindowDataBase() {

    private constructor() : this(ItemExtractionResult(Item(""))) // for object deserializers

}