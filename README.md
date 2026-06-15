# Hospital Management System - Backend

A robust and scalable backend application for the Hospital Management System (HMS), built using Spring Boot and PostgreSQL. This application provides secure REST APIs for managing patients, doctors, appointments, authentication, and administrative operations.

## Features

* User Authentication and Authorization
* Role-Based Access Control (Admin, Doctor, Patient)
* Patient Management
* Doctor Management
* Appointment Booking and Scheduling
* Profile Management
* RESTful API Architecture
* Global Exception Handling
* DTO-based Request/Response Handling
* PostgreSQL Database Integration

## Tech Stack

* Java 17
* Spring Boot 3.x
* Spring Security
* Spring Data JPA (Hibernate)
* PostgreSQL
* Maven
* Lombok
* JWT Authentication

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/hms/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       ├── dto/
│   │       ├── config/
│   │       ├── security/
│   │       └── exception/
│   └── resources/
│       └── application.yml
```

## Prerequisites

* Java 17
* Maven 3.x
* PostgreSQL

## Installation

1. Clone the repository.

```bash
git clone <repository-url>
```

2. Navigate to the project directory.

```bash
cd hms-backend
```

3. Configure the PostgreSQL database in `application.yml`.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hospital_db
    username: postgres
    password: your_password
```

4. Build the application.

```bash
mvn clean install
```

5. Run the application.

```bash
mvn spring-boot:run
```

The backend server will start on:

```
http://localhost:8081
```

## API Endpoints

| Module         | Endpoints              |
| -------------- | ---------------------- |
| Authentication | `/api/auth/**`         |
| Patients       | `/api/patients/**`     |
| Doctors        | `/api/doctors/**`      |
| Appointments   | `/api/appointments/**` |
| Admin          | `/api/admin/**`        |

## Future Enhancements

* Email Notifications
* Online Payments
* Medical Records Management
* Prescription Module
* Reports and Analytics

## Author

**Manideep Nakka**

Software Developer specializing in Java, Spring Boot, Node.js, TypeScript, React, and PostgreSQL.
