package net.dankito.deepthought.ui.windowdata

import net.dankito.deepthought.model.Item


class EditItemWindowData(val item: Item) : EditItemWindowDataBase() {

    private constructor() : this(Item("")) // for object deserializers

}