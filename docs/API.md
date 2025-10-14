# API Documentation - Would Like API

## Base Information
- **Base URL**: `http://localhost:8080/api/v1`
- **Content-Type**: `application/json`
- **Authentication**: Bearer JWT Token
- **API Version**: v1

## Authentication

### POST /auth/register
Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "id": "user-uuid-123",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2025-01-04T10:00:00Z"
}
```

### POST /auth/login
Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "user@example.com", 
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400,
  "user": {
    "id": "user-uuid-123",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

## User Management

### GET /users/profile
Get current user profile (requires authentication).

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Response (200 OK):**
```json
{
  "id": 123,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2025-01-04T10:00:00Z",
  "updatedAt": "2025-01-04T10:00:00Z"
}
```

### PUT /users/profile
Update user profile.

**Request Body:**
```json
{
  "firstName": "John Updated",
  "lastName": "Doe Updated"
}
```

## Wish Lists

### GET /wishlists
Get all wish lists for the authenticated user.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (name, createdAt, updatedAt)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 123,
      "name": "Birthday Wishes 2025",
      "description": "Things I want for my birthday",
      "isPublic": false,
      "itemCount": 5,
      "createdAt": "2025-01-04T10:00:00Z",
      "updatedAt": "2025-01-04T10:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

### POST /wishlists
Create a new wish list.

**Request Body:**
```json
{
  "name": "Christmas Wishes",
  "description": "My Christmas wish list for 2025",
  "isPublic": false
}
```

**Response (201 Created):**
```json
{
  "id": 456,
  "name": "Christmas Wishes",
  "description": "My Christmas wish list for 2025",
  "isPublic": false,
  "itemCount": 0,
  "createdAt": "2025-01-04T10:00:00Z",
  "updatedAt": "2025-01-04T10:00:00Z"
}
```

### GET /wishlists/{wishlistId}
Get a specific wish list with all items.

**Response (200 OK):**
```json
{
  "id": 123,
  "name": "Birthday Wishes 2025",
  "description": "Things I want for my birthday",
  "isPublic": false,
  "items": [
    {
      "id": 789,
      "name": "Wireless Headphones",
      "description": "Noise-cancelling wireless headphones",
      "price": {
        "amount": 299.99,
        "currency": "USD"
      },
      "priority": "HIGH",
      "category": "Electronics",
      "url": "https://example.com/product/123",
      "imageUrl": "https://example.com/image/123.jpg",
      "notes": "Prefer black color",
      "isPurchased": false,
      "createdAt": "2025-01-04T10:00:00Z"
    }
  ],
  "createdAt": "2025-01-04T10:00:00Z",
  "updatedAt": "2025-01-04T10:00:00Z"
}
```

### PUT /wishlists/{wishlistId}
Update wish list details.

**Request Body:**
```json
{
  "name": "Updated Birthday Wishes",
  "description": "Updated description",
  "isPublic": true
}
```

### DELETE /wishlists/{wishlistId}
Delete a wish list and all its items.

**Response (204 No Content)**

## Wish List Items

### POST /wishlists/{wishlistId}/items
Add an item to a wish list.

**Request Body:**
```json
{
  "name": "Smartphone",
  "description": "Latest model smartphone",
  "price": {
    "amount": 999.99,
    "currency": "USD"
  },
  "priority": "MEDIUM",
  "category": "Electronics",
  "url": "https://example.com/smartphone",
  "notes": "Prefer blue color"
}
```

**Response (201 Created):**
```json
{
  "id": 101,
  "name": "Smartphone",
  "description": "Latest model smartphone",
  "price": {
    "amount": 999.99,
    "currency": "USD"
  },
  "priority": "MEDIUM",
  "category": "Electronics",
  "url": "https://example.com/smartphone",
  "notes": "Prefer blue color",
  "isPurchased": false,
  "createdAt": "2025-01-04T10:00:00Z"
}
```

### PUT /wishlists/{wishlistId}/items/{itemId}
Update an item in a wish list.

**Request Body:**
```json
{
  "name": "Updated Smartphone",
  "price": {
    "amount": 899.99,
    "currency": "USD"
  },
  "priority": "HIGH"
}
```

### DELETE /wishlists/{wishlistId}/items/{itemId}
Remove an item from a wish list.

**Response (204 No Content)**

### PATCH /wishlists/{wishlistId}/items/{itemId}/purchase
Mark an item as purchased/unpurchased.

**Request Body:**
```json
{
  "isPurchased": true
}
```

## Error Responses

### Error Format
All errors follow a consistent structure:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed for the request",
    "details": [
      {
        "field": "email",
        "message": "Email format is invalid"
      }
    ],
    "timestamp": "2025-01-04T10:00:00Z",
    "path": "/api/v1/auth/register"
  }
}
```

