package cl.duoc.ejemplo.microservicio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad con OAuth2 Resource Server.
 * Integración con Azure AD para validación de tokens JWT.
 *
 * - CORS habilitado para permitir requests desde Postman/frontend
 * - CSRF deshabilitado (API REST stateless)
 * - Todos los endpoints requieren autenticación JWT válido
 * - Custom claims (extension_consultaRole) extraídos como roles
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Configurar converter de JWT a Authentication con roles personalizados
        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(new RolesClaimConverter());

        http
            .cors(Customizer.withDefaults())  // Habilitar CORS
            .csrf(csrf -> csrf.disable())      // Deshabilitar CSRF para API REST
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos para health checks
                .requestMatchers("/actuator/**").permitAll()
                // Todos los demás endpoints requieren autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
            );

        return http.build();
    }
}
