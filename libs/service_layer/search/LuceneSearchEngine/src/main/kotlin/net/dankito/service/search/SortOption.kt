package net.dankito.service.search

import org.apache.lucene.search.SortField


data class SortOption(val fieldName: String, val order: SortOrder, val type: SortField.Type)