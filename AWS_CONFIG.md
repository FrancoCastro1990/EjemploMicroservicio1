# AWS_CONFIG.md

Guía paso a paso para configurar la infraestructura AWS necesaria para el microservicio de eventos y tickets.

**Entorno:** Desarrollo/Testing
**Región:** us-east-1
**Método:** AWS CLI

---

## 1. Prerrequisitos

### 1.1 Instalar AWS CLI

AWS CLI es la herramienta de línea de comandos que permite interactuar con todos los servicios de AWS.

**Linux:**
```bash
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

**macOS:**
```bash
curl "https://awscli.amazonaws.com/AWSCLIV2.pkg" -o "AWSCLIV2.pkg"
sudo installer -pkg AWSCLIV2.pkg -target /
```

**Verificar instalación:**
```bash
aws --version
```

### 1.2 Configurar Credenciales

Configura tus credenciales de AWS. Necesitarás Access Key ID y Secret Access Key de tu cuenta AWS.

```bash
aws configure
```

Te pedirá:
- AWS Access Key ID: Tu clave de acceso
- AWS Secret Access Key: Tu clave secreta
- Default region name: `us-east-1`
- Default output format: `json`

### 1.3 Verificar Acceso

Confirma que tienes acceso correctamente configurado:

```bash
aws sts get-caller-identity
```

Deberías ver tu Account ID y ARN de usuario.

---

## 2. Configuración de IAM

IAM (Identity and Access Management) gestiona los permisos de acceso a los servicios AWS. Crearemos un usuario específico para este proyecto.

### 2.1 Crear Política de Permisos

Esta política otorga los permisos mínimos necesarios para S3, RDS y EFS:

```bash
aws iam create-policy \
  --policy-name MicroservicioEventosPolicy \
  --policy-document '{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "S3Access",
        "Effect": "Allow",
        "Action": [
          "s3:ListBucket",
          "s3:GetBucketLocation",
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:CopyObject"
        ],
        "Resource": [
          "arn:aws:s3:::microservicio-eventos-s3",
          "arn:aws:s3:::microservicio-eventos-s3/*"
        ]
      },
      {
        "Sid": "EFSAccess",
        "Effect": "Allow",
        "Action": [
          "elasticfilesystem:DescribeFileSystems",
          "elasticfilesystem:DescribeMountTargets"
        ],
        "Resource": "*"
      }
    ]
  }'
```

### 2.2 Crear Usuario IAM

Crea un usuario programático para el microservicio:

```bash
aws iam create-user --user-name microservicio-eventos-user
```

### 2.3 Asociar Política al Usuario

Reemplaza `ACCOUNT_ID` con tu ID de cuenta AWS:

```bash
aws iam attach-user-policy \
  --user-name microservicio-eventos-user \
  --policy-arn arn:aws:iam::ACCOUNT_ID:policy/MicroservicioEventosPolicy
```

### 2.4 Generar Access Keys

Genera las credenciales para el usuario:

```bash
aws iam create-access-key --user-name microservicio-eventos-user
```

**Guarda el `AccessKeyId` y `SecretAccessKey`** - los necesitarás para las variables de entorno.

---

## 3. Crear Bucket S3

S3 (Simple Storage Service) almacenará los PDFs de los tickets generados por el microservicio.

### 3.1 Crear el Bucket

```bash
aws s3 mb s3://microservicio-eventos-s3 --region us-east-1
```

> **Nota:** Los nombres de bucket deben ser únicos globalmente. Si el nombre ya existe, agrega un sufijo único (ej: `microservicio-eventos-s3-tuusuario`).

### 3.2 Verificar Creación

```bash
aws s3 ls
```

Deberías ver `microservicio-eventos-s3` en la lista.

### 3.3 Configurar CORS (Opcional)

Si necesitas acceso desde navegadores web:

```bash
aws s3api put-bucket-cors \
  --bucket microservicio-eventos-s3 \
  --cors-configuration '{
    "CORSRules": [{
      "AllowedHeaders": ["*"],
      "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
      "AllowedOrigins": ["*"],
      "ExposeHeaders": []
    }]
  }'
