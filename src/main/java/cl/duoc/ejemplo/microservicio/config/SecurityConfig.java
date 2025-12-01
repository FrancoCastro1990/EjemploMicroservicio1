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
 * - Todos los endpoints requieren token JWT válido
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // TODO: Configuración de roles deshabilitada temporalmente
        // La extracción del claim 'extension_Roles' desde Azure AD no está funcionando correctamente.
        // Cuando se resuelva la configuración en Azure AD, descomentar el siguiente bloque:
        //
        // JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        // jwtAuthConverter.setJwtGrantedAuthoritiesConverter(new RolesClaimConverter());
        //
        // Y cambiar .authenticated() por .hasRole("ADMIN") en authorizeHttpRequests
        // Y agregar .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)) en oauth2ResourceServer

        http
            .cors(Customizer.withDefaults())  // Habilitar CORS
            .csrf(csrf -> csrf.disable())      // Deshabilitar CSRF para API REST
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos para health checks
                .requestMatchers("/actuator/**").permitAll()
                // TODO: Cambiar a .hasRole("ADMIN") cuando se configure correctamente Azure AD
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );

        return http.build();
    }
}
