package com.pretz.geographic.infrastructure.configuration;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 * Central authorization rules. Roles form a hierarchy: {@code ADMIN > USER_WRITE > USER_READ}.
 * Users are authenticated with HTTP Basic for now.
 */
//TODO replace HTTP Basic with OAuth2 resource server (JWT) once the identity provider is chosen
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    public static final String ADMIN = "ADMIN";
    public static final String USER_WRITE = "USER_WRITE";
    public static final String USER_READ = "USER_READ";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(POST, "/api/games", "/api/players").hasRole(ADMIN)
                        .requestMatchers(POST, "/api/daily-entries").hasRole(USER_WRITE)
                        .requestMatchers(GET, "/api/weekly-ranking").hasRole(USER_WRITE)
                        .requestMatchers(GET, "/api/**").hasRole(USER_READ)
                        .anyRequest().denyAll())
                .build();
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(ADMIN).implies(USER_WRITE)
                .role(USER_WRITE).implies(USER_READ)
                .build();
    }
}
