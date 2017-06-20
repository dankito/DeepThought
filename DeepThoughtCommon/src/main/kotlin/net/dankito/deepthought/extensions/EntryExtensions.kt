package net.dankito.deepthought.extensions

import net.dankito.deepthought.model.Entry
import org.jsoup.Jsoup
import java.text.DateFormat


private val PublishingDateFormat = DateFormat.getDateInstance(DateFormat.SHORT)


val Entry.abstractPlainText: String
    get() {
        println("Getting plain text for abstract for $this")
        return getPlainTextForHtml(this.abstractString)
    }

val Entry.contentPlainText: String
    get() {
        println("Getting plain text for content for $this")
        return getPlainTextForHtml(this.content)
    }

val Entry.entryPreview: String
    get() {
        println("Getting entry preview for $this")
        var preview = this.abstractPlainText

        if (preview.length < 200) {
            if (preview.length > 0) {
                preview += " "
            }

            preview += this.contentPlainText
        }

        return preview
    }

val Entry.referencePreview: String
    get() {
        println("Getting reference preview for $this")
        reference?.let { reference ->
            var preview = reference.title

            var publisherAndDate = reference.series ?: ""

            reference.publishingDate?.let { publisherAndDate += " " + PublishingDateFormat.format(it) }

            if(publisherAndDate.isNullOrBlank() == false) {
                preview = publisherAndDate.trim() + " - " + preview
            }

            return preview
        }

        return ""
    }

private fun getPlainTextForHtml(htmlString: String): String {
    return Jsoup.parseBodyFragment(htmlString).text()
}