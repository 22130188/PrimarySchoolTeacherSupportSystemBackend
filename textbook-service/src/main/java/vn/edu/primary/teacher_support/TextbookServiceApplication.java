package vn.edu.primary.teacher_support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TextbookServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TextbookServiceApplication.class, args);
    }
}
