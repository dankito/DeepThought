package net.dankito.newsreader.model

import java.util.*


data class Article(
        var url : String,
        var title : String,
        var content : String,
        var abstract : String? = null,
        var publishingDate : Date? = null,
        var previewImageUrl : String? = null) {

    private constructor() : this("", "", "") { // for Jackson

    }
}