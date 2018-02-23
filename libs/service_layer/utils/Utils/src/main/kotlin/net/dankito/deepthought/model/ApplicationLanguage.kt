package net.dankito.deepthought.model


enum class ApplicationLanguage(val languageKey: String, val languageNameResourceKey: String, val sortOrder: Int) {


    English("en", "application.language.english", 1),
    German("de", "application.language.german", 2)

}
