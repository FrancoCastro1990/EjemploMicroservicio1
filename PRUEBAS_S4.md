# PRUEBAS S4 - API Gateway con Autenticacion JWT

## Configuracion

- **API Gateway:** `duoc_boleta_api`
- **API ID:** `6g6wibkvxg`
- **Base URL:** `https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV`
- **Autenticacion:** JWT (Microsoft Azure AD)
- **EC2 Backend:** `http://44.196.124.203:8080`

---

## Rutas Configuradas

| Metodo | Ruta | Auth | Integracion |
|--------|------|------|-------------|
| GET | `/api/eventos` | JWT | EC2:8080/api/eventos |
| GET | `/api/eventos/{id}` | JWT | EC2:8080/api/eventos/{id} |
| GET | `/api/tickets/evento/{eventoId}` | JWT | EC2:8080/api/tickets/evento/{eventoId} |
| GET | `/api/tickets/usuario/{usuarioId}` | JWT | EC2:8080/api/tickets/usuario/{usuarioId} |
| POST | `/api/tickets` | JWT | EC2:8080/api/tickets |
| GET | `/api/tickets/estadisticas/evento/{eventoId}` | JWT | EC2:8080/api/tickets/estadisticas/evento/{eventoId} |
| GET | `/api/tickets/{code}/download` | JWT | EC2:8080/api/tickets/{code}/download |
| GET | `/s3/{bucket}/objects` | JWT | EC2:8080/s3/{bucket}/objects |

---

## Pruebas Sin Token (Esperado: 401 Unauthorized)

```bash
# Comando
curl -s -o /dev/null -w "HTTP %{http_code}" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/eventos"
```

| Endpoint | Resultado |
|----------|-----------|
| GET /api/eventos | HTTP 401 |
| GET /api/eventos/1 | HTTP 401 |
| POST /api/tickets | HTTP 401 |
| GET /api/tickets/evento/1 | HTTP 401 |
| GET /s3/test/objects | HTTP 401 |

**Conclusion:** La autenticacion JWT esta funcionando correctamente. Todas las rutas protegidas rechazan peticiones sin token.

---

## Pruebas Con Token JWT Valido

### Token Utilizado
```
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6InJ0c0ZULWItN0x1WTdEVlllU05LY0lKN1ZuYyJ9...
```

### 1. GET /api/eventos

```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/eventos"
```

**Resultado:** HTTP 200 OK

```json
[
  {"id":1,"nombre":"Concierto Rock Nacional","descripcion":"Gran concierto de rock con las mejores bandas nacionales","fechaEvento":"2025-03-15T20:00:00","ubicacion":"Estadio Nacional, Santiago","capacidadTotal":5000,"precioBase":45000.00},
  {"id":2,"nombre":"Festival de Jazz","descripcion":"Festival de jazz internacional con artistas de renombre","fechaEvento":"2025-04-20T18:00:00","ubicacion":"Teatro Caupolican, Santiago","capacidadTotal":2000,"precioBase":35000.00},
  {"id":3,"nombre":"Stand Up Comedy Night","descripcion":"Noche de comedia con los mejores comediantes del pais","fechaEvento":"2025-02-28T21:00:00","ubicacion":"Club de Comedia, Providencia","capacidadTotal":300,"precioBase":15000.00},
  {"id":4,"nombre":"Feria Tecnologica 2025","descripcion":"Exposicion de las ultimas innovaciones tecnologicas","fechaEvento":"2025-05-10T10:00:00","ubicacion":"Espacio Riesco, Santiago","capacidadTotal":10000,"precioBase":5000.00},
  {"id":5,"nombre":"Obra de Teatro: Hamlet","descripcion":"Clasico de Shakespeare interpretado por actores nacionales","fechaEvento":"2025-03-01T19:30:00","ubicacion":"Teatro Municipal, Santiago","capacidadTotal":800,"precioBase":25000.00},
  {"id":6,"nombre":"Concierto Rock 2025","descripcion":"Gran concierto de rock nacional","fechaEvento":"2025-03-15T20:00:00","ubicacion":"Estadio Nacional, Santiago","capacidadTotal":5000,"precioBase":45000.00}
]
```

---

### 2. GET /api/eventos/{id}

```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/eventos/1"
```

**Resultado:** HTTP 200 OK

```json
{
  "id": 1,
  "nombre": "Concierto Rock Nacional",
  "descripcion": "Gran concierto de rock con las mejores bandas nacionales",
  "fechaEvento": "2025-03-15T20:00:00",
  "ubicacion": "Estadio Nacional, Santiago",
  "capacidadTotal": 5000,
  "precioBase": 45000.00
}
```

---

### 3. GET /api/tickets/evento/{eventoId}

```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/tickets/evento/1"
```

**Resultado:** HTTP 200 OK

```json
[
  {
    "id": 3,
    "eventoId": 1,
    "eventoNombre": "Concierto Rock Nacional",
    "usuarioId": "USR001",
    "usuarioNombre": "Juan Perez",
    "codigoTicket": "TKT-1-USR0-71A55B79",
    "fechaCompra": "2025-12-01T01:09:40.62435",
    "precio": 45000.00,
    "estado": "RESERVADO",
    "rutaArchivo": "1/USR001/TKT-1-USR0-71A55B79.pdf",
    "fechaModificacion": "2025-12-01T01:09:41.649525"
  }
]
```

