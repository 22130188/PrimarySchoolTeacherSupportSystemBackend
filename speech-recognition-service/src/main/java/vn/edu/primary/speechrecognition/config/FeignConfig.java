package vn.edu.primary.speechrecognition.config;

import feign.RequestInterceptor;
import feign.form.spring.SpringFormEncoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Feign configuration for multipart/form-data support
 */
@Configuration
public class FeignConfig {

    @Bean
    public SpringFormEncoder feignFormEncoder(HttpMessageConverters converters) {
        return new SpringFormEncoder(new SpringEncoder(() -> converters));
    }

    /** Gọi Python nội bộ — không để FastAPI ghi action log trùng với Gateway. */
    @Bean
    public RequestInterceptor actionLogSkipInterceptor() {
        return template -> template.header("X-Action-Logged-By-Gateway", "true");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
