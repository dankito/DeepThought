package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.dialogs.ConfirmationDialogButton
import net.dankito.utils.ui.dialogs.IDialogService
import javax.inject.Inject


abstract class SeriesPresenterBase(private val router: IRouter, private val dialogService: IDialogService, private val deleteEntityService: DeleteEntityService) {

    @Inject
    protected lateinit var localization: Localization


    init {
        CommonComponent.component.inject(this)
    }


    fun editSeries(series: Series) {
        router.showEditSeriesView(series)
    }


    fun confirmDeleteSeriesAsync(series: Series) {
        dialogService.showConfirmationDialog(localization.getLocalizedString("alert.message.really.delete.series", series.title)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                deleteSeriesAsync(series)
            }
        }
    }

    fun deleteSeriesAsync(series: Series) {
        deleteEntityService.deleteSeriesAsync(series)
    }

}