package net.dankito.deepthought.ui.windowdata

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Tag


abstract class EditItemWindowDataBase {

    var editedContent: String? = null

    var editedSummary: String? = null

    var editedTags: Collection<Tag>? = null

    var editedSourceTitle: String? = null

    var editedIndicator: String? = null

    var editedFiles: Collection<FileLink>? = null

}