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
package com.chensoul.core.util;

import com.chensoul.core.i18n.DefaultI18NProvider;
import com.chensoul.core.i18n.I18NProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility class for locale handling.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @since 1.0
 */
public final class LocaleUtil {

    private LocaleUtil() {
    }

    /**
     * Get the exact locale match for the given request in the provided locales.
     *
     * @param request         request to get locale for
     * @param providedLocales application provided locales
     * @return found locale or null if no exact matches
     */
    public static Optional<Locale> getExactLocaleMatch(HttpServletRequest request,
                                                       List<Locale> providedLocales) {
        Locale foundLocale = null;
        Enumeration<Locale> locales = request.getLocales();
        while (locales.hasMoreElements()) {
            Locale locale = locales.nextElement();
            if (providedLocales.contains(locale)) {
                foundLocale = locale;
                break;
            }
        }
        return Optional.ofNullable(foundLocale);
    }

    /**
     * Get the locale matching the language of the request locale in the
     * provided locales.
     *
     * @param request         request to get locale for
     * @param providedLocales application provided locales
     * @return found locale or null if no matches by language
     */
    public static Optional<Locale> getLocaleMatchByLanguage(
            HttpServletRequest request, List<Locale> providedLocales) {
        Locale foundLocale = null;
        Enumeration<Locale> locales = request.getLocales();
        while (locales.hasMoreElements()) {
            Locale locale = locales.nextElement();
            Optional<Locale> matching = providedLocales
                    .stream().filter(providedLocale -> providedLocale
                            .getLanguage().equals(locale.getLanguage()))
                    .findFirst();
            if (matching.isPresent()) {
                foundLocale = matching.get();
                break;
            }
        }
        return Optional.ofNullable(foundLocale);
    }

    /**
     * Get the I18nProvider from the current VaadinService.
     * <p>
     *
     * @return the optional value of I18nProvider
     */
    public static Optional<I18NProvider> getI18NProvider() {
        return Optional.of(new DefaultI18NProvider(Arrays.asList(Locale.getDefault())));
//        return Optional.ofNullable(VaadinService.getCurrent())
//                .map(VaadinService::getInstantiator)
//                .map(Instantiator::getI18NProvider);
    }

    public static Locale getLocale(
            Supplier<Optional<I18NProvider>> i18NProvider) {
        return Locale.getDefault();
//        return Optional.ofNullable(Reques).map(UI::getLocale)
//                .or(() -> i18NProvider.get()
//                        .map(I18NProvider::getProvidedLocales)
//                        .flatMap(locales -> locales.stream().findFirst()))
//                .orElseGet(Locale::getDefault);
    }

    public static Locale getLocale() {
        return getLocale(LocaleUtil::getI18NProvider);
    }
}
