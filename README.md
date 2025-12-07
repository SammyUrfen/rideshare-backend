# RideShare Backend

## Project Overview & Implementation Summary

This project implements a secure, role-based backend for a simple ride-sharing application (Passenger requests -> Driver accepts -> Complete). It demonstrates proficiency in Spring Boot, MongoDB, and JWT authentication. The architecture follows a clean Controller -> Service -> Repository pattern, enforced with global exception handling and DTO validation.

### Key Features

- ✅ **Secure JWT Authentication & Authorization**
- ✅ **Role-Based Access Control** (ROLE_USER / ROLE_DRIVER)
- ✅ **MongoDB Persistence** with Spring Data
- ✅ **Global Exception Handling** (400, 403, 404, 500)
- ✅ **Clean Request-Service-Repository Architecture**
- ✅ **Input Validation** with Jakarta Validation

---

## Technology Stack

| Category | Technology |
|----------|------------|
| Framework | Spring Boot 3+ |
| Database | MongoDB |
| Security | Spring Security & JWT (JJWT 0.11.5) |
| Language | Java 21 |
| Utilities | Lombok, Jakarta Validation |

---

## Project Structure

The backend adheres strictly to the required package structure:

```
src/main/java/org/example/rideshare/
├── model/           # MongoDB Entities (User, Ride)
├── repository/      # Data Access Interfaces
├── service/         # Core Business Logic
├── controller/      # REST API Endpoints
├── config/          # Spring Security & JWT Configuration
├── dto/             # Data Transfer Objects with Validation
├── exception/       # Global Exception Handlers
├── security/        # JWT Authentication Filter
└── util/            # JWT Token Utilities
```

---

## API Endpoints Summary

The application runs on **port 8081**.

| Role | Method | Endpoint | Action |
|------|--------|----------|--------|
| PUBLIC | POST | `/api/auth/register` | Create User (ROLE_USER or ROLE_DRIVER) |
| PUBLIC | POST | `/api/auth/login` | Return JWT Token |
| USER | POST | `/api/v1/rides` | Create Ride (Status: REQUESTED) |
| USER | GET | `/api/v1/user/rides` | View rides requested by the current user |
| DRIVER | GET | `/api/v1/driver/rides/requests` | View all pending (REQUESTED) rides |
| DRIVER | POST | `/api/v1/driver/rides/{id}/accept` | Accept Ride (Status → ACCEPTED) |
| USER/DRIVER | POST | `/api/v1/rides/{id}/complete` | Complete Ride (Status → COMPLETED) |

---

## Ride Status Flow

```
REQUESTED  ──[Driver Accepts]──>  ACCEPTED  ──[Complete]──>  COMPLETED
```

---

## Getting Started

### Prerequisites

- Java 21+
- MongoDB running on `localhost:27017`
- Maven 3.9+

### Running the Application

```bash
# Clone the repository
git clone https://github.com/SammyUrfen/rideshare-backend.git
cd rideshare-backend

# Run with Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/rideshare-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8081`.

---

## Configuration

Key properties in `application.properties`:

```properties
server.port=8081
spring.data.mongodb.uri=mongodb://localhost:27017/rideshare

# JWT Configuration
jwt.secret=yourSecretKeyHere12345678901234567890
jwt.expiration=86400000
```

---

## Testing: Full Ride Lifecycle Script (cURL)

Ensure MongoDB and the Spring Boot application are running on port 8081. This script simulates the full workflow from user registration to ride completion.

### Quick Test Commands

**1. Register a Passenger:**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "passenger_p", "password": "password123", "role": "ROLE_USER"}'
```

**2. Register a Driver:**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "driver_d", "password": "password123", "role": "ROLE_DRIVER"}'
```

**3. Login (Get Token):**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "passenger_p", "password": "password123"}'
```

### Full Test Script

Run the complete test suite with the provided script:

```bash
chmod +x test-rideshare.sh
./test-rideshare.sh
```

<details>
<summary>📜 Click to expand full test script</summary>

```bash
#!/bin/bash

# =============================================================================
# RideShare Backend - Sequential cURL Test Script
# =============================================================================
# This script tests the complete ride flow:
# 1. Register Passenger & Driver
# 2. Login both users
# 3. Passenger requests a ride
# 4. Driver views and accepts the ride
# 5. Passenger completes the ride
# 6. Verify ride history
# 7. Test authorization failure
# =============================================================================

BASE_URL="http://localhost:8081"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper function to print step headers
print_step() {
    echo ""
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${YELLOW}STEP $1: $2${NC}"
    echo -e "${BLUE}============================================================${NC}"
}

# Helper function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Helper function to print failure
print_failure() {
    echo -e "${RED}✗ $1${NC}"
}

