# FlickIt Backend API - Postman Collection

## 📋 Overview

This Postman collection provides a comprehensive set of API requests for testing and interacting with the FlickIt Backend application. The collection includes all endpoints organized by functional modules with proper authentication and environment variables.

## 🚀 Quick Start

### 1. Import Collection
1. Download `FlickIt_Backend_API.postman_collection.json`
2. Open Postman and click "Import"
3. Select the downloaded collection file
4. The collection will appear in your Postman workspace

### 2. Import Environment
1. Download `FlickIt_Local.postman_environment.json`
2. In Postman, click "Import" again
3. Select the environment file
4. Select the "FlickIt Local Development" environment from the dropdown

### 3. Start the Backend
```bash
# Using Docker Compose
make docker-run

# Or using Maven
make run
```

## 🔐 Authentication Flow

### Step 1: User Registration
1. Use the **"User Registration"** request in the User Management folder
2. This will create a new user account
3. The response will contain a `user_id` that gets automatically stored

### Step 2: User Login
1. Use the **"User Login"** request in the Authentication folder
2. Use the credentials from registration
3. The JWT token will be automatically extracted and stored

### Step 3: Use Protected Endpoints
- All subsequent requests will automatically include the JWT token
- The token is valid for the duration specified in your JWT configuration

## 📁 Collection Structure

### 🔐 Authentication
- **User Login**: Authenticate and receive JWT token
- **Generate Test Token**: Create tokens for testing (Admin only)

### 👥 User Management
- **User Registration**: Create new user accounts
- **Get User Profile**: Retrieve current user information

### 🎯 Event Management
- **Create Event**: Create new events (Vendor/Admin only)
- **Search Events**: Find events by location and radius

### 🎫 Claim System
- **Claim Event**: Claim events (Customer only)

### ⭐ Rating System
- **Rate Event**: Rate and comment on events (Customer only)

### 📱 Notification Service
- **Subscribe**: Set up location-based notifications
- **Get Subscriptions**: View current notification settings
- **Unsubscribe**: Remove notification subscriptions

### 🤖 AI Service
- **Generate Content**: AI-powered content generation (Vendor/Admin only)
- **Health Check**: Verify AI service status
- **Test Generation**: Test endpoint (Admin only)

### 🔍 Testing & Utilities
- **Swagger UI**: Access interactive API documentation
- **OpenAPI Spec**: Get API specification
- **Health Check**: Application health status

## 🔧 Environment Variables

The collection uses the following environment variables:

| Variable | Description | Default Value |
|-----------|-------------|---------------|
| `base_url` | Backend API base URL | `http://localhost:8080` |
| `jwt_token` | JWT authentication token | Auto-populated |
| `user_id` | Current user ID | Auto-populated |
| `event_id` | Current event ID | Auto-populated |
| `test_user_phone` | Test user phone number | `+48123456789` |
| `test_user_password` | Test user password | `password123` |
| `test_vendor_phone` | Test vendor phone number | `+48987654321` |
| `test_vendor_password` | Test vendor password | `vendor123` |
| `test_admin_phone` | Test admin phone number | `+48111222333` |
| `test_admin_password` | Test admin password | `admin123` |
| `test_fcm_token` | Test FCM token | `fcm_test_token_12345` |
| `test_latitude` | Test latitude | `52.2297` |
| `test_longitude` | Test longitude | `21.0122` |
| `test_radius_meters` | Test radius | `2000` |
| `test_image_url` | Test image URL | `https://example.com/test-image.jpg` |

## 🧪 Testing Workflow

### 1. Basic User Flow
```bash
1. Register User → Get user_id
2. Login → Get JWT token
3. Create Event → Get event_id
4. Claim Event (as different user)
5. Rate Event
6. Subscribe to Notifications
```

### 2. Vendor Flow
```bash
1. Register Vendor → Get user_id
2. Login → Get JWT token
3. Create Event → Get event_id
4. Use AI Content Generation
5. Check Event Status
```

### 3. Admin Flow
```bash
1. Register Admin → Get user_id
2. Login → Get JWT token
3. Generate Test Tokens
4. Test AI Generation
5. Monitor System Health
```

## 📝 Request Examples

### User Registration
```json
{
  "name": "John Doe",
  "phone": "{{test_user_phone}}",
  "password": "{{test_user_password}}"
}
```

### Event Creation
```json
{
  "titleVendor": "Specjalna Promocja Lunch",
  "descriptionVendor": "Dzisiejsze menu w super cenie!",
  "lat": {{test_latitude}},
  "lon": {{test_longitude}},
  "alt": 120.0,
  "floor": 1,
  "category": "FOOD",
  "expiresAt": "2025-08-20T20:00:00Z",
  "images": [
    "{{test_image_url}}"
  ]
}
```

### AI Content Generation
```json
{
  "imageUrl": "{{test_image_url}}",
  "businessType": "restaurant",
  "additionalPrompt": "Specjalna promocja na lunch",
  "preferredLanguage": "pl"
}
```

## 🚨 Common Issues

### 1. Authentication Errors (401/403)
- Ensure you've completed the login flow
- Check that the JWT token is valid
- Verify user has required role for the endpoint

### 2. Validation Errors (400)
- Check required fields in request body
- Verify data types and formats
- Ensure phone numbers are in international format

### 3. Server Errors (500)
- Check backend logs for detailed error information
- Verify database connection
- Check if all required services are running

## 🔄 Auto-Population Scripts

The collection includes Postman scripts that automatically:
- Extract JWT tokens from login responses
- Store user IDs from registration responses
- Store event IDs from event creation responses
- Set environment variables for subsequent requests

## 📊 Monitoring & Testing

### Health Checks
- Use the health check endpoints to verify service status
- Monitor response times and error rates
- Check AI service availability

### Performance Testing
- Use Postman's built-in performance testing features
- Monitor response times under load
- Test concurrent user scenarios

## 🔗 Related Documentation

- [API Reference](../api-reference.md)
- [Development Guide](../development-guide.md)
- [Deployment Guide](../deployment-guide.md)
- [Architecture Diagrams](../architecture-diagrams.md)

## 📞 Support

For issues with the API or Postman collection:
1. Check the backend logs
2. Verify environment configuration
3. Review the API documentation
4. Check GitHub issues for known problems

---

**Note**: This collection is designed for local development. For production testing, update the `base_url` environment variable and ensure proper security measures are in place.
