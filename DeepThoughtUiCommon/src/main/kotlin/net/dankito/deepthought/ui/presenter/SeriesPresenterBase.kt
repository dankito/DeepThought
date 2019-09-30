package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Series
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.dialogs.ConfirmationDialogButton
import net.dankito.utils.ui.dialogs.IDialogService


abstract class SeriesPresenterBase(private val dialogService: IDialogService, private val deleteEntityService: DeleteEntityService) {


    fun confirmDeleteSeriesAsync(series: Series) {
        dialogService.showConfirmationDialog(dialogService.getLocalization().getLocalizedString("alert.message.really.delete.series", series.title)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                deleteSeriesAsync(series)
            }
        }
    }

    fun deleteSeriesAsync(series: Series) {
        deleteEntityService.deleteSeriesAsync(series)
    }

}