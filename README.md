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
## Requisitos previos
- JDK 21.
- PostgreSQL.
- Maven Wrapper incluido en el repositorio.
- Variables de entorno necesarias:
  - `DB_URL`
  - `DB_USER`
  - `DB_PASSWORD`
  - `YACHAQ_TOKEN_SECRET`, con al menos 32 caracteres.
- Opcionalmente, `RESEND_API_KEY` para el envío de códigos de recuperación.
## Instalación y ejecución
1. Cree una base de datos PostgreSQL.
2. Configure las variables de entorno. En Windows PowerShell:
```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/yachaq"
$env:DB_USER = "<usuario-postgresql>"
$env:DB_PASSWORD = "<contraseña-postgresql>"
$env:YACHAQ_TOKEN_SECRET = "<clave-aleatoria-de-al-menos-32-caracteres>"
```
3. Ejecute la aplicación:
```powershell
.\mvnw.cmd spring-boot:run
```
La aplicación utiliza el puerto `8080` por defecto o el valor configurado en `PORT`.
4. Compruebe el estado del servicio:
```text
http://localhost:8080/health
```
## Autor
- [Francowski25](https://github.com/Francowski25)
