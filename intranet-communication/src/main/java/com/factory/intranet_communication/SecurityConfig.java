package com.factory.intranet_communication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2/**").permitAll()
                        .requestMatchers("/channels/*/latest").hasAnyRole("TERMINAL", "CONTROLLER", "CONTROL_ROOM")
                        .requestMatchers("/channels/*/message").hasAnyRole("TERMINAL", "CONTROLLER")
                        .requestMatchers("/channels/*/history").hasRole("CONTROL_ROOM")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        // Required for H2 console
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        return new InMemoryUserDetailsManager(
                User.withUsername("terminal")
                        .password("{noop}terminal123")
                        .roles("TERMINAL")
                        .build(),

                User.withUsername("controller")
                        .password("{noop}controller123")
                        .roles("CONTROLLER")
                        .build(),

                User.withUsername("admin")
                        .password("{noop}admin123")
                        .roles("CONTROL_ROOM")
                        .build()
        );
    }
}
