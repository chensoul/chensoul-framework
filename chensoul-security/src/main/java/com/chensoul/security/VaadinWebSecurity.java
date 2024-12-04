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
package com.chensoul.security;

import com.chensoul.security.stateless.VaadinStatelessSecurityConfigurer;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.DelegatingAccessDeniedHandler;
import org.springframework.security.web.access.RequestMatcherDelegatingAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public abstract class VaadinWebSecurity {
    @Autowired(required = false)
    private VaadinRolePrefixHolder vaadinRolePrefixHolder;

    @Value("#{servletContext.contextPath}")
    private String servletContextPath;

    private final AuthenticationContext authenticationContext = new AuthenticationContext();

    @PostConstruct
    void afterPropertiesSet() {
        authenticationContext.setRolePrefixHolder(vaadinRolePrefixHolder);
    }

    @Bean(name = "VaadinSecurityFilterChainBean")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        configure(http);
        http.logout(cfg -> {
            cfg.invalidateHttpSession(true);
            addLogoutHandlers(cfg::addLogoutHandler);
        });

        DefaultSecurityFilterChain securityFilterChain = http.build();
        Optional.ofNullable(vaadinRolePrefixHolder)
                .ifPresent(vaadinRolePrefixHolder -> vaadinRolePrefixHolder
                        .resetRolePrefix(securityFilterChain));
        AuthenticationContext.applySecurityConfiguration(http, authenticationContext);
        return securityFilterChain;
    }

    @Bean(name = "VaadinAuthenticationContext")
    public AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    protected void configure(HttpSecurity http) throws Exception {
        // api 接口异常时，返回 401 状态码
        http.exceptionHandling(
                cfg -> cfg.accessDeniedHandler(createAccessDeniedHandler())
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")));
        // api 接口不需要 csrf
        http.csrf(cfg -> cfg.ignoringRequestMatchers("/api/**"));

        http.authorizeHttpRequests(urlRegistry -> {
            urlRegistry.requestMatchers("/public/**", "/error").permitAll();
            urlRegistry.requestMatchers("/fac").permitAll();
            urlRegistry.anyRequest().authenticated();
        });
    }

    @Bean(name = "VaadinWebSecurityCustomizerBean")
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            try {
                configure(web);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    protected void configure(WebSecurity web) throws Exception {
        // no-operation
    }

    protected RequestMatcher[] antMatchers(String... patterns) {
        return Stream.of(patterns).map(AntPathRequestMatcher::new)
                .toArray(RequestMatcher[]::new);
    }

    protected void setLoginPage(HttpSecurity http, String loginPage) throws Exception {
        setLoginPage(http, loginPage, getDefaultLogoutUrl());
    }

    protected void setLoginPage(HttpSecurity http, String loginPage,
                                String logoutSuccessUrl) throws Exception {
        http.formLogin(formLogin -> {
            formLogin.loginPage(loginPage).permitAll().successHandler(getSuccessHandler(http));
        });
        configureLogout(http, logoutSuccessUrl);
        http.exceptionHandling(cfg -> cfg.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint(loginPage), AnyRequestMatcher.INSTANCE));
    }

    protected void setOAuth2LoginPage(HttpSecurity http, String oauth2LoginPage)
            throws Exception {
        http.oauth2Login(cfg ->
                cfg.loginPage(oauth2LoginPage).permitAll().successHandler(getSuccessHandler(http))
        );
    }

    protected void setStatelessAuthentication(HttpSecurity http,
                                              SecretKey secretKey, String issuer) throws Exception {
        setStatelessAuthentication(http, secretKey, issuer, 1800L);
    }

    protected void setStatelessAuthentication(HttpSecurity http,
                                              SecretKey secretKey, String issuer, long expiresIn)
            throws Exception {
        VaadinStatelessSecurityConfigurer.apply(http,
                cfg -> cfg.withSecretKey().secretKey(secretKey).and()
                        .issuer(issuer).expiresIn(expiresIn));
    }


    protected void addLogoutHandlers(Consumer<LogoutHandler> registry) {

    }

    private void configureLogout(HttpSecurity http, String logoutSuccessUrl)
            throws Exception {
        VaadinSimpleUrlLogoutSuccessHandler logoutSuccessHandler = new VaadinSimpleUrlLogoutSuccessHandler();
        logoutSuccessHandler.setDefaultTargetUrl(logoutSuccessUrl);
        logoutSuccessHandler.setRedirectStrategy(new DefaultRedirectStrategy());
        http.logout(cfg -> cfg.logoutSuccessHandler(logoutSuccessHandler));
    }

    private String getDefaultLogoutUrl() {
        return servletContextPath.startsWith("/") ? servletContextPath
                :"/" + servletContextPath;
    }

    private VaadinSavedRequestAwareAuthenticationSuccessHandler getSuccessHandler(
            HttpSecurity http) {
        VaadinSavedRequestAwareAuthenticationSuccessHandler vaadinSavedRequestAwareAuthenticationSuccessHandler = new VaadinSavedRequestAwareAuthenticationSuccessHandler();
        vaadinSavedRequestAwareAuthenticationSuccessHandler.setDefaultTargetUrl("/");
        RequestCache requestCache = http.getSharedObject(RequestCache.class);
        if (requestCache!=null) {
            vaadinSavedRequestAwareAuthenticationSuccessHandler
                    .setRequestCache(requestCache);
        }
        http.setSharedObject(VaadinSavedRequestAwareAuthenticationSuccessHandler.class, vaadinSavedRequestAwareAuthenticationSuccessHandler);
        return vaadinSavedRequestAwareAuthenticationSuccessHandler;
    }

    private AccessDeniedHandler createAccessDeniedHandler() {
        final AccessDeniedHandler defaultHandler = new AccessDeniedHandlerImpl();

        final AccessDeniedHandler http401UnauthorizedHandler = new Http401UnauthorizedAccessDeniedHandler();

        final LinkedHashMap<Class<? extends AccessDeniedException>, AccessDeniedHandler> exceptionHandlers = new LinkedHashMap<>();
        exceptionHandlers.put(CsrfException.class, http401UnauthorizedHandler);

        final LinkedHashMap<RequestMatcher, AccessDeniedHandler> matcherHandlers = new LinkedHashMap<>();
        matcherHandlers.put(new AntPathRequestMatcher("/api/**"),
                new DelegatingAccessDeniedHandler(exceptionHandlers,
                        new AccessDeniedHandlerImpl()));

        return new RequestMatcherDelegatingAccessDeniedHandler(matcherHandlers,
                defaultHandler);
    }

    private static class Http401UnauthorizedAccessDeniedHandler
            implements AccessDeniedHandler {
        @Override
        public void handle(HttpServletRequest request,
                           HttpServletResponse response,
                           AccessDeniedException accessDeniedException)
                throws IOException, ServletException {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
