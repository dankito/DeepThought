package net.dankito.deepthought.javafx.ui.controls

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding
import impl.org.controlsfx.autocompletion.SuggestionProvider
import javafx.scene.control.TextField


class AutoCompletionBinding<T>(private val textField: TextField, private val suggestionProvider: SuggestionProvider<T> = SuggestionProvider.create(emptyList<T>()))
    : AutoCompletionTextFieldBinding<T>(textField, suggestionProvider) {


    private var currentQueryToSelectFromAutoCompletionList: String? = null


    init {
        suggestionProvider.isShowAllIfEmpty = true

        textField.focusedProperty().addListener { _, _, newValue -> textFieldFocusedChanged(newValue) }
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

}