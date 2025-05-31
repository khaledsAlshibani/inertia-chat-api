<h1>Inertia Chat App</h1>

- [Getting Started](#getting-started)
  - [1. Clone the Repository](#1-clone-the-repository)
  - [2. Set Up Configuration](#2-set-up-configuration)
  - [2. Start the Database (PostgreSQL)](#2-start-the-database-postgresql)
  - [3. Build and Run the Application](#3-build-and-run-the-application)
    - [Build the Project:](#build-the-project)
    - [Run the Application:](#run-the-application)
- [API Testing](#api-testing)
  - [HTTP Test Files:](#http-test-files)
- [WebSocket \& Chat Testing](#websocket--chat-testing)

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


### 2. Start the Database (PostgreSQL)

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

### 3. Build and Run the Application

> You can skip these steps if you're using an IDE like IntelliJ to run the application.

#### Build the Project:

```sh
./mvnw clean install
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

## WebSocket & Chat Testing

> Default WebSocket testing runs on port `9090`. Update URLs if your port differs.

1. Log in via [http://localhost:9090/login-test.html](http://localhost:9090/login-test.html) or sign up via [http://localhost:9090/signup-test.html](http://localhost:9090/signup-test.html). Tokens will be saved locally. Or use a http client tool to login and then copy the access token and paste it in the token field in step 2.
2. Visit [http://localhost:9090/websocket-test.html](http://localhost:9090/websocket-test.html) to see available users, and chatting.
3. Click on a user to initiate a 1:1 chat session.
4. If no other users are available, repeat step 1 in an incognito window or different browser to register another user. Then refresh the chat page and try again.