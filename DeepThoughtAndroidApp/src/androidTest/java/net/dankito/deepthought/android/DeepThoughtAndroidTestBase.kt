package net.dankito.deepthought.android

import android.support.test.InstrumentationRegistry
import net.dankito.deepthought.android.di.ActivitiesModule
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.di.DaggerTestComponent
import net.dankito.deepthought.android.di.TestComponent
import net.dankito.deepthought.android.util.UiNavigator
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonModule
import net.dankito.deepthought.model.*
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.*
import net.dankito.utils.version.Versions
import java.util.*
import javax.inject.Inject


abstract class DeepThoughtAndroidTestBase {

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var itemService: EntryService

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var sourceService: ReferenceService

    @Inject
    protected lateinit var seriesService: SeriesService

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService


    protected val res = InstrumentationRegistry.getInstrumentation().context.resources

    protected val navigator = UiNavigator()


    init {
        setupDi()

        // deleting database is not possible as target app gets started before we even have a chance to execute code -> delete database entities
        dataManager.addInitializationListener {
            clearAllData()
        }
    }


    private fun setupDi() {
        val component = DaggerTestComponent.builder()
                .commonModule(CommonModule())
                .activitiesModule(ActivitiesModule(InstrumentationRegistry.getTargetContext()))
                .build()

        BaseComponent.component = component
        CommonComponent.component = component
        AppComponent.setComponentInstance(component)
        TestComponent.setComponentInstance(component)

        TestComponent.component.inject(this)
    }


    private fun clearAllData() {
        clearLocalSettings()

        clearAllUserData()
    }

    protected open fun clearLocalSettings() {
        dataManager.localSettings = LocalSettings(Versions.CommunicationProtocolVersion, Versions.SearchIndexVersion, Versions.HtmlEditorVersion, Date(0), 0, Date(0))
    }

    protected open fun clearAllUserData() {
        val entityManager = dataManager.entityManager

        entityManager.getAllEntitiesOfType(Source::class.java).forEach { deleteEntityService.deleteReference(it) }

        entityManager.getAllEntitiesOfType(Series::class.java).forEach { deleteEntityService.deleteSeries(it) }

        entityManager.getAllEntitiesOfType(ReadLaterArticle::class.java).forEach { readLaterArticleService.delete(it) }

        entityManager.getAllEntitiesOfType(Tag::class.java).forEach { deleteEntityService.deleteTag(it) }

        // delete sources before items as otherwise alert gets shown 'this was single item with that source, do you also want to delete that source?' and app would block / end
        entityManager.getAllEntitiesOfType(Item::class.java).forEach { deleteEntityService.deleteEntry(it) }
    }


    protected open fun persistItem(item: Item) {
        itemService.persist(item)
    }

    protected open fun persistTag(tag: Tag) {
        tagService.persist(tag)
    }

    protected open fun persistSource(source: Source) {
        sourceService.persist(source)
    }

    protected open fun persistSeries(series: Series) {
        seriesService.persist(series)
    }

    protected open fun persistReadLaterArticle(article: ReadLaterArticle) {
        readLaterArticleService.persist(article)
    }


    protected open fun getString(resourceId: Int): String {
        return res.getString(resourceId)
    }

}