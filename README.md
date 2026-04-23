# Smart Campus API (JAX-RS)

## 1. Overview

This project is a RESTful API developed using **JAX-RS (Jakarta RESTful Web Services)** for managing a Smart Campus system.

The API allows management of:

* Rooms
* Sensors (e.g., Temperature, CO2)
* Sensor Readings (historical data)

The system follows REST principles:

* Resource-based design
* Proper HTTP methods (GET, POST, DELETE)
* JSON communication
* Nested resource structure

### Resource Structure

- /api/v1/rooms  
- /api/v1/sensors  
- /api/v1/sensors/{sensorId}  
- /api/v1/sensors/{sensorId}/readings

This reflects real-world relationships:

* A Room contains Sensors
* A Sensor contains Readings

---

## 2. Technologies Used

* Java (JDK 17+)
* JAX-RS (Jersey)
* Maven
* In-memory storage (HashMap, ArrayList)

---

## 3. How to Build and Run

### Step 1: Open Project

Open the project in NetBeans / IntelliJ / VS Code

### Step 2: Build Project

Run:
mvn clean install

### Step 3: Run Server

Run:
mvn exec:java

OR run the Main class from your IDE

### Step 4: Base URL

http://localhost:8080/api/v1

---

## 4. Sample cURL Commands

### 1. Create Sensor
```bash
curl -X POST "http://localhost:8080/api/v1/sensors" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"S1\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":300,\"roomId\":\"LIB-301\"}"
```

### 2. Get All Sensors
```bash
curl "http://localhost:8080/api/v1/sensors"
```

### 3. Filter Sensors by Type
```bash
curl "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 4. Get Sensor by ID
```bash
curl "http://localhost:8080/api/v1/sensors/S1"
```

### 5. Add Sensor Reading
```bash
curl -X POST "http://localhost:8080/api/v1/sensors/S1/readings" \
  -H "Content-Type: application/json" \
  -d "{\"value\":25.5}"
