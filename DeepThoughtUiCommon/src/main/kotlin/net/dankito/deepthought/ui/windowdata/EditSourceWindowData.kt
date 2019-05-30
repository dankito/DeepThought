package net.dankito.deepthought.ui.windowdata

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source


class EditSourceWindowData(var source: Source, var series: Series? = null, var editedSourceTitle: String? = null) {

    private constructor() : this(Source("")) // for Jackson


    var editedTitle: String? = null

    var editedSeriesTitle: String? = null

    var editedIssue: String? = null

    var editedLength: String? = null

    var editedPublishingDateString: String? = null

    var editedUrl: String? = null

    var editedFiles: Collection<FileLink>? = null

}