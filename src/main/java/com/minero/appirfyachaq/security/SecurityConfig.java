package com.minero.appirfyachaq.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, BearerTokenFilter tokenFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                .requestMatchers("/user/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/plant/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/plant/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/plant/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/terms/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
