package net.dankito.deepthought.model.extensions

import org.jsoup.Jsoup


fun String?.getPlainTextForHtml(): String {
    if(this != null) {
        val document = Jsoup.parseBodyFragment(this)

        document.select("figure").remove() // remove figure captions as they often don't print nicely in plain text

        val textWithTags = document.text()
        return Jsoup.parse(textWithTags).text() // don't know why but html has to be parsed twice in this way to remove tags like <img>, see https://github.com/jhy/jsoup/issues/125
    }

    return ""
}