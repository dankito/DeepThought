package net.dankito.deepthought.javafx.ui.controls

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding
import impl.org.controlsfx.autocompletion.SuggestionProvider
import impl.org.controlsfx.skin.AutoCompletePopup
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import net.dankito.deepthought.javafx.service.extensions.findClickedListCell
import org.controlsfx.control.textfield.AutoCompletionBinding
import org.slf4j.LoggerFactory
import tornadofx.*
import kotlin.reflect.KClass


class AutoCompletionBinding<T>(private val textField: TextField, private val suggestionProvider: SuggestionProvider<T> = SuggestionProvider.create(emptyList<T>()))
    : AutoCompletionTextFieldBinding<T>(textField, suggestionProvider) {

    companion object {
        private val log = LoggerFactory.getLogger(net.dankito.deepthought.javafx.ui.controls.AutoCompletionBinding::class.java)
    }


    var getContextMenuForItemListener: ((item: T) -> ContextMenu?)? = null

    var listCellFragment: KClass<out ListCellFragment<T>>? = null

    private var currentQueryToSelectFromAutoCompletionList: String? = null

    private var suggestionList: ListView<T>? = null

    private var autoCompletionPopup: AutoCompletePopup<T>? = null


    init {
        suggestionProvider.isShowAllIfEmpty = true

        textField.focusedProperty().addListener { _, _, newValue -> textFieldFocusedChanged(newValue) }

        initAutoCompletePopup()
    }

    private fun initAutoCompletePopup() {
        try {
            val autoCompletionPopupField = AutoCompletionBinding::class.java.getDeclaredField("autoCompletionPopup")
            autoCompletionPopupField.isAccessible = true

            this.autoCompletionPopup = (autoCompletionPopupField.get(this) as? AutoCompletePopup<T>)

            autoCompletionPopup?.let { autoCompletionPopup ->
                if(autoCompletionPopup.skin != null) {
                    setSuggestionList(autoCompletionPopup.skin.node as? ListView<T>)
                }
                else {
                    setSuggestionList(null)

                    autoCompletionPopup.skinProperty().addListener { _, _, newValue ->
                        setSuggestionList(newValue?.node as? ListView<T>)
                    }
                }

                autoCompletionPopup.consumeAutoHidingEvents = false
            }
        } catch(e: Exception) {
            log.error("Could not initialized autoCompletionPopup", e)
        }
    }


    override fun completeUserInput(completion: T) {
        // avoid calling super.completeUserInput(completion) as otherwise text field's text gets set to string representation, not to entity's title
    }


    fun setAutoCompleteList(autoCompletionList: Collection<T>, queryToSelectFromAutoCompletionList: String = completionTarget.text) {
        suggestionProvider.clearSuggestions()
        suggestionProvider.addPossibleSuggestions(autoCompletionList)

        currentQueryToSelectFromAutoCompletionList = queryToSelectFromAutoCompletionList

        if(textField.isFocused) { // only show autocompletion list if text field is focused
            setUserInput(queryToSelectFromAutoCompletionList)
        }
    }

    private fun textFieldFocusedChanged(isFocused: Boolean) {
        if(isFocused) {
            currentQueryToSelectFromAutoCompletionList?.let {
                setUserInput(it)
            }
        }
        else {
            hidePopup()
        }
    }


    private fun setSuggestionList(suggestionList: ListView<T>?) {
        this.suggestionList = suggestionList

        suggestionList?.let {
            listCellFragment?.let {
                suggestionList.cellFragment(fragment = it)
            }
        }

        suggestionList?.setOnContextMenuRequested { e ->
            val listCell = e.pickResult?.findClickedListCell<T>()
            listCell?.item?.let { clickedItem ->
                getContextMenuForItemListener?.invoke(clickedItem)?.let { contextMenu ->
                    contextMenu.show(listCell, e.screenX, e.screenY)
                }
            }
        }
    }


    fun updatePopupPosition() {
        if(autoCompletionPopup?.isShowing == true) {
            hidePopup()

            showPopup()
        }
    }

}