### Common Error Codes

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| 400 | VALIDATION_ERROR | Request validation failed |
| 401 | UNAUTHORIZED | Authentication required |
| 403 | FORBIDDEN | Access denied |
| 404 | NOT_FOUND | Resource not found |
| 409 | CONFLICT | Resource conflict (duplicate) |
| 422 | BUSINESS_ERROR | Business rule violation |
| 500 | INTERNAL_ERROR | Server error |

### Specific Error Scenarios

**User Not Found (404):**
```json
{
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User not found with id: 123"
  }
}
```

**Wish List Not Found (404):**
```json
{
  "error": {
    "code": "WISHLIST_NOT_FOUND", 
    "message": "Wish list not found with id: 123"
  }
}
```

**Duplicate Wish List Name (409):**
```json
{
  "error": {
    "code": "DUPLICATE_WISHLIST_NAME",
    "message": "A wish list with this name already exists"
  }
}
```

## Data Models

### User
```json
{
  "id": "integer (BIGINT)",
  "email": "string (email format)",
  "firstName": "string (1-50 chars)",
  "lastName": "string (1-50 chars)", 
  "createdAt": "string (ISO 8601)",
  "updatedAt": "string (ISO 8601)"
}
```

### WishList
```json
{
  "id": "integer (BIGINT)",
  "name": "string (1-100 chars)",
  "description": "string (max 500 chars, optional)",
  "isPublic": "boolean",
  "itemCount": "integer",
  "items": "array of WishListItem (optional)",
  "createdAt": "string (ISO 8601)",
  "updatedAt": "string (ISO 8601)"
}
```

### WishListItem
```json
{
  "id": "integer (BIGINT)",
  "name": "string (1-200 chars)",
  "description": "string (max 1000 chars, optional)",
  "price": "Money object (optional)",
  "priority": "enum (LOW, MEDIUM, HIGH)",
  "category": "string (max 50 chars, optional)",
  "url": "string (URL format, optional)",
  "imageUrl": "string (URL format, optional)",
  "notes": "string (max 500 chars, optional)",
  "isPurchased": "boolean",
  "createdAt": "string (ISO 8601)"
}
```

### Money
```json
{
  "amount": "number (decimal, 2 places)",
  "currency": "string (3 chars, ISO 4217)"
}
```

## Rate Limiting
- **General endpoints**: 100 requests per minute per user
- **Authentication endpoints**: 10 requests per minute per IP
- **File upload endpoints**: 5 requests per minute per user

## Pagination
All collection endpoints support pagination with these parameters:
- `page`: Page number (0-based, default: 0)
- `size`: Page size (1-100, default: 20)
- `sort`: Sort field and direction (e.g., `name,asc` or `createdAt,desc`)

## Versioning Strategy
- URL versioning: `/api/v1/`
- Backward compatibility maintained for at least 12 months
- Deprecation notices provided 6 months in advance
- New versions introduce breaking changes only

## OpenAPI Specification
Full OpenAPI 3.0 specification will be available at:
- JSON format: `GET /api/v1/openapi.json`
- Interactive docs: `GET /swagger-ui.html`
