package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.ConfirmationDialogButton
import net.dankito.utils.ui.dialogs.IDialogService


abstract class SourcePresenterBase(protected var router: IRouter, private val dialogService: IDialogService, private val clipboardService: IClipboardService,
                                   private val deleteEntityService: DeleteEntityService) {


    fun editSource(source: Source) {
        router.showEditSourceView(source)
    }

    fun copySourceUrlToClipboard(source: Source) {
        source.url?.let { clipboardService.copyUrlToClipboard(it) }
    }


    fun confirmDeleteSourcesAsync(sources: List<Source>) {
        dialogService.showConfirmationDialog(dialogService.getLocalization().getLocalizedString("alert.message.really.delete.sources", sources.size)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                deleteSourcesAsync(sources)
            }
        }
    }

    fun deleteSourcesAsync(sources: List<Source>) {
        deleteEntityService.deleteSourcesAsync(sources)
    }

    fun confirmDeleteSourceAsync(source: Source) {
        dialogService.showConfirmationDialog(dialogService.getLocalization().getLocalizedString("alert.message.really.delete.source", source.title)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                deleteSourceAsync(source)
            }
        }
    }

    fun deleteSourceAsync(source: Source) {
        deleteEntityService.deleteSourceAsync(source)
    }

}