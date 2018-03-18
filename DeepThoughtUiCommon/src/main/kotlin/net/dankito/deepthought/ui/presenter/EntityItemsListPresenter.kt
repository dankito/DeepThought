package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService


class EntityItemsListPresenter(deleteEntityService: DeleteEntityService, clipboardService: IClipboardService, router: IRouter, threadPool: IThreadPool)
    : ItemsListPresenterBase(deleteEntityService, clipboardService, router, threadPool) {

}