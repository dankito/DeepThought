package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.*
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.specific.TagsSearch
import net.dankito.service.search.util.CombinedLazyLoadingList
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class TagsListPresenter(private val tagsListView: ITagsListView, private val dataManager: DataManager, private val searchEngine: ISearchEngine, private val router: IRouter)
    : IMainViewSectionPresenter {

    private var lastSearchTermProperty = Search.EmptySearchTerm

    private val calculatedTags = ArrayList<CalculatedTag>()


    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    init {
        thread {
            CommonComponent.component.inject(this)

            eventBus.register(eventBusListener)

            calculatedTags.add(AllEntriesCalculatedTag(dataManager))
            calculatedTags.add(EntriesWithoutTagsCalculatedTag(searchEngine, eventBus))

            dataManager.addInitializationListener { tagsListView.updateDisplayedTags() }
        }
    }

    override fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    override fun getAndShowAllEntities() {
        searchEngine.addInitializationListener { retrieveAndShowTags() }
    }

    private fun retrieveAndShowTags() {
        searchTags(Search.EmptySearchTerm)
    }


    fun searchTags(searchTerm: String) {
        lastSearchTermProperty = searchTerm

        searchEngine.searchTags(TagsSearch(searchTerm) { result ->
            var tags = result.getRelevantMatchesSorted()

            if(searchTerm == Search.EmptySearchTerm) {
                tags = CombinedLazyLoadingList<Tag>(calculatedTags, result.getRelevantMatchesSorted())
            }

            tagsListView.showTags(tags)
        })
    }

    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }


    fun showEntriesForTag(tag: Tag, entries: List<Entry>) {
        router.showEntriesForTag(tag, entries)
    }


    inner class EventBusListener {

        @Handler()
        fun entityChanged(entitiesOfTypeChanged: EntitiesOfTypeChanged) {
            when(entitiesOfTypeChanged.entityType) {
                Tag::class.java -> retrieveAndShowTags()
                Entry::class.java -> tagsListView.updateDisplayedTags() // count entries on tag(s) may have changed
            }
        }

    }

}