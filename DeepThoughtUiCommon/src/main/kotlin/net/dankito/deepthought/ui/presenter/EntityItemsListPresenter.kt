package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService


class EntityItemsListPresenter(deleteEntityService: DeleteEntityService, dialogService: IDialogService, clipboardService: IClipboardService, router: IRouter, threadPool: IThreadPool)
    : ItemsListPresenterBase(deleteEntityService, dialogService, clipboardService, router, threadPool) {

}