package net.dankito.deepthought.data

import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.SeriesService
import net.dankito.service.data.SourceService
import net.dankito.utils.IThreadPool
import java.util.*
import javax.inject.Inject


class SourcePersister(private val sourceService: SourceService, private val seriesService: SeriesService, private val filePersister: FilePersister,
                      private val deleteEntityService: DeleteEntityService) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        BaseComponent.component.inject(this)
    }


    fun saveSourceAsync(source: Source, series: Series?, editedFiles: Collection<FileLink>, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveSource(source, series, editedFiles))
        }
    }

    fun saveSource(source: Source): Boolean {
        return saveSource(source, source.series)
    }

    fun saveSource(source: Source, series: Series?, editedFiles: Collection<FileLink> = source.attachedFiles, doChangesAffectDependentEntities: Boolean = true): Boolean {
        if(series != null && series.isPersisted() == false) { // if series has been newly created but not persisted yet
            seriesService.persist(series)
        }

        if(source.series != null && source.series?.isPersisted() == false) { // series has been deleted in the meantime
            source.series?.let { series ->
                source.series = null

                seriesService.persist(series) // TODO: but why saving it then again?

                source.series = series
            }

        }

        val previousSeries = source.series
        if(previousSeries != null && previousSeries?.id != series?.id) { // remove previous series
            source.series = null
            seriesService.update(previousSeries)
        }

        if(previousSeries?.id != series?.id) {
            source.series = series
        }


        editedFiles.forEach { file ->
            if(file.isPersisted() == false) {
                filePersister.saveFile(file)
            }
        }

        val removedFiles = ArrayList(source.attachedFiles)
        removedFiles.removeAll(editedFiles)

        val addedFiles = ArrayList(editedFiles)
        addedFiles.removeAll(source.attachedFiles)

        source.setAllAttachedFiles(editedFiles.filter { it != null })


        val wasSourcePersisted = source.isPersisted()
        if(wasSourcePersisted == false) {
            sourceService.persist(source)
        }
        else {
            sourceService.update(source, doChangesAffectDependentEntities)
        }

        if(series?.id != previousSeries?.id || wasSourcePersisted == false) {
            series?.let { seriesService.update(series) } // source is now persisted so series needs an update to store source's id
        }


        addedFiles.filterNotNull().forEach { filePersister.saveFile(it) }

        removedFiles.filterNotNull().forEach { file ->
            filePersister.saveFile(file)
            deleteEntityService.mayDeleteFile(file)
        }

        return true
    }

}