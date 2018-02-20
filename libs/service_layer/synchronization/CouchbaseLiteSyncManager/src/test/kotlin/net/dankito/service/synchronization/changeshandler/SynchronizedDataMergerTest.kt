package net.dankito.service.synchronization.changeshandler

import com.couchbase.lite.DocumentChange
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.utils.settings.ILocalSettingsStore
import net.dankito.utils.version.Versions
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test


class SynchronizedDataMergerTest {

    private val entityManager: IEntityManager

    private val underTest: SynchronizedDataMerger


    init {
        val entityManagerConfiguration = EntityManagerConfiguration("test/datamerger", "data_merger_test")

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