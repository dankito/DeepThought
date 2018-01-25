package net.dankito.deepthought.javafx.dialogs.entry.controls

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.service.events.EditingSourceDoneEvent
import net.dankito.deepthought.javafx.ui.controls.EditEntityReferenceField
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.getSeriesAndPublishingDatePreview
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ReferencesListPresenter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import net.engio.mbassy.listener.Handler
import tornadofx.*
import javax.inject.Inject


class EditItemSourceField : EditEntityReferenceField<Source>(FX.messages["edit.item.source.label"], FX.messages["find.source.prompt.text"],
        FX.messages["edit.item.source.indication.label"], FX.messages["source.indication.prompt.text"]) {

    var seriesToEdit: Series? = null

    private var originalSeries: Series? = null


    // create aliases to map to Source's terminology

    val originalSource: Source?
        get() = originalEntity

    val sourceToEdit: Source?
        get() = entityToEdit

    val enteredIndication: String
        get() = enteredSecondaryInformation

    val didIndicationChange = didSecondaryInformationChange


    private val referenceListPresenter: ReferencesListPresenter


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var eventBus: IEventBus


    init {
        AppComponent.component.inject(this)

        referenceListPresenter = ReferencesListPresenter(object : IReferencesListView {

            override fun showEntities(entities: List<Source>) {
                retrievedSearchResults(entities)
            }

        }, searchEngine, router, clipboardService, deleteEntityService)


        this.showSecondaryInformation.value = true
        this.showEditEntityDetailsMenuItem = true
    }


    override fun getCellFragmentClass() = SourceListCellFragment::class

    override fun editEntity(entity: Source) {
        referenceListPresenter.editReference(entity)
    }

    override fun deleteEntity(entity: Source) {
        referenceListPresenter.deleteReference(entity)
    }

    override fun searchEntities(searchTerm: String) {
        referenceListPresenter.searchReferences(searchTerm) { result ->
            retrievedSearchResults(result)
        }
    }


    fun setSourceToEdit(source: Source?, series: Series?, indication: String) {
        originalSeries = series

        setEntityToEdit(source, indication)

        this.seriesToEdit = series // as if series is not yet persisted / set yet on source, call to setEntity(Source) sets seriesToEdit to source.series which is null
        showEntityPreview(source) // then update series and publishing date (= additional entity) preview
    }

    override fun setEntity(entity: Source?) {
        seriesToEdit = entity?.series

        super.setEntity(entity)
    }

    override fun didEntityChange(): Boolean {
        return super.didEntityChange() || seriesToEdit != originalSeries
    }

    override fun getEntityTitle(entity: Source?): String? {
        return entity?.title
    }

    override fun getEntityAdditionalPreview(entity: Source?): String? {
        return entity?.getSeriesAndPublishingDatePreview(seriesToEdit)
    }

    override fun createNewEntity(entityTitle: String): Source {
        return Source(entityTitle)
    }

    override fun showEditEntityDialog() {
        eventBus.register(EventBusListener())

        router.showEditEntryReferenceView(entityToEdit, seriesToEdit, enteredTitle)
    }


    inner class EventBusListener {

        @Handler
        fun editingSourceDone(event: EditingSourceDoneEvent) {
            eventBus.unregister(this)

            if(event.didSaveSource) {
                runLater {
                    setEntity(event.savedSource)
                }
            }
        }

    }

}