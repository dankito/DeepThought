package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.presenter.util.ReferencePersister
import java.text.DateFormat
import java.util.*


class EditReferencePresenter(private val referencePersister: ReferencePersister) {


    fun convertPublishingDateToText(publishingDate: Date): String {
        return getLongDateFormat().format(publishingDate)
    }

    private fun getLongDateFormat(): DateFormat {
        return DateFormat.getDateInstance(DateFormat.LONG)
    }


    fun saveReferenceAsync(reference: Reference, callback: (Boolean) -> Unit) {
        referencePersister.saveReferenceAsync(reference, callback)
    }

}