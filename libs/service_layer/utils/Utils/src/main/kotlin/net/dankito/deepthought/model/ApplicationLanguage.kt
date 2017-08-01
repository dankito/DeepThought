package net.dankito.deepthought.model

import net.dankito.deepthought.model.enums.ExtensibleEnumeration


class ApplicationLanguage : ExtensibleEnumeration {

    companion object {
        private const val serialVersionUID = -446610923063763955L

        fun getApplicationLanguages(): List<ApplicationLanguage> {
            val languages = ArrayList<ApplicationLanguage>()

            languages.add(ApplicationLanguage("application.language.english", "en", true, 1))
            languages.add(ApplicationLanguage("application.language.german", "de", true, 2))

            return languages
        }
    }


    var languageKey: String = ""
        private set


    private constructor() : this("")

    constructor(name: String) : super(name) {}

    constructor(nameResourceKey: String, languageKey: String, isSystemValue: Boolean, sortOrder: Int) : super(nameResourceKey, isSystemValue, sortOrder) {
        this.languageKey = languageKey
    }

}
