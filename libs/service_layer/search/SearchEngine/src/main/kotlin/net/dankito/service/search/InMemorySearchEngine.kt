package net.dankito.service.search

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.abstractPlainText
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.service.search.specific.*
import net.dankito.utils.IThreadPool


class InMemorySearchEngine(private val entityManager: IEntityManager, threadPool: IThreadPool) : SearchEngineBase(threadPool) {

    init {
        searchEngineInitialized()
    }


    override fun close() {
    }

    override fun searchItems(search: ItemsSearch, termsToSearchFor: List<String>) {
        if(search.searchOnlyItemsWithoutTags) {
            getItemsWithoutTags(search)
        }
        else {
            searchForEntitiesOfType(Item::class.java, search, termsToSearchFor, { it.sortedByDescending { it.createdOn } }) { item ->
                val entityValues = listOf(item.contentPlainText.toLowerCase(), item.abstractPlainText.toLowerCase(), item.source?.title?.toLowerCase() ?: "",
                        item.source?.publishingDateString ?: "", item.source?.series?.title?.toLowerCase() ?: "").toMutableList()
                entityValues.addAll(item.tags.map { it.name.toLowerCase() })

                entityValues
            }
        }
    }

    private fun getItemsWithoutTags(search: ItemsSearch) {
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
            search.setRelevantMatchesSorted(tags.sortedBy { it.name.toLowerCase() })
        }
        else {
            termsToSearchFor.forEach { term ->
                val result = ArrayList<Tag>()
                val termLowerCased = term.toLowerCase()
                tags.forEach { tag ->
                    if(tag.name.toLowerCase().contains(termLowerCased)) {
                        result.add(tag)
                    }
                }
                search.addResult(TagsSearchResult(term, result))
            }

            val relevantMatchesSorted = HashSet<Tag>()
            search.results.results.forEach { relevantMatchesSorted.addAll(it.allMatches) }
            search.setRelevantMatchesSorted(relevantMatchesSorted.sortedBy { it.name.toLowerCase() })
        }

        search.fireSearchCompleted()
    }

    override fun searchFilteredTags(search: FilteredTagsSearch, termsToSearchFor: List<String>) {
        // TODO
    }

    override fun searchReferences(search: ReferenceSearch, termsToSearchFor: List<String>) {
        searchForEntitiesOfType(Source::class.java, search, termsToSearchFor, { it.sortedBy { it.title }.sortedByDescending { it.publishingDate }.sortedBy { it.series?.title }}) { source ->
            listOf(source.title.toLowerCase(), source.subTitle.toLowerCase(), source.series?.title?.toLowerCase() ?: "",
                    source.publishingDateString ?: "", source.issue?.toLowerCase() ?: "")
        }
    }

    override fun searchSeries(search: SeriesSearch, termsToSearchFor: List<String>) {
        searchForEntitiesOfType(Series::class.java, search, termsToSearchFor, { it.sortedBy { it.title } }) { series ->
            listOf(series.title.toLowerCase())
        }
    }

    override fun searchReadLaterArticles(search: ReadLaterArticleSearch, termsToSearchFor: List<String>) {
        searchForEntitiesOfType(ReadLaterArticle::class.java, search, termsToSearchFor, { it.sortedByDescending { it.createdOn } }) { article ->
            listOf(article.itemPreview.toLowerCase(), article.sourcePreview.toLowerCase())
        }
    }

    override fun searchFiles(search: FilesSearch, termsToSearchFor: List<String>) {
        // TODO: implement fileType, searchUri, ...
        searchForEntitiesOfType(FileLink::class.java, search, termsToSearchFor, { it.sortedByDescending { it.createdOn } }) { file ->
            listOf(file.uriString.toLowerCase(), file.name.toLowerCase(), file.description.toLowerCase(), file.sourceUriString.toLowerCase())
        }
    }

    override fun searchLocalFileInfo(search: LocalFileInfoSearch) {
        val allLocalFileInfo = entityManager.getAllEntitiesOfType(LocalFileInfo::class.java)

        allLocalFileInfo.forEach { localFileInfo ->
            search.hasSyncStatus?.let {
                if(localFileInfo.syncStatus == it) {
                    search.addResult(localFileInfo)
                }
            }

            search.doesNotHaveSyncStatus?.let {
                if(localFileInfo.syncStatus != it) {
                    search.addResult(localFileInfo)
                }
            }
        }

        search.fireSearchCompleted()
    }


    private fun <T : BaseEntity> searchForEntitiesOfType(type: Class<T>, search: SearchWithCollectionResult<T>, termsToSearchFor: List<String>, sortResults: ((List<T>) -> List<T>)? = null,
                                                         mapEntityToValues: (T) -> List<String>) {
        val items = entityManager.getAllEntitiesOfType(type)
        var results: List<T> = ArrayList()

        if(termsToSearchFor.isEmpty()) {
            results = items
        }
        else {
            items.forEach itemLoop@ { item ->
                if(containsSearchTerms(termsToSearchFor, *mapEntityToValues(item).toTypedArray())) {
                    (results as MutableList).add(item)
                }

            }
        }

        sortResults?.let {
            results = it(results)
        }

        results.forEach { search.addResult(it) }

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