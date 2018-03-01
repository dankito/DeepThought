package net.dankito.synchronization.sync.changeshandler

import com.couchbase.lite.DocumentChange
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.jpa.couchbaselite.JavaCouchbaseLiteEntityManager
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.jpa.entitymanager.IEntityManager
import net.dankito.synchronization.database.sync.changeshandler.SynchronizedDataMerger
import net.dankito.util.filesystem.JavaFileStorageService
import net.dankito.util.settings.ILocalSettingsStore
import net.dankito.utils.version.Versions
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File


class SynchronizedDataMergerTest {

    companion object {
        private val TestFolder = "data/test/datamerger"
    }


    private val underTest: SynchronizedDataMerger

    private val entityManager: IEntityManager

    private val entityManagerConfiguration = EntityManagerConfiguration(TestFolder, "data_merger_test")

    private val fileStorageService = JavaFileStorageService()


    init {
        fileStorageService.deleteFolderRecursively(File(entityManagerConfiguration.dataFolder))
        
        entityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration, object : ILocalSettingsStore {

            override fun getDataFolder(): String {
                return entityManagerConfiguration.dataFolder
            }

            override fun setDataFolder(dataFolder: String) { }

            override fun getDatabaseDataModelVersion(): Int {
                return Versions.DataModelVersion
            }

            override fun setDatabaseDataModelVersion(newDataModelVersion: Int) { }

        })

        entityManager.open(entityManagerConfiguration)

        underTest = SynchronizedDataMerger(entityManager)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        entityManager.close()

        fileStorageService.deleteFolderRecursively(File(entityManagerConfiguration.dataFolder))
    }


    @Test
    fun updateCachedSynchronizedEntity_RelatedEntityNotRetrievedYet() {
        val itemWithNotYetRetrievedSource = Item("Item")
        entityManager.persistEntity(itemWithNotYetRetrievedSource)

        val source = Source("Source")
        itemWithNotYetRetrievedSource.source = source // set source so persisting source persists item's id
        entityManager.persistEntity(source)
        entityManager.updateEntity(itemWithNotYetRetrievedSource)

        itemWithNotYetRetrievedSource.source = null // now simulate item has been synchronized but source not yet -> updateCachedSynchronizedEntity() has to set it again


        underTest.updateCachedSynchronizedEntity(DocumentChange(source.id!!), Source::class.java)


        assertThat(itemWithNotYetRetrievedSource.source, notNullValue())
        assertThat(source.items.size, `is`(1))
        assertThat(source.items[0], `is`(itemWithNotYetRetrievedSource))
    }

    @Test
    fun updateCachedSynchronizedEntity_RelatedCollectionNotRetrievedYet() {
        val sourceWithNotYetRetrievedItems = Source("Source")
        entityManager.persistEntity(sourceWithNotYetRetrievedItems)

        val item1 = Item("Item1")
        item1.source = sourceWithNotYetRetrievedItems
        entityManager.persistEntity(item1)

        val item2 = Item("Item2")
        item2.source = sourceWithNotYetRetrievedItems
        entityManager.persistEntity(item2)

        entityManager.updateEntity(sourceWithNotYetRetrievedItems)

        item1.source = null // now simulate source has been synchronized but items not yet -> updateCachedSynchronizedEntity() has to set it again
        item2.source = null


        underTest.updateCachedSynchronizedEntity(DocumentChange(item2.id!!), Item::class.java)


        assertThat(sourceWithNotYetRetrievedItems.items.size, `is`(2))
        assertThat(sourceWithNotYetRetrievedItems.items.contains(item1), `is`(true))
        assertThat(sourceWithNotYetRetrievedItems.items.contains(item2), `is`(true))
        assertThat(item2.source, notNullValue())
    }

}