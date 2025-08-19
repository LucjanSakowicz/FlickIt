# FlickIt Backend - Architecture Diagrams

## 📊 System Architecture Overview

```mermaid
graph TB
    subgraph "Mobile App"
        MA[Mobile App]
    end
    
    subgraph "Backend API"
        API[Spring Boot API]
        SEC[Security Layer]
        JWT[JWT Authentication]
    end
    
    subgraph "Services"
        US[User Service]
        ES[Event Service]
        CS[Claim Service]
        RS[Rating Service]
        NS[Notification Service]
        AI[AI Service]
    end
    
    subgraph "Data Layer"
        DB[(PostgreSQL)]
        H2[(H2 Test DB)]
    end
    
    subgraph "External Services"
        FCM[Firebase Cloud Messaging]
        OPENAI[OpenAI API]
    end
    
    MA --> API
    API --> SEC
    SEC --> JWT
    API --> US
    API --> ES
    API --> CS
    API --> RS
    API --> NS
    API --> AI
    
    US --> DB
    ES --> DB
    CS --> DB
    RS --> DB
    NS --> DB
    AI --> DB
    
    NS --> FCM
    AI --> OPENAI
    
    H2 -.->|Test| US
    H2 -.->|Test| ES
    H2 -.->|Test| CS
    H2 -.->|Test| RS
    H2 -.->|Test| NS
    H2 -.->|Test| AI
```

## 🔐 Authentication & Authorization Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant JwtService
    participant UserService
    participant SecurityConfig
    
    Client->>AuthController: POST /auth/login
    AuthController->>UserService: authenticate(phone, password)
    UserService->>UserService: validate credentials
    UserService-->>AuthController: UserEntity + role
    
    AuthController->>JwtService: generateToken(userId, role)
    JwtService-->>AuthController: JWT Token
    
    AuthController-->>Client: JWT Token
    
    Note over Client,SecurityConfig: Subsequent requests
    
    Client->>SecurityConfig: Request + JWT Header
    SecurityConfig->>JwtService: validateToken(token)
    JwtService-->>SecurityConfig: User details + role
    
    SecurityConfig->>SecurityConfig: Check @PreAuthorize
    SecurityConfig-->>Client: 200 OK / 403 Forbidden
```

## 🎯 Event Management Flow

```mermaid
flowchart TD
    A[Vendor Login] --> B[Create Event]
    B --> C{AI Generation?}
    
    C -->|Yes| D[Upload Image]
    C -->|No| E[Manual Entry]
    
    D --> F[AI Service Analysis]
    F --> G[Generate Title/Description]
    G --> H[Save Event]
    
    E --> H
    
    H --> I[Event Published]
    I --> J[Notification Service]
    J --> K[Find Subscribers in Radius]
    K --> L[Send FCM Notifications]
    
    L --> M[Customer Receives Notification]
    M --> N[Customer Views Event]
    N --> O[Customer Claims Event]
    
    O --> P[Claim Service]
    P --> Q[Update Event Status]
    Q --> R[Notify Vendor]
```

## 🏗️ Module Dependencies

```mermaid
graph LR
    subgraph "Controllers"
        UC[UserController]
        EC[EventController]
        CC[ClaimController]
        RC[RatingController]
        NC[NotificationController]
        AC[AIController]
    end
    
    subgraph "Services"
        US[UserService]
        ES[EventService]
        CS[ClaimService]
        RS[RatingService]
        NS[NotificationService]
        AIS[AIService]
    end
    
    subgraph "Repositories"
        UR[UserRepository]
        ER[EventRepository]
        CR[ClaimRepository]
        RR[RatingRepository]
        NR[NotificationRepository]
    end
    
    UC --> US
    EC --> ES
    EC --> CS
    CC --> CS
    RC --> RS
    NC --> NS
    AC --> AIS
    
    US --> UR
    ES --> ER
    CS --> ER
    RS --> RR
    RS --> ER
    NS --> NR
    AIS --> ER
