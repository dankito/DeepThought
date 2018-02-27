package net.dankito.deepthought.android.play_store

import net.dankito.data_access.network.communication.callback.DeviceRegistrationHandlerBase
import net.dankito.synchronization.device.communication.callback.IDeviceRegistrationHandler
import net.dankito.deepthought.android.DeepThoughtActivityTestRule
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.MainActivity
import net.dankito.deepthought.android.di.TestComponent
import net.dankito.deepthought.android.util.TestUtil
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import net.dankito.deepthought.beta.preview.test.R
import net.dankito.deepthought.model.*
import net.dankito.synchronization.model.enums.OsType
import net.dankito.richtexteditor.command.CommandName
import net.dankito.service.search.specific.TagsSearch
import net.dankito.synchronization.model.DiscoveredDevice
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.slf4j.LoggerFactory
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.text.SimpleDateFormat
import java.util.concurrent.CountDownLatch
import javax.inject.Inject


class CreatePlayStoreScreenShots : DeepThoughtAndroidTestBase() {

    companion object {

        @get:ClassRule
        @JvmStatic
        val localeTestRule = LocaleTestRule()


        const val ItemsListScreenshotName = "01_Items_List"

        const val RichTextEditorScreenshotName = "02_Rich_Text_Editor"

        const val SearchScreenshotName = "03_Search"

        const val TagsListScreenshotName = "04_Tags"

        const val SyncDataScreenshotName = "06_Sync_Data"


        private val log = LoggerFactory.getLogger(CreatePlayStoreScreenShots::class.java)
    }


    @get:Rule
    val testRule = DeepThoughtActivityTestRule<MainActivity>(MainActivity::class.java)


    @get:Rule
    var takeScreenshotOnError = TakeScreenshotOnErrorTestRule()


    @Inject
    protected lateinit var deviceRegistrationHandler: IDeviceRegistrationHandler



    @Before
    fun setUp() {
        TestComponent.component.inject(this)

        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

        createTestData()
    }


    @Test
    fun createScreenshots() {
        TestUtil.sleep(1000)
        takeScreenShot(ItemsListScreenshotName)


        createRichTextEditorScreenshot()

        createSearchScreenshot()

        createSyncDataScreenshot()

        createTagsListScreenshot() // take tag list screenshot at last as it alters tags and items

        Thread.sleep(5 * 1000)
    }

    private fun createRichTextEditorScreenshot() {
        navigator.navigateFromMainActivityToEditItemActivity()
        TestUtil.sleep(500)

        navigator.hideKeyboard()

        navigator.enterText(R.string.item_content_editor_introduction_line)

        navigator.enterNewLine()
        navigator.clickOnEditorCommand(CommandName.INSERTUNORDEREDLIST)

        navigator.clickOnEditorCommand(CommandName.BOLD)
        navigator.enterText(R.string.item_content_editor_bold)
        navigator.clickOnEditorCommand(CommandName.BOLD)
        navigator.enterNewLine()

        navigator.clickOnEditorCommand(CommandName.ITALIC)
        navigator.enterText(R.string.item_content_editor_italic)
        navigator.clickOnEditorCommand(CommandName.ITALIC)
        navigator.enterNewLine()

        navigator.clickOnEditorCommand(CommandName.UNDERLINE)
        navigator.enterText(R.string.item_content_editor_underline)
        navigator.clickOnEditorCommand(CommandName.UNDERLINE)
        navigator.enterNewLine()

        navigator.enterText(R.string.item_content_editor_text_color_introduction)
        navigator.clickOnEditorCommand(CommandName.BACKCOLOR)
        navigator.enterText(R.string.item_content_editor_text_background_color)
        navigator.clickOnEditorCommand(CommandName.BACKCOLOR)
        navigator.enterText(R.string.item_content_editor_text_color_end)
        navigator.enterNewLine()

        navigator.clickOnEditorCommand(CommandName.INSERTUNORDEREDLIST)
        navigator.clickOnEditorCommand(CommandName.INSERTORDEREDLIST)

        navigator.enterText(R.string.item_content_editor_ordered_list)
        navigator.enterNewLine()
        navigator.enterText(R.string.item_content_editor_unordered_list)
        navigator.enterNewLine()
        navigator.enterText(R.string.item_content_editor_redo)
        navigator.enterNewLine()
        navigator.enterText(R.string.item_content_editor_undo)
        navigator.enterNewLine()

        TestUtil.sleep(500)
        navigator.clickOnEditorCommand(CommandName.INSERTORDEREDLIST)

        takeScreenShot(RichTextEditorScreenshotName)

        navigator.navigateFromEditItemActivityContentEditorToMainActivity()
        TestUtil.sleep(1000)
    }


    private fun createSearchScreenshot() {
        navigator.search(getString(R.string.search_items_query))
        navigator.hideKeyboard()
        TestUtil.sleep(1000)

        takeScreenShot(SearchScreenshotName)

        TestUtil.sleep(1000)
        navigator.pressBack()
        navigator.pressBack()
    }


    private fun createTagsListScreenshot() {
        val createdItems = updateTestDataForTagListScreenshot()

        TestUtil.sleep(1000)
        navigator.navigateToTabTags()

        takeScreenShot(TagsListScreenshotName)

        createdItems.forEach { deleteEntityService.deleteItem(it) }
        TestUtil.sleep(1000)
    }


