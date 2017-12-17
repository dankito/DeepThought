package net.dankito.deepthought.android.views


data class TagAutoCompleteResult(val enteredText: String, val autoCompletedText: String,
                                 val enteredTagName: String, val autoCompletedTagName: String, val enteredTagNameWithoutTagsSeparator: String)