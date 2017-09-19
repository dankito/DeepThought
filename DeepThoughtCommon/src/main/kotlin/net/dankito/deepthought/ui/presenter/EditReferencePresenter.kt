package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.ReferencePersister
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class EditReferencePresenter(searchEngine: ISearchEngine, router: IRouter, clipboardService: IClipboardService, deleteEntityService: DeleteEntityService,
                             private val referencePersister: ReferencePersister) : ReferencesPresenterBase(searchEngine, router, clipboardService, deleteEntityService) {

    companion object {
        private val ShortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT)
        private val MediumDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
        private val LongDateFormat = DateFormat.getDateInstance(DateFormat.LONG)

        private val MonthAndYearSeparatedBySlashDateFormat = SimpleDateFormat("MM/yyyy")
        private val MonthAndShortYearSeparatedBySlashDateFormat = SimpleDateFormat("MM/yy")
        private val MonthAndYearSeparatedByDotDateFormat = SimpleDateFormat("MM.yyyy")
        private val MonthAndShortYearSeparatedByDotDateFormat = SimpleDateFormat("MM.yy")
        private val YearDateFormat = SimpleDateFormat("yyyy")
    }


    fun convertPublishingDateToText(publishingDate: Date): String {
        return getBestDateFormatForLanguage().format(publishingDate)
    }

    private fun getBestDateFormatForLanguage(): DateFormat {
        if(Locale.getDefault().language == "de") {
            return MediumDateFormat
        }

        return ShortDateFormat
    }


    fun parsePublishingDate(dateString: String): Date? {
        try { return ShortDateFormat.parse(dateString) } catch(ignored: Exception) { }

        try { return MediumDateFormat.parse(dateString) } catch(ignored: Exception) { }

        try { return LongDateFormat.parse(dateString) } catch(ignored: Exception) { }

        try { return MonthAndYearSeparatedBySlashDateFormat.parse(dateString) } catch(ignored: Exception) { }
        try { return MonthAndShortYearSeparatedBySlashDateFormat.parse(dateString) } catch(ignored: Exception) { }

        try { return MonthAndYearSeparatedByDotDateFormat.parse(dateString) } catch(ignored: Exception) { }
        try { return MonthAndShortYearSeparatedByDotDateFormat.parse(dateString) } catch(ignored: Exception) { }

        try { return YearDateFormat.parse(dateString) } catch(ignored: Exception) { }

        return null
    }


    fun saveReferenceAsync(reference: Reference, callback: (Boolean) -> Unit) {
        referencePersister.saveReferenceAsync(reference, callback)
    }

}