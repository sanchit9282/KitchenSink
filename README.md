# Kitchensink Application

This is a migrated version of the JBoss EAP Quickstart Kitchensink application, now using Spring Boot, MongoDB, and React.

## Technologies Used

- Backend: Spring Boot 3.1.0 with Java 21
- Database: MongoDB
- Frontend: React 18 with TypeScript
- API Documentation: Swagger/OpenAPI

## Setup Instructions

### Prerequisites

- Java 21
- Node.js and npm
- MongoDB

### Backend Setup

1. Clone the repository
2. Navigate to the project root directory
3. Run `./mvnw spring-boot:run` to start the backend server

### Frontend Setup

1. Navigate to the `frontend` directory
2. Run `npm install` to install dependencies
3. Run `npm start` to start the development server

## API Documentation

Once the application is running, you can access the Swagger UI at `http://localhost:8080/swagger-ui.html`

## Running Tests

To run the backend tests, use the command `./mvnw test` in the project root directory.

To run the frontend tests, use the command `npm test` in the `frontend` directory.

## Deployment

To build the application for production:

1. Build the backend: `./mvnw clean package`
2. Build the frontend: `cd frontend && npm run build`

The resulting artifacts can be deployed to your preferred hosting platform.

