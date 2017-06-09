package net.dankito.deepthought.model.enums


import net.dankito.deepthought.model.config.TableConfig

import javax.persistence.Column
import javax.persistence.Entity

@Entity(name = TableConfig.ApplicationLanguageTableName)
class ApplicationLanguage : ExtensibleEnumeration {

    companion object {
        private const val serialVersionUID = -446610923063763955L
    }


    @Column(name = TableConfig.ApplicationLanguageLanguageKeyColumnName)
    var languageKey: String = ""
        private set


    private constructor() : this("")

    constructor(name: String) : super(name) {}

    constructor(nameResourceKey: String, languageKey: String, isSystemValue: Boolean, isDeletable: Boolean, sortOrder: Int) : super(nameResourceKey, isSystemValue, isDeletable, sortOrder) {
        this.languageKey = languageKey
    }

}
