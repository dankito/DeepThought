package net.dankito.deepthought.javafx.dialogs.source.controls

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.ui.controls.EditEntityField
import net.dankito.deepthought.javafx.ui.controls.EditEntityReferenceField
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.SeriesListPresenter
import net.dankito.deepthought.ui.view.ISeriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import tornadofx.*
import javax.inject.Inject


class EditSourceSeriesField : EditEntityReferenceField<Series>(FX.messages["edit.source.series.field.series.label"], FX.messages["find.series.prompt.text"]) {


    // create aliases to map to Series' terminology

    val seriesToEdit: Series?
        get() = entityToEdit


    private val seriesListPresenter: SeriesListPresenter


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var eventBus: IEventBus


    init {
        AppComponent.component.inject(this)

        seriesListPresenter = SeriesListPresenter(object : ISeriesListView {

            override fun showEntities(entities: List<Series>) {
                retrievedSearchResults(entities)
            }

        }, searchEngine, router, deleteEntityService)
    }


    override fun getPrefFieldHeight() = EditEntityField.EntityFieldHeight

    override fun getPrefLabelWidth() = EditEntityField.EntityFieldLabelWidth - LabelRightMargin

    override fun getPrefTextFieldHeight() = EditEntityField.EntityFieldTextFieldHeight

    override fun getPrefButtonSize() = getPrefTextFieldHeight() - 2.0


    override fun getCellFragmentClass() = null

    override fun editEntity(entity: Series) {
        seriesListPresenter.editSeries(entity)
    }

    override fun deleteEntity(entity: Series) {
        seriesListPresenter.deleteSeriesAsync(entity)
    }

    override fun searchEntities(searchTerm: String) {
        seriesListPresenter.searchSeries(searchTerm) { result ->
            retrievedSearchResults(result)
        }
    }


    fun setSeriesToEdit(series: Series?) {
        setEntityToEdit(series)
    }

    override fun getEntityTitle(entity: Series?): String? {
        return entity?.title
    }

    override fun createNewEntity(entityTitle: String): Series {
        return Series(entityTitle)
    }

    override fun showEditEntityDialog() {
        // nothing to do here
    }

}