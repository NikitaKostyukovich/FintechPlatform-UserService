# User Service

A robust, containerized Spring Boot microservice responsible for managing user profiles and their associated payment methods. This service is a core component of a larger e-commerce microservices architecture.

## 🚀 Tech Stack

* **Java 21+** & **Spring Boot 3.4.0+**
* **Database:** PostgreSQL
* **Caching:** Redis
* **Database Migration:** Liquibase
* **Object Mapping:** MapStruct
* **Testing:** JUnit 5, Mockito, Testcontainers
* **DevOps:** Docker, Docker Compose, GitHub Actions (CI/CD)

## ✨ Key Features

* **User & Card Management:** Full CRUD operations for user profiles and payment cards.
* **Business Logic Validation:** Enforces a strict limit of a maximum of 5 active cards per user.
* **Performance Optimization:** Implements Redis caching for fast user and card data retrieval, with proper cache eviction strategies.
* **Data Persistence:** Utilizes Spring Data JPA with entity auditing (`created_at`, `updated_at`).
* **Database Versioning:** Automated database schema initialization and migrations using Liquibase.
* **Global Exception Handling:** Centralized `@ControllerAdvice` for graceful error responses and custom exceptions (e.g., `UserNotFoundException`, `CardLimitExceededException`).

## 📡 API Reference

### User Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/users` | Create a new user profile |
| `GET` | `/api/users` | Fetch a paginated list of all users |
| `GET` | `/api/users/{id}` | Fetch a specific user by ID |
| `PUT` | `/api/users/{id}` | Update a specific user by ID |
| `DELETE`| `/api/users/{id}` | Delete a specific user (cascades to cards) |
| `PATCH` | `/api/users/{id}/status`| Activate or deactivate a user account |

### Card Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/users/{userId}/cards` | Add a new card to a specific user |
| `GET` | `/api/users/{userId}/cards` | Fetch all cards belonging to a specific user |
| `GET` | `/api/cards/{cardId}` | Fetch a specific card |
| `PUT` | `/api/cards/{cardId}` | Update a specific card |
| `DELETE`| `/api/cards/{cardId}` | Deactivate/Delete a specific card |

## 🛠️ Running the Application Locally

1. **Clone the repository:**
   Ensure you are on the `main` branch or your specific feature branch.
2. **Environment Variables:**
   Copy the provided `.env.example` file and rename it to `.env`. Fill in your secure credentials for PostgreSQL and Redis.
3. **Run via Docker Compose:**
   The project includes a `docker-compose.yaml` file that spins up PostgreSQL, Redis, and the User Service application.
   ```bash
   docker-compose up --build