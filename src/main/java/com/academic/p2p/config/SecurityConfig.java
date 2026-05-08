package com.academic.p2p.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            
            // UPDATE: Let all requests through. Your WebController will handle security via HttpSession.
            .authorizeRequests()
                .antMatchers("/**").permitAll()
            .and()
            
            .formLogin().disable()
            .httpBasic().disable()
            .headers().frameOptions().disable();

        return http.build();
    }
}