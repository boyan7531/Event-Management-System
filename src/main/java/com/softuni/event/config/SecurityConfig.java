package com.softuni.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/about", "/events", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/users/register", "/users/login").permitAll()
                .requestMatchers("/events/details/**").permitAll()
                .requestMatchers("/faq", "/terms", "/events/calendar").permitAll()
                .requestMatchers("/error-page").permitAll()
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/admin", "/users/admin/**").hasRole("ADMIN")
                .requestMatchers("/locations/create", "/locations/edit/**", "/locations/delete/**").hasRole("ADMIN")
                .requestMatchers("/events/create").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/users/login")
                .loginProcessingUrl("/users/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/error-page")
            )
            .securityContext(securityContext -> securityContext
                .securityContextRepository(securityContextRepository())
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
    
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }
} 