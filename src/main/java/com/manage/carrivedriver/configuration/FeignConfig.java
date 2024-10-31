package com.manage.carrivedriver.configuration;

import com.manage.carrivedriver.security.JwtRequestFilter;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String token = JwtRequestFilter.driver.getToken();
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}
