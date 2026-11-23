# Minero Yachaq API

Backend REST para la aplicación minera Yachaq. El proyecto permite gestionar usuarios, plantas oficiales y personales, términos de liquidación, precios de metales y cálculos de pago con historial.

## Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Lombok
- Docker
- Resend

## Arquitectura

El proyecto utiliza **Domain-Driven Design (DDD)** como enfoque para organizar la lógica del negocio, separando las responsabilidades y facilitando el mantenimiento y la evolución del sistema.

Se organiza en capas:

- **Domain:** representa las entidades y reglas principales del negocio.
- **Business:** contiene la lógica y los servicios de negocio.
- **Repository:** gestiona el acceso y persistencia de datos.
- **Controller:** expone los endpoints REST y recibe las solicitudes.
- **DTO:** define los objetos utilizados para transferir información entre capas.

## Funcionalidades principales

- Registro e inicio de sesión de usuarios.
- Recuperación de contraseña mediante códigos OTP enviados por correo.
- Gestión de usuarios y roles de administrador y minero.
- Gestión de plantas oficiales y plantas personales.
- Configuración de términos generales y personalizados por planta.
- Consulta y actualización de precios de cobre, plata y oro.
- Cálculo de liquidaciones y almacenamiento del historial de cálculos.
- Actualización programada de precios mediante GoldPrice API.
- Endpoint de salud en `/health`.

## Seguridad

- Autenticación mediante tokens JWT.
- Protección de endpoints mediante Spring Security.
- Gestión de roles de administrador y minero.
- Recuperación de contraseña mediante códigos OTP.

## Integraciones externas

- **Resend:** envío de correos electrónicos para recuperación de contraseña.
- **GoldPrice API:** consulta de precios de metales preciosos.

## Requisitos del sistema

- JDK 21.
- PostgreSQL.
- Maven Wrapper incluido en el repositorio.
- Variables de entorno:
  - `DB_URL`
  - `DB_USER`
  - `DB_PASSWORD`
  - `YACHAQ_TOKEN_SECRET`
  - `RESEND_API_KEY`.

## Autor

- [Francowski25](https://github.com/Francowski25)
