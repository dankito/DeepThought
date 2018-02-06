package net.dankito.deepthought.javafx.service.import_export

import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.data.SourcePersister
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.service.import_export.IDataExporter
import net.dankito.deepthought.service.import_export.IDataImporter
import net.dankito.deepthought.service.import_export.bibtex.BibTeXExporter
import net.dankito.deepthought.service.import_export.bibtex.BibTeXImporter
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.IThreadPool
import java.util.*


class DataImporterExporterManager(private val searchEngine: ISearchEngine, private val itemPersister: ItemPersister, private val tagService: TagService,
                                  private val sourcePersister: SourcePersister, private val seriesPersister: SeriesPersister, private val threadPool: IThreadPool) {

    val importer: List<IDataImporter> = ArrayList<IDataImporter>()
        get

    val exporter: Collection<IDataExporter> = ArrayList<IDataExporter>()
        get


    init {
        createImporterAndExporter()
    }


    private fun createImporterAndExporter() {
        (importer as? MutableCollection<IDataImporter>)?.let { importer ->
            importer.add(BibTeXImporter(searchEngine, itemPersister, tagService, sourcePersister, seriesPersister, threadPool))
        }

        (exporter as? MutableCollection<IDataExporter>)?.let { exporter ->
            exporter.add(BibTeXExporter(threadPool))
        }
    }

}