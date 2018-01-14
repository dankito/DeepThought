package net.dankito.deepthought.ui.tags

import net.dankito.deepthought.model.Tag


data class TagAutoCompleteResult(val replacementIndex: Int, val enteredText: String, val autoCompletedText: String,
                                 val enteredTagName: String, val autoCompletedTagName: String,
                                 val enteredTagNameTrimmedWithoutTagsSeparator: String, val autoCompletedTagNameTrimmedWithoutTagsSeparator: String,
                                 val autoCompletedTag: Tag)