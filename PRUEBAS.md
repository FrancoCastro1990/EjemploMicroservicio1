# PRUEBAS.md

Documentación de pruebas realizadas sobre la aplicación desplegada en EC2.

**Fecha:** 2025-11-30
**Servidor:** 44.196.124.203:8080
**Bucket S3:** microservicio-s3-bucket

---

## 1. Endpoints de Prueba (/microservicio)

### 1.1 GET /microservicio/{id}

```bash
curl http://44.196.124.203:8080/microservicio/1
```

**Respuesta:**
```
Integración OK - GET, recibido path variable: 1
HTTP Status: 200
```

### 1.2 POST /microservicio

```bash
curl -X POST http://44.196.124.203:8080/microservicio \
  -H "Content-Type: application/json" \
  -d '{"mensaje": "test desde claude"}'
```

**Respuesta:**
```
Integración OK - POST, recibido body: {mensaje=test desde claude}
HTTP Status: 200
```

### 1.3 PUT /microservicio?status=

```bash
curl -X PUT "http://44.196.124.203:8080/microservicio?status=OK"
```

**Respuesta:**
```
Integración OK - PUT, recibido query param: OK
HTTP Status: 200
```

### 1.4 DELETE /microservicio

```bash
curl -X DELETE http://44.196.124.203:8080/microservicio \
  -H "Authorization: mi-token-secreto"
```

**Respuesta:**
```
Integración OK - DELETE, recibido header: mi-token-secreto
HTTP Status: 200
```

---

## 2. Endpoints de AWS S3 (/s3)

### 2.1 Listar objetos (bucket vacío inicialmente)

```bash
curl http://44.196.124.203:8080/s3/microservicio-s3-bucket/objects
```

**Respuesta:**
```json
[]
HTTP Status: 200
```

### 2.2 Subir archivo

```bash
curl -X POST "http://44.196.124.203:8080/s3/microservicio-s3-bucket/object?key=test-file.txt" \
  -F "file=@test-file.txt"
```

**Contenido del archivo:**
```
Archivo de prueba creado por Claude - Sun Nov 30 07:02:39 PM -03 2025
```

**Respuesta:**
```
HTTP Status: 200
```

### 2.3 Verificar upload (listar objetos)

```bash
curl http://44.196.124.203:8080/s3/microservicio-s3-bucket/objects
```

**Respuesta:**
```json
[{"key":"test-file.txt","size":70,"lastModified":"2025-11-30T22:02:41Z"}]
HTTP Status: 200
```

### 2.4 Descargar archivo

```bash
curl "http://44.196.124.203:8080/s3/microservicio-s3-bucket/object?key=test-file.txt"
```

**Respuesta:**
```
Archivo de prueba creado por Claude - Sun Nov 30 07:02:39 PM -03 2025
HTTP Status: 200
```

### 2.5 Mover archivo

```bash
curl -X POST "http://44.196.124.203:8080/s3/microservicio-s3-bucket/move?sourceKey=test-file.txt&destKey=carpeta/test-file-moved.txt"
```

**Respuesta:**
```
HTTP Status: 200
```

### 2.6 Verificar movimiento

```bash
curl http://44.196.124.203:8080/s3/microservicio-s3-bucket/objects
```

**Respuesta:**
```json
[{"key":"carpeta/test-file-moved.txt","size":70,"lastModified":"2025-11-30T22:02:54Z"}]
HTTP Status: 200
```

### 2.7 Eliminar archivo

```bash
curl -X DELETE "http://44.196.124.203:8080/s3/microservicio-s3-bucket/object?key=carpeta/test-file-moved.txt"
```

**Respuesta:**
```
HTTP Status: 204
```

### 2.8 Verificar eliminación

```bash
curl http://44.196.124.203:8080/s3/microservicio-s3-bucket/objects
```

**Respuesta:**
```json
[]
HTTP Status: 200
```

---

## 3. Resumen de Resultados

| Endpoint | Método | HTTP Status | Estado |
|----------|--------|-------------|--------|
| `/microservicio/1` | GET | 200 | OK |
| `/microservicio` | POST | 200 | OK |
| `/microservicio?status=OK` | PUT | 200 | OK |
| `/microservicio` | DELETE | 200 | OK |
| `/s3/{bucket}/objects` | GET | 200 | OK |
| `/s3/{bucket}/object?key=` | POST | 200 | OK |
| `/s3/{bucket}/object?key=` | GET | 200 | OK |
| `/s3/{bucket}/move` | POST | 200 | OK |
| `/s3/{bucket}/object?key=` | DELETE | 204 | OK |

**Conclusión:** Todas las pruebas pasaron exitosamente. La aplicación está completamente funcional en el servidor EC2.
