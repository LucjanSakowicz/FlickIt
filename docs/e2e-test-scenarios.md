# FlickIt Backend - E2E Test Scenarios

## 📋 Overview

This document contains comprehensive End-to-End (E2E) test scenarios for the FlickIt Backend application. These scenarios cover all user flows, business processes, and edge cases to ensure complete application functionality.

## 🎯 Test Categories

### 1. **Authentication & Authorization**
### 2. **User Management**
### 3. **Event Management**
### 4. **Claim System**
### 5. **Rating System**
### 6. **Notification Service**
### 7. **AI Service**
### 8. **Integration Flows**
### 9. **Error Handling**
### 10. **Performance & Load**

---

## 🔐 **1. Authentication & Authorization Scenarios**

### **1.1 User Registration Flow**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `AUTH-001` | **Successful User Registration** | 1. Send POST `/users` with valid data<br>2. Verify response 201<br>3. Check user created in DB | User created, returns user data with ID |
| `AUTH-002` | **Duplicate Phone Registration** | 1. Register user with phone +48123456789<br>2. Try to register another user with same phone | Returns 400 with duplicate phone error |
| `AUTH-003` | **Invalid Phone Format** | 1. Send registration with invalid phone format | Returns 400 with validation error |
| `AUTH-004` | **Missing Required Fields** | 1. Send registration without name/phone/password | Returns 400 with field validation errors |
| `AUTH-005` | **Password Strength Validation** | 1. Try registration with weak password | Returns 400 with password strength error |

### **1.2 User Login Flow**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `AUTH-006` | **Successful Login** | 1. Send POST `/auth/login` with valid credentials<br>2. Verify JWT token returned | Returns 200 with valid JWT token |
| `AUTH-007` | **Invalid Credentials** | 1. Try login with wrong password | Returns 401 Unauthorized |
| `AUTH-008` | **Non-existent User** | 1. Try login with non-existent phone | Returns 401 Unauthorized |
| `AUTH-009` | **Malformed Login Request** | 1. Send login without required fields | Returns 400 Bad Request |

### **1.3 JWT Token Management**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `AUTH-010` | **Valid Token Access** | 1. Login to get token<br>2. Use token for protected endpoint | Access granted to protected resource |
| `AUTH-011` | **Expired Token Access** | 1. Wait for token expiration<br>2. Try to access protected endpoint | Returns 401 with expired token error |
| `AUTH-012` | **Invalid Token Format** | 1. Send request with malformed token | Returns 401 with invalid token error |
| `AUTH-013` | **Missing Token** | 1. Access protected endpoint without token | Returns 401 with missing token error |

### **1.4 Role-Based Access Control**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `AUTH-014` | **Customer Role Access** | 1. Login as customer<br>2. Try to create event | Returns 403 Forbidden |
| `AUTH-015` | **Vendor Role Access** | 1. Login as vendor<br>2. Create event successfully | Event created successfully |
| `AUTH-016` | **Admin Role Access** | 1. Login as admin<br>2. Access admin-only endpoints | Access granted to admin resources |
| `AUTH-017` | **Role Escalation Prevention** | 1. Try to modify user role via API | Returns 403 Forbidden |

---

## 👥 **2. User Management Scenarios**

### **2.1 User Profile Operations**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `USER-001` | **Get User Profile** | 1. Login as user<br>2. GET `/users/me` | Returns user profile data |
| `USER-002` | **Update User Profile** | 1. Login as user<br>2. PUT `/users/me` with new data | Profile updated successfully |
| `USER-003` | **Profile Privacy** | 1. User A tries to access User B profile | Returns 403 Forbidden |
| `USER-004` | **Profile Validation** | 1. Try to update with invalid data | Returns 400 with validation errors |

### **2.2 User Data Management**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `USER-005` | **User Deletion** | 1. Login as admin<br>2. DELETE user account | User account removed |
| `USER-006` | **User Search** | 1. Admin searches for users | Returns filtered user list |
| `USER-007` | **User Statistics** | 1. Admin requests user analytics | Returns user count and metrics |

---

## 🎯 **3. Event Management Scenarios**

### **3.1 Event Creation**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `EVENT-001` | **Successful Event Creation** | 1. Login as vendor<br>2. POST `/events` with valid data | Event created with 201 status |
| `EVENT-002` | **Event with AI Content** | 1. Create event with AI-generated content | Event created with AI titles/descriptions |
| `EVENT-003` | **Event Validation** | 1. Try to create event with invalid data | Returns 400 with validation errors |
| `EVENT-004` | **Location Validation** | 1. Create event with invalid coordinates | Returns 400 with location error |
| `EVENT-005` | **Image URL Validation** | 1. Create event with invalid image URLs | Returns 400 with image validation error |

### **3.2 Event Retrieval & Search**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `EVENT-006` | **Get Latest Events** | 1. GET `/events/latest` | Returns paginated event list |
| `EVENT-007` | **Location-Based Search** | 1. Search events near specific coordinates | Returns events within radius |
| `EVENT-008` | **Category Filtering** | 1. Filter events by category | Returns filtered event list |
| `EVENT-009` | **Date Range Filtering** | 1. Filter events by date range | Returns events in date range |
| `EVENT-010` | **Vendor Event List** | 1. Get events for specific vendor | Returns vendor's events |

