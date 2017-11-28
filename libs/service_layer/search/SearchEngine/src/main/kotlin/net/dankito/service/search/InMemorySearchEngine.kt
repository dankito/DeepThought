package net.dankito.service.search

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.abstractPlainText
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.service.search.specific.*
import net.dankito.utils.IThreadPool
import java.util.*
import kotlin.collections.ArrayList


class InMemorySearchEngine(private val entityManager: IEntityManager, threadPool: IThreadPool) : SearchEngineBase(threadPool) {

    init {
        searchEngineInitialized()
    }


    override fun close() {
    }

    override fun searchEntries(search: EntriesSearch, termsToSearchFor: List<String>) {
        if(search.filterOnlyEntriesWithoutTags) {
            getItemsWithoutTags(search)
        }
        else {
            searchForEntitiesOfType(Item::class.java, search, termsToSearchFor) { item ->
                val entityValues = Arrays.asList(item.contentPlainText.toLowerCase(), item.abstractPlainText.toLowerCase(), item.source?.title?.toLowerCase() ?: "",
                        item.source?.publishingDateString ?: "", item.source?.series?.title?.toLowerCase() ?: "").toMutableList()
                entityValues.addAll(item.tags.map { it.name.toLowerCase() })

                entityValues
            }
        }
    }

    private fun getItemsWithoutTags(search: EntriesSearch) {
        val items = entityManager.getAllEntitiesOfType(Item::class.java)

        items.forEach { item ->
            if(item.hasTags() == false) {
                search.addResult(item)
            }
        }

        search.fireSearchCompleted()
    }

    override fun searchTags(search: TagsSearch, termsToSearchFor: List<String>) {
        val tags = entityManager.getAllEntitiesOfType(Tag::class.java)

        if(termsToSearchFor.isEmpty()) {
            search.addResult(TagsSearchResult(search.searchTerm, tags))
            search.setRelevantMatchesSorted(tags)
        }
        else {
            termsToSearchFor.map { it.toLowerCase() }.forEach { term ->
                val result = ArrayList<Tag>()
                tags.forEach { tag ->
                    if(tag.name.toLowerCase().contains(term)) {
                        result.add(tag)
                    }
                }
                search.addResult(TagsSearchResult(term, result))
            }
        }

        search.fireSearchCompleted()
    }

    override fun searchFilteredTags(search: FilteredTagsSearch, termsToSearchFor: List<String>) {
        // TODO
    }

    override fun searchReferences(search: ReferenceSearch, termsToSearchFor: List<String>) {
        searchForEntitiesOfType(Source::class.java, search, termsToSearchFor) { source ->
            Arrays.asList(source.title.toLowerCase(), source.subTitle.toLowerCase(), source.series?.title?.toLowerCase() ?: "",
                    source.publishingDateString ?: "", source.issue?.toLowerCase() ?: "")
        }
    }

    override fun searchSeries(search: SeriesSearch, termsToSearchFor: List<String>) {
        searchForEntitiesOfType(Series::class.java, search, termsToSearchFor) { series ->
            Arrays.asList(series.title.toLowerCase())
        }
    }

    override fun searchReadLaterArticles(search: ReadLaterArticleSearch, termsToSearchFor: List<String>) {
        searchForEntitiesOfType(ReadLaterArticle::class.java, search, termsToSearchFor) { article ->
            Arrays.asList(article.itemPreview.toLowerCase(), article.sourcePreview.toLowerCase())
        }
    }


    private fun <T : BaseEntity> searchForEntitiesOfType(type: Class<T>, search: SearchWithCollectionResult<T>, termsToSearchFor: List<String>, mapEntityToValues: (T) -> List<String>) {
        val items = entityManager.getAllEntitiesOfType(type)

        if(termsToSearchFor.isEmpty()) {
            items.forEach { search.addResult(it) }
        }
        else {
            items.forEach itemLoop@ { item ->
                if(containsSearchTerms(termsToSearchFor, *mapEntityToValues(item).toTypedArray())) {
                    search.addResult(item)
                }

            }
        }

        search.fireSearchCompleted()
    }

    private fun containsSearchTerms(termsToSearchFor: List<String>, vararg entityValues: String): Boolean {
        termsToSearchFor.forEach { term ->
            var containsTerm = false

            entityValues.forEach { value ->
                if(value.contains(term)) {
                    containsTerm = true
                    return@forEach
                }
            }

            if(containsTerm == false) {
                return false
            }
        }

        return true
    }


}