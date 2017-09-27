package net.dankito.service.search


object FieldName {

    /*      Common      */

    val ModifiedOn = "modified_on"


    /*      Entries      */

    val EntryId = "entry_id"

    val EntryAbstract = "abstract"

    val EntryContent = "content"

    val EntryIndex = "entry_index"

    val EntryCreated = "created"

    val EntryTagsIds = "tags_ids"
    val EntryTagsNames = "tags_names"
    val EntryNoTags = "no_tags"

    val EntryReference = "reference"
    val EntryReferenceId = "reference_id"
    val EntryNoReference = "no_reference"

    val EntryNotes = "notes"
    val EntryNoNotes = "no_notes"


    /*      Tags      */

    val TagId = "tag_id"

    val TagName = "tag_name"


    /*      References      */

    val ReferenceId = "reference_id"

    val ReferenceTitle = "reference_title"
    val ReferenceSubTitle = "reference_subtitle"
    val ReferenceSeries = "reference_series"
    val ReferenceIssue = "reference_issue"
    val ReferencePublishingDate = "reference_publishing_date"
    val ReferencePublishingDateString = "reference_publishing_date_string"


    /*      Series      */

    val SeriesId = "series_id"

    val SeriesTitle = "series_title"


    /*      ReadLaterArticles      */

    val ReadLaterArticleId = "read_later_article_id"

    val ReadLaterArticleEntry = "read_later_article_entry"
    val ReadLaterArticleReference = "read_later_article_reference"


    /*      Notes      */

    val NoteId = "note_id"

    val NoteNote = "note_note"


    /*      Files      */

    val FileId = "file_id"

    val FileName = "file_name"

    val FileUri = "file_uri"

    val FileSourceUri = "file_source_uri"

    val FileFileType = "file_type"

    val FileIsEmbeddableInHtml = "file_embeddable_in_html"

    val FileDescription = "file_description"

    val FileIsAttachedToEntries = "file_attached_to_entries"

    val FileIsEmbeddedInEntries = "file_embedded_in_entries"

    val FileIsAttachedToReferenceBase = "file_attached_in_reference_bases"

    val FileIsEmbeddedInReferenceBase = "file_embedded_to_reference_bases"


}
