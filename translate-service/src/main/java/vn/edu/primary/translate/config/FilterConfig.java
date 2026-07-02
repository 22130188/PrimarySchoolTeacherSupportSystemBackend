package vn.edu.primary.translate.config;

import vn.edu.primary.translate.security.JwtFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/translate/*", "/document/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
