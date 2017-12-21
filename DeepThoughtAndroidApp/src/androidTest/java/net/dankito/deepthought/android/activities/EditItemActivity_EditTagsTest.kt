package net.dankito.deepthought.android.activities

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.pressImeActionButton
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import net.dankito.deepthought.android.DeepThoughtActivityTestRule
import net.dankito.deepthought.android.DeepThoughtAndroidTestBase
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityParameters
import net.dankito.deepthought.android.di.TestComponent
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.util.matchers.RecyclerViewInViewMatcher
import net.dankito.deepthought.android.util.matchers.RecyclerViewItemCountAssertion
import net.dankito.deepthought.android.util.screenshot.TakeScreenshotOnErrorTestRule
import net.dankito.deepthought.android.views.TagsPreviewViewHelper
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag
import net.dankito.service.search.SearchEngineBase
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
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
        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(UnPersistedTag1Name)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())

        navigator.enterText(UnPersistedTag2Name)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag2Name), *testItem.tags.toTypedArray())

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag2Name), *testItem.tags.toTypedArray())

        navigator.enterText(UnPersistedTag3Name)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag2Name), Tag(UnPersistedTag3Name), *testItem.tags.toTypedArray())

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag2Name), Tag(UnPersistedTag3Name), *testItem.tags.toTypedArray())

        assertThat(testItem.tags.size, `is`(3)) // Item is not saved yet, but displayed tags preview must get updated

        onView(withId(R.id.mnSaveEntry)).perform(click())
        assertThat(testItem.tags.size, `is`(6))
        testItem.tags.forEach { tag ->
            assertThat(tag.isPersisted(), `is`(true))
        }
    }


    @Test
    fun addPersistedAndNotPersistedTags_TagsGetSaved() {
        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(UnPersistedTag1Name)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
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
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag3Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(0)

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag3Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(0)

        navigator.enterText(PersistedTag3Name)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag3Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), Tag(UnPersistedTag3Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        assertThat(testItem.tags.size, `is`(3)) // Item is not saved yet, but displayed tags preview must get updated

        onView(withId(R.id.mnSaveEntry)).perform(click())
        assertThat(testItem.tags.size, `is`(5))
        testItem.tags.forEach { tag ->
            assertThat(tag.isPersisted(), `is`(true))
        }
    }


    @Test
    fun removedEnteredTag_RemovedTagIsAtBeginningOfSearchTerm() {
        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(PersistedTag1Name.toLowerCase())
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        navigator.enterText(UnPersistedTag1Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(0)

        navigator.enterText(UnPersistedTag2Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag2Name), Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(0)


        removeTagWithName(PersistedTag1Name)


        assertPersistedTag1GotRemoved()
    }


    @Test
    fun removedEnteredTag_RemovedTagIsInTheMiddleOfSearchTerm() {
        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(UnPersistedTag1Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(0)

        // Tag to remove soon
        navigator.enterText(PersistedTag1Name.toLowerCase())
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        navigator.enterText(UnPersistedTag2Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag2Name), Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(0)


        removeTagWithName(PersistedTag1Name)


        assertPersistedTag1GotRemoved()
    }

    @Test
    fun removedEnteredTag_RemovedTagIsAtEndOfSearchTerm() {
        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(UnPersistedTag1Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(0)

        navigator.enterText(UnPersistedTag2Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag2Name), Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(0)

        navigator.enterText(PersistedTag1Name.toLowerCase())
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)
        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag2Name), Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)


        removeTagWithName(PersistedTag1Name)


        assertPersistedTag1GotRemoved()
    }


    @Test
    fun enterTagNameStartAtBeginning_SelectFromResultList_TagGetsAutoCompleted() {
        persistTag(Tag(UnPersistedTag1Name))

        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(UnPersistedTag1Name.substring(0, 3)) // only enter first three letters of tag name
        checkCountItemsInRecyclerViewTagSearchResults(1)

        clickOnFirstDisplayedTagSearchResult()

        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        checkDisplayedTagsValue(UnPersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator)
    }

    @Test
    fun enterTagNameStartAtEnd_SelectFromResultList_TagGetsAutoCompleted() {
        persistTag(Tag(UnPersistedTag1Name))

        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(PersistedTag1Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)

        navigator.enterText(UnPersistedTag1Name.substring(0, 3)) // only enter first three letters of tag name
        checkCountItemsInRecyclerViewTagSearchResults(1)

        clickOnFirstDisplayedTagSearchResult()

        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        removeWhitespacesEnteredByKeyboardApp()
        checkDisplayedTagsValue(PersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator + UnPersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator)
    }

    @Test
    fun enterTagNameStartAtEndWithTagsSeparator_SelectFromResultList_TagGetsAutoCompleted() {
        persistTag(Tag(UnPersistedTag1Name))

        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(PersistedTag1Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)

        navigator.enterText(UnPersistedTag1Name.substring(0, 3)) // only enter first three letters of tag name
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator) // now additionally enter TagsSearchTermSeparator to confuse auto completion
        checkCountItemsInRecyclerViewTagSearchResults(1)

        clickOnFirstDisplayedTagSearchResult()

        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        removeWhitespacesEnteredByKeyboardApp()
        checkDisplayedTagsValue(PersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator + UnPersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator)
    }


    @Test
    fun enterTagNameStartAtBeginning_PressAction_TagGetsAutoCompleted() {
        persistTag(Tag(UnPersistedTag1Name))

        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(UnPersistedTag1Name.substring(0, 3)) // only enter first three letters of tag name
        checkCountItemsInRecyclerViewTagSearchResults(1)

        pressAction()

        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        checkDisplayedTagsValue(UnPersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator)
    }

    @Test
    fun enterTagNameStartAtEnd_PressAction_TagGetsAutoCompleted() {
        persistTag(Tag(UnPersistedTag1Name))

        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(PersistedTag1Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)

        navigator.enterText(UnPersistedTag1Name.substring(0, 3)) // only enter first three letters of tag name
        checkCountItemsInRecyclerViewTagSearchResults(1)

        pressAction()

        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        removeWhitespacesEnteredByKeyboardApp()
        checkDisplayedTagsValue(PersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator + UnPersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator)
    }

    @Test
    fun enterTagNameStartAtEndWithTagsSeparator_PressAction_TagGetsAutoCompleted() {
        persistTag(Tag(UnPersistedTag1Name))

        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(PersistedTag1Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)

        navigator.enterText(UnPersistedTag1Name.substring(0, 3)) // only enter first three letters of tag name
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator) // now additionally enter TagsSearchTermSeparator to confuse auto completion
        checkCountItemsInRecyclerViewTagSearchResults(1)

        pressAction()

        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        removeWhitespacesEnteredByKeyboardApp()
        checkDisplayedTagsValue(PersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator + UnPersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator)
    }


    @Test
    fun enterTagNameStartAtEndWithTagsSeparator_SelectFromResultList_RemoveAutoCompletedTag_OriginalEnteredTextGetsDisplayed() {
        persistTag(Tag(UnPersistedTag1Name))

        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(PersistedTag1Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)

        navigator.enterText(UnPersistedTag1Name.substring(0, 3)) // only enter first three letters of tag name
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator) // now additionally enter TagsSearchTermSeparator to confuse auto completion
        checkCountItemsInRecyclerViewTagSearchResults(1)


        clickOnFirstDisplayedTagSearchResult()

        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        removeWhitespacesEnteredByKeyboardApp()
        checkDisplayedTagsValue(PersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator + UnPersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator)


        clickOnFirstDisplayedTagSearchResult() // now remove tag again

        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        removeWhitespacesEnteredByKeyboardApp()
        checkDisplayedTagsValue(PersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator)
    }

    @Test
    fun enterTagNameStartAtEndWithTagsSeparator_PressAction_RemoveAutoCompletedTag_OriginalEnteredTextGetsDisplayed() {
        persistTag(Tag(UnPersistedTag1Name))

        assertThat(testItem.tags.size, `is`(3))
        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())

        onView(withId(R.id.lytTagsPreview)).perform(ViewActions.click())

        navigator.enterText(PersistedTag1Name)
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator)

        navigator.enterText(UnPersistedTag1Name.substring(0, 3)) // only enter first three letters of tag name
        navigator.enterText(SearchEngineBase.TagsSearchTermSeparator) // now additionally enter TagsSearchTermSeparator to confuse auto completion
        checkCountItemsInRecyclerViewTagSearchResults(1)


        pressAction()

        checkDisplayedPreviewTagsMatch(Tag(UnPersistedTag1Name), *testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        removeWhitespacesEnteredByKeyboardApp()
        checkDisplayedTagsValue(PersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator + UnPersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator)


        removeTagWithName(UnPersistedTag1Name) // now remove tag again

        checkDisplayedPreviewTagsMatch(*testItem.tags.toTypedArray())
        checkCountItemsInRecyclerViewTagSearchResults(1)

        removeWhitespacesEnteredByKeyboardApp()
        checkDisplayedTagsValue(PersistedTag1Name + SearchEngineBase.TagsSearchTermSeparator)
    }


    private fun assertPersistedTag1GotRemoved() {
        val newDisplayedTags = ArrayList(testItem.tags)
        newDisplayedTags.remove(persistedTag1)
        newDisplayedTags.add(Tag(UnPersistedTag1Name))
        newDisplayedTags.add(Tag(UnPersistedTag2Name))
        checkDisplayedPreviewTagsMatch(*newDisplayedTags.toTypedArray())

        // now check if PersistedTag1Name got removed from edtxtEntityFieldValue
        removeWhitespacesEnteredByKeyboardApp() // my keyboard app enters after each comma a white space
        onView(allOf(withId(R.id.edtxtEntityFieldValue), isDescendantOfA(withId(R.id.lytTagsPreview))))
                .check(matches(withText(`is`("$UnPersistedTag1Name${SearchEngineBase.TagsSearchTermSeparator}" +
                        "$UnPersistedTag2Name${SearchEngineBase.TagsSearchTermSeparator}"))))

        onView(withId(R.id.mnSaveEntry)).perform(click())
        assertThat(testItem.tags.size, `is`(newDisplayedTags.size))
        testItem.tags.forEach { tag ->
            assertThat(tag.isPersisted(), `is`(true))
        }
    }

    private fun clickOnFirstDisplayedTagSearchResult() {
        onView(RecyclerViewInViewMatcher.withRecyclerView(R.id.lytTagsPreview, R.id.rcySearchResults)
                .atPosition(0))
                .perform(click())
    }

    private fun pressAction() {
        onView(allOf(withId(R.id.edtxtEntityFieldValue), isDescendantOfA(withId(R.id.lytTagsPreview))))
                .perform(pressImeActionButton())
    }


    private fun removeTagWithName(tagNameToRemove: String) {
        enumerateDisplayedTagsPreviews().forEach { tagView ->
            (tagView.findViewById(R.id.txtTagName) as? TextView)?.text?.let { tagName ->
                if(tagName.toString() == tagNameToRemove) {
                    InstrumentationRegistry.getInstrumentation().runOnMainSync {
                        (tagView.findViewById(R.id.btnRemoveTagFromEntry) as? Button)?.performClick()
                    }
                    return@forEach
                }
            }
        }
    }

    private fun checkDisplayedPreviewTagsMatch(vararg tags: Tag) {
        val displayedTagNames = ArrayList<String>()

        enumerateDisplayedTagsPreviews().forEach { tagView ->
            (tagView.findViewById(R.id.txtTagName) as? TextView)?.text?.let { tagName -> displayedTagNames.add(tagName.toString()) }
        }

        tags.forEach { tag ->
            assertThat("Tag name ${tag.name} not found in displayed tag names $displayedTagNames", displayedTagNames.contains(tag.name), `is`(true))
        }

        displayedTagNames.removeAll(tags.map { it.name }) // assert that no other tags then in tags are displayed
        assertThat("Tags that are displayed but shouldn't: $displayedTagNames", displayedTagNames.size, `is`(0))
    }

    private fun enumerateDisplayedTagsPreviews(): List<View> {
        val tagPreviews = ArrayList<View>()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val lytTagsPreview = testRule.activity.findViewById(R.id.lytTagsPreview) as? ViewGroup
            val lytPreview = lytTagsPreview?.findViewById(R.id.lytPreview) as? ViewGroup

            lytPreview?.let {
                for(i in 0..lytPreview.childCount) {
                    val child = lytPreview.getChildAt(i)
                    if(child != null && child.tag == TagsPreviewViewHelper.TagViewTag) { // don't know why null is ever returned
                        tagPreviews.add(child)
                    }
                }
            }
        }

        return tagPreviews
    }

    private fun removeWhitespacesEnteredByKeyboardApp() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val lytTagsPreview = testRule.activity.findViewById(R.id.lytTagsPreview) as? ViewGroup
            val edtxtEntityFieldValue = lytTagsPreview?.findViewById(R.id.edtxtEntityFieldValue) as? EditText

            edtxtEntityFieldValue?.let {
                edtxtEntityFieldValue.setText(edtxtEntityFieldValue.text.toString().replace(", ", ","))
            }
        }
    }

    private fun checkCountItemsInRecyclerViewTagSearchResults(expectedCount: Int) {
        onView(allOf(withId(R.id.rcySearchResults), isDescendantOfA(withId(R.id.lytTagsPreview))))
                .check(RecyclerViewItemCountAssertion(expectedCount))
    }

    private fun checkDisplayedTagsValue(tags: Collection<Tag>) {
        checkDisplayedTagsValue(tags.sortedBy { it.name }.joinToString { it.name })
    }

    private fun checkDisplayedTagsValue(tagsDisplayName: String) {
        removeWhitespacesEnteredByKeyboardApp() // my keyboard app enters after each comma a white space

        checkDisplayedValueInEditEntityField(tagsDisplayName, R.id.lytTagsPreview)
    }

    private fun checkDisplayedValueInEditEntityField(valueToMatch: String, editEntityFieldId: Int) {
        onView(allOf(withId(R.id.edtxtEntityFieldValue), isDescendantOfA(withId(editEntityFieldId)))) // find edtxtEntityFieldValue in EditEntityField
                .check(matches(withText(`is`(valueToMatch))))
    }

}