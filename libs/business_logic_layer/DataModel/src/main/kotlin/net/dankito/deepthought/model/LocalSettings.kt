package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.synchronization.model.BaseEntity
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity


@Entity(name = TableConfig.LocalSettingsTableName)
data class LocalSettings(

        @Column(name = TableConfig.LocalSettingsCommunicationProtocolVersionColumnName)
        var communicationProtocolVersion: Int,

        @Column(name = TableConfig.LocalSettingsSearchIndexVersionColumnName)
        var searchIndexVersion: Int,

        @Column(name = TableConfig.LocalSettingsHtmlEditorVersionColumnName)
        var htmlEditorVersion: Int,

        @Column(name = TableConfig.LocalSettingsLastDatabaseOptimizationTimeColumnName)
        var lastDatabaseOptimizationTime: Date,

        @Column(name = TableConfig.LocalSettingsLastSearchIndexUpdateSequenceNumberColumnName)
        var lastSearchIndexUpdateSequenceNumber: Long,

        @Column(name = TableConfig.LocalSettingsLastSearchIndexOptimizationTimeColumnName)
        var lastSearchIndexOptimizationTime: Date,

        @Column(name = TableConfig.LocalSettingsDidUserCreateDataEntityColumnName)
        var didUserCreateDataEntity: Boolean = false,

        @Column(name = TableConfig.LocalSettingsDidShowListItemActionsHelpColumnName)
        var didShowListItemActionsHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsDidShowSearchTagsHelpColumnName)
        var didShowSearchTagsHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsCountTagSearchesColumnName)
        var countTagSearches: Int = 0,

        @Column(name = TableConfig.LocalSettingsCountTagsOnItemSearchesColumnName)
        var countTagsOnItemSearches: Int = 0,

        @Column(name = TableConfig.LocalSettingsDidShowAddItemPropertiesHelpColumnName)
        var didShowAddItemPropertiesHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsDidShowReaderViewHelpColumnName)
        var didShowReaderViewHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsDidShowItemInformationFullscreenHelpColumnName)
        var didShowItemInformationFullscreenHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsDidShowItemInformationFullscreenGesturesHelpColumnName)
        var didShowItemInformationFullscreenGesturesHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsDidShowSavedReadLaterArticleIsNowInItemsHelpColumnName)
        var didShowSavedReadLaterArticleIsNowInItemsHelp: Boolean = false

) : BaseEntity() {


    companion object {
        const val ShowSearchTagsHelpOnCountSearches = 30

        private const val serialVersionUID = 7190724856152328858L
    }


    internal constructor() : this(0, 0, 0, Date(0), 0, Date(0))

}