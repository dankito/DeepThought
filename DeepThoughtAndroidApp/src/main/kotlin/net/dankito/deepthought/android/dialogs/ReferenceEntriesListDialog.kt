package net.dankito.deepthought.android.dialogs

import android.support.v4.app.FragmentManager
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference


class ReferenceEntriesListDialog: EntriesListDialogBase() {

    private lateinit var reference: Reference


    fun showDialog(fragmentManager: FragmentManager, reference: Reference) {
        this.reference = reference

        showDialog(fragmentManager)
    }

    override fun retrieveEntries(callback: (List<Entry>) -> Unit) {
        callback(reference.entries) // TODO: currently no sorting is applied
    }
}