package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Item
import net.dankito.service.data.ItemService
import net.dankito.service.data.messages.ItemChanged
import net.dankito.service.eventbus.EventBusPriorities
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.service.search.specific.ItemsSearch
import net.dankito.service.search.writerandsearcher.sorting.getLuceneSortOptions
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.engio.mbassy.listener.Handler
import org.apache.lucene.index.Term
import org.apache.lucene.search.WildcardQuery


/**
 * An Index that only contains item ids so that searching for all items (e.g. on app start) is a little bit faster than searching in big ItemIndexWriterAndSearcher
 */
class ItemIdsIndexWriterAndSearcher(itemService: ItemService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : ItemIndexWriterAndSearcherBase(itemService, eventBus, osHelper, threadPool) {


    override fun getDirectoryName(): String {
        return "item_ids"
    }

    override fun getIdFieldName(): String {
        return FieldName.ItemIdsId
    }


    fun searchItemIds(search: ItemsSearch, termsToFilterFor: List<String>) {
        val query = WildcardQuery(Term(getIdFieldName(), "*"))

        executeQueryForSearchWithCollectionResult(search, query, Item::class.java, MaxItemsSearchResults, *search.getLuceneSortOptions())
    }


    override fun createEntityChangedListener(): Any {
        return object {

            @Handler(priority = EventBusPriorities.Indexer)
            fun entityChanged(itemChanged: ItemChanged) {
                handleEntityChange(itemChanged)
            }

        }
    }

}