# 🏨 Hotel Booking Backend

A full-featured hotel booking REST API built with **Spring Boot 4**, **MySQL**, and **JWT authentication**. Supports multi-role access for Customers, Hotel Managers, and Admins with complete booking lifecycle management.

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.2 |
| Language | Java 21 |
| Database | MySQL 8+ |
| Migrations | Flyway |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Docs | SpringDoc OpenAPI / Swagger UI |
| Build | Maven |
| Containerization | Docker |
| Utilities | Lombok |

---

## 📦 Features

- **Authentication & Authorization** — JWT-based auth with role guards (`CUSTOMER`, `HOTEL_MANAGER`, `ADMIN`)
- **Hotel Management** — Create, update, and manage hotels with photos, amenities, accessibility info, policies, and nearby places
- **Room Management** — Rooms with availability tracking, pricing, photos, and accessibility features
- **Advanced Search** — Filter hotels by city, dates, guest count, star rating, amenities, pet policy, cancellation policy, bed type, and more
- **Booking Lifecycle** — Full flow: `PENDING → CONFIRMED → COMPLETED / CANCELLED / NO_SHOW`
- **Mock Payments** — Simulated payment processing with failure testing support and refunds
- **Reviews** — Customers can leave reviews on completed stays
- **Saved Hotels** — Customers can bookmark favourite hotels
- **Notifications** — Notification port for booking events
- **Admin Dashboard** — Approve/reject hotels, suspend users, view platform stats
- **Actuator** — Health, metrics, and info endpoints

---

## 🗂️ Project Structure

```
src/main/java/com/HotelBook/HotelBooking/
├── Admin/              # Admin dashboard, hotel approval, user management
├── Booking/            # Booking entity, lifecycle service, controller
├── Cancellation/       # Cancellation policies
├── Common/             # Shared DTOs, pagination, response wrappers
├── Customer/           # Customer profile management
├── Hotel/              # Hotel entity, CRUD, photos
├── HotelAccessibility/ # Accessibility features for hotels
├── HotelAmenity/       # Hotel amenity management
├── HotelLocation/      # Location data
├── HotelNearby/        # Nearby places
├── HotelPhoto/         # Hotel photo management
├── HotelPolicy/        # Hotel policies (pets, breakfast, etc.)
├── Notification/       # Notification port
├── Payment/            # Mock payment processing & refunds
├── Pricing/            # Room pricing
├── Review/             # Customer reviews
├── Room/               # Room CRUD
├── RoomAccessibility/  # Room accessibility features
├── RoomAmenity/        # Room amenity management
├── RoomAvailability/   # Date availability & blocking
├── RoomPhoto/          # Room photo management
├── SavedHotel/         # Customer wishlists
├── Search/             # Advanced hotel search with JPA Specifications
├── Security/           # JWT filter, UserDetailsService, SecurityConfig
└── User/               # Auth (register/login), user profiles, roles
```

---

## ⚙️ Prerequisites

- Java 21+
- Maven 3.9+
- MySQL 8+
- Docker (optional, for containerized setup)

---

## 🛠️ Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/hotel-booking-backend.git
cd hotel-booking-backend
```

### 2. Create the database

```sql
CREATE DATABASE booking_glitchies CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure environment variables

Copy the local profile template and fill in your values:

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Edit `application-local.properties`:

```properties
DB_HOST=localhost
DB_NAME=booking_glitchies
DB_USERNAME=root
DB_PASSWORD=your_password

JWT_SECRET=your_base64_secret_here
```

> **Tip:** Generate a secure JWT secret with:
> ```bash
> openssl rand -base64 32
> ```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The server starts on **http://localhost:8080**

---

## 🐳 Docker Setup

Build and run with Docker:

```bash
docker build -t hotel-booking-backend .
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_NAME=booking_glitchies \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_base64_secret \
  hotel-booking-backend
```

---

## 📖 API Documentation

Once running, visit the interactive Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

Raw OpenAPI spec:

```
http://localhost:8080/v3/api-docs
```

---

## 🔐 Authentication

All protected endpoints require a Bearer token in the `Authorization` header:

```
Authorization: Bearer <your_jwt_token>
```

### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secret123",
  "firstName": "Jane",
  "lastName": "Doe",
  "role": "CUSTOMER"   // or "HOTEL_MANAGER"
}
```

> Note: `ADMIN` accounts cannot be self-registered. They are seeded via `DataSeeder`.

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secret123"
}
```

---

## 🔑 User Roles

| Role | Capabilities |
|---|---|
| `CUSTOMER` | Search hotels, create bookings, pay, cancel, leave reviews, save hotels |
| `HOTEL_MANAGER` | Manage own hotel, rooms, photos, pricing, availability |
| `ADMIN` | Approve/reject hotels, suspend users, view all bookings and hotels |

---

## 📋 Key API Endpoints

### Public
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/hotels` | List all active hotels |
| `GET` | `/api/hotels/{id}` | Get hotel details |
| `GET` | `/api/hotels/search` | Advanced search with filters |
| `GET` | `/api/reviews/hotel/{hotelId}` | Get reviews for a hotel |

### Auth
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login and receive JWT |
| `GET` | `/api/auth/me` | Get current user profile |
| `PATCH` | `/api/auth/me` | Update profile |
| `PATCH` | `/api/auth/me/password` | Change password |

### Bookings (Customer)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/bookings` | Create a booking (status: `PENDING`) |
| `GET` | `/api/bookings` | List my bookings |
| `GET` | `/api/bookings/{id}` | Get booking details |
| `POST` | `/api/bookings/{id}/payment` | Pay for a booking → auto-confirms |
| `POST` | `/api/bookings/{id}/cancel` | Cancel a booking |

### Admin
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/hotels` | List all hotels (filterable by status) |
| `PATCH` | `/api/admin/hotels/{id}/approve` | Approve a hotel |
| `PATCH` | `/api/admin/hotels/{id}/reject` | Reject a hotel |
| `GET` | `/api/admin/users` | List all users |
| `PATCH` | `/api/admin/users/{id}/suspend` | Suspend a user |

---

## 🔍 Search Example

```http
GET /api/hotels/search?city=Dubai&checkIn=2025-12-24&checkOut=2025-12-27
    &adults=2&children=1&childrenAges=5
    &stars=4&stars=5
    &hotelAmenities=pool&hotelAmenities=gym
    &bedType=KING
    &freeCancellation=true
    &petsAllowed=true
    &sortBy=stars&sortOrder=desc
    &page=0&size=20
```

---

## 🏥 Health Check

```
GET http://localhost:8080/actuator/health
```

---

## 🧪 Running Tests

```bash
./mvnw test
```

Tests use an **H2 in-memory database** — no MySQL required for testing.

---

## 📝 Environment Variables Reference

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_NAME` | `booking_glitchies` | Database name |
| `DB_USERNAME` | `root` | DB username |
| `DB_PASSWORD` | *(empty)* | DB password |
| `JWT_SECRET` | *(required)* | Base64-encoded JWT signing secret |

---

## 📄 License

This project is for educational purposes.