### **3.3 Event Updates & Management**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `EVENT-011` | **Update Event** | 1. Login as event owner<br>2. PUT `/events/{id}` | Event updated successfully |
| `EVENT-012` | **Unauthorized Update** | 1. User tries to update another's event | Returns 403 Forbidden |
| `EVENT-013` | **Event Deletion** | 1. Owner deletes event | Event removed from system |
| `EVENT-014` | **Event Expiration** | 1. Check expired events | Expired events marked appropriately |

---

## 🎫 **4. Claim System Scenarios**

### **4.1 Event Claiming**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `CLAIM-001` | **Successful Event Claim** | 1. Customer claims available event | Event status changed to CLAIMED |
| `CLAIM-002` | **Already Claimed Event** | 1. Try to claim already claimed event | Returns 400 with already claimed error |
| `CLAIM-003` **Expired Event Claim** | 1. Try to claim expired event | Returns 400 with expired event error |
| `CLAIM-004` | **Vendor Self-Claim** | 1. Vendor tries to claim own event | Returns 400 with invalid claim error |
| `CLAIM-005` | **Claim Validation** | 1. Claim event with invalid data | Returns 400 with validation error |

### **4.2 Claim Management**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `CLAIM-006` | **Get User Claims** | 1. User requests their claims | Returns list of claimed events |
| `CLAIM-007` | **Claim Cancellation** | 1. User cancels their claim | Claim status updated to CANCELLED |
| `CLAIM-008` | **Claim Completion** | 1. Mark claim as completed | Claim status updated to COMPLETED |

---

## ⭐ **5. Rating System Scenarios**

### **5.1 Event Rating**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `RATING-001` | **Successful Event Rating** | 1. Customer rates claimed event | Rating saved with 201 status |
| `RATING-002` | **Duplicate Rating Prevention** | 1. Try to rate same event twice | Returns 400 with already rated error |
| `RATING-003` | **Rating Validation** | 1. Submit invalid rating (0 or >5) | Returns 400 with rating validation error |
| `RATING-004` | **Unauthorized Rating** | 1. User tries to rate unclaimed event | Returns 400 with unauthorized error |
| `RATING-005` | **Rating with Comment** | 1. Submit rating with comment | Rating saved with comment |

### **5.2 Rating Retrieval & Analytics**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `RATING-006` | **Get Event Ratings** | 1. Request ratings for specific event | Returns event rating list |
| `RATING-007` | **Vendor Rating Summary** | 1. Get vendor's average rating | Returns vendor rating statistics |
| `RATING-008` | **Rating Filtering** | 1. Filter ratings by score range | Returns filtered rating list |

---

## 📱 **6. Notification Service Scenarios**

### **6.1 Notification Subscriptions**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `NOTIF-001` | **Subscribe to Notifications** | 1. User subscribes with location | Subscription created successfully |
| `NOTIF-002` | **Duplicate Subscription** | 1. Try to subscribe with same FCM token | Returns 400 with duplicate error |
| `NOTIF-003` | **Location Validation** | 1. Subscribe with invalid coordinates | Returns 400 with location error |
| `NOTIF-004` | **Radius Validation** | 1. Subscribe with invalid radius | Returns 400 with radius error |

### **6.2 Notification Delivery**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `NOTIF-005` | **Event Creation Notification** | 1. Vendor creates event<br>2. Check nearby subscribers | Notifications sent to subscribers |
| `NOTIF-006` | **Rating Notification** | 1. Customer rates event<br>2. Check vendor notification | Vendor receives rating notification |
| `NOTIF-007` | **Location-Based Targeting** | 1. Create event in specific area<br>2. Verify notification radius | Only nearby users notified |

### **6.3 Subscription Management**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `NOTIF-008` | **Get User Subscriptions** | 1. User requests subscriptions | Returns user's notification settings |
| `NOTIF-009` | **Update Subscription** | 1. Modify notification preferences | Subscription updated successfully |
| `NOTIF-010` | **Unsubscribe** | 1. User unsubscribes from notifications | Subscription removed |

---

## 🤖 **7. AI Service Scenarios**

### **7.1 Content Generation**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `AI-001` | **AI Content Generation** | 1. Vendor requests AI content<br>2. Provide image and context | Returns AI-generated title/description |
| `AI-002` | **Image Analysis** | 1. Submit image for analysis | Returns detected business type and labels |
| `AI-003` | **Language Preference** | 1. Request content in specific language | Returns content in requested language |
| `AI-004` | **Style Customization** | 1. Request specific content style | Returns content matching style |

### **7.2 AI Service Health**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `AI-005` | **Service Health Check** | 1. GET `/ai/health` | Returns service status |
| `AI-006` | **Service Degradation** | 1. Simulate AI service failure | Returns appropriate error response |
| `AI-007` | **Performance Monitoring** | 1. Monitor AI response times | Response times within acceptable limits |

---

## 🔗 **8. Integration Flow Scenarios**

