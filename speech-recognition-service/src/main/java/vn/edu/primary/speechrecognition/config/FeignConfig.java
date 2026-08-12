package vn.edu.primary.speechrecognition.config;

import feign.RequestInterceptor;
import feign.Request;
import feign.form.spring.SpringFormEncoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

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
    public Request.Options pronunciationRequestOptions(
            @Value("${python.api.connect-timeout:10000}") long connectTimeout,
            @Value("${python.api.read-timeout:180000}") long readTimeout) {
        return new Request.Options(
                connectTimeout, TimeUnit.MILLISECONDS,
                readTimeout, TimeUnit.MILLISECONDS,
                true
        );
    }
}
