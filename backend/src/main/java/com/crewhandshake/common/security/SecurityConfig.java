package com.crewhandshake.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 SessionAuthFilter sessionAuthFilter,
                                                 CsrfCookieFilter csrfCookieFilter,
                                                 CookieCsrfTokenRepository csrfTokenRepository) throws Exception {
    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository)
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            .ignoringRequestMatchers(
                new AntPathRequestMatcher("/api/v1/auth/**"),
                new AntPathRequestMatcher("/api/v1/public/**"),
                new AntPathRequestMatcher("/api/v1/me/active-company")
            )
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**", "/api/v1/public/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .addFilterBefore(sessionAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(csrfCookieFilter, CsrfFilter.class)
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable());
    return http.build();
  }

  @Bean
  public CookieCsrfTokenRepository csrfTokenRepository() {
    CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    repository.setCookieName("CH-XSRF-TOKEN");
    repository.setHeaderName("X-CH-XSRF-TOKEN");
    repository.setCookiePath("/");
    return repository;
  }
}
