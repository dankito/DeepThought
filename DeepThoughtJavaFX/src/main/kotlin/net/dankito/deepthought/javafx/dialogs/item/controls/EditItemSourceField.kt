package net.dankito.deepthought.javafx.dialogs.item.controls

import javafx.scene.control.ContextMenu
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.service.events.EditingSourceDoneEvent
import net.dankito.deepthought.javafx.ui.controls.EditEntityReferenceField
import net.dankito.deepthought.javafx.ui.controls.cell.SourceListCellFragment
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.getSeriesAndPublishingDatePreview
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.SourcesListPresenter
import net.dankito.deepthought.ui.view.ISourcesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService
import net.engio.mbassy.listener.Handler
import tornadofx.*
import javax.inject.Inject


class EditItemSourceField : EditEntityReferenceField<Source>(FX.messages["edit.item.source.label"], FX.messages["find.sources.prompt.text"],
        FX.messages["edit.item.source.indication.label"], FX.messages["source.indication.prompt.text"]) {

    var seriesToEdit: Series? = null

    private var originalSeries: Series? = null


    // create aliases to map to Source's terminology

    val originalSource: Source?
        get() = originalEntity

    val sourceToEdit: Source?
        get() = entityToEdit

    var enteredIndication: String
        get() = enteredSecondaryInformation
        set(value) {
            enteredSecondaryInformation = value
        }

    val didIndicationChange = didSecondaryInformationChange


    private val presenter: SourcesListPresenter


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var dialogService: IDialogService

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

        presenter = SourcesListPresenter(object : ISourcesListView {

            override fun showEntities(entities: List<Source>) {
                retrievedSearchResults(entities)
            }

        }, searchEngine, router, dialogService, clipboardService, deleteEntityService)


        this.showSecondaryInformation.value = true
        this.showEditEntityDetailsMenuItem = true
    }


    override fun getListCellFragment() = SourceListCellFragment::class

    override fun editEntity(entity: Source) {
        presenter.editSource(entity)
    }

    override fun deleteEntity(entity: Source) {
        presenter.confirmDeleteSourceAsync(entity)
    }

    override fun searchEntities(searchTerm: String) {
        presenter.searchSources(searchTerm) { result ->
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


    override fun createContextMenu(): ContextMenu {
        val contextMenu = super.createContextMenu()

        sourceToEdit?.let { source ->
            if(source.url.isNullOrBlank() == false) {
                contextMenu.item(messages["edit.item.source.field.copy.url.to.clipboard"]) {
                    action { copySourceUrlToClipboard(source) }
                }
            }
        }

        return contextMenu
    }

    override fun createNewEntity(entityTitle: String): Source {
        return Source(entityTitle)
    }

    override fun showEditEntityDialog() {
        eventBus.register(EventBusListener())

        router.showEditItemSourceView(entityToEdit, seriesToEdit, enteredTitle)
    }

    private fun copySourceUrlToClipboard(source: Source) {
        presenter.copySourceUrlToClipboard(source)
    }


    override fun onDock() {
        super.onDock()

        presenter.viewBecomesVisible()
    }

    override fun onUndock() {
        presenter.viewGetsHidden()

        super.onUndock()
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