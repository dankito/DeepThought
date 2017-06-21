package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.messages.TagChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.TagsSearch
import net.engio.mbassy.listener.Handler
import javax.inject.Inject
import kotlin.concurrent.thread


class TagsListPresenter(private val tagsListView: ITagsListView, private var router: IRouter, private var searchEngine: ISearchEngine) {

    @Inject
    protected lateinit var eventBus: IEventBus

    private val eventBusListener = EventBusListener()


    init {
        thread {
            CommonComponent.component.inject(this)

            eventBus.register(eventBusListener)

            searchEngine.addInitializationListener {
                searchEngineInitialized()
            }
        }
    }

    fun cleanUp() {
        eventBus.unregister(eventBusListener)
    }


    private fun searchEngineInitialized() {
        retrieveAndShowTags()
    }

    private fun retrieveAndShowTags() {
        searchEngine.searchTags(TagsSearch { result ->
            tagsListView.showTags(result.getAllMatches())
        })
    }


    fun showEntriesForTag(tag: Tag) {
        // TODO
    }


    inner class EventBusListener {

        @Handler()
        fun tagsChanged(tagChanged: TagChanged) {
            retrieveAndShowTags()
        }

    }

}