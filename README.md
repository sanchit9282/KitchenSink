# Kitchensink Project

This project is a full-stack application built with **Spring Boot** for the backend and **React** for the frontend. It demonstrates essential features like authentication, member management, and seamless integration with a MongoDB database.

## Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [Technology Stack](#technology-stack)
4. [Prerequisites](#prerequisites)
5. [Setup Instructions](#setup-instructions)
   - [Backend Setup](#backend-setup)
   - [Frontend Setup](#frontend-setup)
   - [Database Setup](#database-setup)
6. [Running the Application](#running-the-application)
7. [Testing](#testing)
8. [Docker Setup](#docker-setup)
9. [Project Structure](#project-structure)
10. [Contributing](#contributing)
11. [License](#license)

---

## Overview

The Kitchensink project is designed to showcase modern web development best practices, including security, exception handling, validation, and test-driven development. The application has been migrated from JBoss to Spring Boot and React, with MongoDB as the database backend.

## Features
- User authentication (JWT-based).
- Member management with CRUD operations.
- Secure and configurable CORS setup.
- Global exception handling and validations.
- Comprehensive unit and integration tests.
- Dockerized environment for consistent deployments.

## Technology Stack

**Backend:**
- Spring Boot 3.1.0
- MongoDB 1.44.7
- Java 21

**Frontend:**
- React 18
- TypeScript

**Deployment Tools:**
- Docker
- Docker Compose

**Testing Tools:**
- JUnit
- Jest (for frontend)
- React Testing Library

**API Documentation:**
- Swagger/OpenAPI

## Prerequisites

Ensure the following are installed on your system:
- [Java 21](https://www.oracle.com/java/technologies/javase-downloads.html)
- [Node.js (16.x or higher)](https://nodejs.org/)
- [Maven](https://maven.apache.org/)
- [MongoDB](https://www.mongodb.com/try/download/community)
- [Docker Desktop](https://www.docker.com/)
- [Git Desktop](https://git-scm.com/)

## Setup Instructions

### Backend Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/sanchit9282/KitchenSink.git
   cd KitchenSink
   ```

2. Navigate to the backend directory:
   ```bash
   cd src
   ```

3. Update the database configuration in `src/main/resources/application.properties`:
   ```properties
   spring.data.mongodb.host=localhost
   spring.data.mongodb.port=27017
   spring.data.mongodb.database=KitchenSink_DB
   ```

4. Build the backend:
   ```bash
   mvn clean install
   ```

5. Run the backend:
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the frontend:
   ```bash
   npm start
   ```

### Database Setup

1. Ensure MongoDB is running locally:
   ```bash
   mongod
   ```

2. Create a database named `KitchenSink_DB`:
   ```bash
   mongo
   use KitchenSink_DB
   ```

## Running the Application

1. Start the backend server as described above.
2. Start the frontend server.
3. Open your browser and navigate to `http://localhost:3000`.

## Testing

### Backend Testing
Run the backend tests:
```bash
mvn test
```

### Frontend Testing
Run the frontend tests:
```bash
npm test
```

## Docker Setup

### Build and Run Containers

1. Build the Docker images:
   ```bash
   docker-compose build
   ```

2. Start the containers:
   ```bash
   docker-compose up
   ```

3. Access the application:
   - Frontend: `http://localhost:3000`
   - Backend: `http://localhost:8080`

### Stopping Containers

```bash
   docker-compose down
```

## Project Structure

### Backend (`src`)
- **`config`**: Configuration files (e.g., CORS, MongoDB, Security).
- **`controller`**: REST API endpoints.
- **`service`**: Business logic.
- **`repository`**: Data access layer.
- **`model`**: MongoDB data models.
- **`exception`**: Custom exception handling.
- **`dto`**: Data Transfer Objects.

### Frontend (`frontend`)
- **`src/components`**: React components.
- **`src/context`**: Context API for global state management.
- **`src/hooks`**: Custom hooks.
- **`src/services`**: API services.
- **`src/utils`**: Helper utilities.
- **`src/integration-tests`**: Integration tests.

### Root Directory
- **`Dockerfile`**: Backend Docker configuration.
- **`docker-compose.yml`**: Multi-container configuration.
- **`README.md`**: Documentation.

## Contributing

Contributions are welcome! Please fork the repository, create a new branch, and submit a pull request.
