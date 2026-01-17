### Spring Boot eCommerce REST API

## Overview

This repository contains a **backend REST API for an eCommerce application**, built using **Java and Spring Boot**.
The project focuses on **real-world backend development**, including API design, security, database integration, documentation, and cloud deployment.

The API is designed to be consumed by a frontend application and follows **clean, production-style backend practices**.

---

### What has been Implemented

* RESTful API using **Spring Boot**
* Authentication and authorization with **Spring Security 7 and JWT**
* Role-based access control
* Database persistence using **Spring Data JPA (Hibernate)**
* PostgreSQL database
* API documentation using **Swagger / OpenAPI**
* Centralized exception handling
* DTO-based request and response models
* **Deployment to AWS using Elastic Beanstalk**
* **PostgreSQL hosted on AWS RDS**, managed with PgAdmin

---

### Technology Stack

* Java 21
* Spring Boot
* Spring Data JPA
* Spring Security 
* JWT (JSON Web Tokens)
* PostgreSQL
* Swagger / OpenAPI (springdoc)
* AWS Elastic Beanstalk
* AWS RDS

---

## Architecture & Code Structure

The application follows a **layered architecture**:

```
controller → service → repository → database
```

### Package Structure

* **config** – It contains different app configurations that was implemented. Like swagger configuration
* **controller** – Exposes REST endpoints and handles HTTP requests and responses
* **Exception** – It contains custom exceptions to handle user friendly errors.
* **service** – Contains business logic and application rules
* **repository** – Handles database access using Spring Data JPA
* **model** – Domain objects mapped to database tables
* **payload** – DTOs for API request and response bodies
* **security** – JWT authentication, authorization, and security configuration
* **util** – AuthUtils

## Error Handling & DTO Design

* Centralized exception handling provides consistent and meaningful error responses.
* Custom exceptions and proper HTTP status codes are used.
* DTOs in the `payload` package separate API contracts from internal entities.
* This improves security, clarity, and long-term maintainability.

---

## API Documentation

The API is documented using **Springdoc OpenAPI**.

Swagger UI is available locally at:

```
http://localhost:8080/swagger-ui/index.html
```

It allows viewing and testing all endpoints directly from the browser.

---

## AWS Deployment

* Application deployed using **AWS Elastic Beanstalk**
* PostgreSQL database hosted on **AWS RDS**
* Database connection managed and tested using **PgAdmin**
* Environment-based configuration used for production setup

### Skills Mapping Aligned with Backend Job Roles

This project demonstrates experience with:

* Building REST APIs using Spring Boot
* Securing APIs with JWT and Spring Security
* Designing layered backend architectures
* Working with relational databases (PostgreSQL, JPA/Hibernate)
* Using DTOs and exception handling for clean API design
* Documenting APIs with Swagger/OpenAPI
* Deploying backend services to AWS

## Running the Project Locally

1. Clone the repository:

   ```bash
   git clone https://github.com/iamnana-tyc/springboot-java-fullstack-ecommerce-embarkx.git
   ```

2. Configure database credentials in `application.properties`

3. Run the application:

   ```bash
   mvn spring-boot:run
   ```

4. Access the API:

   ```
   http://localhost:8080
   ```

## Summary

I built a Spring Boot backend for an eCommerce application to practice real-world backend development.
The API follows a layered architecture with controllers, services, and repositories. 

I secured endpoints using Spring Security with JWT, used DTOs to separate API models from entities, and implemented centralized exception handling for clean error responses.

Data is stored in PostgreSQL using Spring Data JPA, and the API is fully documented with Swagger. I also deployed the application to AWS using Elastic Beanstalk, with the database hosted on RDS, which gave me hands-on experience with cloud deployment and environment configuration.
