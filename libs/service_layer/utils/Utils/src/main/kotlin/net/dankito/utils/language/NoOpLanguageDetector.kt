package net.dankito.utils.language


class NoOpLanguageDetector : ILanguageDetector {

    override fun detectLanguage(text: String): Language {
        return ILanguageDetector.CouldNotDetectLanguage
    }

}