/* Copyright 2014 Norconex Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.norconex.language.detector;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.ErrorCode;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.util.LangProfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
* Should be cached if reused for optimal performance.
* @author Pascal Essiembre
*
*/
// using well-formed IETF BCP 47 language tag representing this locale.
public class LanguageDetector {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageDetector.class);

    private static final String[] DEFAULT_SHORTTEXT_LANGUAGES = new String[] {
        "ar", "bg", "bn", "ca", "cs", "da", "de", "el", "en", "es", "et", "fa",
        "fi", "fr", "gu", "he", "hi", "hr", "hu", "id", "it", "ja", "ko", "lt",
        "lv", "mk", "ml", "nl", "no", "pa", "pl", "pt", "ro", "ru", "si", "sq",
        "sv", "ta", "te", "th", "tl", "tr", "uk", "ur", "vi", "zh-cn", "zh-tw"
    };

    private static final String[] DEFAULT_LONGTEXT_LANGUAGES = new String[] {
        "af", "ar", "bg", "bn", "cs", "da", "de", "el", "en", "es", "et", "fa",
        "fi", "fr", "gu", "he", "hi", "hr", "hu", "id", "it", "ja", "kn", "ko",
        "lt", "lv", "mk", "ml", "mr", "ne", "nl", "no", "pa", "pl", "pt", "ro",
        "ru", "sk", "sl", "so", "sq", "sv", "sw", "ta", "te", "th", "tl", "tr",
        "uk", "ur", "vi", "zh-cn", "zh-tw"
    };

    static {
        Arrays.sort(DEFAULT_LONGTEXT_LANGUAGES);
        Arrays.sort(DEFAULT_SHORTTEXT_LANGUAGES);
    }

    private static final int MIN_LANGUAGES = 2;
    private static final int MAX_WORD_LENGTH = 3;
    private static final String LONG_TEXT_PATH = "/profiles/longtext/";
    private static final String SHORT_TEXT_PATH = "/profiles/shorttext/";
    private static final LanguageProfile[] EMPTY_PROFILES = new LanguageProfile[]{};

    //TODO use concurent hashmap?
    private final HashMap<String, double[]> wordLangProbMap = new HashMap<>();
    private final ArrayList<String> detectableLanguages = new ArrayList<>();

    public LanguageDetector() throws LanguageDetectorException {
        this(false);
    }

    public LanguageDetector(boolean shortText) throws LanguageDetectorException {
        if(shortText) {
            initProfilesFromTags(shortText, DEFAULT_SHORTTEXT_LANGUAGES);
        }
        else {
            initProfilesFromTags(shortText, DEFAULT_LONGTEXT_LANGUAGES);
        }
    }
    /**
     * Creates a new language detector with custom language profiles.
     * @param languageProfiles language profiles
     */
    public LanguageDetector(LanguageProfile... languageProfiles) throws LanguageDetectorException {
        super();
        if(languageProfiles == null || languageProfiles.length < MIN_LANGUAGES) {
            throw new LanguageDetectorException("Must supply at least " + MIN_LANGUAGES + " language profiles.");
        }

        initProfiles(languageProfiles);
    }

    public LanguageDetector(String... languageTags) throws LanguageDetectorException {
        this(false, languageTags);
    }

    public LanguageDetector(boolean shortText, String... detectableLanguages) throws LanguageDetectorException {
        super();

        if(detectableLanguages == null || detectableLanguages.length < MIN_LANGUAGES) {
            throw new LanguageDetectorException( "Must supply at least " + MIN_LANGUAGES + " languages.");
        }

        initProfilesFromTags(shortText, detectableLanguages);
    }

    public LanguageDetector(Locale... locales) throws LanguageDetectorException {
        this(false, locales);
    }

    public LanguageDetector(boolean shortText, Locale... locales) throws LanguageDetectorException {
        if(locales == null || locales.length < MIN_LANGUAGES) {
            throw new LanguageDetectorException("Must supply at least " + MIN_LANGUAGES + " locales.");
        }

        String[] languageTags = new String[locales.length];
        for(int i = 0; i < locales.length; i++) {
            languageTags[i] = locales[i].toLanguageTag();
        }

        initProfilesFromTags(shortText, languageTags);
    }

    public static String[] getDefaultShortTextLanguages() {
        return Arrays.copyOf(DEFAULT_SHORTTEXT_LANGUAGES, DEFAULT_SHORTTEXT_LANGUAGES.length);
    }

    public static String[] getDefaultLongTextLanguages() {
        return Arrays.copyOf(DEFAULT_LONGTEXT_LANGUAGES, DEFAULT_LONGTEXT_LANGUAGES.length);
    }

    public DetectedLanguages detect(Reader reader) throws LanguageDetectorException {
        //TODO wrap Reader in BufferedReader?
        Detector shuyoDetector = new Detector(wordLangProbMap, detectableLanguages, 0L);
        try {
            shuyoDetector.append(reader);
            // read the rest of Reader instance to ensure cleanliness
            int data = reader.read();
            while(data != -1){
                data = reader.read();
            }
        } catch (IOException e) {
            throw new LanguageDetectorException("Could not detect language from Reader.", e);
        }

        return doDetect(shuyoDetector);
    }

    public DetectedLanguages detect(String text) throws LanguageDetectorException {
        Detector shuyoDetector = new Detector( wordLangProbMap, detectableLanguages, 0L);
        shuyoDetector.append(text);

        return doDetect(shuyoDetector);
    }

    public DetectedLanguages doDetect(Detector shuyoDetector) throws LanguageDetectorException {
        try {
            return new DetectedLanguages(shuyoDetector.getProbabilities());
        } catch (LangDetectException e) {
            throw new LanguageDetectorException("Cannot detect language(s).", e);
        }
    }

    //TODO JVM-cache profiles loaded from classpath
    private void initProfilesFromTags(boolean shortText, String[] languageTags) throws LanguageDetectorException {
        List<LanguageProfile> profiles = new ArrayList<>();

        for(String languageTag : languageTags) {
            LanguageProfile profile = getClassPathProfile(shortText, languageTag);
            if(profile != null) {
                profiles.add(profile);
            }
        }

        initProfiles(profiles.toArray(EMPTY_PROFILES));
    }

    private LanguageProfile getClassPathProfile(boolean shortText, String langTag) {
        String path = LONG_TEXT_PATH;

        if(shortText) {
            path = SHORT_TEXT_PATH;
        }

        return LanguageProfileLoader.loadFromClasspath(getClass(), path + langTag);
    }

    private void initProfiles(LanguageProfile[] languageProfiles) throws LanguageDetectorException {
        int langsize = languageProfiles.length;

        for (int i = 0; i < languageProfiles.length; i++) {
            LanguageProfile languageProfile = languageProfiles[i];
            initProfile(languageProfile, i, langsize);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initialized " + languageProfiles.length + " language profiles for language detection.");
        }
    }

    /**
     * @param profile
     * @param langsize
     * @param index
     * @throws LangDetectException
     */
    private void initProfile(LanguageProfile languageProfile, int index, int langsize) throws LanguageDetectorException {
        LangProfile shuyoProfile = languageProfile.getShuyoLangProfile();
        String language = shuyoProfile.name;

        if (detectableLanguages.contains(language)) {
            throw new LanguageDetectorException("Cannot initialize profile for language tag: " + language,
                    new LangDetectException(ErrorCode.DuplicateLangError, "duplicate the same language profile"));
        }

        detectableLanguages.add(language);

        for (String word: shuyoProfile.freq.keySet()) {
            if(!wordLangProbMap.containsKey(word)) {
                wordLangProbMap.put(word, new double[langsize]);
            }

            int length = word.length();

            if(length >= 1 && length <= MAX_WORD_LENGTH) {
                double prob = shuyoProfile.freq.get(word).doubleValue() / shuyoProfile.n_words[length - 1];
                wordLangProbMap.get(word)[index] = prob;
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((detectableLanguages == null) ? 0 : detectableLanguages.hashCode());
        result = prime * result + ((wordLangProbMap == null) ? 0 : wordLangProbMap.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj == null) {
            return false;
        }

        if(!(obj instanceof LanguageDetector)) {
            return false;
        }

        LanguageDetector other = (LanguageDetector) obj;
        if(detectableLanguages == null) {
            if(other.detectableLanguages != null) {
                return false;
            }
        }
        else if (!detectableLanguages.equals(other.detectableLanguages)) {
            return false;
        }

        if(wordLangProbMap == null) {
            if(other.wordLangProbMap != null) {
                return false;
            }
        }
        else if(!wordLangProbMap.equals(other.wordLangProbMap)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return "LanguageDetector [wordLangProbMap=" + (wordLangProbMap != null
                        ? toString(wordLangProbMap.entrySet(), maxLen) : null)
                + ", detectableLanguages=" + (detectableLanguages != null
                        ? toString(detectableLanguages, maxLen) : null) + "]";
    }

    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;

        for(Iterator<?> iterator = collection.iterator();
                iterator.hasNext() && i < maxLen; i++) {
            if(i > 0) {
                builder.append(", ");
            }

            builder.append(iterator.next());
        }

        builder.append("]");

        return builder.toString();
    }
}
