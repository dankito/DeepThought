package net.dankito.utils.language

import com.norconex.language.detector.LanguageDetector
import org.slf4j.LoggerFactory


/**
 * <p>
 *  This actually is a Wrapper for the Norconex Wrapper (https://github.com/Norconex/language-detector) <br />
 *  for the really amazing "language-detection" library from Nakatani Shuyo (https://code.google.com/p/language-detection). <br /> <br />
 *  Why did i use the Wrapper? The language-detection library is really great, but it's not perfectly clear how to configure at first glance. <br />
 *  So i used the Norconex Wrapper, which already did this job for me, and this way saved me a lot of time.
 * </p>
 */
class NorconexLanguageDetector(private val supportedLanguages: SupportedLanguages) : ILanguageDetector {

    companion object {
        private val MinProbability = 0.75

        private val log = LoggerFactory.getLogger(NorconexLanguageDetector::class.java)
    }


    private var detector: LanguageDetector? = null


    /**
     * Do not initialize LanguageDetector right at start as it reads many files from file system which slows down app start by seconds
     */
    private fun initializeLanguageDetector(): LanguageDetector {
        try {
            return LanguageDetector(true)
        } catch (e: Exception) {
            log.error("Could not create LanguageDetector", e)
            throw e
        }
    }


    override fun detectLanguage(text: String): Language {
        getLanguageTagOfText(text)?.let { languageKey ->
            supportedLanguages.getLanguageForKey(languageKey)?.let {
                return it
            }
        }

        return ILanguageDetector.CouldNotDetectLanguage
    }

    private fun getLanguageTagOfText(text: String): String? {
        try {
            val detectedLanguages = detector().detect(text)

            for(probableLanguage in detectedLanguages) {
                if(probableLanguage.probability > MinProbability) {
                    return probableLanguage.tag
                }
            }
        } catch(e: Exception) {
            log.warn("Could not detect language for text " + text, e);
        }

        return null
    }

    private fun detector(): LanguageDetector {
        detector?.let { return it }

        val detector = initializeLanguageDetector()
        this.detector = detector

        return detector
    }

}