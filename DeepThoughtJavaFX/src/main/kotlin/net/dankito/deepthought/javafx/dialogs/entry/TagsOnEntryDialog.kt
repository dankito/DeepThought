package net.dankito.deepthought.javafx.dialogs.entry

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.TagsOnEntryListPresenter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.tags.TagsSearcherButtonState
import net.dankito.deepthought.ui.view.ITagsOnEntryListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IDialogService
import tornadofx.*
import javax.inject.Inject


class TagsOnEntryDialog : DialogFragment(), ITagsOnEntryListView {

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var searchResultsUtil: TagsSearchResultsUtil

    @Inject
    protected lateinit var dialogService: IDialogService


    val tagsOnEntry: ObservableSet<Tag> by param()

    private val presenter: TagsOnEntryListPresenter

    private val searchResults: ObservableList<Tag> = FXCollections.observableArrayList()


    private lateinit var txtfldSearchTags: TextField

    private lateinit var btnCreateOrToggleTags: Button

    private var btnCreateOrToggleTagsState: TagsSearcherButtonState = TagsSearcherButtonState.DISABLED


    init {
        AppComponent.component.inject(this)

        presenter = TagsOnEntryListPresenter(this, searchEngine, tagService, deleteEntityService, searchResultsUtil, dialogService)

        presenter.searchTags(Search.EmptySearchTerm)
    }


    override val root = vbox {

        prefWidth = 600.0
        prefHeight = 300.0

        hbox {
            prefHeight = 40.0
            useMaxWidth = true
            alignment = Pos.CENTER_LEFT

            txtfldSearchTags = textfield {
                hboxConstraints {
                    hgrow = Priority.ALWAYS
                    marginLeftRight(6.0)
                }

                promptText = messages["find.tags.prompt.text"]

                textProperty().addListener { _, _, newValue -> presenter.searchTags(newValue) }
                setOnKeyReleased { event ->
                    if(event.code == KeyCode.ENTER) {
                        createOrToggleTags()
                    }
                    else if(event.code == KeyCode.ESCAPE) {
                        clear()
                    }
                }
            }

            btnCreateOrToggleTags = button(messages["tags.on.item.dialog.create.tag"]) {
                action { createOrToggleTags() }
            }
        }

        listview(searchResults) {
            vgrow = Priority.ALWAYS

            onDoubleClick { toggleTagOnEntry(selectionModel.selectedItem) }

            contextmenu {
                item(messages["context.menu.tag.edit"]) {
                    isDisable = true
                    action {
                        selectedItem?.let { presenter.editTag(it) }
                    }
                }

                separator()

                item(messages["context.menu.tag.delete"]) {
                    action {
                        selectedItem?.let {
                            tagsOnEntry.remove(it)
                            presenter.deleteTagAsync(it)
                        }
                    }
                }
            }
        }

    }


    private fun setButtonState() {
        val buttonState = presenter.getButtonStateForSearchResult(tagsOnEntry)

        applyButtonState(buttonState)
    }

    private fun applyButtonState(state: TagsSearcherButtonState) {
        this.btnCreateOrToggleTagsState = state

        btnCreateOrToggleTags?.let { button ->
            button.isDisable = state == TagsSearcherButtonState.DISABLED

            if(state == TagsSearcherButtonState.CREATE_TAG) {
                button.text = messages["tags.on.item.dialog.create.tag"]
            }
            else if(state == TagsSearcherButtonState.ADD_TAGS) {
                button.text = messages["tags.on.item.dialog.add.tags"]
            }
            else if(state == TagsSearcherButtonState.REMOVE_TAGS) {
                button.text = messages["tags.on.item.dialog.remove.tags"]
            }
            else if(state == TagsSearcherButtonState.TOGGLE_TAGS) {
                button.text = messages["tags.on.item.dialog.toggle.tags"]
            }
        }
    }

    private fun createOrToggleTags() {
        if(btnCreateOrToggleTagsState == TagsSearcherButtonState.CREATE_TAG) {
            presenter.createNewTags(tagsOnEntry)
        }
        else {
            toggleTagsOnEntry()
        }
    }

    private fun toggleTagsOnEntry() {
        presenter.toggleTagsOnEntry(tagsOnEntry, btnCreateOrToggleTagsState)

        setButtonState()
    }

    private fun toggleTagOnEntry(tag: Tag?) {
        tag?.let { tag ->
            if(tagsOnEntry.contains(tag)) {
                tagsOnEntry.remove(tag)
            }
            else {
                tagsOnEntry.add(tag)
            }
        }
    }


    /*          ITagsListView implementation            */

    override fun showEntities(entities: List<Tag>) {
        FXUtils.runOnUiThread {
            searchResults.setAll(entities)
            setButtonState()
        }
    }

    override fun updateDisplayedTags() {
        // nothing to do in JavaFX
    }

    override fun shouldCreateNotExistingTags(notExistingTags: List<String>, tagsShouldGetCreatedCallback: (tagsOnEntry: MutableCollection<Tag>) -> Unit) {
        // TODO
    }

}