# Pruebas Semana 5 - Integración IDaaS y API Manager

**Fecha:** 2025-12-01
**Proyecto:** Microservicio de Eventos
**Curso:** Desarrollo Cloud Native (CDY2204)

---

## Configuración Implementada

### Spring Security con OAuth2 Resource Server

**Archivos modificados/creados:**
- `pom.xml` - Agregadas dependencias de Spring Security
- `src/main/java/cl/duoc/ejemplo/microservicio/config/SecurityConfig.java` - Configuración de seguridad
- `src/main/resources/application.properties` - Issuer URI de Azure AD B2C

**Dependencias agregadas:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

**Configuración SecurityConfig.java:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```

**Issuer URI configurado:**
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/4a280743-d50e-4879-adcc-dd240118a204/v2.0
```

---

## Configuración Azure AD B2C

| Campo | Valor |
|-------|-------|
| Tenant | duocboleta.onmicrosoft.com |
| Tenant ID | 4a280743-d50e-4879-adcc-dd240118a204 |
| Client ID | dca1180a-001c-4f7a-91c0-10e21067beac |
| Issuer URL | https://login.microsoftonline.com/4a280743-d50e-4879-adcc-dd240118a204/v2.0 |

---

## Configuración AWS API Gateway

| Campo | Valor |
|-------|-------|
| API Name | duoc_boleta_api |
| API ID | 6g6wibkvxg |
| Stage | DEV |
| URL Base | https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV |
| Authorizer | JWT (Azure AD B2C) |
| EC2 Backend | 44.196.124.203:8080 |

---

## Pruebas de Autenticación

### Test 1: EC2 Directo SIN Token

**Request:**
```bash
curl http://44.196.124.203:8080/api/eventos
```

**Response:**
```
HTTP Status: 401 Unauthorized
```

**Resultado:** PASSED - Spring Security bloquea acceso sin token

---

### Test 2: EC2 Directo CON Token

**Request:**
```bash
curl -H "Authorization: Bearer {JWT_TOKEN}" http://44.196.124.203:8080/api/eventos
```

**Response:**
```json
HTTP Status: 200 OK

[
  {"id":1,"nombre":"Concierto Rock Nacional","descripcion":"Gran concierto de rock con las mejores bandas nacionales","fechaEvento":"2025-03-15T20:00:00","ubicacion":"Estadio Nacional, Santiago","capacidadTotal":5000,"precioBase":45000.00},
  {"id":2,"nombre":"Festival de Jazz","descripcion":"Festival de jazz internacional con artistas de renombre","fechaEvento":"2025-04-20T18:00:00","ubicacion":"Teatro Caupolican, Santiago","capacidadTotal":2000,"precioBase":35000.00},
  {"id":3,"nombre":"Stand Up Comedy Night","descripcion":"Noche de comedia con los mejores comediantes del pais","fechaEvento":"2025-02-28T21:00:00","ubicacion":"Club de Comedia, Providencia","capacidadTotal":300,"precioBase":15000.00},
  {"id":4,"nombre":"Feria Tecnologica 2025","descripcion":"Exposicion de las ultimas innovaciones tecnologicas","fechaEvento":"2025-05-10T10:00:00","ubicacion":"Espacio Riesco, Santiago","capacidadTotal":10000,"precioBase":5000.00},
  {"id":5,"nombre":"Obra de Teatro: Hamlet","descripcion":"Clasico de Shakespeare interpretado por actores nacionales","fechaEvento":"2025-03-01T19:30:00","ubicacion":"Teatro Municipal, Santiago","capacidadTotal":800,"precioBase":25000.00},
  {"id":6,"nombre":"Concierto Rock 2025","descripcion":"Gran concierto de rock nacional","fechaEvento":"2025-03-15T20:00:00","ubicacion":"Estadio Nacional, Santiago","capacidadTotal":5000,"precioBase":45000.00}
]
```

**Resultado:** PASSED - Spring Security valida JWT y permite acceso

---

### Test 3: API Gateway SIN Token

**Request:**
```bash
curl https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/eventos
```

**Response:**
```json
HTTP Status: 401 Unauthorized

{"message":"Unauthorized"}
```

**Resultado:** PASSED - API Gateway bloquea acceso sin token

---

### Test 4: API Gateway CON Token

**Request:**
```bash
curl -H "Authorization: Bearer {JWT_TOKEN}" https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/eventos
```

**Response:**
```json
HTTP Status: 200 OK

[
  {"id":1,"nombre":"Concierto Rock Nacional",...},
  {"id":2,"nombre":"Festival de Jazz",...},
  ...
]
```

**Resultado:** PASSED - Doble validación (API Gateway + Spring Security) exitosa

---

### Test 5: GET /api/eventos/{id}

**Request:**
```bash
curl -H "Authorization: Bearer {JWT_TOKEN}" https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/eventos/1
```

