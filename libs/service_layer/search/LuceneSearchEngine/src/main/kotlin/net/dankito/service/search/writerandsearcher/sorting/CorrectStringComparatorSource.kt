package net.dankito.service.search.writerandsearcher.sorting

import org.apache.lucene.search.FieldComparator
import org.apache.lucene.search.FieldComparatorSource


class CorrectStringComparatorSource : FieldComparatorSource() {

    override fun newComparator(fieldName: String, numHits: Int, sortPos: Int, reversed: Boolean): FieldComparator<*> {
        return CorrectStringFieldComparator(numHits, fieldName, reversed)
    }

}