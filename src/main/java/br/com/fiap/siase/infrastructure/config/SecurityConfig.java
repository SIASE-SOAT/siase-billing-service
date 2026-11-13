package br.com.fiap.siase.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")).build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/api/swagger-ui/**", "/api/swagger-ui.html",
                                "/api/v3/api-docs/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/billing/webhook/**").permitAll() // webhook MP
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}));
        return http.build();
    }
}