# Helper function to print response
print_response() {
    echo -e "${BLUE}Response:${NC}"
    echo "$1" | jq . 2>/dev/null || echo "$1"
}

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  RideShare Backend Test Suite${NC}"
echo -e "${GREEN}  Base URL: $BASE_URL${NC}"
echo -e "${GREEN}========================================${NC}"

# =============================================================================
# STEP 1: Register a new Passenger (USER)
# =============================================================================
print_step "1" "Register a new Passenger (USER)"

REGISTER_PASSENGER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "passenger_p",
        "password": "password123",
        "role": "ROLE_USER"
    }')

REGISTER_PASSENGER_BODY=$(echo "$REGISTER_PASSENGER_RESPONSE" | sed '$d')
REGISTER_PASSENGER_STATUS=$(echo "$REGISTER_PASSENGER_RESPONSE" | tail -n1)

print_response "$REGISTER_PASSENGER_BODY"
echo "HTTP Status: $REGISTER_PASSENGER_STATUS"

if [ "$REGISTER_PASSENGER_STATUS" -eq 200 ] || [ "$REGISTER_PASSENGER_STATUS" -eq 201 ]; then
    print_success "Passenger registered successfully"
else
    print_failure "Failed to register passenger (Status: $REGISTER_PASSENGER_STATUS)"
fi

# =============================================================================
# STEP 2: Register a new Driver (DRIVER)
# =============================================================================
print_step "2" "Register a new Driver (DRIVER)"

REGISTER_DRIVER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "driver_d",
        "password": "password123",
        "role": "ROLE_DRIVER"
    }')

REGISTER_DRIVER_BODY=$(echo "$REGISTER_DRIVER_RESPONSE" | sed '$d')
REGISTER_DRIVER_STATUS=$(echo "$REGISTER_DRIVER_RESPONSE" | tail -n1)

print_response "$REGISTER_DRIVER_BODY"
echo "HTTP Status: $REGISTER_DRIVER_STATUS"

if [ "$REGISTER_DRIVER_STATUS" -eq 200 ] || [ "$REGISTER_DRIVER_STATUS" -eq 201 ]; then
    print_success "Driver registered successfully"
else
    print_failure "Failed to register driver (Status: $REGISTER_DRIVER_STATUS)"
fi

# =============================================================================
# STEP 3: Login Passenger (Get USER_TOKEN)
# =============================================================================
print_step "3" "Login Passenger (Get USER_TOKEN)"

LOGIN_PASSENGER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "passenger_p",
        "password": "password123"
    }')

LOGIN_PASSENGER_BODY=$(echo "$LOGIN_PASSENGER_RESPONSE" | sed '$d')
LOGIN_PASSENGER_STATUS=$(echo "$LOGIN_PASSENGER_RESPONSE" | tail -n1)

print_response "$LOGIN_PASSENGER_BODY"
echo "HTTP Status: $LOGIN_PASSENGER_STATUS"

# Extract USER_TOKEN from response
USER_TOKEN=$(echo "$LOGIN_PASSENGER_BODY" | jq -r '.token // empty')

if [ -n "$USER_TOKEN" ] && [ "$USER_TOKEN" != "null" ]; then
    print_success "Passenger login successful"
    echo -e "${GREEN}USER_TOKEN: ${USER_TOKEN:0:50}...${NC}"
else
    print_failure "Failed to get USER_TOKEN"
    exit 1
fi

# =============================================================================
# STEP 4: Login Driver (Get DRIVER_TOKEN)
# =============================================================================
print_step "4" "Login Driver (Get DRIVER_TOKEN)"

LOGIN_DRIVER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "driver_d",
        "password": "password123"
    }')

LOGIN_DRIVER_BODY=$(echo "$LOGIN_DRIVER_RESPONSE" | sed '$d')
LOGIN_DRIVER_STATUS=$(echo "$LOGIN_DRIVER_RESPONSE" | tail -n1)

print_response "$LOGIN_DRIVER_BODY"
echo "HTTP Status: $LOGIN_DRIVER_STATUS"

# Extract DRIVER_TOKEN from response
DRIVER_TOKEN=$(echo "$LOGIN_DRIVER_BODY" | jq -r '.token // empty')

if [ -n "$DRIVER_TOKEN" ] && [ "$DRIVER_TOKEN" != "null" ]; then
    print_success "Driver login successful"
    echo -e "${GREEN}DRIVER_TOKEN: ${DRIVER_TOKEN:0:50}...${NC}"
else
    print_failure "Failed to get DRIVER_TOKEN"
    exit 1
fi

