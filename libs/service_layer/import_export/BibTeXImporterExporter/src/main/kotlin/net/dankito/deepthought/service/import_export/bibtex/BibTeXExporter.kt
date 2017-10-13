package net.dankito.deepthought.service.import_export.bibtex

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.abstractPlainText
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.deepthought.service.import_export.IDataExporter
import net.dankito.utils.IThreadPool
import org.jbibtex.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter


class BibTeXExporter(private val threadPool: IThreadPool) : IDataExporter {

    companion object {
        private val log = LoggerFactory.getLogger(BibTeXExporter::class.java)
    }


    override val name: String
        get() = "BibTeX"


    override fun exportAsync(destinationFile: File, items: Collection<Item>) {
        threadPool.runAsync {
            export(destinationFile, items)
        }
    }

    override fun export(destinationFile: File, items: Collection<Item>) {
        log.info("Starting to export ${items.size} items to $destinationFile")

        val database = mapEntriesToDatabase(items)

        val writer = FileWriter(destinationFile)

        val bibTeXFormatter = BibTeXFormatter()

        bibTeXFormatter.format(database, writer)
        writer.flush()
        writer.close()

        log.info("Exported ${database.entries.size} items to $destinationFile")
    }


    private fun mapEntriesToDatabase(items: Collection<Item>): BibTeXDatabase {
        val database = BibTeXDatabase()

        items.forEach { mapEntry(it, database) }

        return database
    }

    private fun mapEntry(item: Item, database: BibTeXDatabase) {
        val bibTeXEntry = BibTeXEntry(Key("misc"), Key(item.itemIndex.toString())) // TODO: itemIndex is not necessarily unique

        var entryContent = item.contentPlainText
        if(item.summary.isNullOrBlank() == false) {
            entryContent += item.abstractPlainText + if(entryContent.isNullOrBlank()) "" else " - " + item.contentPlainText
        }
        addField(bibTeXEntry, "abstract", entryContent)

        if(item.indication.isNullOrBlank() == false) {
            addField(bibTeXEntry, "pages", item.indication)
        }

        addField(bibTeXEntry, "keywords", item.tags.joinToString(BibTeXImporter.TagsSeparator) { it.name })

        item.source?.let { mapReference(it, bibTeXEntry) }

        // TODO: add files

        database.addObject(bibTeXEntry)
    }

    private fun mapReference(source: Source, bibTeXEntry: BibTeXEntry) {
        addField(bibTeXEntry, "title", source.title)

        source.url?.let { addField(bibTeXEntry, "url", it) }

        source.publishingDate?.let { addField(bibTeXEntry, "year", BibTeXImporter.YearFormat.format(it)) }

        source.lastAccessDate?.let { addField(bibTeXEntry, "urldate", BibTeXImporter.DateFormat.format(it)) }

        source.issue?.let { addField(bibTeXEntry, "number", it) }

        source.isbnOrIssn?.let { addField(bibTeXEntry, "isbn", it) }

        source.series?.let { addField(bibTeXEntry, "series", it.title) }
    }

    private fun addField(bibTeXEntry: BibTeXEntry, key: String, value: String) {
        bibTeXEntry.addField(Key(key), StringValue(value, StringValue.Style.BRACED))
    }

}