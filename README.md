# SmartParkingLot

A backend system for managing a smart parking lot built with **Spring Boot 4.1**, **Spring Data JPA**, and **PostgreSQL**. It handles vehicle check-in/check-out, automatic parking spot allocation based on vehicle size, real-time availability tracking, and hourly fee calculation with concurrency-safe operations.

---

## Features

- **Automatic Spot Allocation** — Assigns the smallest compatible spot to incoming vehicles (Motorcycle → Small/Medium/Large, Car → Medium/Large, Bus → Large only).
- **Check-In & Check-Out** — Records entry/exit times and manages parking ticket lifecycle.
- **Fee Calculation** — Hourly rates per vehicle type (Motorcycle: ₹10/hr, Car: ₹20/hr, Bus: ₹50/hr) with partial hours rounded up.
- **Real-Time Availability** — Query available spots broken down by size.
- **Concurrency Handling** — Pessimistic locking prevents double-booking of spots.
- **Input Validation** — Request DTOs validated using Jakarta Bean Validation.
- **Global Error Handling** — Structured JSON error responses for all exceptions.

---

## Tech Stack

| Technology | Purpose |
|:---|:---|
| Java 17 | Language |
| Spring Boot 4.1.0 | Framework |
| Spring Data JPA | ORM / Database access |
| PostgreSQL | Production database |
| H2 | In-memory test database |
| Lombok | Boilerplate reduction |
| JUnit 5 + Mockito | Unit & Integration testing |

---

## Prerequisites

- **Java 17** or higher
- **Gradle** (wrapper included)
- **PostgreSQL** running locally

---

## Setup & Run

### 1. Clone the repository

```bash
git clone https://github.com/YagnikAkbari/SmartParkingLot.git
cd SmartParkingLot
```

### 2. Configure the database

Create a PostgreSQL database:

```sql
CREATE DATABASE smart_parking_lot;
```

Update credentials in `src/main/resources/application.properties` if needed:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/smart_parking_lot
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### 3. Build the project

```bash
./gradlew build
```

### 4. Run the application

```bash
./gradlew bootRun
```

The server starts at **http://localhost:8080**.

---

## API Endpoints

### Admin — Manage Spots

| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/api/admin/spots` | Create a parking spot |
| `GET` | `/api/admin/spots` | List all parking spots |

**Create Spot Request:**
```json
{
  "spotNumber": "SPOT-A01",
  "floor": 1,
  "spotSize": "MEDIUM"
}
```

### Parking — Vehicle Operations

| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/api/parking/check-in` | Check in a vehicle |
| `POST` | `/api/parking/check-out` | Check out a vehicle |
| `GET` | `/api/parking/availability` | Get real-time availability |

**Check-In Request:**
```json
{
  "licensePlate": "MH-12-AB-1234",
  "vehicleType": "CAR"
}
```

**Check-Out Request:**
```json
{
  "licensePlate": "MH-12-AB-1234"
}
```

---

## Running Tests

```bash
./gradlew test
```

This runs **32 tests** including:
- Unit tests for fee calculation and parking service logic
- Integration tests for all API endpoints with H2 in-memory database

---

## Project Structure

```
src/main/java/com/example/smartparkinglot/
├── controller/        # REST API controllers
├── dto/               # Request/Response DTOs
├── exception/         # Global exception handler
├── model/             # JPA Entities & Enums
├── repository/        # Spring Data JPA repositories
└── service/           # Business logic
```