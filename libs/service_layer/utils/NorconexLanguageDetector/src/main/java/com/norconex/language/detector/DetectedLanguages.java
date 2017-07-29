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

import java.util.ArrayList;
import java.util.List;

import com.cybozu.labs.langdetect.Language;

/**
* @author Pascal Essiembre
*
*/
public class DetectedLanguages extends ArrayList<DetectedLanguage> {

    private static final long serialVersionUID = 2506831927979398855L;

    /**
     * Constructor.
     */
    /*default*/ DetectedLanguages(ArrayList<Language> shuyoLanguages) {
        super(toDetectedLanguageList(shuyoLanguages));
    }

    private static List<DetectedLanguage> toDetectedLanguageList(
            ArrayList<Language> shuyoLanguages) {
        List<DetectedLanguage> languages = new ArrayList<>();
        for (Language language : shuyoLanguages) {
            languages.add(new DetectedLanguage(language));
        }
        return languages;
    }

    public DetectedLanguage getBestLanguage() {
        if (!isEmpty()) {
            return get(0);
        }
        return null;
    }

    public DetectedLanguage getLanguage(String language) {
        for (DetectedLanguage lang : this) {
            if (lang.getTag().equals(language)) {
                return lang;
            }
        }
        return null;
    }

    public boolean containsLanguage(String language) {
        return getLanguage(language) != null;
    }
}
