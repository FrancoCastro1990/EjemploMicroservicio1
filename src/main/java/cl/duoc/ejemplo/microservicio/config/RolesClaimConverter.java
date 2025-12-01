package cl.duoc.ejemplo.microservicio.config;

import java.util.Collection;
import java.util.Collections;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * TODO: Esta clase está temporalmente sin usar.
 *
 * Converter que extrae el claim personalizado 'extension_Roles' del JWT
 * y lo convierte en GrantedAuthority para Spring Security.
 *
 * Solo acepta el rol "ADMIN". Si el claim no es "ADMIN", retorna lista vacía.
 *
 * NOTA: La configuración de roles en Azure AD no está funcionando correctamente.
 * Esta clase se reactivará en SecurityConfig cuando se resuelva la configuración
 * del claim 'extension_Roles' en Azure AD B2C.
 *
 * @see SecurityConfig#filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity)
 */
public class RolesClaimConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLES_CLAIM = "extension_Roles";
    private static final String REQUIRED_ROLE = "ADMIN";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String role = jwt.getClaimAsString(ROLES_CLAIM);

        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }

        if (!REQUIRED_ROLE.equalsIgnoreCase(role.trim())) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
