package com.chensoul.framework.config;

import static com.chensoul.framework.AppConstants.PROFILE_NOT_PROD;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile(PROFILE_NOT_PROD)
@Configuration(proxyBeanMethods = false)
// @OpenAPIDefinition(info = @Info(title = "app", version = "v1"))
class SpringDocConfig {
    static {
        SpringDocUtils.getConfig().replaceWithClass(ByteBuffer.class, String.class);
    }

    @Bean
    OpenAPI openApi(@Value("${spring.application.name}") String applicationName) {
        String name = StringUtils.uncapitalize(applicationName).replaceAll("-", " ");
        return new OpenAPI().info(new Info().title(name + " APIs").version("v1.0.0"));
    }

    @Bean
    public ServerBaseUrlCustomizer serverBaseUrlRequestCustomizer() {
        return (serverBaseUrl, request) -> {
            List<String> forwardedPrefix = request.getHeaders().get("X-Forwarded-Prefix");
            if (forwardedPrefix != null && forwardedPrefix.size() > 0) {
                return forwardedPrefix.get(0);
            }
            return serverBaseUrl;
        };
    }
}