**Response:**
```json
HTTP Status: 200 OK

{"id":1,"nombre":"Concierto Rock Nacional","descripcion":"Gran concierto de rock con las mejores bandas nacionales","fechaEvento":"2025-03-15T20:00:00","ubicacion":"Estadio Nacional, Santiago","capacidadTotal":5000,"precioBase":45000.00}
```

**Resultado:** PASSED

---

### Test 6: GET /api/tickets/evento/{eventoId}

**Request:**
```bash
curl -H "Authorization: Bearer {JWT_TOKEN}" https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/tickets/evento/1
```

**Response:**
```json
HTTP Status: 200 OK

[{"id":3,"eventoId":1,"eventoNombre":"Concierto Rock Nacional","usuarioId":"USR001","usuarioNombre":"Juan Perez","codigoTicket":"TKT-1-USR0-71A55B79","fechaCompra":"2025-12-01T01:09:40.62435","precio":45000.00,"estado":"RESERVADO","rutaArchivo":"1/USR001/TKT-1-USR0-71A55B79.pdf","fechaModificacion":"2025-12-01T01:09:41.649525"}]
```

**Resultado:** PASSED

---

### Test 7: GET /api/tickets/estadisticas/evento/{eventoId}

**Request:**
```bash
curl -H "Authorization: Bearer {JWT_TOKEN}" https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/tickets/estadisticas/evento/1
```

**Response:**
```json
HTTP Status: 200 OK

{"eventoId":1,"nombreEvento":"Concierto Rock Nacional","totalTickets":1,"ticketsConfirmados":0,"ticketsReservados":1,"ticketsCancelados":0,"ticketsUsados":0,"ingresoTotal":0,"capacidadTotal":5000,"capacidadDisponible":4999}
```

**Resultado:** PASSED

---

### Test 8: GET /s3/{bucket}/objects

**Request:**
```bash
curl -H "Authorization: Bearer {JWT_TOKEN}" https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/s3/microservicio-eventos-s3/objects
```

**Response:**
```json
HTTP Status: 200 OK

[
  {"key":"1/USR001/TKT-1-USR0-71A55B79.pdf","size":1703,"lastModified":"2025-12-01T01:09:42Z"},
  {"key":"2/USR002/TKT-2-USR0-6309242A.pdf","size":1706,"lastModified":"2025-12-01T08:19:52Z"},
  {"key":"2/USR_API/TKT-2-USR_-2EC58F98.pdf","size":1715,"lastModified":"2025-12-01T08:51:38Z"},
  {"key":"3/USR003/TKT-3-USR0-754669CD.pdf","size":1712,"lastModified":"2025-12-01T08:24:32Z"},
  {"key":"test.txt","size":10,"lastModified":"2025-12-01T01:04:23Z"}
]
```

**Resultado:** PASSED

---

## Resumen de Resultados

| Test | Endpoint | Sin Token | Con Token | Estado |
|------|----------|-----------|-----------|--------|
| 1 | EC2 /api/eventos | 401 | - | PASSED |
| 2 | EC2 /api/eventos | - | 200 | PASSED |
| 3 | API GW /api/eventos | 401 | - | PASSED |
| 4 | API GW /api/eventos | - | 200 | PASSED |
| 5 | API GW /api/eventos/1 | - | 200 | PASSED |
| 6 | API GW /api/tickets/evento/1 | - | 200 | PASSED |
| 7 | API GW /api/tickets/estadisticas/evento/1 | - | 200 | PASSED |
| 8 | API GW /s3/{bucket}/objects | - | 200 | PASSED |

**Total: 8/8 pruebas exitosas**

---

## Arquitectura de Seguridad Implementada

```
Cliente (Postman)
    │
    ├─ Obtiene JWT de Azure AD B2C (OAuth 2.0 Client Credentials)
    │
    ▼
AWS API Gateway (Primera capa de seguridad)
    │
    ├─ Valida JWT con Authorizer configurado
    ├─ Issuer: https://login.microsoftonline.com/{tenant-id}/v2.0
    ├─ Audience: dca1180a-001c-4f7a-91c0-10e21067beac
    │
    ▼
EC2 + Spring Boot (Segunda capa de seguridad)
    │
    ├─ Spring Security OAuth2 Resource Server
    ├─ Valida JWT usando issuer-uri
    ├─ Si válido → Procesa request
    ├─ Si inválido → 401 Unauthorized
    │
    ▼
Respuesta al Cliente
```

---

## Conclusión

La integración de Spring Security con OAuth2 Resource Server y Azure AD B2C fue exitosa. El sistema implementa doble autenticación:

1. **API Gateway (AWS):** Valida el JWT antes de reenviar al backend
2. **Spring Security (Backend):** Valida nuevamente el JWT antes de procesar la solicitud

Todos los endpoints del microservicio están protegidos y requieren un token JWT válido emitido por Azure AD B2C para acceder a los recursos.
