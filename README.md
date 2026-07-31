# Smart Expense Tracker API

A REST API for tracking personal expenses, built for the Diligent Software Engineering Apprenticeship 2026 take-home assignment.

## Tech Stack

- Java 17
- Spring Boot 3.2.5 (Web, Validation)
- Maven
- JUnit 5 + Spring's MockMvc for testing
- In-memory storage (`ConcurrentHashMap`) — no database required

## Features

- Add an expense (title, amount, category, date)
- View all expenses
- Filter expenses by category
- Calculate total expenses (overall, or scoped to one category)
- Delete an expense
- Input validation (amount must be positive, required fields enforced, no future-dated expenses)
- Consistent JSON error responses (404 for missing expenses, 400 with field-level messages for invalid input)

## Prerequisites

- Java 17 or later (`java -version`)
- Maven 3.8+ (`mvn -version`)

## Install Dependencies

```bash
mvn install -DskipTests
```

## Run the Server

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

## Run the Tests

```bash
mvn test
```

## API Reference

### Add an expense
```
POST /api/expenses
Content-Type: application/json

{
  "title": "Groceries",
  "amount": 1250.50,
  "category": "Food",
  "date": "2026-07-15"
}
```
Returns `201 Created` with the saved expense (includes a server-generated `id`).

### View all expenses
```
GET /api/expenses
```

### Filter by category
```
GET /api/expenses?category=Food
```

### Total expenses (overall)
```
GET /api/expenses/total
```

### Total expenses (by category)
```
GET /api/expenses/total?category=Food
```
Both return:
```json
{ "category": "Food", "total": 1250.50, "count": 1 }
```
`category` is `null` when totaling across everything.

### Delete an expense
```
DELETE /api/expenses/{id}
```
Returns `204 No Content` on success, `404 Not Found` if the id doesn't exist.

## Example curl session

```bash
# Add an expense
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"title":"Groceries","amount":1250.50,"category":"Food","date":"2026-07-15"}'

# View all
curl http://localhost:8080/api/expenses

# Filter by category
curl "http://localhost:8080/api/expenses?category=Food"

# Total overall
curl http://localhost:8080/api/expenses/total

# Total for one category
curl "http://localhost:8080/api/expenses/total?category=Food"

# Delete
curl -X DELETE http://localhost:8080/api/expenses/{id}
```

## Design Notes

- Data lives in memory (`ConcurrentHashMap`) and resets on restart. This matches the assignment spec, which allows in-memory storage.
- IDs are server-generated UUIDs — clients never supply their own id.
- `GET /api/expenses` is sorted by date, most recent first.
- Category matching is case-insensitive so `?category=food` and `?category=Food` behave the same.

## Project Structure

```
src/main/java/com/diligent/expensetracker/
  ExpenseTrackerApplication.java
  model/Expense.java
  dto/ExpenseRequest.java
  dto/TotalResponse.java
  service/ExpenseService.java
  controller/ExpenseController.java
  exception/ExpenseNotFoundException.java
  exception/GlobalExceptionHandler.java
src/test/java/com/diligent/expensetracker/
  service/ExpenseServiceTest.java
  controller/ExpenseControllerTest.java
```
