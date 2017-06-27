package net.dankito.deepthought.model.config


class TableConfig {

    companion object {


        /*          BaseEntity Column Names        */

        const val BaseEntityIdColumnName = "id"
        const val BaseEntityCreatedOnColumnName = "created_on"
        const val BaseEntityModifiedOnColumnName = "modified_on"
        const val BaseEntityVersionColumnName = "version"
        const val BaseEntityDeletedColumnName = "deleted"


        /*          UserDataEntity Column Names        */

        const val UserDataEntityCreatedByJoinColumnName = "created_by"
        const val UserDataEntityModifiedByJoinColumnName = "modified_by"
        const val UserDataEntityDeletedByJoinColumnName = "deleted_by"
        const val UserDataEntityOwnerJoinColumnName = "owner"


        /*          DeepThoughtApplication Table Config        */

        const val DeepThoughtApplicationTableName = "application"

        const val DeepThoughtApplicationAppSettingsJoinColumnName = "app_settings_id"
        const val DeepThoughtApplicationDataModelVersionColumnName = "data_model_version"
        const val DeepThoughtApplicationLastLoggedOnUserJoinColumnName = "last_logged_on_user_id"
        const val DeepThoughtApplicationAutoLogOnLastLoggedOnUserColumnName = "auto_log_on_last_logged_on_user"
        const val DeepThoughtApplicationLocalDeviceJoinColumnName = "local_device_id"
        const val DeepThoughtApplicationNextEntryIndexColumnName = "next_entry_index"


        /*          User Table Config        */

        const val UserTableName = "user_dt" // 'user' is not allowed as table name as it's a system table, so i used user_dt (for _deep_thought)

        const val UserUniversallyUniqueIdColumnName = "universally_unique_id"
        const val UserUserNameColumnName = "user_name"
        const val UserFirstNameColumnName = "first_name"
        const val UserLastNameColumnName = "last_name"
        const val UserPasswordColumnName = "password"
        const val UserLastViewedDeepThoughtColumnName = "last_viewed_deep_thought"


        /*          User Device Join Table Config        */

        const val UserDeviceJoinTableName = "user_device_join_table"

        const val UserDeviceJoinTableUserIdColumnName = "user_id"
        const val UserDeviceJoinTableDeviceIdColumnName = "device_id"


        /*          User_SynchronizedDevices JoinTable Column Names        */

        const val USER_SYNCHRONIZED_DEVICES_JOIN_TABLE_NAME = "LocalConfig_SynchronizedDevices";

        const val USER_SYNCHRONIZED_DEVICES_LOCAL_CONFIG_ID_COLUMN_NAME = "local_config_id";
        const val USER_SYNCHRONIZED_DEVICES_DEVICE_ID_COLUMN_NAME = "device_id";


        /*          User_IgnoredDevices JoinTable Column Names        */

        const val USER_IGNORED_DEVICES_JOIN_TABLE_NAME = "LocalConfig_IgnoredDevices";

        const val USER_IGNORED_DEVICES_LOCAL_CONFIG_ID_COLUMN_NAME = "local_config_id";
        const val USER_IGNORED_DEVICES_DEVICE_ID_COLUMN_NAME = "device_id";



        /*          Device Table Config        */

        const val DeviceTableName = "device"

        const val UniqueDeviceIdColumnName = "unique_device_id"
        const val DeviceNameColumnName = "name"
        const val DeviceDescriptionColumnName = "description"
        const val DeviceOsTypeColumnName = "os_type"
        const val DeviceOsNameColumnName = "os_name"
        const val DeviceOsVersionColumnName = "os_version"
        const val DeviceIconColumnName = "device_icon"


        /*          DeepThought Table Config        */

        const val DeepThoughtTableName = "deep_thought"


        /*          Entry Table Config        */

        const val EntryTableName = "entry"

        const val EntryAbstractColumnName = "abstract"
        const val EntryContentColumnName = "content"
        const val EntryReferenceJoinColumnName = "reference_id"
        const val EntryIndicationColumnName = "indication"
        const val EntryPreviewImageJoinColumnName = "preview_image_id"
        const val EntryPreviewImageUrlColumnName = "preview_image_url"

        const val EntryEntryIndexColumnName = "entry_index"
        const val EntryLanguageJoinColumnName = "language_id"

        const val EntryDeepThoughtJoinColumnName = "deep_thought_id"


        /*          Entry Tag Join Table Config        */

        const val EntryTagJoinTableName = "entry_tag_join_table"

        const val EntryTagJoinTableEntryIdColumnName = "entry_id"
        const val EntryTagJoinTableTagIdColumnName = "tag_id"


        /*          Entry Category Join Table Config        */

        const val EntryCategoryJoinTableName = "entry_category_join_table"

        const val EntryCategoryJoinTableEntryIdColumnName = "entry_id"
        const val EntryCategoryJoinTableCategoryIdColumnName = "category_id"


        /*          Entry Attached Files Join Table Config        */

        const val EntryAttachedFilesJoinTableName = "entry_attached_files_join_table"

        const val EntryAttachedFilesJoinTableEntryIdColumnName = "entry_id"
        const val EntryAttachedFilesJoinTableFileLinkIdColumnName = "file_id"


        /*          Entry Embedded Files Join Table Config        */

        const val EntryEmbeddedFilesJoinTableName = "entry_embedded_files_join_table"

        const val EntryEmbeddedFilesJoinTableEntryIdColumnName = "entry_id"
        const val EntryEmbeddedFilesJoinTableFileLinkIdColumnName = "file_id"


        /*          Entry EntriesGroup Join Table Config        */

        const val EntryEntriesGroupJoinTableName = "entries_group_join_table"

        const val EntryEntriesGroupJoinTableEntryIdColumnName = "entry_id"
        const val EntryEntriesGroupJoinTableEntriesGroupIdColumnName = "entries_group_id"


        /*          EntriesGroup Config        */

        const val EntriesGroupTableName = "entries_group"

        const val EntriesGroupGroupNameColumnName = "name"
        const val EntriesGroupNotesColumnName = "notes"
        const val EntriesGroupDeepThoughtJoinColumnName = "deep_thought_id"


        /*          Tag Table Config        */

        const val TagTableName = "tag"

        const val TagNameColumnName = "name"
        const val TagDescriptionColumnName = "description"
        const val TagDeepThoughtJoinColumnName = "deep_thought_id"


        /*          Category Table Config        */

        const val CategoryTableName = "category"

        const val CategoryNameColumnName = "name"
        const val CategoryDescriptionColumnName = "description"
        const val CategoryIsExpandedColumnName = "is_expanded"
        const val CategoryCategoryOrderColumnName = "category_order"
        const val CategoryParentCategoryJoinColumnName = "parent_category_id"
        const val CategoryDeepThoughtJoinColumnName = "deep_thought_id"


        /*          Person Table Config        */

        const val PersonTableName = "person"

        const val PersonFirstNameColumnName = "first_name"
        const val PersonLastNameColumnName = "last_name"
        const val PersonNotesColumnName = "notes"
        const val PersonDeepThoughtJoinColumnName = "deep_thought_id"


        /*          EntryPersonJoinTable Table Config        */

        const val EntryPersonAssociationTableName = "entry_person_association"

        const val EntryPersonAssociationEntryJoinColumnName = "entry_id"
        const val EntryPersonAssociationPersonJoinColumnName = "person_id"
        const val EntryPersonAssociationPersonOrderColumnName = "person_order"


        /*          Note Table Config        */

        const val NoteTableName = "notes"

        const val NoteNoteColumnName = "notes"
        const val NoteNoteTypeJoinColumnName = "note_type_id"
        const val NoteEntryJoinColumnName = "entry_id"
        const val NoteDeepThoughtJoinColumnName = "deep_thought_id"


        /*          FileLink Table Config        */

        const val FileLinkTableName = "file"

        const val FileLinkUriColumnName = "uri"
        const val FileLinkNameColumnName = "name"
        const val FileLinkIsFolderColumnName = "folder"
        const val FileLinkFileTypeColumnName = "file_type"
        const val FileLinkDescriptionColumnName = "description"
        const val FileLinkSourceUriColumnName = "source_uri"
        const val FileLinkDeepThoughtJoinColumnName = "deep_thought_id"


        /*          Reference Table Config        */

        const val ReferenceTableName = "reference"

        const val ReferenceTitleColumnName = "title"
        const val ReferenceSubTitleColumnName = "sub_title"
        const val ReferenceAbstractColumnName = "abstract"
        const val ReferenceLengthColumnName = "length"
        const val ReferenceUrlColumnName = "url"
        const val ReferenceLastAccessDateColumnName = "last_access_date"
        const val ReferenceNotesColumnName = "notes"
        const val ReferencePreviewImageJoinColumnName = "preview_image_id"

        const val ReferenceSeriesColumnName = "series"
        const val ReferenceTableOfContentsColumnName = "table_of_contents"
        const val ReferenceIssueOrPublishingDateColumnName = "issue_or_publishing_date"
        const val ReferenceIsbnOrIssnColumnName = "isbn_or_issn"
        const val ReferencePublishingDateColumnName = "publishing_date"


        /*          Reference Attached Files Join Table Config        */

        const val ReferenceBaseAttachedFileJoinTableName = "reference_base_attached_files_join_table"

        const val ReferenceBaseAttachedFileJoinTableReferenceBaseIdColumnName = "reference_base_id"
        const val ReferenceBaseAttachedFileJoinTableFileLinkIdColumnName = "file_id"


        /*          Reference Embedded Files Join Table Config        */

        const val ReferenceBaseEmbeddedFileJoinTableName = "reference_base_embedded_files_join_table"

        const val ReferenceBaseEmbeddedFileJoinTableReferenceBaseIdColumnName = "reference_base_id"
        const val ReferenceBaseEmbeddedFileJoinTableFileLinkIdColumnName = "file_id"


        /*          ExtensibleEnumeration Table Config        */

        const val ExtensibleEnumerationNameColumnName = "name"
        const val ExtensibleEnumerationNameResourceKeyColumnName = "name_resource_key"
        const val ExtensibleEnumerationDescriptionColumnName = "description"
        const val ExtensibleEnumerationSortOrderColumnName = "sort_order"
        const val ExtensibleEnumerationIsSystemValueColumnName = "is_system_value"
        const val ExtensibleEnumerationIsDeletableColumnName = "is_deletable"
        const val ExtensibleEnumerationDeepThoughtJoinColumnName = "deep_thought_id"


        /*          ApplicationLanguage Table Config        */

        const val ApplicationLanguageTableName = "application_language"

        const val ApplicationLanguageLanguageKeyColumnName = "language_key"
        const val ApplicationLanguageDeepThoughtApplicationJoinColumnName = "application_id"


        /*          Language Table Config        */

        const val LanguageTableName = "language"

        const val LanguageLanguageKeyColumnName = "language_key"
        const val LanguageNameInLanguageColumnName = "name_in_language"


        /*          NoteType Table Config        */

        const val NoteTypeTableName = "note_type"


        /*          FileType Table Config        */

        const val FileTypeTableName = "file_type"

        const val FileTypeFolderNameColumnName = "folder"
        const val FileTypeIconColumnName = "icon"


        /*          BackupFileServiceType Table Config        */

        const val BackupFileServiceTypeTableName = "backup_file_service_type"


        /*          ReadLaterArticle Table Config        */

        const val ReadLaterArticleTableName = "read_later_article"

        const val ReadLaterArticleEntryExtractionResultColumnName = "entry_extraction_result"

    }
}