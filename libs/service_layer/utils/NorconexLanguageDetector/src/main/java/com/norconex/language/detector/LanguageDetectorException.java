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

/**
* @author Pascal Essiembre
*
*/
public class LanguageDetectorException extends RuntimeException {

    private static final long serialVersionUID = -8753683145084939801L;

    /**
     * Constructor.
     */
    public LanguageDetectorException() {
        super();
    }

    /**
     * Constructor.
     * @param message
     */
    public LanguageDetectorException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param cause
     */
    public LanguageDetectorException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     * @param message
     * @param cause
     */
    public LanguageDetectorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public LanguageDetectorException(
            String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
