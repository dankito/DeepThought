package net.dankito.deepthought.javafx.dialogs.source.model

import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source


// TODO: store / restore from source.id
class EditSourceWindowData(var source: Source, var series: Series? = null, var editedSourceTitle: String? = null) {

    private constructor() : this(Source("")) // for Jackson

}