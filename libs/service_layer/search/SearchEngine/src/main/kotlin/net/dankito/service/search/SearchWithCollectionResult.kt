package net.dankito.service.search

import net.dankito.deepthought.model.BaseEntity


open class SearchWithCollectionResult<TEntity : BaseEntity>(searchTerm: String, completedListener: (List<TEntity>) -> Unit) :
        Search<List<TEntity>>(searchTerm, completedListener) {

    init {
        results = mutableListOf()
    }


    fun addResult(result: TEntity): Boolean {
        val resultsReference = results // needed for smart cast

        if(resultsReference is MutableList<TEntity>) {
            return resultsReference.add(result)
        }

        return false
    }

    fun getResultsCount(): Int {
        return results.size
    }

}