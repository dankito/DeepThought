package net.dankito.deepthought.javafx.dialogs.source.controls

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.ui.controls.EditEntityField
import net.dankito.deepthought.javafx.ui.controls.EditEntityReferenceField
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.presenter.SeriesListPresenter
import net.dankito.deepthought.ui.view.ISeriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.dialogs.IDialogService
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
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService


    init {
        AppComponent.component.inject(this)

        seriesListPresenter = SeriesListPresenter(object : ISeriesListView {

            override fun showEntities(entities: List<Series>) {
                retrievedSearchResults(entities)
            }

        }, searchEngine, dialogService, deleteEntityService)
    }


    override fun getPrefFieldHeight() = EditEntityField.EntityFieldHeight

    override fun getPrefLabelWidth() = EditEntityField.EntityFieldLabelWidth - LabelRightMargin

    override fun getPrefTextFieldHeight() = EditEntityField.EntityFieldTextFieldHeight

    override fun getPrefButtonSize() = getPrefTextFieldHeight() - 2.0


    override fun editEntity(entity: Series) {
        // method never gets called as showEditEntityDetailsMenuItem is set to false, therefore nothing to do here
    }

    override fun deleteEntity(entity: Series) {
        seriesListPresenter.confirmDeleteSeriesAsync(entity)
    }

    override fun searchEntities(searchTerm: String) {
        seriesListPresenter.searchSeries(searchTerm) { result ->
            retrievedSearchResults(result)
        }
    }


    override fun onDock() {
        super.onDock()

        seriesListPresenter.viewBecomesVisible()
    }

    override fun onUndock() {
        seriesListPresenter.viewGetsHidden()

        super.onUndock()
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