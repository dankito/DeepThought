package net.dankito.deepthought.model.extensions

import net.dankito.deepthought.model.Entry
import org.jsoup.Jsoup


private const val MaxPreviewLength = 400


val Entry.abstractPlainText: String
    get() {
        return getPlainTextForHtml(this.abstractString)
    }


val Entry.contentPlainText: String
    get() {
        return getPlainTextForHtml(this.content)
    }


val Entry.entryPreview: String
    get() {
        var preview = this.abstractPlainText

        if(preview.length < MaxPreviewLength) {
            if(preview.isNotEmpty()) {
                preview += " "
            }

            preview += this.contentPlainText
        }

        if(preview.length > MaxPreviewLength) {
            preview = preview.substring(0, MaxPreviewLength) + "..."
        }

        return preview
    }


val Entry.referencePreview: String
    get() {
        this.reference?.let { reference ->
            var preview = reference.title

            if(reference.subTitle.isNullOrBlank() == false) {
                preview = reference.subTitle + ": " + preview
            }

            return preview
        }

        return ""
    }


val Entry.tagsPreview: String
    get() {
        return this.tags.sortedBy { it.name.toLowerCase() }.joinToString { it.name }
    }


private fun getPlainTextForHtml(htmlString: String): String {
    if(htmlString != null) { // to be on the safe side, happened sometimes
        return Jsoup.parseBodyFragment(htmlString).text()
    }

    return ""
}