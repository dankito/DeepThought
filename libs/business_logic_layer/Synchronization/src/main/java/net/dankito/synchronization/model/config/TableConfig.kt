package net.dankito.synchronization.model.config


class TableConfig {

    companion object {


        /*          BaseEntity Column Names        */

        const val BaseEntityIdColumnName = "id"
        const val BaseEntityCreatedOnColumnName = "created_on"
        const val BaseEntityModifiedOnColumnName = "modified_on"
        const val BaseEntityVersionColumnName = "version"
        const val BaseEntityDeletedColumnName = "deleted"


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


        /*          FileLink Table Config        */

        const val FileLinkTableName = "file"

        const val FileLinkUriColumnName = "uri"
        const val FileLinkNameColumnName = "name"
        const val FileLinkIsLocalFileColumnName = "is_local_file"
        const val FileLinkMimeTypeColumnName = "mime_type"
        const val FileLinkFileTypeColumnName = "file_type"
        const val FileLinkFileSizeColumnName = "file_size"
        const val FileLinkFileLastModifiedColumnName = "file_last_modified"
        const val FileLinkFileHashSHA256ColumnName = "hash_sha_256"
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
        const val LocalFileInfoFileHashSHA256ColumnName = "hash_sha_256"

    }
}