# =============================================================================
# STEP 5: Passenger Requests a Ride (Create Ride)
# =============================================================================
print_step "5" "Passenger Requests a Ride (Create Ride)"

CREATE_RIDE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/rides" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -d '{
        "pickupLocation": "Koramangala",
        "dropLocation": "Indiranagar"
    }')

CREATE_RIDE_BODY=$(echo "$CREATE_RIDE_RESPONSE" | sed '$d')
CREATE_RIDE_STATUS=$(echo "$CREATE_RIDE_RESPONSE" | tail -n1)

print_response "$CREATE_RIDE_BODY"
echo "HTTP Status: $CREATE_RIDE_STATUS"

# Extract RIDE_ID from response
RIDE_ID=$(echo "$CREATE_RIDE_BODY" | jq -r '.id // empty')

if [ -n "$RIDE_ID" ] && [ "$RIDE_ID" != "null" ]; then
    print_success "Ride created successfully"
    echo -e "${GREEN}RIDE_ID: $RIDE_ID${NC}"

    # Validate ride status is REQUESTED
    RIDE_STATUS=$(echo "$CREATE_RIDE_BODY" | jq -r '.status // empty')
    if [ "$RIDE_STATUS" == "REQUESTED" ]; then
        print_success "Ride status is REQUESTED"
    else
        print_failure "Expected status REQUESTED, got: $RIDE_STATUS"
    fi
else
    print_failure "Failed to create ride or extract RIDE_ID"
    exit 1
fi

# =============================================================================
# STEP 6: Driver Views Pending Ride Requests
# =============================================================================
print_step "6" "Driver Views Pending Ride Requests"

PENDING_RIDES_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/driver/rides/requests" \
    -H "Authorization: Bearer $DRIVER_TOKEN")

PENDING_RIDES_BODY=$(echo "$PENDING_RIDES_RESPONSE" | sed '$d')
PENDING_RIDES_STATUS=$(echo "$PENDING_RIDES_RESPONSE" | tail -n1)

print_response "$PENDING_RIDES_BODY"
echo "HTTP Status: $PENDING_RIDES_STATUS"

if [ "$PENDING_RIDES_STATUS" -eq 200 ]; then
    # Check if RIDE_ID is in the list
    FOUND_RIDE=$(echo "$PENDING_RIDES_BODY" | jq -r ".[] | select(.id == \"$RIDE_ID\") | .id")
    FOUND_STATUS=$(echo "$PENDING_RIDES_BODY" | jq -r ".[] | select(.id == \"$RIDE_ID\") | .status")

    if [ "$FOUND_RIDE" == "$RIDE_ID" ]; then
        print_success "Ride $RIDE_ID found in pending rides"
        if [ "$FOUND_STATUS" == "REQUESTED" ]; then
            print_success "Ride status is REQUESTED"
        else
            print_failure "Expected status REQUESTED, got: $FOUND_STATUS"
        fi
    else
        print_failure "Ride $RIDE_ID not found in pending rides"
    fi
else
    print_failure "Failed to get pending rides (Status: $PENDING_RIDES_STATUS)"
fi

# =============================================================================
# STEP 7: Driver Accepts the Ride
# =============================================================================
print_step "7" "Driver Accepts the Ride"

ACCEPT_RIDE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/driver/rides/$RIDE_ID/accept" \
    -H "Authorization: Bearer $DRIVER_TOKEN")

ACCEPT_RIDE_BODY=$(echo "$ACCEPT_RIDE_RESPONSE" | sed '$d')
ACCEPT_RIDE_STATUS=$(echo "$ACCEPT_RIDE_RESPONSE" | tail -n1)

print_response "$ACCEPT_RIDE_BODY"
echo "HTTP Status: $ACCEPT_RIDE_STATUS"

if [ "$ACCEPT_RIDE_STATUS" -eq 200 ]; then
    # Validate status is ACCEPTED
    RIDE_STATUS=$(echo "$ACCEPT_RIDE_BODY" | jq -r '.status // empty')
    DRIVER_ID=$(echo "$ACCEPT_RIDE_BODY" | jq -r '.driverId // empty')

    if [ "$RIDE_STATUS" == "ACCEPTED" ]; then
        print_success "Ride status is ACCEPTED"
    else
        print_failure "Expected status ACCEPTED, got: $RIDE_STATUS"
    fi

    if [ -n "$DRIVER_ID" ] && [ "$DRIVER_ID" != "null" ]; then
        print_success "Driver ID is set: $DRIVER_ID"
    else
        print_failure "Driver ID is not set"
    fi
else
    print_failure "Failed to accept ride (Status: $ACCEPT_RIDE_STATUS)"
fi

