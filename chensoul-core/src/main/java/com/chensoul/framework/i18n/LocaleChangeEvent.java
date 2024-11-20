/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.chensoul.framework.i18n;

import java.util.EventObject;
import java.util.Locale;

/**
 * Event object with data related to locale change.
 *
 * @author Vaadin Ltd
 * @since 1.0
 */
public class LocaleChangeEvent extends EventObject {

    private final Locale locale;

    public LocaleChangeEvent(Locale locale) {
        super(locale);
        this.locale = locale;
    }

    /**
     * Get the new locale that was set.
     *
     * @return set locale
     */
    public Locale getLocale() {
        return locale;
    }
}