```

---

## 4. Crear Base de Datos RDS PostgreSQL

RDS (Relational Database Service) alojará la base de datos PostgreSQL con las tablas de eventos y tickets.

### 4.1 Crear Subnet Group

Primero, obtén las subnets de tu VPC por defecto:

```bash
# Obtener VPC por defecto
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=is-default,Values=true" --query "Vpcs[0].VpcId" --output text)
echo "VPC ID: $VPC_ID"

# Obtener subnets de la VPC
SUBNET_IDS=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" --query "Subnets[*].SubnetId" --output text)
echo "Subnets: $SUBNET_IDS"
```

Crea el subnet group para RDS:

```bash
aws rds create-db-subnet-group \
  --db-subnet-group-name microservicio-db-subnet \
  --db-subnet-group-description "Subnet group para microservicio" \
  --subnet-ids $SUBNET_IDS
```

### 4.2 Crear Security Group para RDS

```bash
# Crear security group
RDS_SG_ID=$(aws ec2 create-security-group \
  --group-name microservicio-rds-sg \
  --description "Security group para RDS PostgreSQL" \
  --vpc-id $VPC_ID \
  --query "GroupId" --output text)

echo "RDS Security Group ID: $RDS_SG_ID"

# Permitir conexiones PostgreSQL (puerto 5432)
aws ec2 authorize-security-group-ingress \
  --group-id $RDS_SG_ID \
  --protocol tcp \
  --port 5432 \
  --cidr 0.0.0.0/0
```

### 4.3 Crear Instancia RDS

```bash
aws rds create-db-instance \
  --db-instance-identifier tickets-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 15 \
  --master-username ticketsadmin \
  --master-user-password TuPasswordSeguro123 \
  --allocated-storage 20 \
  --db-name ticketsdb \
  --vpc-security-group-ids $RDS_SG_ID \
  --db-subnet-group-name microservicio-db-subnet \
  --publicly-accessible \
  --no-multi-az \
  --storage-type gp2
```

> **Importante:** Cambia `TuPasswordSeguro123` por una contraseña segura.

### 4.4 Esperar y Obtener Endpoint

La creación toma varios minutos. Verifica el estado:

```bash
aws rds describe-db-instances \
  --db-instance-identifier tickets-db \
  --query "DBInstances[0].DBInstanceStatus" \
  --output text
```

Cuando el estado sea `available`, obtén el endpoint:

```bash
aws rds describe-db-instances \
  --db-instance-identifier tickets-db \
  --query "DBInstances[0].Endpoint.Address" \
  --output text
```

**Guarda este endpoint** - lo necesitarás como `RDS_ENDPOINT`.

---

## 5. Crear Sistema de Archivos EFS

EFS (Elastic File System) proporciona almacenamiento compartido para archivos temporales durante el procesamiento de PDFs.

### 5.1 Crear Security Group para EFS

```bash
# Crear security group
EFS_SG_ID=$(aws ec2 create-security-group \
  --group-name microservicio-efs-sg \
  --description "Security group para EFS" \
  --vpc-id $VPC_ID \
  --query "GroupId" --output text)

echo "EFS Security Group ID: $EFS_SG_ID"

# Permitir NFS (puerto 2049)
aws ec2 authorize-security-group-ingress \
  --group-id $EFS_SG_ID \
  --protocol tcp \
  --port 2049 \
  --cidr 0.0.0.0/0
```

### 5.2 Crear EFS

```bash
EFS_ID=$(aws efs create-file-system \
  --performance-mode generalPurpose \
  --throughput-mode bursting \
  --encrypted \
  --tags Key=Name,Value=microservicio-efs \
  --query "FileSystemId" --output text)

