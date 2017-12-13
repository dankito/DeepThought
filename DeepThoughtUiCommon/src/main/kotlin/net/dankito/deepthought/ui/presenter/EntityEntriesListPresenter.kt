package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.IClipboardService


class EntityEntriesListPresenter(deleteEntityService: DeleteEntityService, clipboardService: IClipboardService, router: IRouter) : EntriesListPresenterBase(deleteEntityService, clipboardService, router) {

}