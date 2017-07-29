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

import com.cybozu.labs.langdetect.util.LangProfile;

/**
* @author Pascal Essiembre
*
*/
public class LanguageProfile {

    private final String language;
    private final LangProfile shuyoLangProfile;

    /*default*/ LanguageProfile(LangProfile shuyoLangProfile) {
        super();
        this.language = shuyoLangProfile.name;
        this.shuyoLangProfile = shuyoLangProfile;
    }

    public String getLanguage() {
        return language;
    }

    /*default*/ LangProfile getShuyoLangProfile() {
        return shuyoLangProfile;
    }
}