echo "EFS ID: $EFS_ID"
```

### 5.3 Crear Mount Targets

Crea un mount target en cada subnet para alta disponibilidad:

```bash
# Obtener primera subnet
SUBNET_ID=$(aws ec2 describe-subnets \
  --filters "Name=vpc-id,Values=$VPC_ID" \
  --query "Subnets[0].SubnetId" --output text)

# Crear mount target
aws efs create-mount-target \
  --file-system-id $EFS_ID \
  --subnet-id $SUBNET_ID \
  --security-groups $EFS_SG_ID
```

### 5.4 Verificar EFS

```bash
aws efs describe-file-systems \
  --file-system-id $EFS_ID
```

---

## 6. Crear Instancia EC2

EC2 (Elastic Compute Cloud) es la máquina virtual donde correrá el contenedor Docker con el microservicio.

### 6.1 Crear Key Pair

El key pair permite conectarte por SSH a la instancia:

```bash
aws ec2 create-key-pair \
  --key-name microservicio-key \
  --query "KeyMaterial" \
  --output text > microservicio-key.pem

chmod 400 microservicio-key.pem
```

**Guarda este archivo** - lo necesitarás para SSH.

### 6.2 Crear Security Group para EC2

```bash
# Crear security group
EC2_SG_ID=$(aws ec2 create-security-group \
  --group-name microservicio-ec2-sg \
  --description "Security group para EC2" \
  --vpc-id $VPC_ID \
  --query "GroupId" --output text)

echo "EC2 Security Group ID: $EC2_SG_ID"

# Permitir SSH (puerto 22)
aws ec2 authorize-security-group-ingress \
  --group-id $EC2_SG_ID \
  --protocol tcp \
  --port 22 \
  --cidr 0.0.0.0/0

# Permitir HTTP para el microservicio (puerto 8080)
aws ec2 authorize-security-group-ingress \
  --group-id $EC2_SG_ID \
  --protocol tcp \
  --port 8080 \
  --cidr 0.0.0.0/0
```

### 6.3 Obtener AMI de Amazon Linux 2023

```bash
AMI_ID=$(aws ec2 describe-images \
  --owners amazon \
  --filters "Name=name,Values=al2023-ami-2023*-x86_64" \
            "Name=state,Values=available" \
  --query "Images | sort_by(@, &CreationDate) | [-1].ImageId" \
  --output text)

echo "AMI ID: $AMI_ID"
```

### 6.4 Crear Instancia EC2

```bash
INSTANCE_ID=$(aws ec2 run-instances \
  --image-id $AMI_ID \
  --instance-type t2.micro \
  --key-name microservicio-key \
  --security-group-ids $EC2_SG_ID \
  --subnet-id $SUBNET_ID \
  --associate-public-ip-address \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=microservicio-server}]' \
  --query "Instances[0].InstanceId" --output text)

echo "Instance ID: $INSTANCE_ID"
```

### 6.5 Obtener IP Pública

Espera a que la instancia esté en estado `running`:

```bash
aws ec2 describe-instances \
  --instance-ids $INSTANCE_ID \
  --query "Reservations[0].Instances[0].State.Name" \
  --output text
```

Obtén la IP pública:

```bash
EC2_IP=$(aws ec2 describe-instances \
  --instance-ids $INSTANCE_ID \
  --query "Reservations[0].Instances[0].PublicIpAddress" \
  --output text)

echo "EC2 IP: $EC2_IP"
```

**Guarda esta IP** - es tu `EC2_HOST`.

---

## 7. Configurar EC2

Conéctate a la instancia EC2 para instalar Docker y montar EFS.

### 7.1 Conectar por SSH

```bash
ssh -i microservicio-key.pem ec2-user@$EC2_IP
```

### 7.2 Instalar Docker

Una vez conectado a EC2, ejecuta:

```bash
# Actualizar paquetes
sudo yum update -y

# Instalar Docker
sudo yum install -y docker

# Iniciar Docker
sudo systemctl start docker
sudo systemctl enable docker

