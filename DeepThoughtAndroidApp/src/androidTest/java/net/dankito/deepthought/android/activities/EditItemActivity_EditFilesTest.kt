package net.dankito.deepthought.android.activities

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import net.dankito.deepthought.android.DeepThoughtActivityTestRule
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditItemActivityParameters
import net.dankito.deepthought.android.adapter.ListRecyclerSwipeAdapter
import net.dankito.deepthought.android.adapter.viewholder.FileLinkViewHolder
import net.dankito.deepthought.android.di.TestComponent
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.util.matchers.RecyclerViewItemCountAssertion
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import net.dankito.deepthought.android.views.EditEntityFilesField
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Item
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import javax.inject.Inject
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

@RunWith(AndroidJUnit4::class)
class EditItemActivity_EditFilesTest : DeepThoughtAndroidTestBase() {

    companion object {
        private val PersistedFile1Name = "Persisted File 1"
        private val PersistedFile1Path = "~/pictures/Persisted.png"

        private val UnPersistedFile1Name = "Love"
        private val UnPersistedFile1LocalPath = "~/pictures/Love.png"
        private val UnPersistedFile2Name = "Cuddle"
        private val UnPersistedFile2RemotePath = "https://www.cuddle.net/pictures/cuddle.png"

        private val log = LoggerFactory.getLogger(EditItemActivity_EditFilesTest::class.java)
    }


    @Inject
    protected lateinit var parameterHolder: ActivityParameterHolder


    private val testItem = Item("Test Content")

    private val persistedFile1 = FileLink(PersistedFile1Name, PersistedFile1Path, true)


    @get:Rule
    var takeScreenshotOnError = TakeScreenshotOnErrorTestRule()

    @get:Rule
    val testRule = DeepThoughtActivityTestRule<EditItemActivityBase>(EditItemActivityBase::class.java)


    init {
        TestComponent.component.inject(this)

        persistFile(persistedFile1)

        testItem.addAttachedFile(persistedFile1)

        testRule.setActivityParameter(parameterHolder, EditItemActivityParameters(testItem))
    }


    @Test
    fun addNotPersistedFiles_FilesGetSaved() {
        assertThat(testItem.attachedFiles.size, `is`(1))
        checkDisplayedPreviewFilesMatch(*testItem.attachedFiles.toTypedArray())


        val unpersistedFile1 = FileLink(UnPersistedFile1LocalPath, UnPersistedFile1Name, true)

        addFileToItem(unpersistedFile1)

        checkDisplayedPreviewFilesMatch(unpersistedFile1, *testItem.attachedFiles.toTypedArray())


        val unpersistedFile2 = FileLink(UnPersistedFile2RemotePath, UnPersistedFile2Name, false)

        addFileToItem(unpersistedFile2)

        checkDisplayedPreviewFilesMatch(unpersistedFile2, unpersistedFile1, *testItem.attachedFiles.toTypedArray())


        assertThat(testItem.attachedFiles.size, `is`(1)) // Item is not saved yet, but displayed tags preview must get updated

        onView(withId(R.id.mnSaveItem)).perform(click())
        assertThat(testItem.attachedFiles.size, `is`(3))
        testItem.attachedFiles.forEach { file ->
            assertThat(file.isPersisted(), `is`(true))
        }
    }


    private fun addFileToItem(file: FileLink) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            try {
                (testRule.activity.findViewById(R.id.lytFilesPreview) as? EditEntityFilesField)?.let { editFilesField ->
                    editFilesField.javaClass.kotlin.declaredMemberFunctions
                            .filter { it.name == "addFile" }.firstOrNull()?.let { addFileMethod ->
                        addFileMethod.isAccessible = true
                        addFileMethod.call(editFilesField, file)
                    }
                }
            } catch(e: Exception) { log.error("Could not add file ($file) to EditItemActivityBase (${testRule.activity})", e) }
        }
    }

    private fun checkDisplayedPreviewFilesMatch(vararg files: FileLink) {
        checkCountItemsInRecyclerViewFiles(files.size)

        val displayedFiles = getDisplayedFiles()
        assertThat(displayedFiles.size, `is`(files.size))

        displayedFiles.removeAll(files) // check that displayedFiles contains exactly files
        assertThat(displayedFiles.size, `is`(0))
    }

    private fun getDisplayedFiles(): MutableList<FileLink> {
        val displayedFiles = ArrayList<FileLink>()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val lytFilesPreview = testRule.activity.findViewById(R.id.lytFilesPreview) as? ViewGroup
            val rcySearchResults = lytFilesPreview?.findViewById(R.id.rcySearchResults) as? RecyclerView

            (rcySearchResults?.adapter as? ListRecyclerSwipeAdapter<FileLink, FileLinkViewHolder>)?.let {
                displayedFiles.addAll(it.items)
            }
        }

        return displayedFiles
    }

    private fun checkCountItemsInRecyclerViewFiles(expectedCount: Int) {
        onView(allOf(withId(R.id.rcySearchResults), isDescendantOfA(withId(R.id.lytFilesPreview))))
                .check(RecyclerViewItemCountAssertion(expectedCount))
    }

}