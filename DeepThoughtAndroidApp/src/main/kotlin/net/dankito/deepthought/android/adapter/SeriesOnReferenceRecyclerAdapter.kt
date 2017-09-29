package net.dankito.deepthought.android.adapter

import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.presenter.SeriesPresenterBase


class SeriesOnReferenceRecyclerAdapter(presenter: SeriesPresenterBase): SeriesRecyclerAdapterBase(presenter) {

    var selectedSeries: Series? = null


    override val shouldShowImageIsSeriesSetOnReference: Boolean
        get() = true

    override val shouldShowChevronRight: Boolean
        get() = false

    override val shouldShowButtonEditSeries: Boolean
        get() = false


    override fun isSetOnReference(series: Series): Boolean {
        return selectedSeries?.id == series.id
    }

}