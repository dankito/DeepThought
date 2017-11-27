package net.dankito.deepthought.android.play_store

import net.dankito.data_access.network.communication.callback.DeviceRegistrationHandlerBase
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.deepthought.android.DeepThoughtActivityTestRule
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.MainActivity
import net.dankito.deepthought.android.di.TestComponent
import net.dankito.deepthought.android.util.TestUtil
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.enums.OsType
import net.dankito.deepthought.preview.test.R
import net.dankito.richtexteditor.android.command.Command
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.slf4j.LoggerFactory
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.util.*
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
        TestUtil.sleep(1000L)
        takeScreenShot(ItemsListScreenshotName)


        createRichTextEditorScreenshot()

        createSearchScreenshot()

        createTagsListScreenshot()

        createSyncDataScreenshot()

        Thread.sleep(5 * 1000L)
    }

    private fun createRichTextEditorScreenshot() {
        navigator.navigateFromMainActivityToEditItemActivityContentEditor()
        TestUtil.sleep(500L)

        navigator.hideKeyboard()

        navigator.enterText(R.string.item_content_editor_introduction_line)

        navigator.enterNewLine()
        navigator.clickOnEditorCommand(Command.INSERTUNORDEREDLIST)

        navigator.clickOnEditorCommand(Command.BOLD)
        navigator.enterText(R.string.item_content_editor_bold)
        navigator.clickOnEditorCommand(Command.BOLD)
        navigator.enterNewLine()

        navigator.clickOnEditorCommand(Command.ITALIC)
        navigator.enterText(R.string.item_content_editor_italic)
        navigator.clickOnEditorCommand(Command.ITALIC)
        navigator.enterNewLine()

        navigator.clickOnEditorCommand(Command.UNDERLINE)
        navigator.enterText(R.string.item_content_editor_underline)
        navigator.clickOnEditorCommand(Command.UNDERLINE)
        navigator.enterNewLine()

        navigator.enterText(R.string.item_content_editor_text_color_introduction)
        navigator.clickOnEditorCommand(Command.BACKCOLOR)
        navigator.enterText(R.string.item_content_editor_text_background_color)
        navigator.clickOnEditorCommand(Command.BACKCOLOR)
        navigator.enterText(R.string.item_content_editor_text_color_end)
        navigator.enterNewLine()

        navigator.clickOnEditorCommand(Command.INSERTUNORDEREDLIST)
        navigator.clickOnEditorCommand(Command.INSERTORDEREDLIST)

        navigator.enterText(R.string.item_content_editor_ordered_list)
        navigator.enterNewLine()
        navigator.enterText(R.string.item_content_editor_unordered_list)
        navigator.enterNewLine()
        navigator.enterText(R.string.item_content_editor_redo)
        navigator.enterNewLine()
        navigator.enterText(R.string.item_content_editor_undo)
        navigator.enterNewLine()

        TestUtil.sleep(500)
        navigator.clickOnEditorCommand(Command.INSERTORDEREDLIST)

        takeScreenShot(RichTextEditorScreenshotName)
    }


    private fun createSearchScreenshot() {
        navigator.navigateFromEditItemActivityContentEditorToMainActivity()
        TestUtil.sleep(1000L)

        navigator.search(getString(net.dankito.deepthought.preview.test.R.string.search_items_query))
        navigator.hideKeyboard()
        TestUtil.sleep(1000L)

        takeScreenShot(SearchScreenshotName)

        TestUtil.sleep(1000L)
        navigator.pressBack()
        navigator.pressBack()
    }


    private fun createTagsListScreenshot() {
        TestUtil.sleep(1000L)
        navigator.navigateToTabTags()

        takeScreenShot(TagsListScreenshotName)
    }


    private fun createSyncDataScreenshot() {
        TestUtil.sleep(1000L)
        navigator.navigateToTabItems()

        val device = Device("name", "id", OsType.DESKTOP, getString(R.string.sync_data_os_name), getString(R.string.sync_data_os_version))
        val discoveredDevice = DiscoveredDevice(device, getString(R.string.sync_data_ip_address))
        (deviceRegistrationHandler as DeviceRegistrationHandlerBase).showUnknownDeviceDiscoveredView(discoveredDevice) { _, _ -> }
        TestUtil.sleep(1000L)

        takeScreenShot(SyncDataScreenshotName)
    }


    private fun createTestData() {
        val tagQuote = Tag(getString(R.string.tag_quote_name))
        persistTag(tagQuote)

        val tagHLMencken = Tag(getString(R.string.tag_quote_h_l_mencken))
        persistTag(tagHLMencken)

        val tagAshleighBrilliant = Tag(getString(R.string.tag_quote_ashleight_brilliant))
        persistTag(tagAshleighBrilliant)

        val tagExampleNewspaperItem1 = Tag(getString(R.string.tag_example_newspaper_item_1))
        persistTag(tagExampleNewspaperItem1)

        val tagExampleNewspaperItem2 = Tag(getString(R.string.tag_example_newspaper_item_2))
        persistTag(tagExampleNewspaperItem2)


        val seriesExampleNewspaper = Series(getString(R.string.series_example_newspaper_item))
        persistSeries(seriesExampleNewspaper)

        val sourceExampleNewspaperArticle = Source(getString(R.string.source_example_newspaper_item_title), "", Date(2017, 10, 26), series = seriesExampleNewspaper)
        persistSource(sourceExampleNewspaperArticle)


        val itemAshleighBrilliantQuote = Item(getString(R.string.item_content_ashleigh_brilliant_quote))
        itemAshleighBrilliantQuote.addTag(tagQuote)
        itemAshleighBrilliantQuote.addTag(tagAshleighBrilliant)
        persistItem(itemAshleighBrilliantQuote)

        val itemMenckenQuote = Item(getString(R.string.item_content_mencken_quote))
        itemMenckenQuote.addTag(tagQuote)
        itemMenckenQuote.addTag(tagHLMencken)
        persistItem(itemMenckenQuote)

        val itemNewspaper = Item("", getString(R.string.item_abstract_example_newspaper_item))
        itemNewspaper.addTag(tagExampleNewspaperItem1)
        itemNewspaper.addTag(tagExampleNewspaperItem2)
        itemNewspaper.source = sourceExampleNewspaperArticle
        persistItem(itemNewspaper)
    }


    private fun takeScreenShot(screenshotName: String) {
        try {
            Screengrab.screenshot(screenshotName)
        } catch (e: Exception) {
            log.error("Could not take screenshot for " + screenshotName, e)
        }

    }

}