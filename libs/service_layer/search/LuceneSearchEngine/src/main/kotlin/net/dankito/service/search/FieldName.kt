package net.dankito.service.search


object FieldName {

    /*      Entries      */

    val EntryId = "entry_id"

    val EntryAbstract = "abstract"

    val EntryContent = "content"

    val EntryIndex = "entry_index"

    val EntryCreated = "created"
    val EntryModified = "modified"

    val EntryTags = "tags"
    val EntryTagsIds = "tags_ids"
    val EntryNoTags = "no_tags"

    val EntryCategories = "categories"
    val EntryNoCategories = "no_categories"

    val EntryPersons = "persons"
    val EntryNoPersons = "no_persons"

    val EntrySeries = "series"
    val EntryNoSeries = "no_series"

    val EntryReference = "reference"
    val EntryNoReference = "no_reference"

    val EntryReferenceSubDivision = "reference_sub_division"
    val EntryNoReferenceSubDivision = "no_reference_sub_division"

    val EntryNotes = "notes"
    val EntryNoNotes = "no_notes"


    /*      EntryTags      */

    val TagId = "tag_id"

    val TagName = "tag_name"


    /*      Categories      */

    val CategoryId = "category_id"

    val CategoryName = "category_name"
    val CategoryDescription = "category_description"

    val CategoryParentCategoryId = "parent_category_id"
    val CategoryParentCategoryName = "parent_category_name"


    /*      Persons      */

    val PersonId = "person_id"

    val PersonFirstName = "person_first_name"
    val PersonLastName = "person_last_name"


    /*      References      */

    val ReferenceBaseId = "reference_base_id"
    val ReferenceBaseType = "reference_base_type"

    val SeriesTitleTitle = "series_title_title"

    val ReferenceTitle = "reference_title"
    val ReferenceIssueOrPublishingDate = "reference_issue_or_publishing_date"
    val ReferencePublishingDate = "reference_publishing_date"

    val ReferenceSubDivisionTitle = "reference_sub_division_title"


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