### **8.1 Complete User Journey**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `FLOW-001` | **Customer Journey** | 1. Register customer account<br>2. Login and browse events<br>3. Claim event<br>4. Rate event<br>5. Subscribe to notifications | Complete customer experience works |
| `FLOW-002` | **Vendor Journey** | 1. Register vendor account<br>2. Login and create event<br>3. Use AI content generation<br>4. Monitor event performance | Complete vendor experience works |
| `FLOW-003` | **Admin Journey** | 1. Login as admin<br>2. Monitor system health<br>3. Manage users and events<br>4. Generate reports | Complete admin experience works |

### **8.2 Cross-Module Interactions**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `FLOW-004` | **Event-Notification Flow** | 1. Create event<br>2. Verify notifications sent<br>3. Check notification content | Notifications contain event details |
| `FLOW-005` | **Rating-Notification Flow** | 1. Rate event<br>2. Verify vendor notification<br>3. Check rating impact | Vendor notified and rating updated |
| `FLOW-006` | **AI-Event Flow** | 1. Generate AI content<br>2. Create event with AI content<br>3. Verify content integration | AI content properly integrated |

---

## ❌ **9. Error Handling Scenarios**

### **9.1 Input Validation Errors**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `ERROR-001` | **Invalid JSON Format** | 1. Send malformed JSON | Returns 400 with parsing error |
| `ERROR-002` | **Missing Required Fields** | 1. Omit required fields in requests | Returns 400 with field validation errors |
| `ERROR-003` | **Invalid Data Types** | 1. Send wrong data types | Returns 400 with type validation errors |
| `ERROR-004` | **Field Length Limits** | 1. Exceed field length limits | Returns 400 with length validation errors |

### **9.2 Business Logic Errors**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `ERROR-005` | **Business Rule Violations** | 1. Violate business rules | Returns 400 with business logic error |
| `ERROR-006` | **Resource Not Found** | 1. Access non-existent resource | Returns 404 Not Found |
| `ERROR-007` | **Conflict Resolution** | 1. Create conflicting resources | Returns 409 Conflict |

### **9.3 System Errors**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `ERROR-008` | **Database Connection Failure** | 1. Simulate DB connection loss | Returns 503 Service Unavailable |
| `ERROR-009` | **External Service Failure** | 1. Simulate AI service failure | Returns 503 with fallback response |
| `ERROR-010` | **Timeout Handling** | 1. Simulate slow response | Returns 408 Request Timeout |

---

## 📊 **10. Performance & Load Scenarios**

### **10.1 Response Time Testing**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `PERF-001` | **Single Request Performance** | 1. Measure response time for key endpoints | Response time < 500ms |
| `PERF-002** | **Concurrent User Load** | 1. Simulate multiple concurrent users | System handles load without errors |
| `PERF-003` | **Database Query Performance** | 1. Test complex queries with large datasets | Queries complete within acceptable time |

### **10.2 Scalability Testing**
| Scenario ID | Description | Test Steps | Expected Result |
|-------------|-------------|------------|-----------------|
| `PERF-004` | **User Growth Simulation** | 1. Gradually increase user load | System scales appropriately |
| `PERF-005` | **Data Volume Testing** | 1. Test with large event datasets | Performance remains stable |
| `PERF-006` | **Memory Usage Monitoring** | 1. Monitor memory during load tests | Memory usage within limits |

---

## 🧪 **Test Implementation Guidelines**

### **Test Data Management**
- Use unique test data for each test run
- Clean up test data after test completion
- Use realistic but safe test values
- Implement data factories for consistent test data

### **Environment Configuration**
- Separate test environment from development
- Use test-specific database and services
- Configure test timeouts and retry policies
- Enable detailed logging for debugging

### **Test Execution Strategy**
- Run tests in isolation to avoid interference
- Implement proper test ordering and dependencies
- Use test categories for selective execution
- Implement retry mechanisms for flaky tests

### **Reporting & Monitoring**
- Generate detailed test execution reports
- Track test execution time and success rates
- Monitor system resources during testing
- Implement test result notifications

---

## 📋 **Test Execution Checklist**

### **Pre-Test Setup**
- [ ] Test environment is ready
- [ ] Test data is prepared
- [ ] External services are available
- [ ] Database is in known state

### **Test Execution**
- [ ] Run tests in correct order
- [ ] Monitor system resources
- [ ] Capture detailed logs
- [ ] Handle test failures gracefully

### **Post-Test Cleanup**
- [ ] Clean up test data
- [ ] Restore system state
- [ ] Generate test reports
- [ ] Archive test artifacts

---

## 🔧 **Next Steps**

1. **Tool Selection**: Choose E2E testing framework (Cypress, Playwright, Selenium)
2. **Test Environment**: Set up dedicated test environment
3. **Test Implementation**: Implement scenarios based on selected tool
4. **CI/CD Integration**: Integrate tests into deployment pipeline
5. **Monitoring**: Set up test execution monitoring and reporting

---

**Note**: This document serves as a comprehensive test plan. Each scenario should be implemented as an automated test case using the selected E2E testing framework.
