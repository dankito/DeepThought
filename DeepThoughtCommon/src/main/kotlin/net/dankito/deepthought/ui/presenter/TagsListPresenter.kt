package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.AllEntriesCalculatedTag
import net.dankito.deepthought.model.EntriesWithoutTagsCalculatedTag
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.TagService
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.TagsSearchResults
import net.dankito.service.search.util.CombinedLazyLoadingList
import net.dankito.utils.ui.IDialogService
import javax.inject.Inject
import kotlin.concurrent.thread


class TagsListPresenter(tagsListView: ITagsListView, private val dataManager: DataManager, searchEngine: ISearchEngine, searchResultsUtil: TagsSearchResultsUtil, tagService: TagService,
                        dialogService: IDialogService, private val router: IRouter)
    : TagsListPresenterBase(tagsListView, searchEngine, tagService, searchResultsUtil, dialogService), IMainViewSectionPresenter {


    @Inject
    protected lateinit var entityChangedNotifier: EntityChangedNotifier


    init {
        thread {
            CommonComponent.component.inject(this)

            initialized()

            calculatedTags.add(AllEntriesCalculatedTag(searchEngine, eventBus, entityChangedNotifier, localization))
            calculatedTags.add(EntriesWithoutTagsCalculatedTag(searchEngine, eventBus, entityChangedNotifier, localization))

            dataManager.addInitializationListener { tagsListView.updateDisplayedTags() }
        }
    }

    override fun cleanUp() {
        destroy()
    }


    override fun getAndShowAllEntities() {
        searchEngine.addInitializationListener { searchTags() }
    }


    override fun getTagsFromSearchTagsWithoutFilterResult(result: TagsSearchResults): List<Tag> {
        if(result.hasEmptySearchTerm) {
            return CombinedLazyLoadingList<Tag>(calculatedTags, result.getRelevantMatchesSorted())
        }

        return super.getTagsFromSearchTagsWithoutFilterResult(result)
    }


    fun isTagFiltered(tag: Tag): Boolean {
        return tagsFilter.contains(tag)
    }

    fun toggleFilterTag(tag: Tag) {
        if(isTagFiltered(tag)) {
            tagsFilter.remove(tag)
        }
        else {
            tagsFilter.add(tag)
        }

        searchTags()
    }


    override fun getLastSearchTerm(): String {
        return lastSearchTermProperty
    }


    fun showEntriesForTag(tag: Tag) {
        router.showEntriesForTag(tag, tagsFilter)
    }

}