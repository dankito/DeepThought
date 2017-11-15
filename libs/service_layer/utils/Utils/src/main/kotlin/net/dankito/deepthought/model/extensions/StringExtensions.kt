package net.dankito.deepthought.model.extensions

import org.jsoup.Jsoup


fun String?.getPlainTextForHtml(): String {
    if(this != null) {
        val textWithTags = Jsoup.parseBodyFragment(this).text()
        return Jsoup.parse(textWithTags).text() // don't know why but html has to be parsed twice in this way to remove tags like <img>, see https://github.com/jhy/jsoup/issues/125
    }

    return ""
}