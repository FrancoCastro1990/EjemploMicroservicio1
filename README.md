# EjemploMicroservicio1 - 3

Este es un microservicio de ejemplo que expone cuatro endpoints (`GET`, `POST`, `PUT` y `DELETE`). Fue creado para probar integraciones y no utiliza una base de datos: todas las respuestas están en duro (hardcoded).

---

## Endpoints disponibles

### GET `/microservicio/1`

Obtiene información básica de prueba.

**Ejemplo con `curl`:**

```bash
curl --location 'http://localhost:8080/microservicio/1'
```

---

### POST `/microservicio`

Envía un mensaje para simular una integración.

**Ejemplo con `curl`:**

```bash
curl --location 'http://localhost:8080/microservicio' \
  --header 'Content-Type: application/json' \
  --data '{
    "mensaje": "integración ok"
}'
```

---

### PUT `/microservicio?status=OK`

Actualiza un estado simulado en el microservicio.

**Ejemplo con `curl`:**

```bash
curl --location --request PUT 'http://localhost:8080/microservicio?status=OK'
```

---

### DELETE `/microservicio`

Simula la eliminación de un recurso. Requiere header de autorización.

**Ejemplo con `curl`:**

```bash
curl --location --request DELETE 'http://localhost:8080/microservicio' \
  --header 'Authorization: hola mundo'
```

---

## Notas

- Todos los endpoints responden con datos estáticos (hardcoded).
- Este microservicio no realiza operaciones reales sobre bases de datos o sistemas externos.
- Ideal para probar pipelines de integración y despliegue.
