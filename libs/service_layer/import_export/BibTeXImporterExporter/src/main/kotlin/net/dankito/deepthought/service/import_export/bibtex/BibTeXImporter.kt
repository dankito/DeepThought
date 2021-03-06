package net.dankito.deepthought.service.import_export.bibtex

import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.data.SourcePersister
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.model.*
import net.dankito.deepthought.service.import_export.IDataImporter
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.SearchEngineBase
import net.dankito.service.search.specific.SeriesSearch
import net.dankito.service.search.specific.TagsSearch
import net.dankito.service.search.specific.TagsSearchResult
import net.dankito.utils.IThreadPool
import org.jbibtex.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


// TODO: actually we should get rid of searchEngine and all that persisters.
// The importer should map the file to Items, Tags etc. and nothing else
// -> Create an ImportProcessor with methods like ensureTagExists().
// Another advantage is, all other imports could re-use exactly that functionality.
class BibTeXImporter(private val searchEngine: ISearchEngine, private val itemPersister: ItemPersister, private val tagService: TagService,
                     private val sourcePersister: SourcePersister, private val seriesPersister: SeriesPersister, private val threadPool: IThreadPool) : IDataImporter {

    companion object {
        val DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val ShortDateFormat = SimpleDateFormat("yy-MM-dd")
        val YearFormat = SimpleDateFormat("yyyy")

        const val TagsSeparator = ";"

        private val log = LoggerFactory.getLogger(BibTeXImporter::class.java)
    }


    override val name: String
        get() = "BibTeX"


    override fun importAsync(bibTeXFile: File, done: (Collection<Item>) -> Unit) {
        threadPool.runAsync {
            done(import(bibTeXFile))
        }
    }

    override fun import(bibTeXFile: File): Collection<Item> {
        log.info("Going to parse BibTeX file $bibTeXFile ...")
        val reader = FileReader(bibTeXFile)
        val filterReader = CharacterFilterReader(reader)

        val bibTeXParser = object : BibTeXParser() {

            override fun checkStringResolution(key: Key?, string: BibTeXString?) {
                log.info("String resolution: $key = $string")
                super.checkStringResolution(key, string)
            }

            override fun checkCrossReferenceResolution(key: Key?, entry: BibTeXEntry?) {
                log.info("Reference resolution: $key = $entry")
                super.checkCrossReferenceResolution(key, entry)
            }

        }

        val database = bibTeXParser.parseFully(filterReader)

        return mapDatabaseToItems(database)
    }

    private fun mapDatabaseToItems(database: BibTeXDatabase): Collection<Item> {
        log.info("Parsed BibTeX file with ${database.entries.size} entries, going to map them to items ...")
        val latexParser = LaTeXParser()
        val latexPrinter = LaTeXPrinter()

        return database.entries.map { mapEntry -> mapBibTeXEntryToItem(mapEntry.value, latexParser, latexPrinter) }.filterNotNull()
    }

    private fun mapBibTeXEntryToItem(bibTeXEntry: BibTeXEntry, latexParser: LaTeXParser, latexPrinter: LaTeXPrinter): Item {
        val item = Item("")
        val tags = ArrayList<Tag>()
        val files = ArrayList<FileLink>() // TODO
        val source = Source()
        sourcePersister.saveSource(source)

        try { item.itemIndex = bibTeXEntry.key.value.toLong() } catch(ignored: Exception) { } // only works for BibTeX exported by DeepThought
        bibTeXEntry.fields.forEach { key, value ->
            mapEntryField(item, tags, source, key, value, latexParser, latexPrinter)
        }

        itemPersister.saveItemAsync(item, source, source.series, tags, files) { }

        return item
    }

    private fun mapEntryField(item: Item, tags: MutableList<Tag>, source: Source, bibTeXKey: Key, bibTeXValue: Value, latexParser: LaTeXParser, latexPrinter: LaTeXPrinter) {
        if (bibTeXValue is StringValue) { // TODO: parse others
            val stringValue = getStringFromBibTeXValue(bibTeXValue, latexParser, latexPrinter)

            mapEntryStringField(item, tags, source, bibTeXKey.value, stringValue)
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

    private fun mapEntryStringField(item: Item, tags: MutableList<Tag>, source: Source, fieldName: String, fieldValue: String) {
        when(fieldName) {
            "annote" -> item.content = item.content + if(item.content.isNotEmpty()) "\n" else "" + fieldValue
            "abstract" -> item.summary = fieldValue
            "pages" -> item.indication = fieldValue

            "keywords" -> mapEntryTags(tags, fieldValue)

            "title" -> source.title = fieldValue
            "url" -> source.url = fieldValue
            "year" -> setSourcePublishingDate(source, fieldValue)
            "urldate" -> setSourceLastAccessDate(source, fieldValue)
            "number" -> source.issue = fieldValue
            "isbn" -> source.isbnOrIssn = fieldValue
            "issn" -> source.isbnOrIssn = fieldValue

            "series" -> setItemSeries(source, fieldValue)
            "journal" -> setItemSeries(source, fieldValue)

            // TODO
            "file" -> { }
        }
    }

    private fun mapEntryTags(tags: MutableList<Tag>, tagNames: String) {
        if(tagNames.isBlank()) {
            return
        }

        val tagNamesAdjusted = tagNames.replace(TagsSeparator, SearchEngineBase.TagsSearchTermSeparator) // tags or often separated by ';' in BibTeX

        val countDownLatch = CountDownLatch(1)

        searchEngine.searchTags(TagsSearch(tagNamesAdjusted) { results ->
            results.results.forEach { result ->
                tags.add(getTagForTagSearchResult(result))
            }

            countDownLatch.countDown()
        })

        countDownLatch.await(2, TimeUnit.MINUTES)
    }

    private fun getTagForTagSearchResult(result: TagsSearchResult): Tag {
        if(result.hasExactMatches()) {
            result.exactMatches.sortedByDescending { it.countItems }.firstOrNull()?.let { return it }
        }

        val newTag = Tag(result.searchTerm)
        tagService.persist(newTag)

        return newTag
    }


    private fun setItemSeries(source: Source, seriesTitle: String) {
        val countDownLatch = CountDownLatch(1)

        searchEngine.searchSeries(SeriesSearch(seriesTitle) { result ->
            addSeriesForSearchResult(source, seriesTitle, result)

            countDownLatch.countDown()
        })

        countDownLatch.await(2, TimeUnit.SECONDS)
    }

    private fun addSeriesForSearchResult(source: Source, seriesTitle: String, result: List<Series>) {
        result.filter { it.title.toLowerCase() == seriesTitle.toLowerCase() }.sortedByDescending { it.countSources }.firstOrNull()?.let {
            source.series = it
        }

        if(source.series == null) { // no series with that name found
            val series = Series(seriesTitle)
            seriesPersister.saveSeries(series)

            source.series = series
        }
    }


    private fun setSourcePublishingDate(source: Source, publishingDateString: String) {
        val publishingDate = tryToParseDateString(publishingDateString)

        source.setPublishingDate(publishingDate, publishingDateString)
    }

    private fun setSourceLastAccessDate(source: Source, lastAccessDateString: String) {
        source.lastAccessDate = tryToParseDateString(lastAccessDateString)
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