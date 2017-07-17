package net.dankito.deepthought.ui.tags


enum class TagSearchResultState {

    EXACT_OR_SINGLE_MATCH_BUT_NOT_OF_LAST_RESULT,
    MATCH_BUT_NOT_OF_LAST_RESULT,
    EXACT_MATCH_OF_LAST_RESULT,
    SINGLE_MATCH_OF_LAST_RESULT,
    DEFAULT

}