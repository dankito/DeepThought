package net.dankito.service.search.writerandsearcher.sorting

import net.dankito.service.search.FieldName
import net.dankito.service.search.SortOption
import net.dankito.service.search.SortOrder
import net.dankito.service.search.specific.ItemsSearch
import org.apache.lucene.search.SortField


private val ItemDefaultSortOption = SortOption(FieldName.ItemCreated, SortOrder.Descending, SortField.Type.LONG)

private val ItemDefaultSortOptions = Array(1) { ItemDefaultSortOption }

private val ItemSourcePreviewAscendingSortOptions = listOf(
        SortOption(FieldName.ItemSeries, SortOrder.Ascending, SortField.Type.STRING),
        SortOption(FieldName.ItemSourcePublishingDate, SortOrder.Ascending, SortField.Type.LONG),
        SortOption(FieldName.ItemSourcePublishingDateString, SortOrder.Ascending, SortField.Type.STRING),
        SortOption(FieldName.ItemSource, SortOrder.Ascending, SortField.Type.STRING),
        SortOption(FieldName.ItemSummaryForSorting, SortOrder.Ascending, SortField.Type.STRING),
        SortOption(FieldName.ItemIndication, SortOrder.Ascending, SortField.Type.STRING)
)

private val ItemSourcePreviewDescendingSortOptions = listOf(
        SortOption(FieldName.ItemSeries, SortOrder.Descending, SortField.Type.STRING),
        SortOption(FieldName.ItemSourcePublishingDate, SortOrder.Descending, SortField.Type.LONG),
        SortOption(FieldName.ItemSourcePublishingDateString, SortOrder.Descending, SortField.Type.STRING),
        SortOption(FieldName.ItemSource, SortOrder.Descending, SortField.Type.STRING),
        SortOption(FieldName.ItemSummaryForSorting, SortOrder.Descending, SortField.Type.STRING),
        SortOption(FieldName.ItemIndication, SortOrder.Descending, SortField.Type.STRING)
)


fun ItemsSearch.getLuceneSortOptions(): Array<SortOption> {
    if(this.sortOptions.isEmpty()) { // then use default sorting: sort by ItemCreated
        return ItemDefaultSortOptions
    }
    else {
        return this.sortOptions.map { mapToLuceneSortOption(it) }.filterNotNull().flatten().toTypedArray()
    }
}

private fun mapToLuceneSortOption(sortOption: net.dankito.service.search.util.SortOption): List<SortOption>? {
    return when(sortOption.property) {
        FieldName.ItemPreviewForSorting -> listOf(SortOption(sortOption.property, mapToSortOrder(sortOption.ascending), SortField.Type.STRING))
        FieldName.ItemSourcePreviewForSorting -> if (sortOption.ascending) ItemSourcePreviewAscendingSortOptions else ItemSourcePreviewDescendingSortOptions
        FieldName.ModifiedOn -> listOf(SortOption(sortOption.property, mapToSortOrder(sortOption.ascending), SortField.Type.LONG))
        FieldName.ItemCreated -> listOf(SortOption(sortOption.property, mapToSortOrder(sortOption.ascending), SortField.Type.LONG))
        else -> null
    }
}

private fun mapToSortOrder(ascending: Boolean): SortOrder {
    return if(ascending) SortOrder.Ascending else SortOrder.Descending
}
