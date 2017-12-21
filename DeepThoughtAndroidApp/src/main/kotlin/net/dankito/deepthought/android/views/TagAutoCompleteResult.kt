package net.dankito.deepthought.android.views


data class TagAutoCompleteResult(val replacementIndex: Int, val enteredText: String, val autoCompletedText: String,
                                 val enteredTagName: String, val autoCompletedTagName: String,
                                 val enteredTagNameTrimmedWithoutTagsSeparator: String, val autoCompletedTagNameTrimmedWithoutTagsSeparator: String)