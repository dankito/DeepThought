package net.dankito.deepthought.android.dialogs

import android.support.v4.app.FragmentManager
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference


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
        reference.let { reference ->
            callback(reference.entries) // TODO: currently no sorting is applied
        }
    }

    override fun getDialogTitle(entries: List<Entry>): String {
        return reference.title
    }

}