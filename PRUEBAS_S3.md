# PRUEBAS S3 - Sistema de Gestión de Reservas de Eventos con Tickets

**Fecha**: 2025-12-01
**Ambiente**: Docker + AWS (RDS PostgreSQL + S3)

---

## 1. Infraestructura AWS Creada

### RDS PostgreSQL
- **Identificador**: tickets-db
- **Endpoint**: `tickets-db.cnciuumg6ft8.us-east-1.rds.amazonaws.com`
- **Motor**: PostgreSQL 15.14
- **Clase**: db.t3.micro
- **Base de datos**: ticketsdb

### Security Group
- **ID**: sg-06d7d9a070a188f61
- **Nombre**: rds-tickets-sg
- **Regla**: TCP 5432 desde 0.0.0.0/0

### S3 Bucket
- **Nombre**: microservicio-eventos-s3
- **Región**: us-east-1

---

## 2. Pruebas de Endpoints

### 2.1 Crear Evento

**Request:**
```bash
curl -X POST http://localhost:8081/api/eventos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre":"Concierto Rock 2025",
    "descripcion":"Gran concierto de rock nacional",
    "fechaEvento":"2025-03-15T20:00:00",
    "ubicacion":"Estadio Nacional, Santiago",
    "capacidadTotal":5000,
    "precioBase":45000
  }'
```

**Response:**
```json
{
  "id": 6,
  "nombre": "Concierto Rock 2025",
  "descripcion": "Gran concierto de rock nacional",
  "fechaEvento": "2025-03-15T20:00:00",
  "ubicacion": "Estadio Nacional, Santiago",
  "capacidadTotal": 5000,
  "precioBase": 45000
}
```

### 2.2 Listar Eventos

**Request:**
```bash
curl http://localhost:8081/api/eventos
```

**Response:**
```json
[
  {
    "id": 1,
    "nombre": "Concierto Rock Nacional",
    "descripcion": "Gran concierto de rock con las mejores bandas nacionales",
    "fechaEvento": "2025-03-15T20:00:00",
    "ubicacion": "Estadio Nacional, Santiago",
    "capacidadTotal": 5000,
    "precioBase": 45000.00
  },
  {
    "id": 2,
    "nombre": "Festival de Jazz",
    "descripcion": "Festival de jazz internacional con artistas de renombre",
    "fechaEvento": "2025-04-20T18:00:00",
    "ubicacion": "Teatro Caupolican, Santiago",
    "capacidadTotal": 2000,
    "precioBase": 35000.00
  },
  {
    "id": 3,
    "nombre": "Stand Up Comedy Night",
    "descripcion": "Noche de comedia con los mejores comediantes del pais",
    "fechaEvento": "2025-02-28T21:00:00",
    "ubicacion": "Club de Comedia, Providencia",
    "capacidadTotal": 300,
    "precioBase": 15000.00
  },
  {
    "id": 4,
    "nombre": "Feria Tecnologica 2025",
    "descripcion": "Exposicion de las ultimas innovaciones tecnologicas",
    "fechaEvento": "2025-05-10T10:00:00",
    "ubicacion": "Espacio Riesco, Santiago",
    "capacidadTotal": 10000,
    "precioBase": 5000.00
  },
  {
    "id": 5,
    "nombre": "Obra de Teatro: Hamlet",
    "descripcion": "Clasico de Shakespeare interpretado por actores nacionales",
    "fechaEvento": "2025-03-01T19:30:00",
    "ubicacion": "Teatro Municipal, Santiago",
    "capacidadTotal": 800,
    "precioBase": 25000.00
  },
  {
    "id": 6,
    "nombre": "Concierto Rock 2025",
    "descripcion": "Gran concierto de rock nacional",
    "fechaEvento": "2025-03-15T20:00:00",
    "ubicacion": "Estadio Nacional, Santiago",
    "capacidadTotal": 5000,
    "precioBase": 45000.00
  }
]
```

### 2.3 Generar Ticket (con PDF a S3)

**Request:**
```bash
curl -X POST http://localhost:8081/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "usuarioId": "USR001",
    "usuarioNombre": "Juan Perez"
  }'
```

**Response:**
```json
{
  "id": 3,
  "eventoId": 1,
  "eventoNombre": "Concierto Rock Nacional",
  "usuarioId": "USR001",
  "usuarioNombre": "Juan Perez",
  "codigoTicket": "TKT-1-USR0-71A55B79",
  "fechaCompra": "2025-12-01T01:09:40.624349725",
  "precio": 45000.00,
  "estado": "RESERVADO",
  "rutaArchivo": "1/USR001/TKT-1-USR0-71A55B79.pdf",
  "fechaModificacion": "2025-12-01T01:09:40.624366985"
}
```

### 2.4 Verificar PDF en S3

**Comando:**
```bash
aws s3 ls s3://microservicio-eventos-s3/ --recursive
```

**Output:**
```
2025-11-30 22:09:42       1703 1/USR001/TKT-1-USR0-71A55B79.pdf
2025-11-30 22:04:23         10 test.txt
```

**Estructura S3:**
```
microservicio-eventos-s3/
└── 1/                          # eventoId
    └── USR001/                 # usuarioId
        └── TKT-1-USR0-71A55B79.pdf  # codigoTicket.pdf
```

### 2.5 Descargar Ticket