    private fun createSyncDataScreenshot() {
        TestUtil.sleep(1000)

        val device = Device("name", "id", OsType.DESKTOP, getString(R.string.sync_data_os_name), getString(R.string.sync_data_os_version))
        dataManager.entityManager.persistEntity(device)
        val discoveredDevice = DiscoveredDevice(device, getString(R.string.sync_data_ip_address))
        (deviceRegistrationHandler as DeviceRegistrationHandlerBase).showUnknownDeviceDiscoveredView(discoveredDevice) { _, _ -> }
        TestUtil.sleep(1000)

        takeScreenShot(SyncDataScreenshotName)

        (deviceRegistrationHandler as DeviceRegistrationHandlerBase).unknownDeviceDisconnected(discoveredDevice)
        dataManager.entityManager.deleteEntity(device)
    }


    private fun createTestData() {
        val tagQuote = Tag(getString(R.string.tag_quote_name))
        persistTag(tagQuote)

        val tagGauchoMarx = Tag(getString(R.string.tag_gaucho_marx))
        persistTag(tagGauchoMarx)

        val tagSimpsons = Tag(getString(R.string.tag_simpsons))
        persistTag(tagSimpsons)

        val tagHLMencken = Tag(getString(R.string.tag_henry_louis_mencken))
        persistTag(tagHLMencken)

        val tagAshleighBrilliant = Tag(getString(R.string.tag_ashleight_brilliant))
        persistTag(tagAshleighBrilliant)

        val tagExampleNewspaperItem1 = Tag(getString(R.string.tag_example_newspaper_item_1))
        persistTag(tagExampleNewspaperItem1)

        val tagExampleNewspaperItem2 = Tag(getString(R.string.tag_example_newspaper_item_2))
        persistTag(tagExampleNewspaperItem2)

        val tagNonAlternativeFacts = Tag(getString(R.string.tag_non_alternative_facts))
        persistTag(tagNonAlternativeFacts)


        val seriesExampleNewspaper = Series(getString(R.string.series_example_newspaper_item))
        persistSeries(seriesExampleNewspaper)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy")
        val sourceExampleNewspaperArticle = Source(getString(R.string.source_example_newspaper_item_title),
                getString(R.string.source_example_newspaper_item_url),
                dateFormat.parse(getString(R.string.source_example_newspaper_item_publishing_date)))
        persistSource(sourceExampleNewspaperArticle, seriesExampleNewspaper)


        val itemGauchoMarxQuote = Item(getString(R.string.item_content_gaucho_marx_quote), tagGauchoMarx.name)
        persistItem(itemGauchoMarxQuote, null, tagQuote, tagGauchoMarx)

        val itemSimpsonsQuote = Item(getString(R.string.item_content_simpsons_quote), getString(R.string.item_title_simpsons_quote))
        persistItem(itemSimpsonsQuote, null, tagQuote, tagSimpsons)

        val itemAshleighBrilliantQuote = Item(getString(R.string.item_content_ashleigh_brilliant_quote), tagAshleighBrilliant.name)
        persistItem(itemAshleighBrilliantQuote, null, tagQuote, tagAshleighBrilliant)

        val itemFactInternetAccess = Item(getString(R.string.item_content_fact_internet_access), getString(R.string.tag_internet))
        persistItem(itemFactInternetAccess, null, tagNonAlternativeFacts)

        val itemMenckenQuote = Item(getString(R.string.item_content_mencken_quote), tagHLMencken.name)
        persistItem(itemMenckenQuote, null, tagQuote, tagHLMencken)

        val itemNewspaper = Item("", getString(R.string.item_abstract_example_newspaper_item))
        persistItem(itemNewspaper, sourceExampleNewspaperArticle, tagExampleNewspaperItem1, tagExampleNewspaperItem2)
    }

    private fun updateTestDataForTagListScreenshot(): Collection<Item> {
        deleteTagWithName(getString(R.string.tag_ashleight_brilliant))
        deleteTagWithName(getString(R.string.tag_henry_louis_mencken))

        val createdItems = ArrayList<Item>()

        addItemsToTag(getString(R.string.tag_non_alternative_facts), 4, createdItems)
        addItemsToTag(getString(R.string.tag_gaucho_marx), 1, createdItems)
        addItemsToTag(getString(R.string.tag_example_newspaper_item_1), 2, createdItems)

        return createdItems
    }

    private fun addItemsToTag(tagName: String, countItems: Int, createdItems: MutableCollection<Item>) {
        searchTagWithNameAndExecuteActionOnItSynchronized(tagName) { tag ->
            for(i in 0..countItems - 1) {
                val item = Item("")
                persistItem(item, null, tag)
                createdItems.add(item)
            }
        }
    }

    private fun deleteTagWithName(tagName: String) {
        searchTagWithNameAndExecuteActionOnItSynchronized(tagName) { tag ->
            deleteEntityService.deleteTag(tag)
        }
    }

    private fun searchTagWithNameAndExecuteActionOnItSynchronized(tagName: String, action: (Tag) -> Unit) {
        var tag: Tag? = null
        val waitLatch = CountDownLatch(1)

        searchEngine.searchTags(TagsSearch(tagName) { result ->
            tag = result.getRelevantMatchesSorted().first()
            waitLatch.countDown()
        })

        try { waitLatch.await() } catch (ignored: Exception) { }

        tag?.let{ action(it) }
    }


    private fun takeScreenShot(screenshotName: String) {
        try {
            Screengrab.screenshot(screenshotName)
        } catch (e: Exception) {
            log.error("Could not take screenshot for " + screenshotName, e)
        }

    }

}