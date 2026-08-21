package vn.edu.primary.tts.config;

import vn.edu.primary.tts.security.JwtFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/convert", "/convert/*", "/save", "/upload", "/audios", "/audios/*", "/admin/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}