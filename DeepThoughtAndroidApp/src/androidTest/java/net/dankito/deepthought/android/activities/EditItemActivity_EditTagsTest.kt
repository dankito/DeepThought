package net.dankito.deepthought.android.activities

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.view.ViewGroup
import android.widget.TextView
import net.dankito.deepthought.android.DeepThoughtActivityTestRule
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityParameters
import net.dankito.deepthought.android.di.TestComponent
import net.dankito.deepthought.android.dialogs.TagsOnEntryDialogFragment
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.util.matchers.RecyclerViewItemCountAssertion
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag
import net.dankito.service.search.SearchEngineBase
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class EditItemActivity_EditTagsTest : DeepThoughtAndroidTestBase() {

    companion object {
        private val PersistedTag1Name = "Persisted Tag 1"
        private val PersistedTag2Name = "Persisted Tag 2"
        private val PersistedTag3Name = "Persisted Tag 3"

        private val UnPersistedTag1Name = "Love"
        private val UnPersistedTag2Name = "Cuddle"
        private val UnPersistedTag3Name = "Hug"
    }


    @Inject
    protected lateinit var parameterHolder: ActivityParameterHolder


    private val testItem = Item("Test Content")

    private val persistedTag1 = Tag(PersistedTag1Name)

    private val persistedTag2 = Tag(PersistedTag2Name)

    private val persistedTag3 = Tag(PersistedTag3Name)


    @get:Rule
    var takeScreenshotOnError = TakeScreenshotOnErrorTestRule()

    @get:Rule
    val testRule = DeepThoughtActivityTestRule<EditEntryActivity>(EditEntryActivity::class.java)


    init {
        TestComponent.component.inject(this)

        persistTag(persistedTag1)
        persistTag(persistedTag2)
        persistTag(persistedTag3)

        testItem.addTag(persistedTag1)
        testItem.addTag(persistedTag2)
        testItem.addTag(persistedTag3)

        testRule.setActivityParameter(parameterHolder, EditEntryActivityParameters(testItem))
    }


    @Test
    fun addNotPersistedTags_TagsGetSaved() {
        Assert.assertThat(testItem.tags.size, CoreMatchers.`is`(3))
        checkDisplayedTagsValue(testItem.tags)

        Espresso.onView(ViewMatchers.withId(R.id.lytTagsPreview)).perform(ViewActions.click())
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        navigator.enterText(UnPersistedTag1Name)
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())

        navigator.enterText(UnPersistedTag2Name)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag2Name), *testItem.tags.toTypedArray())

        navigator.enterText(UnPersistedTag3Name)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag2Name), *testItem.tags.toTypedArray())

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag2Name), Tag(UnPersistedTag3Name), *testItem.tags.toTypedArray())

        onView(withId(R.id.mnApplyTagsOnEntryChanges)).perform(click())

        assertThat(testItem.tags.size, CoreMatchers.`is`(3)) // Item is not saved yet, but displayed tags preview must get updated
        checkDisplayedTagsValue(listOf(persistedTag1, persistedTag2, persistedTag3, Tag(UnPersistedTag1Name), Tag(UnPersistedTag2Name), Tag(UnPersistedTag3Name)))
        testItem.tags.forEach { tag ->
            assertThat(tag.isPersisted(), `is`(true))
        }

        onView(withId(R.id.mnSaveEntry)).perform(click())
        assertThat(testItem.tags.size, CoreMatchers.`is`(6))
    }


    @Test
    fun addPersistedAndNotPersistedTags_TagsGetSaved() {
        Assert.assertThat(testItem.tags.size, CoreMatchers.`is`(3))
        checkDisplayedTagsValue(testItem.tags)

        Espresso.onView(ViewMatchers.withId(R.id.lytTagsPreview)).perform(ViewActions.click())
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(3)

        navigator.enterText(UnPersistedTag1Name)
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(0)

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(0)

        navigator.enterText(PersistedTag1Name)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray()) // PersistedTag1Name may not gets displayed twice
        checkCountItemsInRecyclerViewTagSearchResults(1)

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        navigator.enterText(UnPersistedTag3Name)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag3Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        navigator.enterText(PersistedTag3Name)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag3Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(2)

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag3Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(2)

        onView(withId(R.id.mnApplyTagsOnEntryChanges)).perform(click())

        assertThat(testItem.tags.size, CoreMatchers.`is`(3)) // Item is not saved yet, but displayed tags preview must get updated
        checkDisplayedTagsValue(listOf(persistedTag1, persistedTag2, persistedTag3, Tag(UnPersistedTag1Name), Tag(UnPersistedTag3Name)))
        testItem.tags.forEach { tag ->
            assertThat(tag.isPersisted(), `is`(true))
        }

        onView(withId(R.id.mnSaveEntry)).perform(click())
        assertThat(testItem.tags.size, CoreMatchers.`is`(5))
    }


    private fun checkDisplayedPreviewTagsMatch(vararg tags: Tag) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val tagsOnEntryDialogFragment = testRule.activity.supportFragmentManager.findFragmentByTag(TagsOnEntryDialogFragment.TAG)
            val lytTagsPreview = tagsOnEntryDialogFragment.view?.findViewById(R.id.lytTagsPreview) as? ViewGroup

            lytTagsPreview?.let {
                val displayedTagNames = ArrayList<String>()

                for(i in 0..lytTagsPreview.childCount) {
                    val tagView = lytTagsPreview.getChildAt(i)
                    (tagView?.findViewById(R.id.txtTagName) as? TextView)?.text?.let { tagName -> displayedTagNames.add(tagName.toString()) }
                }

                tags.forEach { tag ->
                    assertThat("Tag name ${tag.name} not found in displayed tag names $displayedTagNames", displayedTagNames.contains(tag.name), `is`(true))
                }

                displayedTagNames.removeAll(tags.map { it.name }) // assert that no other tags then in tags are displayed
                assertThat("Tags that are displayed but shouldn't: $displayedTagNames", displayedTagNames.size, `is`(0))
            }
        }
    }

    private fun checkCountItemsInRecyclerViewTagSearchResults(expectedCount: Int) {
        onView(allOf(withId(R.id.rcyTags)))
                .check(RecyclerViewItemCountAssertion(expectedCount))
    }

    private fun checkDisplayedTagsValue(tags: Collection<Tag>) {
        checkDisplayedValueInEditEntityField(tags.sortedBy { it.name }.joinToString { it.name }, R.id.lytTagsPreview)
    }

    private fun checkDisplayedValueInEditEntityField(valueToMatch: String, editEntityFieldId: Int) {
        onView(allOf(withId(R.id.edtxtEntityFieldValue), isDescendantOfA(withId(editEntityFieldId)))) // find edtxtEntityFieldValue in EditEntityField
                .check(matches(withText(`is`(valueToMatch))))
    }

}