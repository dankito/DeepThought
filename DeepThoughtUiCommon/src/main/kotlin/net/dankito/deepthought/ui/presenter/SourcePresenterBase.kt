package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.IClipboardService


abstract class SourcePresenterBase(protected var router: IRouter, private val clipboardService: IClipboardService,
                                   private val deleteEntityService: DeleteEntityService) {


    fun editSource(source: Source) {
        router.showEditSourceView(source)
    }

    fun copySourceUrlToClipboard(source: Source) {
        source.url?.let { clipboardService.copyUrlToClipboard(it) }
    }

    fun deleteSource(source: Source) {
        deleteEntityService.deleteSource(source)
    }

}