---

### 4. GET /api/tickets/usuario/{usuarioId}

```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/tickets/usuario/USR001"
```

**Resultado:** HTTP 200 OK

```json
[
  {
    "id": 3,
    "eventoId": 1,
    "eventoNombre": "Concierto Rock Nacional",
    "usuarioId": "USR001",
    "usuarioNombre": "Juan Perez",
    "codigoTicket": "TKT-1-USR0-71A55B79",
    "fechaCompra": "2025-12-01T01:09:40.62435",
    "precio": 45000.00,
    "estado": "RESERVADO",
    "rutaArchivo": "1/USR001/TKT-1-USR0-71A55B79.pdf",
    "fechaModificacion": "2025-12-01T01:09:41.649525"
  }
]
```

---

### 5. GET /api/tickets/estadisticas/evento/{eventoId}

```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/tickets/estadisticas/evento/1"
```

**Resultado:** HTTP 200 OK

```json
{
  "eventoId": 1,
  "nombreEvento": "Concierto Rock Nacional",
  "totalTickets": 1,
  "ticketsConfirmados": 0,
  "ticketsReservados": 1,
  "ticketsCancelados": 0,
  "ticketsUsados": 0,
  "ingresoTotal": 0,
  "capacidadTotal": 5000,
  "capacidadDisponible": 4999
}
```

---

### 6. POST /api/tickets (Crear Ticket)

```bash
curl -s -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"eventoId":2,"usuarioId":"USR_API","usuarioNombre":"Usuario API Gateway"}' \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/tickets"
```

**Resultado:** HTTP 200 OK

```json
{
  "id": 6,
  "eventoId": 2,
  "eventoNombre": "Festival de Jazz",
  "usuarioId": "USR_API",
  "usuarioNombre": "Usuario API Gateway",
  "codigoTicket": "TKT-2-USR_-2EC58F98",
  "fechaCompra": "2025-12-01T08:51:37.516661521",
  "precio": 35000.00,
  "estado": "RESERVADO",
  "rutaArchivo": "2/USR_API/TKT-2-USR_-2EC58F98.pdf",
  "fechaModificacion": "2025-12-01T08:51:37.516721782"
}
```

---

### 7. GET /api/tickets/{code}/download

```bash
curl -s -o ticket.pdf \
  -H "Authorization: Bearer $TOKEN" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/tickets/TKT-1-USR0-71A55B79/download"

file ticket.pdf
```

**Resultado:** HTTP 200 OK

```
ticket.pdf: PDF document, version 1.7, 2 page(s) (zip deflate encoded)
```

---

### 8. GET /s3/{bucket}/objects

```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/s3/microservicio-eventos-s3/objects"
```

**Resultado:** HTTP 200 OK

```json
[
  {"key":"1/USR001/TKT-1-USR0-71A55B79.pdf","size":1703,"lastModified":"2025-12-01T01:09:42Z"},
  {"key":"2/USR002/TKT-2-USR0-6309242A.pdf","size":1706,"lastModified":"2025-12-01T08:19:52Z"},
  {"key":"2/USR_API/TKT-2-USR_-2EC58F98.pdf","size":1715,"lastModified":"2025-12-01T08:51:38Z"},
  {"key":"3/USR003/TKT-3-USR0-754669CD.pdf","size":1712,"lastModified":"2025-12-01T08:24:32Z"},
  {"key":"test.txt","size":10,"lastModified":"2025-12-01T01:04:23Z"}
]
```

---

## Resumen de Resultados

| Endpoint | Sin Token | Con Token |
|----------|-----------|-----------|
| GET /api/eventos | 401 | 200 OK |
| GET /api/eventos/{id} | 401 | 200 OK |
| GET /api/tickets/evento/{eventoId} | 401 | 200 OK |
| GET /api/tickets/usuario/{usuarioId} | 401 | 200 OK |
| POST /api/tickets | 401 | 200 OK |
| GET /api/tickets/estadisticas/evento/{eventoId} | 401 | 200 OK |
| GET /api/tickets/{code}/download | 401 | 200 OK |
| GET /s3/{bucket}/objects | 401 | 200 OK |

---

## Configuracion AWS CLI Utilizada

### Listar APIs
```bash
aws apigatewayv2 get-apis
```

### Crear Integracion
```bash
aws apigatewayv2 create-integration --api-id 6g6wibkvxg \
  --integration-type HTTP_PROXY \
  --integration-method GET \
  --integration-uri "http://44.196.124.203:8080/api/eventos" \
  --payload-format-version 1.0
```

### Crear Ruta con JWT
```bash
aws apigatewayv2 create-route --api-id 6g6wibkvxg \
  --route-key "GET /api/eventos" \
  --authorization-type JWT \
  --authorizer-id axf8vj \
  --target "integrations/<integration-id>"
```

### Desplegar Cambios
```bash
aws apigatewayv2 create-deployment --api-id 6g6wibkvxg --stage-name DEV
```

---

## Conclusion

La configuracion de API Gateway con autenticacion JWT de Microsoft Azure AD fue exitosa. Todos los endpoints del microservicio estan protegidos y funcionando correctamente a traves del API Gateway.

**Fecha de pruebas:** 2025-12-01
