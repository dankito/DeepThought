package net.dankito.deepthought.model.extensions

import org.jsoup.Jsoup


fun String?.getPlainTextForHtml(): String {
    if(this != null) {
        return Jsoup.parseBodyFragment(this).text()
    }

    return ""
}