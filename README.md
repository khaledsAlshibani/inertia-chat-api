# Inertia Chat App

## Environment Setup

1. Copy the configuration template:
```sh
cp src/main/resources/application.yml.template src/main/resources/application.yml
```

2. Configure environment variables (optional):
```sh
# Database
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

# JWT
export JWT_SECRET=your-secret-key-here
export JWT_EXPIRATION=3600000
export JWT_REFRESH_EXPIRATION=604800000

# Server
export SERVER_PORT=9090
```

## Running the Database (PostgreSQL)

To start the PostgreSQL database using Docker Compose:

```sh
docker-compose up -d db
```

## Visual Database Management (DBeaver CloudBeaver)

To start DBeaver CloudBeaver for a web-based DB GUI:

```sh
docker-compose up -d dbeaver
```

- Access the DBeaver UI at: [http://localhost:8978](http://localhost:8978)
- Default login: `admin` / `admin`
- Connect to the database with:
  - Host: `db`
  - Port: `5432`
  - Database: `inertia_chat`
  - User: `postgres`
  - Password: `postgres`

## Running the Application

To start the Spring Boot application:

```sh
./mvnw spring-boot:run
```

## API Testing

The project includes HTTP request files for testing the API endpoints. You can use these with VS Code's REST Client extension or import them into Postman.

- `http/auth.http`: Authentication endpoints (signup, login, refresh, logout)
- `http/chat.http`: Chat-related endpoints

## Cleaning and Building the Project

To clean and build the Maven project run:

```sh
./mvnw clean install
```

## Security

The application uses JWT (JSON Web Tokens) for authentication. Make sure to:
1. Set a strong JWT secret in your environment variables
2. Keep your `application.yml` file secure and never commit it to version control
3. Use environment variables for sensitive configuration in production