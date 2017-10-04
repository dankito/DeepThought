package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.EntryViewModel
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.javafx.util.LazyLoadingObservableList
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.referencePreview
import net.dankito.deepthought.model.extensions.tagsPreview
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IClipboardService
import tornadofx.*
import java.text.DateFormat
import javax.inject.Inject


class EntriesListView : EntitiesListView(), IEntriesListView {

    companion object {
        private val dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG)
    }


    private val entryModel = EntryViewModel()

    private val entries = LazyLoadingObservableList<Entry>()

    private val searchBar: EntriesSearchBar

    private var tableEntries: TableView<Entry> by singleAssign()

    var statusBar: StatusBar? = null

    private val presenter: EntriesListPresenter


    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService


    init {
        AppComponent.component.inject(this)

        presenter = EntriesListPresenter(this, router, searchEngine, deleteEntityService, clipboardService)
        searchBar = EntriesSearchBar(this, presenter)

        (router as? JavaFXRouter)?.entriesListView = this // TODO: this is bad code design

        searchEngine.addInitializationListener {
            searchEntities(Search.EmptySearchTerm)
        }
    }

    override fun onUndock() {
        presenter.cleanUp()
        super.onUndock()
    }


    override val root = vbox {
        add(searchBar.root)

        tableEntries = tableview<Entry>(entries) {
            column(messages["entry.column.header.index"], Entry::entryIndex).prefWidth(46.0)
            column(messages["entry.column.header.reference"], Entry::referencePreview).weigthedWidth(4.0)
            column(messages["entry.column.header.preview"], Entry::preview).weigthedWidth(4.0)
            column(messages["entry.column.header.tags"], Entry::tagsPreview).weigthedWidth(2.0)
    //        column(messages["entry.column.header.created"], stringBinding(Entry::createdOn) { dateTimeFormat.format(this) }).weigthedWidth(1.0)
    //        column(messages["entry.column.header.modified"], stringBinding(Entry::modifiedOn) { dateTimeFormat.format(this) }).weigthedWidth(1.0)

            columnResizePolicy = SmartResize.POLICY

            bindSelected(entryModel)

            vgrow = Priority.ALWAYS

            onDoubleClick { router.showEditEntryView(selectionModel.selectedItem) }

            contextmenu {
                item(messages["context.menu.entry.copy.url.to.clipboard"]) {
                    action {
                        selectedItem?.let { presenter.copyReferenceUrlToClipboard(it) }
                    }
                }

                separator()

                item(messages["context.menu.entry.delete"]) {
                    action {
                        selectedItem?.let { presenter.deleteEntry(it) }
                    }
                }
            }
        }
    }


    override fun searchEntities(query: String) {
        presenter.searchEntries(query)
    }


    /*          IEntriesListView implementation            */

    override fun showEntities(entities: List<Entry>) {
        runLater {
            entries.setAll(entities)
            tableEntries.refresh() // necessary when count entries stays the same (e.g. when an entry has been updated)

            statusBar?.showCountDisplayedEntriesOnUiThread(entities.size)
        }
    }

    override fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>) {
        presenter.showEntriesForTag(tag, tagsFilter)
    }

}