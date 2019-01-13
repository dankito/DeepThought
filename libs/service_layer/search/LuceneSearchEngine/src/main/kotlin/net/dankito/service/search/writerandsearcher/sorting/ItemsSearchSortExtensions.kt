package net.dankito.service.search.writerandsearcher.sorting

import net.dankito.service.search.FieldName
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.specific.ItemsSearch
import org.apache.lucene.search.SortField


fun ItemsSearch.getLuceneSortOptions(): Array<SortOption> {
    if(this.sortOptions.isEmpty()) { // then use default sorting: sort by ItemCreated
        return Array(1) { createDefaultSortOption() }
    }
    else {
        return this.sortOptions.map { mapToLuceneSortOption(it) }.filterNotNull().toTypedArray()
    }
}

private fun mapToLuceneSortOption(sortOption: net.dankito.service.search.util.SortOption): SortOption? {
    return when(sortOption.property) {
        FieldName.ItemPreviewForSorting -> SortOption(sortOption.property, mapToSortOrder(sortOption.ascending), SortField.Type.STRING)
        FieldName.ItemSourcePreviewForSorting -> SortOption(sortOption.property, mapToSortOrder(sortOption.ascending), SortField.Type.STRING)
        FieldName.ModifiedOn -> SortOption(sortOption.property, mapToSortOrder(sortOption.ascending), SortField.Type.LONG)
        FieldName.ItemCreated -> SortOption(sortOption.property, mapToSortOrder(sortOption.ascending), SortField.Type.LONG)
        else -> null
    }
}

private fun createDefaultSortOption() = SortOption(FieldName.ItemCreated, SortOrder.Descending, SortField.Type.LONG)

private fun mapToSortOrder(ascending: Boolean): SortOrder {
    return if(ascending) SortOrder.Ascending else SortOrder.Descending
}
