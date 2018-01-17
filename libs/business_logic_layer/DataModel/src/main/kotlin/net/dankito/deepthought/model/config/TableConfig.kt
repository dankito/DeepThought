package net.dankito.deepthought.model.config


class TableConfig {

    companion object {


        /*          BaseEntity Column Names        */

        const val BaseEntityIdColumnName = "id"
        const val BaseEntityCreatedOnColumnName = "created_on"
        const val BaseEntityModifiedOnColumnName = "modified_on"
        const val BaseEntityVersionColumnName = "version"
        const val BaseEntityDeletedColumnName = "deleted"


        /*          DeepThought Table Config        */

        const val DeepThoughtTableName = "deep_thought"

        const val DeepThoughtLocalUserJoinColumnName = "local_user_id"
        const val DeepThoughtLocalDeviceJoinColumnName = "local_device_id"
        const val DeepThoughtLocalSettingsJoinColumnName = "local_settings_id"
        const val DeepThoughtNextItemIndexColumnName = "next_item_index"


        /*          User Table Config        */

        const val UserTableName = "user_dt" // 'user' is not allowed as table name as it's a system table, so i used user_dt (for _deep_thought)

        const val UserUniversallyUniqueIdColumnName = "universally_unique_id"
        const val UserUserNameColumnName = "user_name"
        const val UserFirstNameColumnName = "first_name"
        const val UserLastNameColumnName = "last_name"
        const val UserPasswordColumnName = "password"


        /*          User SynchronizedDevices JoinTable Column Names        */

        const val UserSynchronizedDevicesJoinTableName = "user_synchronized_devices_join_table"

        const val UserSynchronizedDevicesUserIdColumnName = "user_id"
        const val UserSynchronizedDevicesDeviceIdColumnName = "device_id"


        /*          User IgnoredDevices JoinTable Column Names        */

        const val UserIgnoredDevicesJoinTableName = "user_ignored_devices_join_table"

        const val UserIgnoredDevicesUserIdColumnName = "user_id"
        const val UserIgnoredDevicesDeviceIdColumnName = "device_id"



        /*          Device Table Config        */

        const val DeviceTableName = "device"

        const val UniqueDeviceIdColumnName = "unique_device_id"
        const val DeviceNameColumnName = "name"
        const val DeviceDescriptionColumnName = "description"
        const val DeviceOsTypeColumnName = "os_type"
        const val DeviceOsNameColumnName = "os_name"
        const val DeviceOsVersionColumnName = "os_version"
        const val DeviceIconColumnName = "device_icon"



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

        const val FileLinkTableName = "file"

        const val FileLinkUriColumnName = "uri"
        const val FileLinkNameColumnName = "name"
        const val FileLinkIsLocalFileColumnName = "is_local_file"
        const val FileLinkFileTypeColumnName = "file_type"
        const val FileLinkFileSizeColumnName = "file_size"
        const val FileLinkFileLastModifiedColumnName = "file_last_modified"
        const val FileLinkFileHashSHA512ColumnName = "hash_sha_512"
        const val FileLinkDescriptionColumnName = "description"
        const val FileLinkSourceUriColumnName = "source_uri"


        /*          LocalFileInfo Table Config        */

        const val LocalFileInfoTableName = "local_file_info"

        const val LocalFileInfoFileLinkJoinColumnName = "file_link_id"
        const val LocalFileInfoPathColumnName = "path"
        const val LocalFileInfoIsDeviceThatHasOriginalColumnName = "is_device_that_has_original"
        const val LocalFileInfoSyncStatusColumnName = "sync_status"
        const val LocalFileInfoFileSizeColumnName = "file_size"
        const val LocalFileInfoFileLastModifiedColumnName = "file_last_modified"
        const val LocalFileInfoFileHashSHA512ColumnName = "hash_sha_512"


        /*          Note Table Config        */

        const val NoteTableName = "notes"

        const val NoteNoteColumnName = "notes"
        const val NoteNoteTypeJoinColumnName = "note_type_id"
        const val NoteItemJoinColumnName = "item_id"


        /*          ExtensibleEnumeration Table Config        */

        const val ExtensibleEnumerationNameColumnName = "name"
        const val ExtensibleEnumerationNameResourceKeyColumnName = "name_resource_key"
        const val ExtensibleEnumerationDescriptionColumnName = "description"
        const val ExtensibleEnumerationSortOrderColumnName = "sort_order"
        const val ExtensibleEnumerationIsSystemValueColumnName = "is_system_value"


        /*          NoteType Table Config        */

        const val NoteTypeTableName = "note_type"


        /*          FileType Table Config        */

        const val FileTypeTableName = "file_type"

        const val FileTypeFolderNameColumnName = "folder"


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