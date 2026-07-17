package vn.edu.primary.teacher_support.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Docker Compose: call peer services by container name + port.
 * Do NOT use @LoadBalanced here — Eureka LB + "user-service:8082" causes
 * Connection refused / timeouts and nginx 502 on /api/admin/classrooms.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
