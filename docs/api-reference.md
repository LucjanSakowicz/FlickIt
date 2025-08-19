# 🚀 FlickIt Backend - API Reference

Complete API documentation for the FlickIt Backend application, including all endpoints, request/response formats, and authentication details.

## 📋 Table of Contents

- [**Authentication**](#authentication)
- [**User Management**](#user-management)
- [**Event Management**](#event-management)
- [**Claim System**](#claim-system)
- [**Rating System**](#rating-system)
- [**Notification Service**](#notification-service)
- [**AI Service**](#ai-service)
- [**Data Models**](#data-models)
- [**Error Handling**](#error-handling)

---

## 🔐 Authentication

### **JWT Token Authentication**

All protected endpoints require a valid JWT token in the `Authorization` header:

```http
Authorization: Bearer <jwt_token>
```

### **Token Generation**

#### **Login**
```http
POST /auth/login
Content-Type: application/json

{
  "phone": "+48123456789",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "John Doe",
    "phone": "+48123456789",
    "role": "VENDOR"
  }
}
```

#### **Generate Test Token (Admin Only)**
```http
POST /auth/token
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "role": "VENDOR"
}
```

---

## 👥 User Management

### **User Registration**

```http
POST /users
Content-Type: application/json

{
  "name": "John Doe",
  "phone": "+48123456789",
  "password": "password123"
}
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "John Doe",
  "phone": "+48123456789",
  "role": "CUSTOMER",
  "createdAt": "2025-08-19T20:00:00Z"
}
```

### **Get User Profile**

```http
GET /users/me
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "John Doe",
  "phone": "+48123456789",
  "role": "VENDOR",
  "rating": 4.5,
  "ratingCount": 12,
  "createdAt": "2025-08-19T20:00:00Z",
  "updatedAt": "2025-08-19T20:00:00Z"
}
```

---

## 🎯 Event Management

### **Create Event (VENDOR/ADMIN Only)**

```http
POST /events
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "titleVendor": "Specjalna Promocja Lunch",
  "descriptionVendor": "Dzisiejsze menu w super cenie!",
  "lat": 52.2297,
  "lon": 21.0122,
  "alt": 120.0,
  "floor": 1,
  "category": "FOOD",
  "expiresAt": "2025-08-20T20:00:00Z",
  "images": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ]
}
```

**Response:**
```json
{
  "id": "456e7890-e89b-12d3-a456-426614174000",
  "title": "Specjalna Promocja Lunch",
  "description": "Dzisiejsze menu w super cenie!",
  "lat": 52.2297,
  "lon": 21.0122,
  "alt": 120.0,
  "floor": 1,
  "category": "FOOD",
  "status": "ACTIVE",
  "vendorId": "123e4567-e89b-12d3-a456-426614174000",
  "expiresAt": "2025-08-20T20:00:00Z",
  "createdAt": "2025-08-19T20:00:00Z"
}
```

### **Search Events by Location**

```http
GET /events/latest?lat=52.2297&lon=21.0122&radius=2000
```

**Response:**
```json
{
  "events": [
    {
      "id": "456e7890-e89b-12d3-a456-426614174000",
      "title": "Specjalna Promocja Lunch",
      "description": "Dzisiejsze menu w super cenie!",
      "lat": 52.2297,
      "lon": 21.0122,
      "alt": 120.0,
      "floor": 1,
      "category": "FOOD",
      "status": "ACTIVE",
      "distance": 0.0
    }
  ],
  "total": 1,
  "radius": 2000
}
```

---

## 🎫 Claim System

### **Claim Event (CUSTOMER Only)**

```http
PUT /events/{eventId}/claim
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "eventId": "456e7890-e89b-12d3-a456-426614174000",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "claimedAt": "2025-08-19T20:00:00Z",
  "expiresAt": "2025-08-20T20:00:00Z",
  "status": "CLAIMED"
}
```

---

## ⭐ Rating System

### **Rate Event (CUSTOMER Only)**

```http
POST /ratings
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "eventId": "456e7890-e89b-12d3-a456-426614174000",
  "rating": 5,
  "comment": "Świetna promocja, bardzo polecam!"
}
```

**Response:**
```json
{
  "id": "789e0123-e89b-12d3-a456-426614174000",
  "eventId": "456e7890-e89b-12d3-a456-426614174000",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "rating": 5,
  "comment": "Świetna promocja, bardzo polecam!",
  "createdAt": "2025-08-19T20:00:00Z"
}
```

---

## 📱 Notification Service

### **Subscribe to Notifications (CUSTOMER Only)**

```http
POST /notifications/subscribe
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "fcmToken": "fcm_token_here",
  "radiusMeters": 2000.0,
  "latitude": 52.2297,
  "longitude": 21.0122
}
```

**Response:**
```json
{
  "id": "abc123-def456-ghi789",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "fcmToken": "fcm_token_here",
  "radiusMeters": 2000.0,
  "latitude": 52.2297,
  "longitude": 21.0122,
  "isActive": true,
  "createdAt": "2025-08-19T20:00:00Z"
}
```

### **Get User Subscriptions**

```http
GET /notifications/subscriptions
Authorization: Bearer <jwt_token>
```

### **Unsubscribe from Notifications**

```http
DELETE /notifications/unsubscribe
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "fcmToken": "fcm_token_here"
}
```

---

## 🤖 AI Service

### **Generate AI Content (VENDOR/ADMIN Only)**

```http
POST /ai/generate-content
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "imageUrl": "https://example.com/restaurant-interior.jpg",
  "businessType": "restaurant",
  "additionalPrompt": "Specjalna promocja na lunch",
  "preferredLanguage": "pl"
}
```

**Response:**
```json
{
  "title": "Elegancka Restauracja z Promocją Lunch",
  "description": "Odkryj nasze wyjątkowe menu lunchowe w eleganckim wnętrzu. " +
                 "Specjalne promocje dostępne codziennie w godzinach 12:00-15:00.",
  "suggestedCategory": "FOOD",
  "confidence": 0.92,
  "modelUsed": "gpt-4-vision-preview",
  "processingTimeMs": 1250
}
```

### **AI Service Health Check**

```http
GET /ai/health
```

**Response:**
```
AI Service is healthy and ready to process requests
```

### **Test AI Generation (ADMIN Only)**

```http
POST /ai/test
Authorization: Bearer <admin_token>
```

---

## 📊 Data Models

### **User Entity**

```json
{
  "id": "UUID",
  "name": "String",
  "phone": "String (E.164)",
  "passwordHash": "String (BCrypt)",
  "role": "CUSTOMER | VENDOR | ADMIN",
  "rating": "Double (1.0-5.0)",
  "ratingCount": "Integer",
  "createdAt": "Instant",
  "updatedAt": "Instant"
}
```

### **Event Entity**

```json
{
  "id": "UUID",
  "titleAi": "String",
  "titleVendor": "String",
  "descriptionAi": "String",
  "descriptionVendor": "String",
  "lat": "Double (WGS84)",
  "lon": "Double (WGS84)",
  "alt": "Double (meters)",
  "floor": "Integer",
  "category": "FOOD | SERVICE | OTHER",
  "status": "ACTIVE | CLAIMED | EXPIRED | REMOVED",
  "vendorId": "UUID",
  "expiresAt": "Instant",
  "createdAt": "Instant",
  "updatedAt": "Instant"
}
```

### **Claim Entity**

```json
{
  "eventId": "UUID",
  "userId": "UUID",
  "claimedAt": "Instant",
  "expiresAt": "Instant",
  "status": "CLAIMED | COMPLETED | EXPIRED"
}
```

### **Rating Entity**

```json
{
  "id": "UUID",
  "eventId": "UUID",
  "userId": "UUID",
  "rating": "Integer (1-5)",
  "comment": "String",
  "createdAt": "Instant"
}
```

### **Notification Subscription Entity**

```json
{
  "id": "UUID",
  "userId": "UUID",
  "fcmToken": "String",
  "radiusMeters": "Double",
  "latitude": "Double",
  "longitude": "Double",
  "isActive": "Boolean",
  "createdAt": "Instant",
  "lastNotificationSent": "Instant"
}
```

---

## ❌ Error Handling

### **Standard Error Response Format**

```json
{
  "timestamp": "2025-08-19T20:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error description",
  "path": "/api/endpoint",
  "details": {
    "field": "Additional error details"
  }
}
```

### **Common HTTP Status Codes**

| Status | Description | Common Causes |
|--------|-------------|---------------|
| **200** | OK | Successful request |
| **201** | Created | Resource created successfully |
| **400** | Bad Request | Invalid request data, validation errors |
| **401** | Unauthorized | Missing or invalid authentication |
| **403** | Forbidden | Insufficient permissions for the role |
| **404** | Not Found | Resource doesn't exist |
| **409** | Conflict | Resource already exists or in invalid state |
| **422** | Unprocessable Entity | Business logic validation failed |
| **500** | Internal Server Error | Server-side error |

### **Validation Error Example**

```json
{
  "timestamp": "2025-08-19T20:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/events",
  "details": {
    "titleVendor": "Title is required",
    "lat": "Latitude must be between -90 and 90",
    "expiresAt": "Expiry date must be in the future"
  }
}
```

### **Authentication Error Example**

```json
{
  "timestamp": "2025-08-19T20:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Required roles: VENDOR, ADMIN",
  "path": "/events"
}
```

---

## 🔒 Security & Authorization

### **Role-Based Access Control**

| Endpoint | Method | CUSTOMER | VENDOR | ADMIN | Public |
|----------|--------|----------|--------|-------|--------|
| `/users` | POST | ✅ | ✅ | ✅ | ✅ |
| `/users/me` | GET | ✅ | ✅ | ✅ | ❌ |
| `/events` | POST | ❌ | ✅ | ✅ | ❌ |
| `/events/latest` | GET | ✅ | ✅ | ✅ | ✅ |
| `/events/{id}/claim` | PUT | ✅ | ❌ | ❌ | ❌ |
| `/ratings` | POST | ✅ | ❌ | ❌ | ❌ |
| `/notifications/subscribe` | POST | ✅ | ❌ | ❌ | ❌ |
| `/ai/generate-content` | POST | ❌ | ✅ | ✅ | ❌ |
| `/ai/health` | GET | ✅ | ✅ | ✅ | ✅ |
| `/ai/test` | POST | ❌ | ❌ | ✅ | ❌ |

### **JWT Token Security**

- **Algorithm**: HS256 (HMAC with SHA-256)
- **Expiration**: Configurable (default: 24 hours)
- **Claims**: User ID, Role, Issued At, Expiration
- **Storage**: Secure HTTP-only cookies recommended for production

---

## 📈 Rate Limiting

### **Current Limits**

| Endpoint Category | Rate Limit | Window |
|------------------|------------|---------|
| Authentication | 5 requests | 1 minute |
| Event Creation | 10 requests | 1 hour |
| AI Generation | 20 requests | 1 hour |
| General API | 100 requests | 1 minute |

### **Rate Limit Headers**

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995200
```

---

## 🔧 API Versioning

### **Current Version**
- **Version**: v1
- **Base Path**: `/api/v1` (optional, backward compatible)
- **Deprecation Policy**: 6 months notice for breaking changes

### **Version Header**
```http
Accept: application/vnd.flickit.v1+json
```

---

## 📚 Additional Resources

- **Swagger UI**: Available at `/swagger-ui.html` when running locally
- **OpenAPI Spec**: Available at `/v3/api-docs`
- **Postman Collection**: Available in [docs/postman/](./postman/)
- **Architecture Diagrams**: See [docs/architecture-diagrams.md](./architecture-diagrams.md)

---

**🚀 FlickIt Backend API** - Complete reference for developers and integrators.

*Last updated: August 19, 2025*
