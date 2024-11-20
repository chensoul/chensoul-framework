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
package com.chensoul.framework.security;

import com.chensoul.framework.security.check.AccessAnnotationChecker;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(WebSecurityCustomizer.class)
public class SpringSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AccessAnnotationChecker accessAnnotationChecker() {
        return new AccessAnnotationChecker();
    }

    @Bean
    @ConditionalOnMissingBean
    public VaadinRolePrefixHolder vaadinRolePrefixHolder(
            Optional<GrantedAuthorityDefaults> grantedAuthorityDefaults) {
        return new VaadinRolePrefixHolder(grantedAuthorityDefaults
                .map(GrantedAuthorityDefaults::getRolePrefix).orElse(null));
    }
}