# Agregar usuario al grupo docker
sudo usermod -aG docker ec2-user

# Cerrar sesión y reconectar para aplicar cambios
exit
```

Reconecta y verifica:

```bash
ssh -i microservicio-key.pem ec2-user@$EC2_IP
docker --version
```

### 7.3 Montar EFS

```bash
# Instalar utilidades EFS
sudo yum install -y amazon-efs-utils

# Crear punto de montaje
sudo mkdir -p /mnt/efs

# Montar EFS (reemplaza EFS_ID con tu ID)
sudo mount -t efs -o tls EFS_ID:/ /mnt/efs

# Verificar montaje
df -h | grep efs

# Configurar permisos
sudo chmod 777 /mnt/efs
```

### 7.4 Montaje Automático al Reinicio

Agrega entrada a `/etc/fstab`:

```bash
echo "EFS_ID:/ /mnt/efs efs defaults,_netdev,tls 0 0" | sudo tee -a /etc/fstab
```

---

## 8. Security Groups (Resumen)

Resumen de los Security Groups creados y sus reglas:

| Security Group | Puerto | Protocolo | Origen | Propósito |
|----------------|--------|-----------|--------|-----------|
| microservicio-ec2-sg | 22 | TCP | 0.0.0.0/0 | SSH |
| microservicio-ec2-sg | 8080 | TCP | 0.0.0.0/0 | API REST |
| microservicio-rds-sg | 5432 | TCP | 0.0.0.0/0 | PostgreSQL |
| microservicio-efs-sg | 2049 | TCP | 0.0.0.0/0 | NFS (EFS) |

> **Nota de Seguridad:** Para producción, restringe los orígenes a IPs específicas o al Security Group de EC2.

---

## 9. Configuración de GitHub Actions

El pipeline CI/CD requiere estos secrets en GitHub (Settings > Secrets and variables > Actions):

| Secret | Descripción | Cómo Obtenerlo |
|--------|-------------|----------------|
| `DOCKERHUB_USERNAME` | Usuario de Docker Hub | Tu usuario de Docker Hub |
| `DOCKERHUB_TOKEN` | Token de acceso Docker Hub | Docker Hub > Account Settings > Security > New Access Token |
| `AWS_ACCESS_KEY_ID` | Access Key de IAM | Sección 2.4 de esta guía |
| `AWS_SECRET_ACCESS_KEY` | Secret Key de IAM | Sección 2.4 de esta guía |
| `AWS_SESSION_TOKEN` | Token de sesión (si usas credenciales temporales) | AWS STS o vacío si usas IAM user |
| `EC2_HOST` | IP pública de EC2 | Sección 6.5 de esta guía |
| `EC2_SSH_KEY` | Contenido completo del archivo .pem | `cat microservicio-key.pem` |
| `USER_SERVER` | Usuario SSH de EC2 | `ec2-user` |
| `RDS_ENDPOINT` | Endpoint de RDS | Sección 4.4 de esta guía |
| `RDS_USERNAME` | Usuario de la BD | `ticketsadmin` (o el que configuraste) |
| `RDS_PASSWORD` | Contraseña de la BD | La que configuraste en sección 4.3 |

---

## 10. Variables de Entorno para Docker

### 10.1 Tabla de Variables

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `AWS_ACCESS_KEY_ID` | Clave de acceso AWS | AKIA... |
| `AWS_SECRET_ACCESS_KEY` | Clave secreta AWS | xxxxx |
| `AWS_SESSION_TOKEN` | Token de sesión (opcional) | IQoJb... |
| `AWS_REGION` | Región AWS | us-east-1 |
| `RDS_ENDPOINT` | Host de RDS PostgreSQL | tickets-db.xxx.us-east-1.rds.amazonaws.com |
| `RDS_USERNAME` | Usuario de BD | ticketsadmin |
| `RDS_PASSWORD` | Contraseña de BD | TuPassword |

### 10.2 Comando Docker Run Completo

```bash
docker run -d --name my-app \
  -p 8080:8080 \
  -v /mnt/efs:/app/efs \
  -e AWS_ACCESS_KEY_ID="tu-access-key" \
  -e AWS_SECRET_ACCESS_KEY="tu-secret-key" \
  -e AWS_SESSION_TOKEN="tu-session-token" \
  -e AWS_REGION="us-east-1" \
  -e RDS_ENDPOINT="tickets-db.xxx.us-east-1.rds.amazonaws.com" \
  -e RDS_USERNAME="ticketsadmin" \
  -e RDS_PASSWORD="TuPasswordSeguro123" \
  tu-usuario-dockerhub/my-app:latest
