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

import com.cybozu.labs.langdetect.ErrorCode;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.util.LangProfile;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
* @author Pascal Essiembre
*
*/
public class LanguageProfileLoader {


    /**
     * @param file file containing language profile.
     */
    public static LanguageProfile loadFromFile(File file)
            throws LanguageDetectorException {

        if (!file.isFile()) {
            throw new LanguageDetectorException(
                    "Profile file does not exist: " + file);
        }
        try (InputStream is = new BufferedInputStream(
                new FileInputStream(file))) {
            return loadFromStream(is);
        } catch (IOException e) {
            throw new LanguageDetectorException(
                    "Cannot load language from file: " + file, e);
        }
    }


    /**
     * @param clazz Class from which to obtain the {@link ClassLoader}.
     * @param qualifiedFileName classpath location of profile file, using the
     *             {@link ClassLoader} from the given class.
     */
    public static LanguageProfile loadFromClasspath(
            Class<?> clazz, String qualifiedFileName)
            throws LanguageDetectorException {

        InputStream input = clazz.getResourceAsStream(qualifiedFileName);
        if (input == null) {
            throw new LanguageDetectorException(
                    "Unsupported language candidate ("
                  + "qualifiedName=" + qualifiedFileName + "; classloaderOf="
                  + clazz.getName() + ").");
        }
        try (InputStream is = new BufferedInputStream(input)) {
            return loadFromStream(is);
        } catch (IOException e) {
            throw new LanguageDetectorException(
                    "Cannot load language from classpath ("
                  + "qualifiedName=" + qualifiedFileName + "; classloaderOf="
                  + clazz.getName() + ").", e);
        }
    }

    /**
     * @param is profile input stream
     */
    public static LanguageProfile loadFromStream(InputStream is)
            throws LanguageDetectorException {
        try {
            LangProfile shuyoLangProfile = JSON.decode(is, LangProfile.class);
            return new LanguageProfile(shuyoLangProfile);
        } catch (JSONException e) {
            throw new LanguageDetectorException("Cannot load language profile.",
                    new LangDetectException(
                            ErrorCode.FormatError, "profile format error."));
        } catch (IOException e) {
            throw new LanguageDetectorException("Cannot load language profile.",
                    new LangDetectException(
                            ErrorCode.FileLoadError, "can't read profile."));
        }
    }

}
