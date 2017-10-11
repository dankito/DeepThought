package net.dankito.deepthought.service.import_export.bibtex

import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.data.ReferencePersister
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.service.import_export.IDataImporter
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.SearchEngineBase
import net.dankito.service.search.specific.SeriesSearch
import net.dankito.service.search.specific.TagsSearch
import net.dankito.service.search.specific.TagsSearchResult
import org.jbibtex.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class BibTeXImporter(private val searchEngine: ISearchEngine, private val entryPersister: EntryPersister, private val tagService: TagService,
                     private val referencePersister: ReferencePersister, private val seriesPersister: SeriesPersister) : IDataImporter {

    companion object {
        private val DateFormat = SimpleDateFormat("yyyy-MM-dd")
        private val ShortDateFormat = SimpleDateFormat("yy-MM-dd")
        private val YearFormat = SimpleDateFormat("yyyy")

        private const val TagsSeparator = ";"

        private val log = LoggerFactory.getLogger(BibTeXImporter::class.java)
    }


    override val name: String
        get() = "BibTeX"


    override fun import(bibTeXFile: File): Collection<Entry> {
        val reader = FileReader(bibTeXFile)
        val filterReader = CharacterFilterReader(reader)

        val bibTeXParser = object : BibTeXParser() {

            override fun checkStringResolution(key: Key?, string: BibTeXString?) {
                log.info("String resolution: $key = $string")
                super.checkStringResolution(key, string)
            }

            override fun checkCrossReferenceResolution(key: Key?, entry: BibTeXEntry?) {
                log.info("String resolution: $key = $entry")
                super.checkCrossReferenceResolution(key, entry)
            }

        }

        val database = bibTeXParser.parseFully(filterReader)

        return mapDatabaseToEntries(database)

        // http://www.kleinezeitung.at/oesterreich/5298693/Maskottchen-bestraft_Verhuellungsverbot_In-Wien-musste-ein-Hai
    }

    private fun mapDatabaseToEntries(database: BibTeXDatabase): Collection<Entry> {
        val latexParser = LaTeXParser()
        val latexPrinter = LaTeXPrinter()

        return database.entries.map { mapEntry -> mapBibTeXEntryToEntry(mapEntry.value, latexParser, latexPrinter) }.filterNotNull()
    }

    private fun mapBibTeXEntryToEntry(bibTeXEntry: BibTeXEntry, latexParser: LaTeXParser, latexPrinter: LaTeXPrinter): Entry {
        val entry = Entry("")
        val reference = Reference("")
        referencePersister.saveReference(reference)

        bibTeXEntry.fields.forEach { key, value ->
            mapEntryField(entry, reference, key, value, latexParser, latexPrinter)
        }

        entryPersister.saveEntryAsync(entry, reference, reference.series, entry.tags) {
            log.info("Persisted entry $entry")
        }
        log.info("Created entry $entry")

        return entry
    }

    private fun mapEntryField(entry: Entry, reference: Reference, bibTeXKey: Key, bibTeXValue: Value, latexParser: LaTeXParser, latexPrinter: LaTeXPrinter) {
        if (bibTeXValue is StringValue) { // TODO: parse others
            val stringValue = getStringFromBibTeXValue(bibTeXValue, latexParser, latexPrinter)
            log.info("Parsing $bibTeXKey = $stringValue")

            mapEntryStringField(entry, reference, bibTeXKey.value, stringValue)
        }
    }

    private fun getStringFromBibTeXValue(bibTeXValue: Value, latexParser: LaTeXParser, latexPrinter: LaTeXPrinter): String {
        var stringValue = bibTeXValue.toUserString()

        try {
            val latexObjects = latexParser.parse(stringValue)
            stringValue = latexPrinter.print(latexObjects)
        } catch(e: Exception) {
            if (stringValue.startsWith("http") == false) log.warn("Could not parse \'$stringValue\' with latexParser", e)
        }

        stringValue = stringValue.replace("{\\\"A}", "Ä").replace("{\\\"a}", "ä").replace("{\\\"O}", "Ö").replace("{\\\"o}", "ö").replace("{\\\"U}", "Ü").replace("{\\\"u}", "ü")

        return stringValue
    }

    private fun mapEntryStringField(entry: Entry, reference: Reference, fieldName: String, fieldValue: String) {
        when(fieldName) {
            // TODO: map Umlaute, {\"a} -> ae
            "annote" -> entry.content = entry.content + if(entry.content.isNotEmpty()) "\n" else "" + fieldValue
            "abstract" -> entry.abstractString = fieldValue
            "pages" -> entry.indication = fieldValue

            "keywords" -> mapEntryTags(entry, fieldValue)

            "title" -> reference.title = fieldValue
            "url" -> reference.url = fieldValue
            "year" -> setReferencePublishingDate(reference, fieldValue)
            "urldate" -> setReferenceLastAccessDate(reference, fieldValue)
            "number" -> reference.issue = fieldValue
            "isbn" -> reference.isbnOrIssn = fieldValue
            "issn" -> reference.isbnOrIssn = fieldValue

            "series" -> setEntrySeries(reference, fieldValue)
            "journal" -> setEntrySeries(reference, fieldValue)

            // TODO
            "file" -> { }
        }
    }

    private fun mapEntryTags(entry: Entry, tagNames: String) {
        val tagNamesAdjusted = tagNames.replace(TagsSeparator, SearchEngineBase.TagsSearchTermSeparator) // tags or often separated by ';' in BibTeX

        val countDownLatch = CountDownLatch(1)

        searchEngine.searchTags(TagsSearch(tagNamesAdjusted) { results ->
            results.results.forEach { result ->
                addTagForTagSearchResult(result, entry)
            }

            countDownLatch.countDown()
        })

        countDownLatch.await(2, TimeUnit.MINUTES)
    }

    private fun addTagForTagSearchResult(result: TagsSearchResult, entry: Entry) {
        if(result.hasExactMatches()) {
            result.exactMatches.sortedByDescending { it.countEntries }.firstOrNull()?.let { entry.addTag(it) }
        }
        else {
            val newTag = Tag(result.searchTerm)
            tagService.persist(newTag)

            entry.addTag(newTag)
        }
    }


    private fun setEntrySeries(reference: Reference, seriesTitle: String) {
        val countDownLatch = CountDownLatch(1)

        searchEngine.searchSeries(SeriesSearch(seriesTitle) { result ->
            addSeriesForSearchResult(reference, seriesTitle, result)

            countDownLatch.countDown()
        })

        countDownLatch.await(2, TimeUnit.SECONDS)
    }

    private fun addSeriesForSearchResult(reference: Reference, seriesTitle: String, result: List<Series>) {
        result.filter { it.title.toLowerCase() == seriesTitle.toLowerCase() }.sortedByDescending { it.countReferences }.firstOrNull()?.let {
            reference.series = it
        }

        if(reference.series == null) { // no series with that name found
            val series = Series(seriesTitle)
            seriesPersister.saveSeries(series)

            reference.series = series
        }
    }


    private fun setReferencePublishingDate(reference: Reference, publishingDateString: String) {
        val publishingDate = tryToParseDateString(publishingDateString)

        reference.setPublishingDate(publishingDate, publishingDateString)
    }

    private fun setReferenceLastAccessDate(reference: Reference, lastAccessDateString: String) {
        reference.lastAccessDate = tryToParseDateString(lastAccessDateString)
    }

    private fun tryToParseDateString(dateString: String): Date? {
        var date: Date? = null

        try {
            date = DateFormat.parse(dateString)
        } catch(e: Exception) {
//            log.warn("Could not parse date string \'$dateString\' to Date", e)
        }

        try {
            if(date == null) {
                date = ShortDateFormat.parse(dateString)
            }
        } catch(e: Exception) {
//            log.warn("Could not parse date string \'$dateString\' to short Date", e)
        }

        try {
            if(date == null) {
                date = YearFormat.parse(dateString)
            }
        } catch(e: Exception) {
            log.warn("Could not parse date string \'$dateString\' to year", e)
        }

        return date
    }

}