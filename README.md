# Spring Boot Web Application

Este es un proyecto Spring Boot que implementa una aplicación web con las siguientes características:

## Tecnologías utilizadas

- Java 17
- Spring Boot 3.1.5
- Spring Web
- Spring Data JPA
- Spring Security
- H2 Database
- Lombok

## Estructura del proyecto

```
src/main/java/com/myapp/
├── Application.java
├── controllers/
├── services/
├── repositories/
└── models/
```

## Requisitos previos

- Java 17 o superior
- Maven 3.6 o superior

## Configuración

1. Clonar el repositorio
2. Navegar al directorio del proyecto
3. Ejecutar `mvn clean install`

## Ejecución

Para ejecutar la aplicación:

```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## Acceso a la base de datos H2

La consola de H2 está disponible en `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuario: sa
- Contraseña: (dejar en blanco)

## Seguridad

Credenciales por defecto:
- Usuario: admin
- Contraseña: admin

**Nota**: Estas son credenciales de desarrollo. Cambiarlas antes de desplegar en producción.