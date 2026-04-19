package vn.edu.primary.speechrecognition.config;

import feign.form.FormData;
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

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
