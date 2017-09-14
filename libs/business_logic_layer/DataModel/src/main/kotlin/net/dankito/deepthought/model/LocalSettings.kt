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
        var didShowListItemActionsHelp: Boolean,

        @Column(name = TableConfig.LocalSettingsDidShowSearchTagsHelpColumnName)
        var didShowSearchTagsHelp: Boolean,

        @Column(name = TableConfig.LocalSettingsDidShowSetTagsOnEntryHelpColumnName)
        var didShowSetTagsOnEntryHelp: Boolean,

        @Column(name = TableConfig.LocalSettingsCountTagSearchesColumnName)
        var countTagSearches: Int = 0,

        @Column(name = TableConfig.LocalSettingsCountTagsOnEntrySearchesColumnName)
        var countTagsOnEntrySearches: Int = 0

) : BaseEntity() {


    companion object {
        private const val serialVersionUID = 7190724856152328858L
    }


    internal constructor() : this(0, 0, 0, Date(0), Date(0), Date(0), false, false, false)

}