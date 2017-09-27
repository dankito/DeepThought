package net.dankito.deepthought.android.dialogs

import android.support.v4.app.FragmentManager
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.service.search.specific.EntriesSearch


class ReferenceEntriesListDialog: EntriesListDialogBase() {

    companion object {
        val TAG: String = javaClass.name
    }


    private lateinit var reference: Reference


    override fun getDialogTag() = TAG


    fun showDialog(fragmentManager: FragmentManager, reference: Reference) {
        this.reference = reference

        showDialog(fragmentManager)
    }

    override fun retrieveEntries(callback: (List<Entry>) -> Unit) {
        searchEngine.searchEntries(EntriesSearch(entriesMustHaveThisReference = reference) {
            callback(it)
        })
    }

    override fun getDialogTitle(entries: List<Entry>): String {
        return reference.title
    }

}