# =============================================================================
# STEP 8: Passenger (USER) Completes the Ride
# =============================================================================
print_step "8" "Passenger (USER) Completes the Ride"

COMPLETE_RIDE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/rides/$RIDE_ID/complete" \
    -H "Authorization: Bearer $USER_TOKEN")

COMPLETE_RIDE_BODY=$(echo "$COMPLETE_RIDE_RESPONSE" | sed '$d')
COMPLETE_RIDE_STATUS=$(echo "$COMPLETE_RIDE_RESPONSE" | tail -n1)

print_response "$COMPLETE_RIDE_BODY"
echo "HTTP Status: $COMPLETE_RIDE_STATUS"

if [ "$COMPLETE_RIDE_STATUS" -eq 200 ]; then
    # Validate status is COMPLETED
    RIDE_STATUS=$(echo "$COMPLETE_RIDE_BODY" | jq -r '.status // empty')

    if [ "$RIDE_STATUS" == "COMPLETED" ]; then
        print_success "Ride status is COMPLETED"
    else
        print_failure "Expected status COMPLETED, got: $RIDE_STATUS"
    fi
else
    print_failure "Failed to complete ride (Status: $COMPLETE_RIDE_STATUS)"
fi

# =============================================================================
# STEP 9: Passenger Views Their Own Rides
# =============================================================================
print_step "9" "Passenger Views Their Own Rides"

MY_RIDES_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/user/rides" \
    -H "Authorization: Bearer $USER_TOKEN")

MY_RIDES_BODY=$(echo "$MY_RIDES_RESPONSE" | sed '$d')
MY_RIDES_STATUS=$(echo "$MY_RIDES_RESPONSE" | tail -n1)

print_response "$MY_RIDES_BODY"
echo "HTTP Status: $MY_RIDES_STATUS"

if [ "$MY_RIDES_STATUS" -eq 200 ]; then
    # Check if RIDE_ID is in the list with COMPLETED status
    FOUND_RIDE=$(echo "$MY_RIDES_BODY" | jq -r ".[] | select(.id == \"$RIDE_ID\") | .id")
    FOUND_STATUS=$(echo "$MY_RIDES_BODY" | jq -r ".[] | select(.id == \"$RIDE_ID\") | .status")

    if [ "$FOUND_RIDE" == "$RIDE_ID" ]; then
        print_success "Ride $RIDE_ID found in user's rides"
        if [ "$FOUND_STATUS" == "COMPLETED" ]; then
            print_success "Ride status is COMPLETED"
        else
            print_failure "Expected status COMPLETED, got: $FOUND_STATUS"
        fi
    else
        print_failure "Ride $RIDE_ID not found in user's rides"
    fi
else
    print_failure "Failed to get user's rides (Status: $MY_RIDES_STATUS)"
fi

# =============================================================================
# STEP 10: Test Authorization Failure: Driver tries to view /user/rides
# =============================================================================
print_step "10" "Test Authorization Failure: Driver tries to view /user/rides"

FORBIDDEN_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/user/rides" \
    -H "Authorization: Bearer $DRIVER_TOKEN")

FORBIDDEN_BODY=$(echo "$FORBIDDEN_RESPONSE" | sed '$d')
FORBIDDEN_STATUS=$(echo "$FORBIDDEN_RESPONSE" | tail -n1)

print_response "$FORBIDDEN_BODY"
echo "HTTP Status: $FORBIDDEN_STATUS"

if [ "$FORBIDDEN_STATUS" -eq 403 ]; then
    print_success "Authorization correctly denied (HTTP 403 Forbidden)"
else
    print_failure "Expected HTTP 403, got: $FORBIDDEN_STATUS"
fi

# =============================================================================
# TEST SUMMARY
# =============================================================================
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Test Suite Completed${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Summary of tokens and IDs used:"
echo -e "  USER_TOKEN:   ${USER_TOKEN:0:50}..."
echo -e "  DRIVER_TOKEN: ${DRIVER_TOKEN:0:50}..."
echo -e "  RIDE_ID:      $RIDE_ID"
echo ""
```

</details>

---

## Error Handling

The application provides consistent error responses:

| Status Code | Error Type | Example |
|-------------|------------|---------|
| 400 | Bad Request | Validation errors, invalid state transitions |
| 403 | Forbidden | Unauthorized role access |
| 404 | Not Found | Ride/User not found |
| 500 | Internal Server Error | Unexpected errors |

**Example Error Response:**
```json
{
  "error": "BAD_REQUEST",
  "message": "Ride cannot be accepted. Current status: ACCEPTED",
  "timestamp": "2025-12-07T08:19:52.855Z",
  "status": 400
}
```

---

## License

This project is for demonstration purposes.