```

### 6. Get Sensor Readings
```bash
curl "http://localhost:8080/api/v1/sensors/S1/readings"
```

### 7. Delete Sensor
```bash
curl -X DELETE "http://localhost:8080/api/v1/sensors/S1"
```
---

## 5. API Design Explanation

### RESTful Design

The API is designed using REST principles:

* Each entity is a resource (Room, Sensor, Reading)
* Stateless communication
* Standard HTTP methods are used

---

### Filtering (Query Parameter)

Filtering is implemented using:
/sensors?type=CO2

This approach is better because:

* Flexible for searching
* Allows multiple filters
* Cleaner than using path parameters

---

### Sub-Resource Locator Pattern

Nested resources are used:
/sensors/{id}/readings

Benefits:

* Clean separation of logic
* Better organization of code
* Easier to maintain large APIs

---

### Data Consistency

When a new reading is added:

* It is stored in history
* The parent sensor currentValue is updated

---

### Error Handling

The API returns proper HTTP responses:

* 404 → Sensor not found
* 400 → Bad request
* 403 → Sensor in maintenance
* 422 → Invalid linked resource
* 500 → Internal server error

---

### Logging

Logging is implemented using filters:

* Logs incoming requests (method + URL)
* Logs outgoing responses (status code)

---


##  Notes

* No database is used (in-memory only)
* Only JAX-RS is used (no Spring Boot)
* Designed according to coursework requirements


## 6. Conceptual Report Answers

---

### Part 1: Service Architecture & Setup

#### 1.1. Project & Application Configuration

##### Q: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.


In JAX-RS, resource classes are typically request-scoped, meaning a new instance is created for each incoming HTTP request.

In this Smart Campus API, this behaviour ensures that:

- Each request is handled independently
- There is no unintended shared state between requests
- Thread safety is improved automatically

However, since this project uses in-memory data structures (e.g., HashMap, ArrayList), storing them inside resource classes would result in data loss, because each request gets a new object.

To solve this, the API uses a shared static data store (e.g., DataStore class) where all Rooms, Sensors, and Readings are stored.

This design ensures:

- Data persistence across requests
- Avoidance of race conditions
- Safe concurrent access to shared data

---

#### 1.2. The ”Discovery” Endpoint

##### Q: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

Hypermedia (HATEOAS) allows the API to include navigational links inside responses, enabling clients to dynamically discover available actions.

In this API, the Discovery endpoint (`GET /api/v1`) provides:

- API version information
- Contact details
- Links to resources such as `/rooms` and `/sensors`

This approach benefits clients because:

- They do not need hardcoded URLs
- They can dynamically navigate the API
- The API becomes self-descriptive and easier to use

Compared to static documentation, HATEOAS improves:

- Flexibility
- Maintainability
- Client adaptability

---

### Part 2: Room Management

#### 2.1. Room Resource Implementation 

##### Q: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.


When returning a list of rooms, there are two approaches:

**Returning only IDs:**
- Reduces payload size
- Improves network performance
- Requires additional requests to fetch details

**Returning full objects (used in this API):**
- Provides complete information in one response
- Reduces the need for multiple API calls
- Improves usability for clients

In this API, full room objects are returned because:

- The dataset is small (in-memory)
- It simplifies client-side processing

This design balances performance and usability effectively.

---

#### 2.2.Room Deletion & Safety Logic 

##### Q: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends  the exact same DELETE request for a room multiple times.

The DELETE operation is idempotent, meaning repeated requests produce the same result.

In this API:

**Case 1: First DELETE request**
- Room is successfully deleted
- Returns 200 OK

**Case 2: Repeated DELETE request**
- Room no longer exists
- Returns 404 Not Found

Even though the responses differ, the final system state remains unchanged, which satisfies idempotency.

Additionally, the API enforces a business rule:

- If a room contains sensors → deletion is blocked
- Returns 409 Conflict

This ensures data integrity and prevents orphaned sensors.

---

### Part 3:  Sensor Operations & Linking

#### 3.1. Sensor Resource & Integrity 

##### Q: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The `@Consumes(MediaType.APPLICATION_JSON)` annotation ensures that API endpoints only accept JSON input.

In this API, it is used in:

- `POST /api/v1/sensors`
- `POST /api/v1/sensors/{sensorId}/readings`

If a client sends data in another format (e.g., XML or plain text), JAX-RS:

- Automatically rejects the request
- Returns HTTP 415 – Unsupported Media Type

This guarantees:

- Consistent data format
- Easier parsing and validation
- Improved API reliability

---

#### 3.2. Filtered Retrieval & Search 

##### Q: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g.,/api/vl/sensors/type/CO2). Why is the query  parameter approach generally considered superior for filtering and searching collections?

Filtering in this API is implemented using query parameters:

**Example:**
```text
GET /api/v1/sensors?type=CO2
```

An alternative would be:

```text
/api/v1/sensors/type/CO2
```

However, query parameters are preferred because:

- They are designed for filtering and searching collections
- They allow multiple filters (e.g., `?type=CO2&status=ACTIVE`)
- They keep the API structure clean and flexible

Path parameters are better suited for:

- Identifying specific resources (e.g., `/sensors/{id}`)

Thus, query parameters provide a more scalable and RESTful design.

---

### Part 4: Deep Nesting with Sub- Resources

#### 4.1. The Sub-Resource Locator Pattern 

##### Q: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

The API uses the nested path /sensors/{sensorId}/readings, which delegates request handling to a dedicated SensorReadingResource class using the sub-resource locator pattern.

This approach provides strong architectural benefits. First, it enforces separation of concerns by isolating sensor logic and reading logic into different classes. This makes the system more modular and easier to understand.

Second, it significantly reduces complexity in the main SensorResource class. Instead of managing all nested endpoints in one large class, responsibilities are delegated to specialised components.

Third, it improves scalability and maintainability in large APIs. New features related to readings can be added without modifying the core sensor logic, reducing the risk of errors.

Compared to defining all nested paths in a single controller, this pattern avoids code duplication, improves readability, and supports cleaner API design.

---


### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### 5.2.  Dependency Validation 

##### Q: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

When creating a sensor with a non-existent room:

- The request structure is valid
- But the referenced resource does not exist

In this case, the API returns:

**422 Unprocessable Entity**

This is more accurate than 404 because:

- 404 applies to missing endpoints
- 422 applies to invalid data inside a valid request

Thus, 422 provides better semantic meaning.

---

#### 5.4. The Global Safety Net 

##### Q: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Exposing raw stack traces can reveal:

- Internal class names
- File paths
- Library versions
- Application structure

Attackers can use this information to:

- Identify vulnerabilities
- Perform targeted attacks

To prevent this, the API uses:

- A global exception mapper
- Returns a generic 500 Internal Server Error
- Hides internal details

This improves API security.

---

#### 5.5. API Request & Response Logging Filters

##### Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

The API uses JAX-RS filters:

- `ContainerRequestFilter`
- `ContainerResponseFilter`

These log:

- Incoming requests (method + URI)
- Outgoing responses (status code)

Advantages over manual logging:

- Centralized logging logic
- No code duplication
- Cleaner resource classes
- Easier maintenance

Thus, filters are ideal for cross-cutting concerns like logging.
