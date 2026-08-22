# campus-request-service

Service request management for CampusFlow. Handles request creation, status workflow, comments,
and file attachments.

This repository is a **Git submodule** of [campus-backend-services](https://github.com/TharushaDamsara/campus-backend-services).

**Student:** Tharusha Damsara (241711004)
**GCP Project ID:** campusflow-eca-2026

## Technology Stack

- Java 25
- Spring Boot 3.5.3 (Web, Data MongoDB, Validation, Actuator)
- MongoDB (non-relational database)
- Spring Cloud Netflix Eureka Client
- Spring Cloud Config Client
- Google Cloud Storage (file attachments)
- Google Cloud Firestore (audit events)
- springdoc-openapi (API docs)

## Setup / Getting Started

```bash
mvn clean install
mvn spring-boot:run
```

Runs on port `8082` by default. Requires MongoDB, and the CampusFlow Config Server +
Eureka Server to be running.
