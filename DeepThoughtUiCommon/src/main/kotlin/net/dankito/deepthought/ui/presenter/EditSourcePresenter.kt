package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.data.SourcePersister
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class EditSourcePresenter(router: IRouter, dialogService: IDialogService, clipboardService: IClipboardService, deleteEntityService: DeleteEntityService,
                          private val sourcePersister: SourcePersister) : SourcePresenterBase(router, dialogService, clipboardService, deleteEntityService) {

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


    fun saveSourceAsync(source: Source, series: Series?, publishingDateInput: Date?, publishingDateStringInput: String?, editedFiles: Collection<FileLink>, callback: (Boolean) -> Unit) {
        val publishingDateString = if(publishingDateStringInput.isNullOrBlank()) null else publishingDateStringInput
        val publishingDate = if(publishingDateInput != null) publishingDateInput
                             else if(publishingDateString != null) parsePublishingDate(publishingDateString)
                             else null

        source.setPublishingDate(publishingDate, publishingDateString)

        sourcePersister.saveSourceAsync(source, series, editedFiles, callback)
    }


    fun editSeries(source: Source, series: Series?) {
        router.showEditSourceSeriesView(source, series)
    }

}