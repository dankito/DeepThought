package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
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

        @Column(name = TableConfig.LocalSettingsLastSearchIndexUpdateTimeColumnName)
        var lastSearchIndexUpdateTime: Date,

        @Column(name = TableConfig.LocalSettingsLastSearchIndexOptimizationTimeColumnName)
        var lastSearchIndexOptimizationTime: Date,

        @Column(name = TableConfig.LocalSettingsDidShowListItemActionsHelpColumnName)
        var didShowListItemActionsHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsDidShowSearchTagsHelpColumnName)
        var didShowSearchTagsHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsCountTagSearchesColumnName)
        var countTagSearches: Int = 0,

        @Column(name = TableConfig.LocalSettingsDidShowSetTagsOnEntryHelpColumnName)
        var didShowSetTagsOnEntryHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsCountTagsOnEntrySearchesColumnName)
        var countTagsOnEntrySearches: Int = 0,

        @Column(name = TableConfig.LocalSettingsDidShowSaveEntryChangesHelpColumnName)
        var didShowSaveEntryChangesHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsDidShowEntryInformationFullscreenHelpColumnName)
        var didShowEntryInformationFullscreenHelp: Boolean = false,

        @Column(name = TableConfig.LocalSettingsDidShowEntryInformationFullscreenGesturesHelpColumnName)
        var didShowEntryInformationFullscreenGesturesHelp: Boolean = false

) : BaseEntity() {


    companion object {
        const val ShowSearchTagsHelpOnCountSearches = 30

        const val ShowSetTagsOnEntryHelpOnCountSearches = 30

        private const val serialVersionUID = 7190724856152328858L
    }


    internal constructor() : this(0, 0, 0, Date(0), Date(0), Date(0))

}