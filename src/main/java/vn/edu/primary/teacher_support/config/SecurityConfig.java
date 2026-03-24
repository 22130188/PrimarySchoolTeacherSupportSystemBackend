package vn.edu.primary.teacher_support.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthFilter        jwtAuthFilter;

    public SecurityConfig(OAuth2SuccessHandler oAuth2SuccessHandler,
                          JwtAuthFilter jwtAuthFilter) {
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.jwtAuthFilter        = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**",
                                "/login/oauth2/**",
                                "/oauth2/**").permitAll()
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/auth/**", "/login/oauth2/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/admin/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .authorizationEndpoint(a ->
                                a.baseUri("/oauth2/authorize"))
                        .redirectionEndpoint(r ->
                                r.baseUri("/login/oauth2/code/*"))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:5174"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}