**Request:**
```bash
curl http://localhost:8081/api/tickets/TKT-1-USR0-71A55B79/download -o ticket.pdf
```

**Verificación:**
```bash
$ file ticket.pdf
ticket.pdf: PDF document, version 1.7, 2 page(s) (zip deflate encoded)

$ ls -la ticket.pdf
-rw-r--r-- 1 franco franco 1703 Nov 30 22:10 ticket.pdf
```

### 2.6 Estadísticas por Evento

**Request:**
```bash
curl http://localhost:8081/api/tickets/estadisticas/evento/1
```

**Response:**
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

### 2.7 Listar Tickets por Usuario

**Request:**
```bash
curl http://localhost:8081/api/tickets/usuario/USR001
```

**Response:**
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

## 3. Logs del Contenedor

### Inicio exitoso con conexión a RDS:
```
2025-12-01T01:04:59.913Z  INFO  com.zaxxer.hikari.HikariDataSource : HikariPool-1 - Starting...
2025-12-01T01:05:01.301Z  INFO  com.zaxxer.hikari.pool.HikariPool : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@4a2dbcfc
2025-12-01T01:05:01.302Z  INFO  com.zaxxer.hikari.HikariDataSource : HikariPool-1 - Start completed.
2025-12-01T01:05:01.716Z  INFO  org.hibernate.orm.connections.pooling : HHH10001005: Database info:
    Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
    Database version: 15.14
```

### Creación de tablas:
```sql
Hibernate: create table eventos (id bigint generated by default as identity, capacidad_total integer not null, descripcion varchar(1000), fecha_evento timestamp(6) not null, nombre varchar(255) not null, precio_base numeric(10,2) not null, ubicacion varchar(255) not null, primary key (id))
Hibernate: create table tickets (id bigint generated by default as identity, codigo_ticket varchar(255) not null, estado varchar(255) not null check (estado in ('RESERVADO','CONFIRMADO','CANCELADO','USADO')), fecha_compra timestamp(6) not null, fecha_modificacion timestamp(6), precio numeric(10,2) not null, ruta_archivo varchar(255), usuario_id varchar(255) not null, usuario_nombre varchar(255) not null, evento_id bigint not null, primary key (id))
```

### Generación de ticket exitosa:
```
2025-12-01T01:09:40.XXX  INFO  c.d.e.m.service.TicketService : Generando ticket para evento 1 y usuario USR001
2025-12-01T01:09:40.XXX  INFO  c.d.e.m.service.TicketService : PDF guardado en EFS: /app/efs/temp/TKT-1-USR0-71A55B79.pdf
2025-12-01T01:09:41.XXX  INFO  c.d.e.m.service.TicketService : PDF subido a S3: 1/USR001/TKT-1-USR0-71A55B79.pdf
```

---

## 4. Comandos Docker Utilizados

### Construcción de imagen:
```bash
docker build -t microservicio-tickets:1.0 .
```

### Ejecución con variables de entorno:
```bash
docker run -d --name microservicio-test -p 8081:8080 \
  --env-file docker.env \
  microservicio-tickets:1.0
```

### Contenido de docker.env:
```
AWS_ACCESS_KEY_ID=ASIA6ODU54J2FQRPGE6J
AWS_SECRET_ACCESS_KEY=thVS8pZnLY11Ee7Wo0ZkJAjYFlapQipiTX+vWHFO
AWS_SESSION_TOKEN=IQoJb3JpZ2luX2VjECkaCXVzLXdlc3QtMiJH...
RDS_ENDPOINT=tickets-db.cnciuumg6ft8.us-east-1.rds.amazonaws.com
RDS_USERNAME=ticketsadmin
RDS_PASSWORD=Tickets2024Secure
```

---

## 5. Resumen de Funcionalidades Probadas

| Funcionalidad | Estado | Evidencia |
|---------------|--------|-----------|
| Conexión a RDS PostgreSQL | ✅ OK | HikariPool conectado a PostgreSQL 15.14 |
| Creación de tablas automática | ✅ OK | Hibernate DDL auto-update |
| Carga de datos de prueba | ✅ OK | 6 eventos cargados |
| Crear evento | ✅ OK | POST /api/eventos |
| Listar eventos | ✅ OK | GET /api/eventos |
| Generar ticket con PDF | ✅ OK | POST /api/tickets |
| Guardar PDF en EFS temporal | ✅ OK | /app/efs/temp/*.pdf |
| Subir PDF a S3 | ✅ OK | 1/USR001/TKT-1-USR0-71A55B79.pdf |
| Descargar ticket desde S3 | ✅ OK | GET /api/tickets/{codigo}/download |
| Estadísticas por evento | ✅ OK | GET /api/tickets/estadisticas/evento/{id} |
| Listar tickets por usuario | ✅ OK | GET /api/tickets/usuario/{id} |

---

## 6. Arquitectura Final

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Cliente       │────▶│  Docker         │────▶│  AWS RDS        │
│   (curl/web)    │     │  (Spring Boot)  │     │  PostgreSQL     │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
                                 │
                        ┌────────▼────────┐
                        │   AWS S3        │
                        │   (PDFs)        │
                        └─────────────────┘
```

---

**Conclusión**: Todas las funcionalidades del sistema de gestión de tickets fueron probadas exitosamente, incluyendo la integración completa con AWS RDS y S3.
