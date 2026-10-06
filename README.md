# Banking Transactions API

An in-memory REST API built with Java 17 and Spring Boot for the BrainRidge Java take-home assignment.

## What it does

- Creates user accounts with an initial balance.
- Transfers funds between accounts.
- Returns the transaction history for an account.
- Validates request data and returns consistent error responses.
- Uses dependency injection and a Controller / Service / Repository design.
- Stores data in memory as requested; restarting the application clears all data.

## Technology

- Java 17+
- Spring Boot 3.5.16
- Spring Web
- Spring Validation
- Maven

## Run the application

```bash
./mvnw spring-boot:run
```

If Maven is already installed, the equivalent command is:

```bash
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

## Run tests

```bash
./mvnw test
```

## API endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/accounts` | Create an account |
| `GET` | `/api/v1/accounts/{accountId}` | Get the current account balance |
| `POST` | `/api/v1/transfers` | Transfer money between accounts |
| `GET` | `/api/v1/accounts/{accountId}/transactions` | Get account transaction history |

### Create an account

```bash
curl -i -X POST http://localhost:8080/api/v1/accounts \
  -H 'Content-Type: application/json' \
  -d '{"initialBalance": 100.00}'
```

Example response:

```json
{
  "id": "9df0a8f8-0b0a-4c52-9d69-1c447cb8fa04",
  "balance": 100.00,
  "createdAt": "2026-10-06T15:00:00Z"
}
```

Copy the returned account ID and create a second account before making a transfer.

### Transfer funds

```bash
curl -i -X POST http://localhost:8080/api/v1/transfers \
  -H 'Content-Type: application/json' \
  -d '{
        "fromAccountId": "SOURCE_ACCOUNT_ID",
        "toAccountId": "DESTINATION_ACCOUNT_ID",
        "amount": 25.00
      }'
```

### Get transaction history

```bash
curl -i http://localhost:8080/api/v1/accounts/ACCOUNT_ID/transactions
```

The history includes the initial deposit when it is greater than zero, plus debit and credit entries for transfers. Transfer entries share a `transferId` so the two sides can be correlated.

## Error handling

- `400 Bad Request`: malformed JSON, invalid fields, non-positive transfer, self-transfer, or more than two decimal places.
- `404 Not Found`: account ID does not exist.
- `422 Unprocessable Entity`: source account has insufficient funds.
- `500 Internal Server Error`: unexpected server-side failure.

Errors use the following shape:

```json
{
  "timestamp": "2026-10-06T15:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Insufficient funds in account ...",
  "path": "/api/v1/transfers"
}
```

## Design notes and assumptions

- `BigDecimal` is used for money instead of floating-point types.
- Account and transaction repositories are interfaces with in-memory implementations, keeping the data-access layer replaceable without introducing a database that the assignment does not require.
- Transfers are serialized through a service-level lock. Both accounts are validated before either balance is changed, preventing partial transfers in the in-memory implementation.
- The API accepts amounts with at most two decimal places.
- Authentication, authorization, persistence, and idempotency keys are intentionally out of scope for this take-home assignment.
