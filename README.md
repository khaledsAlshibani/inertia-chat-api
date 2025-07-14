<h1>Inertia Chat App</h1>

- [Getting Started](#getting-started)
  - [1. Clone the Repository](#1-clone-the-repository)
  - [2. Set Up Configuration](#2-set-up-configuration)
    - [Configuration Details](#configuration-details)
      - [Environment Variables](#environment-variables)
      - [Storage Configuration](#storage-configuration)
  - [3. Start the Database (PostgreSQL)](#3-start-the-database-postgresql)
  - [4. Build and Run the Application](#4-build-and-run-the-application)
    - [Build the Project:](#build-the-project)
    - [Run the Application:](#run-the-application)
- [API Testing](#api-testing)
  - [HTTP Test Files:](#http-test-files)
- [WebSocket \& Chat Testing (`deprecated`)](#websocket--chat-testing-deprecated)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/khaledsAlshibani/inertia-chat-app.git
cd inertia-chat-app
```

### 2. Set Up Configuration

Copy the template configuration file:

```sh
cp src/main/resources/application.yml.template src/main/resources/application.yml
# Or use
copy src/main/resources/application.yml.template src/main/resources/application.yml
```

> ⚠️ Important: Never commit application.yml with sensitive data.

> ⚠️ Replace jwt.secret with a strong, securely stored value.

#### Configuration Details

The application uses a template-based configuration system that supports both environment variables and direct configuration. The `application.yml.template` file contains all available configuration options with their default values.

##### Environment Variables

You can override any configuration value using environment variables. The application supports the following key environment variables:

**Development vs Production:**

| Environment | Profile | Storage | Key Settings |
|-------------|---------|---------|--------------|
| **Development** | `dev` | Local | `JWT_SECRET`, `SERVER_PORT=9090` |
| **Production** | `prod` | S3 | `JWT_SECRET`, `AWS_S3_*`, `CORS_ALLOWED_ORIGINS` |

**Critical Variables:**
- `JWT_SECRET` - **Required for both environments**
- `AWS_S3_ENABLE=true` - **Required for production S3 storage**
- `CORS_ALLOWED_ORIGINS` - **Must include your production domain**

##### Storage Configuration

**Development (Local Storage):**
- Files stored in `uploads/` directory
- No additional configuration needed
- Automatic when `spring.profiles.active=dev`

**Production (S3 Storage):**
- Files stored in AWS S3 bucket
- Requires `AWS_S3_ENABLE=true` or `spring.profiles.active=prod`
- Must configure: `AWS_S3_BUCKET`, `AWS_S3_REGION`, `AWS_S3_ACCESS_KEY`, `AWS_S3_SECRET_KEY`

**Storage Selection Priority:**

| Profile | AWS S3 Enable | Storage Used | Use Case |
|---------|---------------|--------------|----------|
| `dev` | `false` | **Local Storage** | Development |
| `dev` | `true` | **S3 Storage** | Dev with S3 testing |
| `prod` | `false` | **S3 Storage** | Production (forced) |
| `prod` | `true` | **S3 Storage** | Production |

### 3. Start the Database (PostgreSQL)

Use Docker Compose to start the PostgreSQL database:

```sh
docker-compose up -d db
```

> **Optional**: Launch CloudBeaver (web-based DB GUI):

```sh
docker-compose up -d dbeaver
```

* Access the UI at: [http://localhost:8978](http://localhost:8978)
* Default credentials:

  * Username: `admin`
  * Password: `admin`

> **Database Connection Details**:
* Host: `db`
* Port: `5432`
* Database: `inertia_chat`
* User: `postgres`
* Password: `postgres`

### 4. Build and Run the Application

> You can skip these steps if you're using an IDE like IntelliJ to run the application.

#### Build the Project:

```sh
./mvnw clean install -DskipTests
```

#### Run the Application:

```sh
./mvnw spring-boot:run
```

---

## API Testing

The project includes `.http` files for testing API endpoints using [REST Client](https://marketplace.visualstudio.com/items?itemName=humao.rest-client) in VS Code or Postman.

### HTTP Test Files:

* `http/auth.http` – Signup, login, refresh, logout
* `http/users.http` – User-related operations

---

## WebSocket & Chat Testing (`deprecated`)

> ⚠️ **Note:** The static HTML test files may not reflect the latest API changes. For testing, use proper API clients or the [frontend application](https://github.com/Muneeb-Almoliky/inertia-chat-web-client/).

> Default WebSocket testing runs on port `9090`. Update URLs if your port differs.

1. Log in via [http://localhost:9090/login-test.html](http://localhost:9090/login-test.html) or sign up via [http://localhost:9090/signup-test.html](http://localhost:9090/signup-test.html). Tokens will be saved locally. Or use a http client tool to login and then copy the access token and paste it in the token field in step 2.
2. Visit [http://localhost:9090/websocket-test.html](http://localhost:9090/websocket-test.html) to see available users, and chatting.
3. Click on a user to initiate a 1:1 chat session.
4. If no other users are available, repeat step 1 in an incognito window or different browser to register another user. Then refresh the chat page and try again.