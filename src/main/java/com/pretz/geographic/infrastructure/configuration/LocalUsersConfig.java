package com.pretz.geographic.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static com.pretz.geographic.infrastructure.configuration.SecurityConfig.ADMIN;
import static com.pretz.geographic.infrastructure.configuration.SecurityConfig.USER_READ;
import static com.pretz.geographic.infrastructure.configuration.SecurityConfig.USER_WRITE;

/**
 * Hardcoded users for local deployments and manual testing (username = password).
 */
@Configuration
@Profile("local")
public class LocalUsersConfig {

    @Bean
    UserDetailsService localUsers() {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password("{noop}admin").roles(ADMIN).build(),
                User.withUsername("writer").password("{noop}writer").roles(USER_WRITE).build(),
                User.withUsername("reader").password("{noop}reader").roles(USER_READ).build());
    }
}
