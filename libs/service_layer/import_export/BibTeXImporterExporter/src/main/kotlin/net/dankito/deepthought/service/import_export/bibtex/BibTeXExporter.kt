package net.dankito.deepthought.service.import_export.bibtex

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
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


    override fun exportAsync(destinationFile: File, entries: Collection<Entry>) {
        threadPool.runAsync {
            export(destinationFile, entries)
        }
    }

    override fun export(destinationFile: File, entries: Collection<Entry>) {
        log.info("Starting to export ${entries.size} entries to $destinationFile")

        val database = mapEntriesToDatabase(entries)

        val writer = FileWriter(destinationFile)

        val bibTeXFormatter = BibTeXFormatter()

        bibTeXFormatter.format(database, writer)
        writer.flush()
        writer.close()

        log.info("Exported ${database.entries.size} entries to $destinationFile")
    }


    private fun mapEntriesToDatabase(entries: Collection<Entry>): BibTeXDatabase {
        val database = BibTeXDatabase()

        entries.forEach { mapEntry(it, database) }

        return database
    }

    private fun mapEntry(entry: Entry, database: BibTeXDatabase) {
        val bibTeXEntry = BibTeXEntry(Key("misc"), Key(entry.entryIndex.toString())) // TODO: entryIndex is not necessarily unique

        var entryContent = entry.contentPlainText
        if(entry.abstractString.isNullOrBlank() == false) {
            entryContent += entry.abstractPlainText + if(entryContent.isNullOrBlank()) "" else "\n\n" + entry.contentPlainText
        }
        addField(bibTeXEntry, "abstract", entryContent)

        if(entry.indication.isNullOrBlank() == false) {
            addField(bibTeXEntry, "pages", entry.indication)
        }

        addField(bibTeXEntry, "keywords", entry.tags.joinToString(BibTeXImporter.TagsSeparator) { it.name })

        entry.reference?.let { mapReference(it, bibTeXEntry) }

        // TODO: add files

        database.addObject(bibTeXEntry)
    }

    private fun mapReference(reference: Reference, bibTeXEntry: BibTeXEntry) {
        addField(bibTeXEntry, "title", reference.title)

        reference.url?.let { addField(bibTeXEntry, "url", it) }

        reference.publishingDate?.let { addField(bibTeXEntry, "year", BibTeXImporter.YearFormat.format(it)) }

        reference.lastAccessDate?.let { addField(bibTeXEntry, "urldate", BibTeXImporter.DateFormat.format(it)) }

        reference.issue?.let { addField(bibTeXEntry, "number", it) }

        reference.isbnOrIssn?.let { addField(bibTeXEntry, "isbn", it) }

        reference.series?.let { addField(bibTeXEntry, "series", it.title) }
    }

    private fun addField(bibTeXEntry: BibTeXEntry, key: String, value: String) {
        bibTeXEntry.addField(Key(key), StringValue(value, StringValue.Style.BRACED))
    }

}