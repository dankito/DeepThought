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

import com.cybozu.labs.langdetect.Language;

/**
* @author Pascal Essiembre
*
*/
public class DetectedLanguage {

    private final Language language;

    /**
     * Constructor.
     */
    public DetectedLanguage(Language language) {
        this.language = language;
    }

    /**
     * @return the language
     */
    public String getTag() {
        return language.lang;
    }
    public double getProbability() {
        return language.prob;
    }

    @Override
    public String toString() {
        return language.toString();
    }
}
