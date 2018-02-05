package net.dankito.deepthought.android

import android.support.test.InstrumentationRegistry
import net.dankito.deepthought.android.di.TestComponent
import net.dankito.deepthought.android.util.TestUtil
import net.dankito.deepthought.android.util.UiNavigator
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.data.ReferencePersister
import net.dankito.deepthought.model.*
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.*
import net.dankito.service.search.ISearchEngine
import org.junit.After
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject


abstract class DeepThoughtAndroidTestBase {

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var itemPersister: ItemPersister

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var sourcePersister: ReferencePersister

    @Inject
    protected lateinit var seriesService: SeriesService

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var fileService: FileService

    @Inject
    protected lateinit var searchEngine: ISearchEngine


    protected val res = InstrumentationRegistry.getInstrumentation().context.resources

    protected val navigator = UiNavigator()


    init {
        TestComponent.component.inject(this)
    }


    @After
    fun tearDown() {
        dataManager.entityManager.close()
        // as same application instance is used for all test cases, restore default values
        dataManager.localSettings = LocalSettings(1, 1, 1, Date(0), 0, Date(0))
    }


    protected open fun persistItem(item: Item, source: Source? = null, vararg tags: Tag) {
        val waitLatch = CountDownLatch(1)

        itemPersister.saveEntryAsync(item, source, source?.series, tags.toList()) { waitLatch.countDown() }

        try { waitLatch.await() } catch(ignored: Exception) { }
        TestUtil.sleep(1000)
    }

    protected open fun persistTag(tag: Tag) {
        tagService.persist(tag)
    }

    protected open fun persistSource(source: Source, series: Series? = source.series) {
        sourcePersister.saveReference(source, series)
    }

    protected open fun persistSeries(series: Series) {
        seriesService.persist(series)
    }

    protected open fun persistReadLaterArticle(article: ReadLaterArticle) {
        readLaterArticleService.persist(article)
    }

    protected open fun persistFile(file: FileLink) {
        fileService.persist(file)
    }


    protected open fun getString(resourceId: Int): String {
        return res.getString(resourceId)
    }

}