```

## 🔄 Data Flow for Event Rating

```mermaid
sequenceDiagram
    participant Customer
    participant RatingController
    participant RatingService
    participant EventService
    participant NotificationService
    participant FCMService
    participant Vendor
    
    Customer->>RatingController: POST /ratings
    RatingController->>RatingService: createRating(request)
    
    RatingService->>RatingService: Validate event exists
    RatingService->>RatingService: Check if already rated
    RatingService->>RatingService: Save rating
    
    RatingService->>EventService: updateVendorRating(eventId)
    EventService->>EventService: Calculate new average rating
    
    RatingService->>NotificationService: sendRatingNotification(eventId, rating)
    NotificationService->>FCMService: Send notification to vendor
    
    FCMService-->>Vendor: Push notification
    Vendor->>Vendor: View rating update
    
    RatingService-->>RatingController: RatingDto
    RatingController-->>Customer: 201 Created
```

## 🗄️ Database Schema

```mermaid
erDiagram
    USERS {
        UUID id PK
        String name
        String phone UK
        String passwordHash
        Enum role
        Timestamp createdAt
        Timestamp updatedAt
    }
    
    EVENTS {
        UUID id PK
        String titleAi
        String titleVendor
        String descriptionAi
        String descriptionVendor
        Decimal lat
        Decimal lon
        Decimal alt
        Integer floor
        Enum category
        Enum status
        UUID vendorId FK
        Timestamp expiresAt
        Timestamp createdAt
        Timestamp updatedAt
    }
    
    CLAIMS {
        UUID eventId PK,FK
        UUID userId PK,FK
        Timestamp claimedAt
        Timestamp expiresAt
        Enum status
    }
    
    RATINGS {
        UUID id PK
        UUID eventId FK
        UUID userId FK
        Integer rating
        String comment
        Timestamp createdAt
    }
    
    NOTIFICATION_SUBSCRIPTIONS {
        UUID id PK
        UUID userId FK
        String fcmToken
        Decimal radiusMeters
        Decimal latitude
        Decimal longitude
        Boolean isActive
        Timestamp createdAt
        Timestamp lastNotificationSent
    }
    
    USERS ||--o{ EVENTS : "creates"
    USERS ||--o{ CLAIMS : "claims"
    USERS ||--o{ RATINGS : "rates"
    USERS ||--o{ NOTIFICATION_SUBSCRIPTIONS : "subscribes"
    EVENTS ||--o{ CLAIMS : "has"
    EVENTS ||--o{ RATINGS : "receives"
```

## 🚀 API Endpoint Structure

```mermaid
graph TD
    subgraph "Public Endpoints"
        P1[POST /users - Registration]
        P2[POST /auth/login - Login]
        P3[GET /events/latest - Search Events]
        P4[GET /ai/health - AI Health Check]
    end
    
    subgraph "Customer Endpoints"
        C1[GET /users/me - Profile]
        C2[PUT /events/{id}/claim - Claim Event]
        C3[POST /ratings - Rate Event]
        C4[POST /notifications/subscribe - Subscribe]
        C5[GET /notifications/subscriptions - List Subscriptions]
        C6[DELETE /notifications/unsubscribe - Unsubscribe]
    end
    
    subgraph "Vendor Endpoints"
        V1[POST /events - Create Event]
        V2[GET /users/me - Profile]
        V3[POST /ai/generate-content - AI Content]
    end
    
    subgraph "Admin Endpoints"
        A1[POST /ai/test - Test AI]
        A2[POST /auth/token - Generate Token]
        A3[POST /auth/test-token - Test Token]
    end
    
    P1 --> C1
    P2 --> C1
    P2 --> V1
    P2 --> A1
```

## 🔧 Security Configuration

```mermaid
graph LR
    subgraph "Security Chain"
        A[HTTP Request] --> B[CSRF Filter]
        B --> C[JWT Authentication Filter]
        C --> D[Authorization Filter]
        D --> E[Controller]
    end
    
    subgraph "JWT Processing"
        C --> F[Extract Token]
        F --> G[Validate Token]
        G --> H[Set Security Context]
    end
    
    subgraph "Role-Based Access"
        D --> I[Check @PreAuthorize]
        I --> J{Has Role?}
        J -->|Yes| E
        J -->|No| K[403 Forbidden]
    end
    
    subgraph "Public Endpoints"
        L[/users] --> M[Permit All]
        N[/events/latest] --> M
        O[/ai/health] --> M
        P[/v3/api-docs/**] --> M
        Q[/swagger-ui/**] --> M
    end
```

## 📱 Notification Flow

```mermaid
flowchart TD
    A[Event Created] --> B[Notification Service]
    B --> C[Find Subscribers in Radius]
    
    C --> D{Subscribers Found?}
    D -->|No| E[End]
    D -->|Yes| F[Group by FCM Token]
    
    F --> G[FCM Service]
    G --> H[Send Batch Notifications]
    
    H --> I{Success?}
    I -->|Yes| J[Update Last Sent]
    I -->|No| K[Log Error]
    
    J --> L[Customer Receives Push]
    K --> M[Retry Later]
    
    L --> N[Customer Opens App]
    N --> O[View Event Details]
    O --> P[Claim Event]
```

## 🧪 Testing Strategy

```mermaid
graph TD
    subgraph "Unit Tests"
        UT1[Service Tests]
        UT2[Repository Tests]
        UT3[Utility Tests]
    end
    
    subgraph "Integration Tests"
        IT1[Controller Tests]
        IT2[Security Tests]
        IT3[Database Tests]
    end
    
    subgraph "Test Configuration"
        TC1[H2 In-Memory DB]
        TC2[Mock External Services]
        TC3[Test Security Context]
    end
    
    subgraph "Test Categories"
        CAT1[Happy Path]
        CAT2[Error Scenarios]
        CAT3[Security Violations]
        CAT4[Edge Cases]
    end
    
    UT1 --> IT1
    UT2 --> IT2
    UT3 --> IT3
    
    IT1 --> TC1
    IT2 --> TC2
    IT3 --> TC3
    
    TC1 --> CAT1
    TC2 --> CAT2
    TC3 --> CAT3
    TC3 --> CAT4
```

## 📊 Performance Metrics

```mermaid
graph LR
    subgraph "Response Times"
        RT1[< 100ms - Health Checks]
        RT2[< 500ms - Simple Queries]
        RT3[< 2000ms - AI Generation]
        RT4[< 1000ms - Complex Queries]
    end
    
    subgraph "Throughput"
        T1[1000 req/s - Read Operations]
        T2[100 req/s - Write Operations]
        T3[50 req/s - AI Operations]
    end
    
    subgraph "Availability"
        A1[99.9% - Core Services]
        A2[99.5% - AI Services]
        A3[99.99% - Health Endpoints]
    end
    
    subgraph "Scalability"
        S1[Horizontal Scaling]
        S2[Database Connection Pooling]
        S3[Async Processing]
        S4[Caching Strategy]
    end
```

---

## 📝 How to Use These Diagrams

### **Mermaid Live Editor**
1. Go to [https://mermaid.live/](https://mermaid.live/)
2. Copy any diagram code from above
3. Paste into the editor
4. View rendered diagram
5. Export as PNG/SVG

### **GitHub Integration**
- GitHub automatically renders Mermaid diagrams in markdown files
- Just commit this file to your repository
- Diagrams will be visible in README.md or any markdown file

### **Documentation Updates**
- Update diagrams when architecture changes
- Keep examples current with actual API responses
- Add new diagrams for new features

### **Team Collaboration**
- Use diagrams in technical discussions
- Include in API documentation
- Reference in development tickets
- Share with frontend team for integration planning
