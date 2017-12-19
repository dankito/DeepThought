package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.IClipboardService


abstract class ReferencesPresenterBase(protected var router: IRouter, private val clipboardService: IClipboardService,
                                       private val deleteEntityService: DeleteEntityService) {


    fun editReference(source: Source) {
        router.showEditReferenceView(source)
    }

    fun copyReferenceUrlToClipboard(source: Source) {
        source.url?.let { clipboardService.copyUrlToClipboard(it) }
    }

    fun deleteReference(source: Source) {
        deleteEntityService.deleteReference(source)
    }

}