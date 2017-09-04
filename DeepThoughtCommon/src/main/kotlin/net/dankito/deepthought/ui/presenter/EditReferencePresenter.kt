package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Reference
import net.dankito.service.data.DeleteEntityService
import net.dankito.deepthought.ui.presenter.util.ReferencePersister
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import java.text.DateFormat
import java.util.*


class EditReferencePresenter(searchEngine: ISearchEngine, clipboardService: IClipboardService, deleteEntityService: DeleteEntityService,
                             private val referencePersister: ReferencePersister) : ReferencesPresenterBase(searchEngine, clipboardService, deleteEntityService) {


    fun convertPublishingDateToText(publishingDate: Date): String {
        return getBestDateFormatForLanguage().format(publishingDate)
    }

    private fun getBestDateFormatForLanguage(): DateFormat {
        if(Locale.getDefault().language == "de") {
            return getMediumDateFormat()
        }

        return getShortDateFormat()
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