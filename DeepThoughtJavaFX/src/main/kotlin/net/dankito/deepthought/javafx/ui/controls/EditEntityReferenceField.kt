package net.dankito.deepthought.javafx.ui.controls

import com.sun.javafx.scene.traversal.Direction
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.Control
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import net.dankito.deepthought.javafx.res.icons.Icons
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.richtexteditor.java.fx.extensions.setImageTintColor
import tornadofx.*
import kotlin.reflect.KClass


abstract class EditEntityReferenceField<T>(entityLabel: String, entityPromptText: String, secondaryInformationLabel: String? = null,
                                           secondaryInformationPromptText: String? = null) : View() {

    companion object {
        internal const val LabelRightMargin = 4.0
    }

    var entityToEdit: T? = null
        protected set


    val showSecondaryInformation = SimpleBooleanProperty(false)

    var showEditEntityDetailsMenuItem = false


    val didEntityChange = SimpleBooleanProperty(false)

    val didTitleChange = SimpleBooleanProperty(false)

    val didSecondaryInformationChange = SimpleBooleanProperty(false)

    val enteredTitle: String
        get() = editedTitle.value

    val enteredSecondaryInformation: String
        get() = editedSecondaryInformation.value


    var originalEntity: T? = null
        private set

    private var originalSecondaryInformation: String? = null


    private val editedTitle = SimpleStringProperty("")

    private val editedSecondaryInformation = SimpleStringProperty("")

    protected val isEntityAdditionalSet = SimpleBooleanProperty()

    protected val entityAdditionalPreview = SimpleStringProperty()

    private val searchResults: ObservableList<T> = FXCollections.observableArrayList()


    private var txtfldTitle: AutoCompletionSearchTextField<T> by singleAssign()



    protected open fun getListCellFragment(): KClass<out ListCellFragment<T>>? = null

    abstract protected fun getEntityTitle(entity: T?): String?

    protected open fun getEntityAdditionalPreview(entity: T?): String? {
        return null
    }

    abstract fun createNewEntity(entityTitle: String): T

    abstract protected fun editEntity(entity: T)

    abstract protected fun deleteEntity(entity: T)

    abstract protected fun searchEntities(searchTerm: String)

    abstract protected fun showEditEntityDialog()



    override val root = vbox {
        hbox {
            prefHeight = getPrefFieldHeight()
            maxHeight = 70.0
            alignment = Pos.CENTER_LEFT
            prefWidthProperty().bind(this@vbox.widthProperty())

            label(entityLabel) {
                minWidth = Control.USE_PREF_SIZE
                prefWidth = getPrefLabelWidth()
                useMaxWidth = true

                hboxConstraints {
                    marginRight = LabelRightMargin
                }
            }

            label {
                minWidth = Control.USE_PREF_SIZE
                useMaxWidth = true

                textProperty().bind(entityAdditionalPreview)
                visibleProperty().bind(isEntityAdditionalSet)
                FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

                cursor = Cursor.HAND
                setOnMouseClicked { entityAdditionalPreviewClicked(it) }

                hboxConstraints {
                    marginRight = 6.0
                }
            }

            txtfldTitle = autocompletionsearchtextfield<T>(editedTitle) {
                hgrow = Priority.ALWAYS
                prefHeight = getPrefTextFieldHeight()

                promptText = entityPromptText

                textProperty().addListener { _, _, newValue -> enteredTitleUpdated(newValue) }
                focusedProperty().addListener { _, _, newValue ->
                    if(newValue) searchEntities(text)
                }

                onAutoCompletion = { entitySelected(it) }
                getContextMenuForItemListener = { item -> createContextMenuForItem(item) }
                listCellFragment = getListCellFragment()

                hboxConstraints {
                    marginRight = 4.0
                }
            }

            button {
                minHeight = getPrefButtonSize()
                maxHeight = minHeight
                minWidth = getPrefButtonSize()
                maxWidth = minWidth

                graphic = imageview(Icons.MoreVerticalIconPath) {
                    fitHeight = getPrefButtonSize()
                    isPreserveRatio = true

                    setImageTintColor(Color.GRAY)
                }

                setOnMouseClicked { buttonClicked(this, it) }
            }


            hbox {
                alignment = Pos.CENTER_LEFT
                visibleProperty().bind(showSecondaryInformation)
                FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

                label(secondaryInformationLabel ?: "") {
                    minWidth = Control.USE_PREF_SIZE
                    useMaxWidth = true

                    hboxConstraints {
                        marginLeft = 18.0
                        marginRight = 4.0
                    }
                }

                textfield(editedSecondaryInformation) {
                    promptText = secondaryInformationPromptText

                    prefWidth = 140.0

                    textProperty().addListener { _, _, newValue -> enteredSecondaryInformationUpdated(newValue) }
                }
            }
        }
    }

    private fun createContextMenuForItem(item: T): ContextMenu {
        val contextMenu = ContextMenu()

        if(showEditEntityDetailsMenuItem) {
            contextMenu.item(messages["action.edit"]) {
                action { editEntity(item) }
            }

            contextMenu.separator()
        }

        contextMenu.item(messages["action.delete"]) {
            action { deleteEntity(item) }
        }

        return contextMenu
    }

    protected open fun getPrefFieldHeight() = 20.0

    protected open fun getPrefLabelWidth() = Region.USE_COMPUTED_SIZE

    protected open fun getPrefTextFieldHeight() = Region.USE_COMPUTED_SIZE

    protected open fun getPrefButtonSize() = 26.0


    private fun enteredTitleUpdated(enteredTitle: String) {
        if(entityToEdit == null && enteredTitle.isNotBlank()) { // user entered a title, but source is null -> create a new Source
            setEntity(createNewEntity(enteredTitle))
        }

        didTitleChange.value = enteredTitle != getEntityTitle(originalEntity)

        searchEntities(enteredTitle)
    }

    private fun enteredSecondaryInformationUpdated(enteredSecondaryInformation: String) {
        didSecondaryInformationChange.value = enteredSecondaryInformation != originalSecondaryInformation
    }

    private fun buttonClicked(node: Node, event: MouseEvent) {
       if(event.button == MouseButton.PRIMARY && event.clickCount == 1) {
           showContextMenu(node)
        }
    }

    private fun showContextMenu(node: Node) {
        val contextMenu = ContextMenu()

        if(showEditEntityDetailsMenuItem) {
            contextMenu.item(messages["edit.entity.reference.field.edit.details.menu.item"]) {
                action { showEditEntityDialog() }
            }
        }

        contextMenu.item(messages["edit.entity.reference.field.create.new.entity.menu.item"]) {
            action { menuItemCreateNewEntitySelected() }
        }

        contextMenu.item(messages["edit.entity.reference.field.remove.entity.menu.item"]) {
            action { setEntity(null) }
        }

        val screenBounds = node.localToScreen(node.boundsInLocal)
        contextMenu.show(node, screenBounds.minX, screenBounds.maxY)
    }

    private fun menuItemCreateNewEntitySelected() {
        setEntity(createNewEntity(""))

        txtfldTitle.requestFocus()
    }


    fun setEntityToEdit(entity: T?, secondaryInformation: String? = null) {
        originalEntity = entity

        secondaryInformation?.let {
            originalSecondaryInformation = secondaryInformation
            editedSecondaryInformation.value = secondaryInformation
        }

        setEntity(entity)
    }

    private fun entitySelected(entity: T?) {
        setEntity(entity)

        txtfldTitle.impl_traverse(Direction.NEXT)
    }

    protected open fun setEntity(entity: T?) {
        entityToEdit = entity

        showEntityPreview(entity)

        didEntityChange.value = didEntityChange()
    }

    protected open fun didEntityChange() = entityToEdit != originalEntity

    protected open fun showEntityPreview(entity: T?) {
        this.editedTitle.value = getEntityTitle(entity) ?: ""
        txtfldTitle.positionCaret(txtfldTitle.text.length)

        this.entityAdditionalPreview.value = getEntityAdditionalPreview(entity) ?: ""
        this.isEntityAdditionalSet.value = this.entityAdditionalPreview.value.isNullOrBlank() == false
    }

    protected fun retrievedSearchResults(result: List<T>) {
        runLater { retrievedSearchResultsOnUiThread(result) }
    }

    protected fun retrievedSearchResultsOnUiThread(result: List<T>) {
        searchResults.setAll(result)

        txtfldTitle.setAutoCompleteList(result)

    }

    private fun entityAdditionalPreviewClicked(event: MouseEvent) {
        if(event.button == MouseButton.PRIMARY) {
            showEditEntityDialog()
        }
    }

}