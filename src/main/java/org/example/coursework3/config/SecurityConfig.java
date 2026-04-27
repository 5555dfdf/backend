package org.example.coursework3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF（开发阶段方便调试接口）
                .authorizeHttpRequests(auth -> auth
                        // 1. release static resource (index.html, css, js)
                        .requestMatchers("/", "/index.html", "/static/**", "/*.html").permitAll()
                        // 2. release API
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/me/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        .requestMatchers("/expertise/**").permitAll()
                        .requestMatchers("/pricing/quote").permitAll()
                        .requestMatchers("/specialist/**").permitAll()
                        .requestMatchers("/specialists/**").permitAll()
                        .requestMatchers("/bookings/**").permitAll()
                        // 3. other request verification demand
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}