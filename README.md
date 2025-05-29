# Inertia Chat App

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

## Cleaning and Building the Project

To clean and build the Maven project run:

```sh
./mvnw clean install
``` 