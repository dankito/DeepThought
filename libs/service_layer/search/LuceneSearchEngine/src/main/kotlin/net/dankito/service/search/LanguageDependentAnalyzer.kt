package net.dankito.service.search

import net.dankito.deepthought.model.Item
import net.dankito.utils.language.ILanguageDetector
import net.dankito.utils.language.Language
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.AnalyzerWrapper
import org.apache.lucene.analysis.ar.ArabicAnalyzer
import org.apache.lucene.analysis.bg.BulgarianAnalyzer
import org.apache.lucene.analysis.cjk.CJKAnalyzer
import org.apache.lucene.analysis.cz.CzechAnalyzer
import org.apache.lucene.analysis.da.DanishAnalyzer
import org.apache.lucene.analysis.de.GermanAnalyzer
import org.apache.lucene.analysis.el.GreekAnalyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.es.SpanishAnalyzer
import org.apache.lucene.analysis.fa.PersianAnalyzer
import org.apache.lucene.analysis.fi.FinnishAnalyzer
import org.apache.lucene.analysis.fr.FrenchAnalyzer
import org.apache.lucene.analysis.hi.HindiAnalyzer
import org.apache.lucene.analysis.hu.HungarianAnalyzer
import org.apache.lucene.analysis.id.IndonesianAnalyzer
import org.apache.lucene.analysis.it.ItalianAnalyzer
import org.apache.lucene.analysis.nl.DutchAnalyzer
import org.apache.lucene.analysis.no.NorwegianAnalyzer
import org.apache.lucene.analysis.pt.PortugueseAnalyzer
import org.apache.lucene.analysis.ro.RomanianAnalyzer
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.sv.SwedishAnalyzer
import org.apache.lucene.analysis.th.ThaiAnalyzer
import org.apache.lucene.analysis.tr.TurkishAnalyzer
import org.apache.lucene.analysis.util.CharArraySet
import org.apache.lucene.util.Version
import java.util.*


class LanguageDependentAnalyzer(private val languageDetector: ILanguageDetector) : AnalyzerWrapper(PER_FIELD_REUSE_STRATEGY) {

    companion object {
        private val DefaultStopWords = CharArraySet(Version.LUCENE_47, 0, true)
    }


    private val defaultAnalyzer: Analyzer

    private val defaultLanguageDependentFieldAnalyzer: Analyzer
    private var currentLanguageAnalyzer: Analyzer

    private var cachedLanguageAnalyzers: MutableMap<Language, Analyzer> = HashMap<Language, Analyzer>()

    init {
        defaultAnalyzer = StandardAnalyzer(Version.LUCENE_47)
        defaultLanguageDependentFieldAnalyzer = StandardAnalyzer(Version.LUCENE_47)
        currentLanguageAnalyzer = defaultLanguageDependentFieldAnalyzer
    }

    override fun getWrappedAnalyzer(fieldName: String): Analyzer {
        // only Item's EntryContent and EntryAbstract are analyzed based on current language (so that stop words get removed and words are being stemmed)
        // this is a bit problematic: as we only be the field name, but not the field value, we cannot determine the language of Abstract and Content directly
        if(FieldName.EntryContent == fieldName || FieldName.EntryAbstract == fieldName) {
            return currentLanguageAnalyzer
        }

        return defaultAnalyzer
    }

    private fun getAnalyzerForTextLanguage(text: String): Analyzer {
        val language = languageDetector.detectLanguage(text)

        if(language === ILanguageDetector.CouldNotDetectLanguage) {
            return defaultAnalyzer
        }

        return getAnalyzerForLanguage(language)
    }

    private fun getAnalyzerForLanguage(language: Language): Analyzer {
        var cachedAnalyzer = cachedLanguageAnalyzers[language]

        if(cachedAnalyzer == null) {
            cachedAnalyzer = createAnalyzerForLanguage(language)
            cachedLanguageAnalyzers.put(language, cachedAnalyzer)
        }

        return cachedAnalyzer
    }

    private fun createAnalyzerForLanguage(language: Language): Analyzer {
        // i only need removing stop words, not stemming (as i'm doing PrefixQuery anyway) and also not normalizing letters like ä -> ae, è -> e, ... (without indexing them the can't be found later on)
        return StandardAnalyzer(Version.LUCENE_47, getLanguageStopWords(language))
    }

    fun getLanguageStopWords(language: Language): CharArraySet {
        if(language === ILanguageDetector.CouldNotDetectLanguage) {
            return DefaultStopWords
        }

        when(language.languageKey) {
            "en" -> return EnglishAnalyzer.getDefaultStopSet()
            "es" -> return SpanishAnalyzer.getDefaultStopSet()
            "fr" -> return FrenchAnalyzer.getDefaultStopSet()
            "it" -> return ItalianAnalyzer.getDefaultStopSet()
            "de" -> {
                val stopWords = GermanAnalyzer.getDefaultStopSet()
                stopWords.add("dass") // can't believe it, dass is missing in Lucene's German Stop Wort List
                return stopWords
            }
            "ar" -> return ArabicAnalyzer.getDefaultStopSet()
            "bg" -> return BulgarianAnalyzer.getDefaultStopSet()
            "br" -> return PortugueseAnalyzer.getDefaultStopSet() // Brazil
            "cs" -> return CzechAnalyzer.getDefaultStopSet()
            "da" -> return DanishAnalyzer.getDefaultStopSet()
            "el" -> return GreekAnalyzer.getDefaultStopSet()
            "fa" -> return PersianAnalyzer.getDefaultStopSet()
            "fi" -> return FinnishAnalyzer.getDefaultStopSet()
            "hi" -> return HindiAnalyzer.getDefaultStopSet()
            "hu" -> return HungarianAnalyzer.getDefaultStopSet()
            "id" -> return IndonesianAnalyzer.getDefaultStopSet()
            "ja" -> return CJKAnalyzer.getDefaultStopSet() // Japanese
            "ko" -> return CJKAnalyzer.getDefaultStopSet() // Korean
            "nl" -> return DutchAnalyzer.getDefaultStopSet()
            "no" -> return NorwegianAnalyzer.getDefaultStopSet()
            "pt" -> return PortugueseAnalyzer.getDefaultStopSet()
            "ro" -> return RomanianAnalyzer.getDefaultStopSet()
            "ru" -> return RussianAnalyzer.getDefaultStopSet()
            "sv" -> return SwedishAnalyzer.getDefaultStopSet()
            "th" -> return ThaiAnalyzer.getDefaultStopSet()
            "tr" -> return TurkishAnalyzer.getDefaultStopSet()
            "zh-cn" -> return CJKAnalyzer.getDefaultStopSet() // Simplified Chinese
            "zh-tw" -> return CJKAnalyzer.getDefaultStopSet() // Traditional Chinese
        }

        return DefaultStopWords
    }

    /**
     *
     *
     * This is a bit problematic: as we only be the field name, but not the field value, we cannot determine the language of Abstract and Content directly
     * So before an Item's Abstract and Content can be analyzed, you have to tell LanguageDependentAnalyzer explicitly which Item is going to be indexed next.
     *
     * @param item
     */
    fun setNextEntryToBeAnalyzed(item: Item, contentPlainText: String, abstractPlainText: String) {
        currentLanguageAnalyzer = getAnalyzerForTextLanguage(abstractPlainText + " " + contentPlainText)
    }

}
