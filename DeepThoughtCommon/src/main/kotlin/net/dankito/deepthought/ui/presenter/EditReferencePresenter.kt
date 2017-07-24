package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.presenter.util.ReferencePersister
import java.text.DateFormat
import java.util.*


class EditReferencePresenter(private val referencePersister: ReferencePersister) {


    fun convertPublishingDateToText(publishingDate: Date): String {
        return getLongDateFormat().format(publishingDate)
    }

    fun parsePublishingDate(dateString: String): Date? {
        try { return getShortDateFormat().parse(dateString) } catch(ignored: Exception) { }

        try { return getMediumDateFormat().parse(dateString) } catch(ignored: Exception) { }

        try { return getLongDateFormat().parse(dateString) } catch(ignored: Exception) { }

        return null
    }

    private fun getShortDateFormat(): DateFormat {
        return DateFormat.getDateInstance(DateFormat.SHORT)
    }

    private fun getMediumDateFormat(): DateFormat {
        return DateFormat.getDateInstance(DateFormat.MEDIUM)
    }

    private fun getLongDateFormat(): DateFormat {
        return DateFormat.getDateInstance(DateFormat.LONG)
    }


    fun saveReferenceAsync(reference: Reference, callback: (Boolean) -> Unit) {
        referencePersister.saveReferenceAsync(reference, callback)
    }

}