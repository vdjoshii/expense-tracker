# AI Notes

## 1. What was AI-generated vs. written by me

I used Claude to scaffold the full Spring Boot project: the Maven config, the model/DTO/service/controller/exception classes, and the JUnit + MockMvc test suite. I gave it the assignment spec directly and asked for a standard layered Spring Boot structure (controller → service → in-memory store) rather than anything exotic.

The layered structure (controller → service → in-memory store), the validation rules, and the exception handling were all AI suggestions I kept as-is, since they matched a standard Spring Boot approach I'd have written myself. I reviewed every file before running anything rather than trusting it blind.

## 2. What I validated, tested, or changed, and why

I did not have Maven available in the environment the code was originally generated in, so none of it had actually been compiled before I got it. I ran the following verification myself, using Postman against a live server (`mvn spring-boot:run`), plus the automated suite (`mvn test`):

- **POST /api/expenses** — created an expense, got `201 Created` back with a server-generated UUID `id`. Confirmed.
- **GET /api/expenses** — returned the full list correctly.
- **GET /api/expenses?category=Food** — filtered correctly.
- **GET /api/expenses/total** and **?category=Food** — totals matched expected sums.
- **DELETE /api/expenses/{id}** — returned `204 No Content` on a valid id, and `404 Not Found` when I retried the same id (confirms the not-found path works, not just the happy path).
- **Validation — negative amount** — sent `"amount": -5`, got `400 Bad Request` with `"amount must be greater than 0"`. Initially thought this had failed because I was reading the response from a separate GET call instead of the POST's own response — re-ran it carefully and confirmed it works correctly.
- **Validation — missing required field** — omitted `amount` entirely, got `400 Bad Request` with `"amount is required"`. Same mix-up initially, confirmed correct on re-check.
- **`mvn test`** — full suite passes.

Everything worked as documented; no code fixes were needed. The only real "bug" was on my end (reading the wrong Postman response tab), not in the code.

## 3. AI suggestions I didn't use, and why

The AI offered file-based JSON persistence as an alternative to in-memory storage. I kept in-memory since the assignment spec allows either and it's simpler to test — no risk of stale state between runs.

