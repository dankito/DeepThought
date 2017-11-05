package net.dankito.service.search


object FieldName {

    /*      Common      */

    val ModifiedOn = "modified_on"


    /*      Entries      */

    val EntryIdsId = "item_ids_id"

    val EntryIdsCreated = "created"


    val EntryId = "item_id"

    val EntryAbstract = "summary"

    val EntryContent = "content"

    val EntryIndex = "item_index"

    val EntryCreated = "created"

    val EntryTagsIds = "tags_ids"
    val EntryTagsNames = "tags_names"
    val EntryNoTags = "no_tags"

    val EntryReference = "source"
    val EntryReferenceId = "source_id"
    val EntryReferenceSeriesId = "source_series_id"
    val EntryNoReference = "no_source"

    val EntryNotes = "notes"
    val EntryNoNotes = "no_notes"


    /*      Tags      */

    val TagId = "tag_id"

    val TagName = "tag_name"


    /*      References      */

    val ReferenceId = "source_id"

    val ReferenceTitle = "source_title"
    val ReferenceSubTitle = "source_subtitle"
    val ReferenceSeries = "source_series"
    val ReferenceSeriesId = "source_series_id"
    val ReferenceIssue = "source_issue"
    val ReferencePublishingDate = "source_publishing_date"
    val ReferencePublishingDateString = "source_publishing_date_string"


    /*      Series      */

    val SeriesId = "series_id"

    val SeriesTitle = "series_title"


    /*      ReadLaterArticles      */

    val ReadLaterArticleId = "read_later_article_id"

    val ReadLaterArticleEntry = "read_later_article_item"
    val ReadLaterArticleReference = "read_later_article_source"


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

    val FileIsAttachedToEntries = "file_attached_to_item"

    val FileIsEmbeddedInEntries = "file_embedded_in_item"

    val FileIsAttachedToReferenceBase = "file_attached_to_source"

    val FileIsEmbeddedInReferenceBase = "file_embedded_in_source"


}
