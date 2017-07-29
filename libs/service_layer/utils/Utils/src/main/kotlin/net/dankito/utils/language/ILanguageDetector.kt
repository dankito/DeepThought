package net.dankito.utils.language


interface ILanguageDetector {

    companion object {
        val CouldNotDetectLanguage = Language("_", "CouldNotDetectLanguage", "CouldNotDetectLanguage", -1)
    }

    fun detectLanguage(text: String): Language

}