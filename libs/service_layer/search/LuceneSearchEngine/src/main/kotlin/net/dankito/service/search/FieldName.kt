package net.dankito.service.search


object FieldName {

    /*      Common      */

    val ModifiedOn = "modified_on"


    /*      Items      */

    val ItemIdsId = "item_ids_id"


    val ItemId = "item_id"

    val ItemSummary = "item_summary"
    val ItemSummaryForSorting = "item_summary_for_sorting"

    val ItemContent = "item_content"

    val ItemIndex = "item_index"

    val ItemCreated = "created"

    val ItemTagsIds = "item_tags_ids"
    val ItemTagsNames = "item_tags_names"
    val ItemNoTags = "item_no_tags"

    val ItemSeries = "item_series"

    val ItemSource = "item_source_title"
    val ItemSourcePublishingDate = "item_source_publishing_date"
    val ItemSourcePublishingDateString = "item_source_publishing_date_string"
    val ItemSourceId = "item_source_id"
    val ItemSourceSeriesId = "item_source_series_id"
    val ItemNoSource = "item_no_source"

    val ItemAttachedFilesIds = "item_attached_files_ids"
    val ItemAttachedFilesDetails = "item_attached_files_details"
    val ItemNoAttachedFiles = "item_no_attached_files"

    val ItemNotes = "item_notes"
    val ItemNoNotes = "item_no_notes"

    val ItemPreviewForSorting = "item_preview_for_sorting"
    val ItemSourcePreviewForSorting = "item_source_preview_for_sorting" // artificial key for ItemSeries, ItemSource, ItemSourcePublishingDate and ItemSourcePublishingDateString


    /*      Tags      */

    val TagId = "tag_id"

    val TagName = "tag_name"


    /*      Sources      */

    val SourceId = "source_id"

    val SourceTitle = "source_title"
    val SourceSubTitle = "source_subtitle"
    val SourceSeries = "source_series"
    val SourceSeriesId = "source_series_id"
    val SourceIssue = "source_issue"
    val SourcePublishingDate = "source_publishing_date"
    val SourcePublishingDateString = "source_publishing_date_string"

    val SourceAttachedFilesIds = "source_attached_files_ids"
    val SourceAttachedFilesDetails = "source_attached_files_details"
    val SourceNoAttachedFiles = "source_no_attached_files"


    /*      Series      */

    val SeriesId = "series_id"

    val SeriesTitle = "series_title"


    /*      ReadLaterArticles      */

    val ReadLaterArticleId = "read_later_article_id"

    val ReadLaterArticleItem = "read_later_article_item"
    val ReadLaterArticleSource = "read_later_article_source"


    /*      Files      */

    val FileId = "file_id"

    val FileName = "file_name"

    val FileUri = "file_uri"

    val FileIsLocalFile = "file_is_local_file"

    val FileMimeType = "file_mime_type"

    val FileFileType = "file_type"

    val FileFileSize = "file_size"

    val FileFileLastModified = "file_last_modified"

    val FileDescription = "file_description"

    val FileSourceUri = "file_source_uri"


    /*      LocalFileInfo      */

    val LocalFileInfoId = "local_file_info_id"

    val LocalFileInfoFile = "local_file_info_file_id"

    val LocalFileInfoSyncStatus = "local_file_info_title_sync_status"


    /*      Notes      */

    val NoteId = "note_id"

    val NoteNote = "note_note"


}
