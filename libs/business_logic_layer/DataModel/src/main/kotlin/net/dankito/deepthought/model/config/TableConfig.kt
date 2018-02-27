package net.dankito.deepthought.model.config


class TableConfig {

    companion object {


        /*          DeepThought Table Config        */

        const val DeepThoughtTableName = "deep_thought"

        const val DeepThoughtLocalUserJoinColumnName = "local_user_id"
        const val DeepThoughtLocalDeviceJoinColumnName = "local_device_id"
        const val DeepThoughtLocalSettingsJoinColumnName = "local_settings_id"
        const val DeepThoughtNextItemIndexColumnName = "next_item_index"



        /*          LocalSettings Table Config        */

        const val LocalSettingsTableName = "local_settings"

        const val LocalSettingsCommunicationProtocolVersionColumnName = "communication_protocol_version"
        const val LocalSettingsSearchIndexVersionColumnName = "search_index_version"
        const val LocalSettingsHtmlEditorVersionColumnName = "html_editor_version"
        const val LocalSettingsLastDatabaseOptimizationTimeColumnName = "last_database_optimization_time"
        const val LocalSettingsLastSearchIndexUpdateSequenceNumberColumnName = "last_search_index_update_sequence_number"
        const val LocalSettingsLastSearchIndexOptimizationTimeColumnName = "last_search_index_optimization_time"
        const val LocalSettingsDidUserCreateDataEntityColumnName = "did_user_create_data_entity"
        const val LocalSettingsDidShowListItemActionsHelpColumnName = "did_show_list_item_actions_help"
        const val LocalSettingsDidShowSearchTagsHelpColumnName = "did_show_search_tags_help"
        const val LocalSettingsCountTagSearchesColumnName = "count_tag_searches"
        const val LocalSettingsCountTagsOnItemSearchesColumnName = "count_tags_on_item_searches"
        const val LocalSettingsDidShowAddItemPropertiesHelpColumnName = "did_show_add_item_properties_help"
        const val LocalSettingsDidShowReaderViewHelpColumnName = "did_show_reader_view_help"
        const val LocalSettingsDidShowItemInformationFullscreenHelpColumnName = "item_information_fullscreen_help"
        const val LocalSettingsDidShowItemInformationFullscreenGesturesHelpColumnName = "item_information_fullscreen_gestures_help"
        const val LocalSettingsDidShowSavedReadLaterArticleIsNowInItemsHelpColumnName = "did_show_saved_read_later_article_is_now_in_items_help"


        /*          Item Table Config        */

        const val ItemTableName = "item"

        const val ItemSummaryColumnName = "summary"
        const val ItemContentColumnName = "content"
        const val ItemSourceJoinColumnName = "source_id"
        const val ItemIndicationColumnName = "indication"
        const val ItemPreviewColumnName = "preview"

        const val ItemItemIndexColumnName = "item_index"


        /*          Item Tag Join Table Config        */

        const val ItemTagJoinTableName = "item_tag_join_table"

        const val ItemTagJoinTableItemIdColumnName = "item_id"
        const val ItemTagJoinTableTagIdColumnName = "tag_id"


        /*          Item Attached Files Join Table Config        */

        const val ItemAttachedFilesJoinTableName = "item_attached_files_join_table"

        const val ItemAttachedFilesJoinTableItemIdColumnName = "item_id"
        const val ItemAttachedFilesJoinTableFileLinkIdColumnName = "file_id"


        /*          Tag Table Config        */

        const val TagTableName = "tag"

        const val TagNameColumnName = "name"
        const val TagDescriptionColumnName = "description"


        /*          Source Table Config        */

        const val SourceTableName = "source"

        const val SourceTitleColumnName = "title"
        const val SourceSubTitleColumnName = "sub_title"
        const val SourceAbstractColumnName = "abstract"
        const val SourceLengthColumnName = "length"
        const val SourceUrlColumnName = "url"
        const val SourceLastAccessDateColumnName = "last_access_date"
        const val SourceNotesColumnName = "notes"
        const val SourcePreviewImageUrlColumnName = "preview_image_url"
        const val SourcePreviewImageJoinColumnName = "preview_image_id"

        const val SourceSeriesJoinColumnName = "series_id"
        const val SourceTableOfContentsColumnName = "table_of_contents"
        const val SourceIssueColumnName = "issue"
        const val SourceIsbnOrIssnColumnName = "isbn_or_issn"
        const val SourcePublishingDateColumnName = "publishing_date"
        const val SourcePublishingDateStringColumnName = "publishing_date_string"


        /*          Series Table Config        */

        const val SeriesTableName = "series"

        const val SeriesTitleColumnName = "title"


        /*          Source Attached Files Join Table Config        */

        const val SourceAttachedFileJoinTableName = "source_attached_files_join_table"

        const val SourceAttachedFileJoinTableSourceBaseIdColumnName = "source_id"
        const val SourceAttachedFileJoinTableFileLinkIdColumnName = "file_id"


        /*          FileLink Table Config        */

        const val DeepThoughtFileLinkTableName = "dt_file"


        /*          Note Table Config        */

        const val NoteTableName = "notes"

        const val NoteNoteColumnName = "notes"
        const val NoteItemJoinColumnName = "item_id"


        /*          ArticleSummaryExtractorConfig Table Config        */

        const val ArticleSummaryExtractorConfigTableName = "article_summary_extractor_config"

        const val ArticleSummaryExtractorConfigUrlColumnName = "url"
        const val ArticleSummaryExtractorConfigNameColumnName = "name"
        const val ArticleSummaryExtractorConfigIconUrlColumnName = "icon_url"
        const val ArticleSummaryExtractorConfigSortOrderColumnName = "sortOrder"
        const val ArticleSummaryExtractorConfigSiteUrlColumnName = "site_url"
        const val ArticleSummaryExtractorConfigIsFavoriteColumnName = "is_favorite"
        const val ArticleSummaryExtractorConfigFavoriteIndexColumnName = "favorite_index"


        /*          ArticleSummaryExtractorConfig Tag Join Table Config        */

        const val ArticleSummaryExtractorConfigTagsToAddJoinTableName = "article_summary_extractor_config_tags_to_add_join_table"

        const val ArticleSummaryExtractorConfigTagsToAddJoinTableArticleSummaryExtractorConfigIdColumnName = "article_summary_extractor_config_id"
        const val ArticleSummaryExtractorConfigTagsToAddJoinTableTagIdColumnName = "tag_id"


        /*          ReadLaterArticle Table Config        */

        const val ReadLaterArticleTableName = "read_later_article"

        const val ReadLaterArticleItemPreviewColumnName = "item_preview"
        const val ReadLaterArticleSourcePreviewColumnName = "source_preview"
        const val ReadLaterArticleSourceUrlColumnName = "source_url"
        const val ReadLaterArticlePreviewImageUrlColumnName = "preview_image_url"
        const val ReadLaterArticleItemExtractionResultColumnName = "item_extraction_result"

    }
}