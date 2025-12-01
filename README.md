# Microservicio de Tickets para Evento

Microservicio Spring Boot para gestionar tickets de eventos con integracion AWS (S3, RDS PostgreSQL, EFS).

## Tecnologias

- **Java 21** / **Spring Boot 3.4.1**
- **Spring Cloud AWS 3.3.1**
- **Spring Data JPA** / **Hibernate**
- **PostgreSQL** (AWS RDS)
- **AWS S3** (almacenamiento de PDFs)
- **AWS EFS** (almacenamiento temporal)
- **iText 7** (generacion de PDFs)
- **Docker**

---

## Arquitectura

```
                    +------------------+
                    |   API Gateway    |
                    |   (JWT Auth)     |
                    +--------+---------+
                             |
                    +--------v---------+
                    |      EC2         |
                    |  Docker + App    |
                    +--------+---------+
                             |
         +-------------------+-------------------+
         |                   |                   |
+--------v-------+  +--------v-------+  +--------v-------+
|    AWS S3      |  |   AWS RDS      |  |    AWS EFS     |
|  (PDFs tickets)|  |  (PostgreSQL)  |  |  (temp files)  |
+----------------+  +----------------+  +----------------+
```

---

## API Endpoints

### Eventos

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/eventos` | Listar todos los eventos |
| GET | `/api/eventos/{id}` | Obtener evento por ID |

### Tickets

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/tickets` | Crear nuevo ticket |
| GET | `/api/tickets/evento/{eventoId}` | Listar tickets por evento |
| GET | `/api/tickets/usuario/{usuarioId}` | Listar tickets por usuario |
| GET | `/api/tickets/{codigoTicket}/download` | Descargar PDF del ticket |
| GET | `/api/tickets/estadisticas/evento/{eventoId}` | Estadisticas del evento |
| PUT | `/api/tickets/{ticketId}` | Modificar ticket |
| DELETE | `/api/tickets/{ticketId}` | Eliminar ticket |

### S3

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/s3/{bucket}/objects` | Listar objetos en bucket |
| GET | `/s3/{bucket}/object?key=` | Descargar objeto |
| POST | `/s3/{bucket}/object?key=` | Subir objeto |
| DELETE | `/s3/{bucket}/object?key=` | Eliminar objeto |

---

## Configuracion

### Variables de Entorno

| Variable | Descripcion | Default |
|----------|-------------|---------|
| `AWS_ACCESS_KEY_ID` | AWS Access Key | - |
| `AWS_SECRET_ACCESS_KEY` | AWS Secret Key | - |
| `AWS_SESSION_TOKEN` | AWS Session Token | - |
| `RDS_ENDPOINT` | Endpoint PostgreSQL RDS | localhost |
| `RDS_USERNAME` | Usuario de base de datos | postgres |
| `RDS_PASSWORD` | Password de base de datos | postgres |

### application.properties

```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://${RDS_ENDPOINT:localhost}:5432/ticketsdb
spring.datasource.username=${RDS_USERNAME:postgres}
spring.datasource.password=${RDS_PASSWORD:postgres}
spring.cloud.aws.region.static=us-east-1
efs.path=/app/efs
```

---

## Ejecucion Local

### Requisitos
- Java 21
- Maven
- PostgreSQL (o Docker)

### Comandos

```bash
# Compilar
mvn clean package

# Ejecutar
mvn spring-boot:run

# Ejecutar tests
mvn test
```

---

## Docker

### Build

```bash
docker build -t microservicio:1.0 .
```

### Run

```bash
docker run -d -p 8080:8080 \
  -v /mnt/efs:/app/efs \
  -e AWS_ACCESS_KEY_ID=<access_key> \
  -e AWS_SECRET_ACCESS_KEY=<secret_key> \
  -e AWS_SESSION_TOKEN=<session_token> \
  -e RDS_ENDPOINT=<rds_endpoint> \
  -e RDS_USERNAME=<username> \
  -e RDS_PASSWORD=<password> \
  microservicio:1.0
```

---

## API Gateway

El microservicio esta expuesto a traves de AWS API Gateway con autenticacion JWT.

**Base URL:** `https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV`

### Autenticacion

Todas las rutas requieren token JWT en el header:

```bash
curl -H "Authorization: Bearer <token>" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/eventos"
```

### Rutas Disponibles

| Metodo | Ruta | Auth |
|--------|------|------|
| GET | `/api/eventos` | JWT |
| GET | `/api/eventos/{id}` | JWT |
| GET | `/api/tickets/evento/{eventoId}` | JWT |
| GET | `/api/tickets/usuario/{usuarioId}` | JWT |
| POST | `/api/tickets` | JWT |
| GET | `/api/tickets/estadisticas/evento/{eventoId}` | JWT |
| GET | `/api/tickets/{code}/download` | JWT |
| GET | `/s3/{bucket}/objects` | JWT |

---

## Ejemplos de Uso

### Listar Eventos

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/eventos"
```

### Crear Ticket

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"eventoId":1,"usuarioId":"USR001","usuarioNombre":"Juan Perez"}' \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/tickets"
```

### Descargar Ticket PDF

```bash
curl -o ticket.pdf \
  -H "Authorization: Bearer $TOKEN" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/tickets/TKT-1-USR0-71A55B79/download"
```

### Estadisticas de Evento

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "https://6g6wibkvxg.execute-api.us-east-1.amazonaws.com/DEV/api/tickets/estadisticas/evento/1"
```

---

## CI/CD

El proyecto utiliza GitHub Actions para CI/CD:

1. Build y push de imagen Docker a Docker Hub
2. Deploy automatico a EC2 via SSH

### Secrets Requeridos

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_SESSION_TOKEN`
- `EC2_HOST`
- `EC2_SSH_KEY`
- `USER_SERVER`
- `RDS_ENDPOINT`
- `RDS_USERNAME`
- `RDS_PASSWORD`

---

## Estructura del Proyecto

```
src/main/java/cl/duoc/ejemplo/microservicio/
├── MicroservicioApplication.java
├── controller/
│   ├── EventoController.java
│   ├── TicketController.java
│   └── AwsS3Controller.java
├── service/
│   ├── TicketService.java
│   ├── PdfGeneratorService.java
│   ├── EfsService.java
│   └── AwsS3Service.java
├── entity/
│   ├── Evento.java
│   ├── Ticket.java
│   └── EstadoTicket.java
├── repository/
│   ├── EventoRepository.java
│   └── TicketRepository.java
└── dto/
    ├── TicketDto.java
    ├── TicketCreateRequest.java
    └── EventoEstadisticasDto.java
```

---

## Licencia

Proyecto academico - DUOC UC
