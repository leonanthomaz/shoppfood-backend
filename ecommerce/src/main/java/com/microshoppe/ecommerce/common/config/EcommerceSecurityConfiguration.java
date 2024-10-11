package com.microshoppe.ecommerce.common.config;

import com.microshoppe.erp.authentication.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class EcommerceSecurityConfiguration {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EcommerceSecurityFilter ecommerceSecurityFilter;

    @Bean
    public SecurityFilterChain ecommerceSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/cart/**").permitAll()
                        .requestMatchers("/checkout/**").permitAll()
                        .requestMatchers("/orders/**").permitAll()
                        .requestMatchers("/payments/**").permitAll()
                        .requestMatchers("/delivery/**").permitAll()
                        .requestMatchers("/store/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger/**", "/v3/api-docs/**", "/actuator/**", "/webjars/**").permitAll()
                        .anyRequest().authenticated() // Requerer autenticação para outras rotas
                )
                .addFilterBefore(ecommerceSecurityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager ecommerceAuthenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder ecommercePasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