```

---

## 11. Verificación Final

Después de desplegar el microservicio, verifica que todo funcione correctamente.

### 11.1 Verificar Conectividad a RDS

Desde EC2:

```bash
# Instalar cliente PostgreSQL
sudo yum install -y postgresql15

# Conectar a RDS
psql -h tickets-db.xxx.us-east-1.rds.amazonaws.com \
     -U ticketsadmin \
     -d ticketsdb
```

Ejecuta una consulta de prueba:
```sql
SELECT * FROM eventos;
\q
```

### 11.2 Verificar Acceso a S3

```bash
# Listar objetos en el bucket
aws s3 ls s3://microservicio-eventos-s3/

# Subir archivo de prueba
echo "test" > test.txt
aws s3 cp test.txt s3://microservicio-eventos-s3/test.txt

# Verificar
aws s3 ls s3://microservicio-eventos-s3/
```

### 11.3 Verificar Montaje EFS

En EC2:

```bash
# Verificar montaje
df -h | grep efs

# Crear archivo de prueba
echo "test" > /mnt/efs/test.txt
ls -la /mnt/efs/
```

### 11.4 Probar Endpoints del Microservicio

Reemplaza `EC2_IP` con la IP pública de tu instancia:

```bash
# Health check básico
curl http://EC2_IP:8080/microservicio

# Listar eventos (cargados desde data.sql)
curl http://EC2_IP:8080/api/eventos

# Obtener estadísticas de un evento
curl http://EC2_IP:8080/api/tickets/estadisticas/1

# Listar objetos S3
curl http://EC2_IP:8080/s3/microservicio-eventos-s3/objects
```

### 11.5 Crear un Ticket de Prueba

```bash
curl -X POST http://EC2_IP:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "usuarioId": "USR001",
    "usuarioNombre": "Usuario Test"
  }'
```

Verifica que el PDF se creó en S3:

```bash
aws s3 ls s3://microservicio-eventos-s3/ --recursive
```

---

## Resumen de Recursos Creados

| Recurso | Nombre/ID | Propósito |
|---------|-----------|-----------|
| IAM Policy | MicroservicioEventosPolicy | Permisos S3/EFS |
| IAM User | microservicio-eventos-user | Credenciales de acceso |
| S3 Bucket | microservicio-eventos-s3 | Almacenamiento de PDFs |
| RDS Instance | tickets-db | Base de datos PostgreSQL |
| EFS | microservicio-efs | Almacenamiento temporal |
| EC2 Instance | microservicio-server | Host del contenedor |
| Key Pair | microservicio-key | Acceso SSH |
| Security Groups | *-sg | Reglas de firewall |

---

## Valores de Referencia Rápida

```bash
# Guardar estos valores después de la configuración:
export VPC_ID="vpc-xxx"
export SUBNET_ID="subnet-xxx"
export EC2_SG_ID="sg-xxx"
export RDS_SG_ID="sg-xxx"
export EFS_SG_ID="sg-xxx"
export EFS_ID="fs-xxx"
export INSTANCE_ID="i-xxx"
export EC2_IP="x.x.x.x"
export RDS_ENDPOINT="tickets-db.xxx.us-east-1.rds.amazonaws.com"
```
