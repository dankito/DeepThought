package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService


abstract class SeriesPresenterBase(private val router: IRouter, private val deleteEntityService: DeleteEntityService) {

    fun editSeries(series: Series) {
        router.showEditSeriesView(series)
    }

    fun deleteSeriesAsync(series: Series) {
        deleteEntityService.deleteSeriesAsync(series)
    }

}