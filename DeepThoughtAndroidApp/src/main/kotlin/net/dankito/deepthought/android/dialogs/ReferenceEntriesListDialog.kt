package net.dankito.deepthought.android.dialogs

import android.support.v4.app.FragmentManager
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference


class ReferenceEntriesListDialog: EntriesListDialogBase() {

    private var reference: Reference? = null


    fun showDialog(fragmentManager: FragmentManager, reference: Reference) {
        this.reference = reference

        showDialog(fragmentManager)

        retrieveAndShowEntries()
    }

    override fun retrieveEntries(callback: (List<Entry>) -> Unit) {
        reference?.let { reference ->
            callback(reference.entries) // TODO: currently no sorting is applied
        }

        if(reference == null) { // sometimes onAttach() is called before reference is set (how can this ever be?)
            callback(emptyList())
        }
    }

}