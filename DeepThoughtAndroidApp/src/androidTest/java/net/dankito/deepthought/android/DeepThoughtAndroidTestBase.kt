package net.dankito.deepthought.android

import android.support.test.InstrumentationRegistry
import net.dankito.deepthought.android.di.ActivitiesModule
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.test.DaggerTestComponent
import net.dankito.deepthought.android.test.TestComponent
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.di.CommonModule
import net.dankito.deepthought.model.*
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.version.Versions
import java.util.*
import javax.inject.Inject


abstract class DeepThoughtAndroidTestBase {

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService


    init {
        setupDi()

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

        entityManager.getAllEntitiesOfType(Item::class.java).forEach { deleteEntityService.deleteEntry(it) }

        entityManager.getAllEntitiesOfType(Tag::class.java).forEach { deleteEntityService.deleteTag(it) }

        entityManager.getAllEntitiesOfType(Source::class.java).forEach { deleteEntityService.deleteReference(it) }

        entityManager.getAllEntitiesOfType(Series::class.java).forEach { deleteEntityService.deleteSeries(it) }

        entityManager.getAllEntitiesOfType(ReadLaterArticle::class.java).forEach { readLaterArticleService.delete(it) }
    }
}