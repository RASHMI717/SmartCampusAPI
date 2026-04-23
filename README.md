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


In JAX-RS, resource classes are typically **request-scoped**, meaning a new instance is created for each incoming HTTP request.

This design prevents shared state issues and improves thread safety. However, since this project uses in-memory data structures (such as HashMaps and Lists), these must be stored in a shared static class (e.g., DataStore).

From a concurrency perspective, this design avoids accidental data overwrites between requests, but in a real-world system, additional synchronization mechanisms would be required to prevent race conditions when multiple users access or modify shared data simultaneously.

---

#### 1.2. The ”Discovery” Endpoint

##### Q: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

Hypermedia (HATEOAS) allows APIs to include navigational links within responses, enabling clients to dynamically discover available actions.

This is considered a hallmark of advanced RESTful design because:
- It reduces tight coupling between client and server
- Clients do not need hardcoded endpoint knowledge
- Improves adaptability when APIs evolve

Compared to static documentation, HATEOAS provides a **self-describing API**, improving developer experience and reducing integration errors.

---

### Part 2: Room Management

#### 2.1. Room Resource Implementation 

##### Q: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.


Returning only IDs reduces payload size and improves network efficiency, especially in large datasets.

However, this increases the number of API calls required by the client to retrieve full details.

Returning full objects increases response size but improves usability by reducing additional requests.

In this implementation, full objects are returned to prioritise simplicity and reduce client-side complexity, which is suitable for this system scale.

---

#### 2.2. Room Resource Implementation 

##### Q: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends  the exact same DELETE request for a room multiple times.

DELETE is considered idempotent because performing the same request multiple times results in the same final system state.

In this implementation:
- The first DELETE removes the room
- Subsequent DELETE requests return 404 (resource not found)

Even though the response differs, the system state remains unchanged, satisfying idempotency principles.

---

### Part 3:  Sensor Operations & Linking

#### 3.1. Sensor Resource & Integrity 

##### Q: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The @Consumes(MediaType.APPLICATION_JSON) annotation specifies that the API only accepts request bodies in JSON format. In this project, it is applied at the class level in both SensorResource and SensorReadingResource, ensuring that all POST operations require JSON input.

If a client sends a request with a different media type (e.g., text/plain or XML), the JAX-RS runtime automatically rejects the request and returns HTTP 415 (Unsupported Media Type), without executing the resource method.

This behaviour enforces strict input validation, improves data consistency, and prevents invalid data formats from being processed by the API.

---

#### 3.2. Filtered Retrieval & Search 

##### Q: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g.,/api/vl/sensors/type/CO2). Why is the query  parameter approach generally considered superior for filtering and searching collections?

Filtering is implemented using query parameters:
/sensors?type=CO2

This is superior to path-based filtering such as:
/sensors/type/CO2

Because:
- Query parameters are designed for dynamic filtering
- Easily support multiple filters (e.g., ?type=CO2&status=ACTIVE)
- More flexible and scalable

Path parameters are better suited for identifying specific resources, not filtering collections.

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

### Part 4.2: Historical Data Management

The API maintains a historical log of sensor readings using the nested endpoint /sensors/{sensorId}/readings.

The GET method retrieves all past readings associated with a sensor, enabling full historical tracking. The POST method appends new readings to this collection.

A key requirement is maintaining data consistency. When a new reading is added, the system updates the parent sensor’s currentValue field to reflect the latest reading.

This ensures that the API provides both an accurate historical record and a consistent real-time representation of the sensor’s current state.

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### 5.2.  Dependency Validation 

##### Q: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

HTTP 422 is more semantically accurate when:
- The request format is correct
- But contains invalid data (e.g., non-existent roomId)

A 404 indicates a missing endpoint, whereas 422 indicates a logical error in the request content.

---

#### 5.4. Room Resource Implementation 

##### Q: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Exposing stack traces can reveal:
- Internal class structures
- File paths
- Library versions
- Application logic

Attackers can use this information to identify vulnerabilities and exploit the system.

Therefore, the API returns generic error messages to protect internal implementation details.

---

#### 5.5. API Request & Response Logging Filters

##### Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

Using JAX-RS filters allows centralized handling of cross-cutting concerns like logging.

Advantages include:
- Avoids repetitive logging code in each method
- Ensures consistent logging across all endpoints
- Improves maintainability and scalability

Compared to manual logging, filters provide a cleaner and more efficient design.
